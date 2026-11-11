# TÍTULO DEL PROYECTO: Nexcell

**PROPÓSITO DEL PROYECTO:** Sistema de gestión comercial con interfaz gráfica de escritorio (Java Swing + FlatLaf) y control de acceso por roles (**Administrador, Gerente, Vendedor**). Utiliza persistencia de datos híbrida mediante **JPA / Hibernate** para el mapeo objeto-relacional y **Procedimientos Almacenados (Stored Procedures) en MySQL** con transacciones atómicas para el procesamiento de ventas, control de stock concurrente y generación de reportes estadísticos. Incluye visualización de gráficos (**JFreeChart**) y exportación de documentos (**Apache POI** para Excel `.xlsx` e **iTextPDF** para comprobantes `.pdf`).

**FECHA:** Noviembre 2026

**AUTORES:** Gómez Agustín - Leonardo Fonteina

---

## CÓMO INICIAR ESTE PROYECTO

### 1. Carga del proyecto
Al abrir el proyecto en su IDE (IntelliJ IDEA recomendado), espere unos segundos para que **Maven** descargue todas las dependencias declaradas en el archivo `pom.xml` (Hibernate ORM, Conector MySQL, FlatLaf, JFreeChart, Apache POI e iTextPDF).

### 2. Creación de la Base de Datos
Abra su cliente de MySQL (MySQL Workbench, DBeaver, phpMyAdmin, etc.) y cree una base de datos vacía:
```sql
CREATE DATABASE nexcell_db;
```
*(Nota: Las tablas se generarán automáticamente gracias a Hibernate en la primera ejecución, pero **deberá ejecutar el script de Procedimientos Almacenados** detallado en el Paso 4).*

### 3. Configuración de la conexión (`persistence.xml`)
1. Diríjase a la carpeta `src/main/resources/META-INF`.
2. Haga una copia del archivo de plantilla `persistence.example.xml` y renómbrela a `persistence.xml`.
3. Abra `persistence.xml` y complete con su usuario, contraseña, puerto y el nombre de la base de datos (`nexcell_db`).

### 4. Instalación de Procedimientos Almacenados y Transacciones (Obligatorio)
Una vez creada la base de datos y generadas las tablas, ejecute el siguiente script SQL en su consola de **MySQL** sobre la base `nexcell_db`. Este script instala la transacción atómica de ventas (con lectura de carrito en formato JSON y bloqueo pesimista `FOR UPDATE`), las bajas lógicas y los motores de reportes para Vendedor y Gerente:

```sql
USE nexcell_db;

DELIMITER $$

-- 1. rendimiento_vendedores (Módulo Gerente)
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

-- 2. procesar_registro_venta (Transacción Atómica de Ventas con JSON)
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

    -- Si ocurre cualquier error, revierte toda la operación (Rollback automático)
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    -- 1. Inicia la transacción en MySQL
    START TRANSACTION;

    -- 2. Inserta la cabecera en la tabla 'ventas'
    INSERT INTO ventas (fecha, total, cliente_dni, usuario_id)
    VALUES (NOW(), p_total, p_cliente_dni, p_usuario_id);

    SET v_venta_id = LAST_INSERT_ID();

    -- 3. Recorre el JSON e impacta en 'detalle_ventas' y 'productos'
    SET v_cantidad_items = JSON_LENGTH(p_items_json);

    WHILE v_i < v_cantidad_items DO
        SET v_prod_id = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', v_i, '].id')));
        SET v_cantidad = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', v_i, '].cantidad')));
        SET v_precio = JSON_UNQUOTE(JSON_EXTRACT(p_items_json, CONCAT('$[', v_i, '].precio')));

        -- Bloqueo pesimista para evitar condiciones de carrera en el stock
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

-- 3. baja_logica_producto (Módulo Administrador)
DROP PROCEDURE IF EXISTS `baja_logica_producto`$$
CREATE PROCEDURE `baja_logica_producto`(
    IN p_id BIGINT
)
BEGIN
    UPDATE productos
    SET estado = 0,
        fecha_modificacion = NOW()
    WHERE id = p_id;
END$$

-- 4. reporte_mejores_clientes (Módulo Vendedor)
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

-- 5. reporte_rendimiento_categoria (Módulo Vendedor)
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

-- 6. reporte_resumen_diario (Módulo Vendedor)
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

-- 7. reporte_top_productos (Módulo Vendedor)
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

-- 8. reporte_ventas_detallado (Módulo Gerente)
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

-- 9. top_productos_mas_vendidos (Módulo Gerente)
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

---

## INSTRUCCIONES PARA EL USUARIO

### 1. Carga de datos de prueba (Seeder)
El sistema incluye un generador automático (`DataSeeder`) que carga datos simulados para facilitar las pruebas iniciales:
* Para la **primera ejecución**, verifique que en su `persistence.xml` la propiedad `hibernate.hbm2ddl.auto` esté configurada en `"create"`:
  ```xml
  <property name="hibernate.hbm2ddl.auto" value="create"/>
  ```
* Ejecute la clase `Main` (y asegúrese de haber corrido el script de Procedimientos Almacenados en su base de datos). En la consola verá cómo se arman las tablas y se insertan usuarios, productos, clientes y el historial de ventas.

### 2. Cambiar a modo `"update"` (Importante)
Una vez que el programa termine de cargar por primera vez y abra la ventana de Login:
1. Cierre la aplicación.
2. Vuelva al archivo `persistence.xml` y cambie el valor `"create"` por `"update"`:
   ```xml
   <property name="hibernate.hbm2ddl.auto" value="update"/>
   ```
   *Nota: Esto es fundamental para que la base de datos no se formatee en los próximos inicios y conserve tanto los registros nuevos como los procedimientos almacenados.*

### 3. Cuentas de acceso por defecto
Para probar los distintos niveles de permisos y funcionalidades, el sistema genera las siguientes cuentas (la contraseña para todas es **`123`**):

* **Vendedor (`pedro`):** Acceso a gestión de clientes (ABM), catálogo de productos activos con autocompletado, carrito de ventas transaccional, descarga de facturas en PDF y reportes propios exportables a Excel.
* **Gerente (`ana`):** Acceso a reportes globales de ventas detalladas, rendimiento y comisiones de vendedores, ranking de productos más vendidos, generación de gráficos estadísticos (polígonos de frecuencia y barras con JFreeChart) y exportación contable a Excel (`.xlsx`).
* **Administrador (`juan`):** Gestión de inventario, control de usuarios, visualización de logs de auditoría y módulo de **Backups** (copias manuales con `mysqldump`, programación de tareas automáticas en Windows con envío por email vía SMTP y restauración segura con doble factor de autorización Admin + Gerente).
