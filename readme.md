# Guía de instalación de Nexcell

Esta guía explica, paso a paso, cómo instalar y ejecutar **Nexcell** en tu computadora.

---

## 1. Requisitos previos

Instalá lo siguiente antes de empezar:

| Programa | Versión recomendada | Para qué se usa |
|---|---|---|
| JDK (Java Development Kit) | 17 o superior | Compilar y ejecutar el programa |
| MySQL Server | 8.0 o superior | Base de datos |
| MySQL Workbench (opcional) | Última | Ejecutar scripts SQL de forma visual |
| IntelliJ IDEA (o similar) | Community | Abrir y ejecutar el proyecto |
| Git | Última | Descargar el proyecto |

Para verificar que Java quedó instalado, abrí una terminal y ejecutá:

```bash
java -version
```

---

## 2. Descargar el proyecto

```bash
git clone https://github.com/leonciokpo/Aplicacion-Nexcell.git
```

Luego abrí la carpeta del proyecto con tu IDE (en IntelliJ: *File > Open* y elegí la carpeta).

---

## 3. Dependencias (Maven)

Al abrir el proyecto, esperá unos segundos a que Maven descargue las dependencias definidas en el `pom.xml`:

- MySQL Connector/J 8.3.0
- Hibernate Core 6.4.4
- FlatLaf 3.4.1 (aspecto visual)
- iText 5.5.13.3 (generación de PDF)
- JFreeChart 1.5.3 (gráficos estadísticos)
- Apache POI 5.2.3 (exportación a Excel .xlsx)

Si no se descargan solas, hacé clic derecho sobre `pom.xml` > *Maven* > *Reload project*. Se necesita conexión a internet.

---

## 4. Crear la base de datos

1. Asegurate de que el servidor MySQL esté en ejecución.
2. Conectate a MySQL (con Workbench o desde la consola) y creá una base de datos vacía:

```sql
CREATE DATABASE nexcell_db;
```

No hace falta crear tablas a mano: Hibernate las genera automáticamente.

---

## 5. Instalar los procedimientos almacenados

El sistema usa procedimientos almacenados para registrar ventas (dentro de una transacción) y para los reportes. Hay que instalarlos manualmente:

1. Abrí una pestaña de consulta SQL en MySQL Workbench (o la consola de MySQL) conectada a tu servidor.
2. Copiá y pegá el script completo de abajo.
3. Ejecutalo completo (en Workbench: ícono del rayo, o `Ctrl + Shift + Enter`).

```sql
USE nexcell_db;

DELIMITER $$

-- 1. rendimiento_vendedores
DROP PROCEDURE IF EXISTS `rendimiento_vendedores`$$
CREATE PROCEDURE `rendimiento_vendedores`(
    IN p_fecha_inicio DATETIME,
    IN p_fecha_fin DATETIME
)
BEGIN
    SELECT 
        u.username AS Vendedor,
        COUNT(v.id) AS CantidadVentas,
        SUM(v.total) AS TotalFacturado,
        (SUM(v.total) * 0.05) AS ComisionEstimada
    FROM ventas v
    INNER JOIN usuarios u ON v.usuario_id = u.id
    WHERE v.fecha BETWEEN p_fecha_inicio AND p_fecha_fin
    GROUP BY u.id, u.username
    ORDER BY TotalFacturado DESC;
END$$

-- 2. procesar_registro_venta
DROP PROCEDURE IF EXISTS `procesar_registro_venta`$$
CREATE PROCEDURE `procesar_registro_venta`(
    IN p_usuario_id BIGINT,
    IN p_cliente_dni VARCHAR(255),
    IN p_total DOUBLE,
    IN p_items_json JSON
)
BEGIN
    DECLARE v_venta_id BIGINT;
    DECLARE v_i INT DEFAULT 0;
    DECLARE v_cantidad_items INT;
    DECLARE v_prod_id BIGINT;
    DECLARE v_cantidad INT;
    DECLARE v_precio DOUBLE;
    DECLARE v_stock_actual INT;

    -- Si ocurre cualquier error, revierte toda la operación
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- 1. Inicia la transacción en MySQL
    START TRANSACTION;

    -- 2. Inserta en la tabla 'ventas' (columnas exactas de Venta.java)
    INSERT INTO ventas (fecha, total, cliente_dni, usuario_id)
    VALUES (NOW(), p_total, p_cliente_dni, p_usuario_id);

    SET v_venta_id = LAST_INSERT_ID();

    -- 3. Recorre el JSON e impacta en 'detalle_ventas' y 'productos'
    SET v_cantidad_items = JSON_LENGTH(p_items_json);

    WHILE v_i < v_cantidad_items DO
        SET v_prod_id = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', v_i, '].id')));
        SET v_cantidad = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', v_i, '].cantidad')));
        SET v_precio = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', v_i, '].precio')));

        SELECT stock INTO v_stock_actual FROM productos WHERE id = v_prod_id FOR UPDATE;

        IF v_stock_actual < v_cantidad THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Stock insuficiente en la base de datos.';
        END IF;

        UPDATE productos
        SET stock = stock - v_cantidad
        WHERE id = v_prod_id;

        INSERT INTO detalle_ventas (cantidad, subtotal, producto_id, venta_id)
        VALUES (v_cantidad, (v_cantidad * v_precio), v_prod_id, v_venta_id);

        SET v_i = v_i + 1;
    END WHILE;

    -- 4. Confirma todos los cambios juntos
    COMMIT;
END$$

-- 3. baja_logica_producto
DROP PROCEDURE IF EXISTS `baja_logica_producto`$$
CREATE PROCEDURE `baja_logica_producto`(
    IN p_id BIGINT
)
BEGIN
    -- Cambia el estado a 0 (inactivo) y actualiza la fecha de modificación automática
    UPDATE productos 
    SET estado = 0,
        fecha_modificacion = NOW()
    WHERE id = p_id;
END$$

-- 4. reporte_mejores_clientes
DROP PROCEDURE IF EXISTS `reporte_mejores_clientes`$$
CREATE PROCEDURE `reporte_mejores_clientes`(
    IN p_vendedor_id BIGINT,
    IN p_ini DATETIME,
    IN p_fin DATETIME
)
BEGIN
    SELECT c.dni, c.nombre, c.apellido, COUNT(v.id) AS compras, SUM(v.total) AS gastado
    FROM ventas v
    JOIN clientes c ON v.cliente_dni = c.dni
    WHERE v.usuario_id = p_vendedor_id AND v.fecha BETWEEN p_ini AND p_fin
    GROUP BY c.dni, c.nombre, c.apellido
    ORDER BY gastado DESC;
END$$

-- 5. reporte_rendimiento_categoria
DROP PROCEDURE IF EXISTS `reporte_rendimiento_categoria`$$
CREATE PROCEDURE `reporte_rendimiento_categoria`(
    IN p_vendedor_id BIGINT,
    IN p_categoria VARCHAR(100),
    IN p_ini DATETIME,
    IN p_fin DATETIME
)
BEGIN
    SELECT p.nombre, SUM(dv.cantidad) AS unidades, SUM(dv.subtotal) AS ingreso
    FROM ventas v
    JOIN detalle_ventas dv ON v.id = dv.venta_id
    JOIN productos p ON dv.producto_id = p.id
    WHERE v.usuario_id = p_vendedor_id AND p.categoria = p_categoria AND v.fecha BETWEEN p_ini AND p_fin
    GROUP BY p.id, p.nombre
    ORDER BY ingreso DESC;
END$$

-- 6. reporte_resumen_diario
DROP PROCEDURE IF EXISTS `reporte_resumen_diario`$$
CREATE PROCEDURE `reporte_resumen_diario`(
    IN p_vendedor_id BIGINT,
    IN p_ini DATETIME,
    IN p_fin DATETIME
)
BEGIN
    SELECT DATE(fecha) AS fecha_op, COUNT(id) AS tickets, SUM(total) AS monto
    FROM ventas
    WHERE usuario_id = p_vendedor_id AND fecha BETWEEN p_ini AND p_fin
    GROUP BY DATE(fecha)
    ORDER BY fecha_op DESC;
END$$

-- 7. reporte_top_productos
DROP PROCEDURE IF EXISTS `reporte_top_productos`$$
CREATE PROCEDURE `reporte_top_productos`(
    IN p_vendedor_id BIGINT,
    IN p_ini DATETIME,
    IN p_fin DATETIME
)
BEGIN
    SELECT p.nombre, p.categoria, SUM(dv.cantidad) AS unidades, SUM(dv.subtotal) AS ingreso
    FROM ventas v
    JOIN detalle_ventas dv ON v.id = dv.venta_id
    JOIN productos p ON dv.producto_id = p.id
    WHERE v.usuario_id = p_vendedor_id AND v.fecha BETWEEN p_ini AND p_fin
    GROUP BY p.id, p.nombre, p.categoria
    ORDER BY unidades DESC;
END$$

-- 8. reporte_ventas_detallado
DROP PROCEDURE IF EXISTS `reporte_ventas_detallado`$$
CREATE PROCEDURE `reporte_ventas_detallado`(
    IN p_fecha_inicio DATETIME,
    IN p_fecha_fin DATETIME
)
BEGIN
    SELECT 
        v.id AS ID_Venta,
        v.fecha AS Fecha,
        v.cliente_dni AS DNI_Cliente,
        u.username AS Vendedor,
        p.nombre AS Producto,
        d.subtotal AS Subtotal
    FROM ventas v
    INNER JOIN detalle_ventas d ON v.id = d.venta_id
    INNER JOIN productos p ON d.producto_id = p.id
    INNER JOIN usuarios u ON v.usuario_id = u.id
    WHERE v.fecha BETWEEN p_fecha_inicio AND p_fecha_fin
    ORDER BY v.fecha DESC;
END$$

-- 9. top_productos_mas_vendidos
DROP PROCEDURE IF EXISTS `top_productos_mas_vendidos`$$
CREATE PROCEDURE `top_productos_mas_vendidos`(
    IN p_fecha_inicio DATETIME,
    IN p_fecha_fin DATETIME
)
BEGIN
    SELECT 
        p.nombre AS Producto, 
        p.categoria AS Categoria, 
        SUM(d.cantidad) AS CantidadVendida, 
        SUM(d.subtotal) AS IngresosGenerados
    FROM detalle_ventas d
    INNER JOIN ventas v ON d.venta_id = v.id
    INNER JOIN productos p ON d.producto_id = p.id
    WHERE v.fecha BETWEEN p_fecha_inicio AND p_fecha_fin
    GROUP BY p.id, p.nombre, p.categoria
    ORDER BY CantidadVendida DESC
    LIMIT 5;
END$$

DELIMITER ;
```

Para verificar que se crearon, ejecutá:

```sql
SHOW PROCEDURE STATUS WHERE Db = 'nexcell_db';
```

Deberías ver 9 procedimientos.

> Podés ejecutar este script antes de la primera corrida del programa: MySQL permite crear los procedimientos aunque las tablas todavía no existan.

---

## 6. Configurar la conexión

1. Entrá a la carpeta `src/main/resources/META-INF`.
2. Copiá el archivo `persistence.example.xml` y renombrá la copia como `persistence.xml`.
3. Abrí `persistence.xml` y ajustá estas propiedades con tus datos:

```xml
<property name="jakarta.persistence.jdbc.url"
          value="jdbc:mysql://localhost:3306/nexcell_db?serverTimezone=UTC"/>
<property name="jakarta.persistence.jdbc.user" value="TU_USUARIO"/>
<property name="jakarta.persistence.jdbc.password" value="TU_CONTRASEÑA"/>
```

**Importante:**

- **Puerto:** MySQL usa por defecto el `3306`. Si tu instalación usa otro (por ejemplo `3307`), modificalo en la URL.
- **Nombre de la base:** debe coincidir con la que creaste (`nexcell_db`).
- **Contraseña:** usá tu propia contraseña de MySQL. Nunca subas tu `persistence.xml` con credenciales reales a GitHub.

---

## 7. Primera ejecución (carga de datos de prueba)

1. En `persistence.xml`, verificá que la propiedad esté en `create`:

```xml
<property name="hibernate.hbm2ddl.auto" value="create"/>
```

2. Ejecutá la clase `Main` (clic derecho > *Run 'Main.main()'*).
3. En la consola vas a ver cómo se crean las tablas y se insertan usuarios, productos, clientes y un historial de ventas de prueba.
4. Cuando se abra la ventana de **Login**, cerrá el programa.

---

## 8. Cambiar a modo "update" (obligatorio)

Volvé a `persistence.xml` y cambiá el valor `create` por `update`:

```xml
<property name="hibernate.hbm2ddl.auto" value="update"/>
```

> Si dejás `create`, **cada vez que abras el programa se borran y se regeneran todos los datos**.

---

## 9. Iniciar sesión

Ejecutá `Main` nuevamente. Podés ingresar con las cuentas de prueba (la contraseña de todas es `123`):

| Rol | Usuario | Permisos |
|---|---|---|
| Vendedor | `pedro` | Solo accede a su propio historial de ventas |
| Gerente | `ana` | Acceso de gerencia |
| Administrador | `juan` | Acceso completo |

---

## 10. Configuración de backups

El módulo de backup (pantalla **BackupUI**, disponible para el Administrador) permite generar copias de la base de datos, programarlas y restaurarlas. Usa herramientas externas de MySQL y de Windows, por lo que requiere una configuración previa.

### Requisitos

- **Sistema operativo Windows.** La programación automática usa el Programador de tareas de Windows (`schtasks`) y PowerShell.
- **`mysqldump.exe` y `mysql.exe`** instalados junto con MySQL Server (carpeta `bin`).

### Valores que hay que ajustar en `BackupController.java`

Abrí `src/main/java/controlador/BackupController.java` y modificá estas constantes según tu equipo:

```java
private final String DB_USER = "TU_USUARIO";
private final String DB_PASS = "TU_CONTRASEÑA";
private final String DB_NAME = "nexcell_db";

private final String CMD_MYSQLDUMP = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe";
private final String CMD_MYSQL     = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe";
```

- **Rutas de `mysqldump.exe` y `mysql.exe`:** si instalaste otra versión de MySQL (por ejemplo 8.4) o en otra carpeta, actualizá las rutas.
- **Credenciales y nombre de base:** deben coincidir con las de tu `persistence.xml`.
- **Puerto:** en el código el puerto `3307` está escrito directamente (en el backup manual, la restauración y el script programado). Si tu MySQL usa otro puerto (por defecto `3306`), reemplazá `3307` en esos lugares y en `persistence.xml`.

### Configurar el envío del backup por correo (backup programado)

El backup programado genera un script de PowerShell que, al terminar, envía el archivo `.sql` por correo con Gmail. En el método `programarBackup()` ajustá:

- `$Username`: la cuenta de Gmail que envía el correo.
- `$Password`: la **contraseña de aplicación** de esa cuenta de Gmail. No es la contraseña normal: se genera en tu cuenta de Google (requiere tener activada la verificación en dos pasos) en *Seguridad > Contraseñas de aplicaciones*.
- `$Destino`: el correo que recibirá el respaldo.

### Cómo se usa

| Acción | Cómo funciona |
|---|---|
| **Generar backup manual** | Elegís dónde guardar el archivo `.sql` y se genera al instante. El nombre sugerido incluye fecha y hora. |
| **Programar backup** | Elegís día, mes, año, hora y minutos (debe ser a futuro). Se crea una tarea de Windows llamada `Nexcell_Backup_Auto` que genera el backup en `C:\backups_nexcell` y lo envía por correo. Al programar uno nuevo, reemplaza al anterior. |
| **Restaurar backup** | Pide confirmación y una **doble autorización**: usuario y contraseña de un Administrador **y** de un Gerente. Luego elegís el archivo `.sql` a restaurar. Se recomienda reiniciar el sistema después. |

### Consideraciones importantes

- **Ejecutá el IDE (o el programa) como Administrador** para poder programar backups; si no, Windows rechaza la creación de la tarea ("Error de Permisos").
- El equipo debe estar **encendido** (o en suspensión, ya que la tarea puede despertarlo) a la hora programada.
- Los archivos generados se guardan en `C:\backups_nexcell`, junto con el script `script_backup_nexcell.ps1` y el XML de la tarea. **Ese script contiene en texto plano las credenciales de la base y del correo**, por lo que conviene restringir el acceso a esa carpeta.
- **Restaurar reemplaza los datos actuales** de la base con los del archivo elegido. Hacé un backup manual antes de restaurar.

---

## 11. Solución de problemas

| Problema | Posible causa y solución |
|---|---|
| `Communications link failure` o no conecta a la base | MySQL no está iniciado, o el puerto/usuario/contraseña en `persistence.xml` son incorrectos. |
| `Unknown database 'nexcell_db'` | Falta crear la base de datos (paso 4). |
| `PROCEDURE ... does not exist` | Falta ejecutar el script de procedimientos almacenados (paso 5). |
| Se pierden los datos al reiniciar | `hibernate.hbm2ddl.auto` quedó en `create`; cambialo a `update` (paso 8). |
| Errores de librerías (`ClassNotFoundException`, imports en rojo) | Maven no descargó las dependencias; recargá el proyecto (paso 3). |
| `Stock insuficiente en la base de datos.` | La venta se canceló completa porque algún producto no tenía stock suficiente. Es el comportamiento esperado. |
| Error de versión de Java | Verificá que el IDE use un JDK 17 o superior (*File > Project Structure > SDK*). |
| El backup manual falla o dice "Error crítico de sistema" | La ruta de `mysqldump.exe` en `BackupController` es incorrecta, o el puerto/credenciales no coinciden con tu MySQL (sección 10). |
| "Windows rechazó la orden. Ejecute su IDE como Administrador." | Al programar el backup faltan permisos. Abrí el IDE o el programa con *Ejecutar como administrador*. |
| El backup programado se hizo pero no llegó el correo | Revisá `$Username`, `$Password` (debe ser una contraseña de aplicación de Gmail) y `$Destino` en `programarBackup()`. Verificá también la carpeta spam. |
| "Autorización denegada" al restaurar | Hay que ingresar credenciales válidas de un Administrador y de un Gerente a la vez. |
