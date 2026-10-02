TÍTULO DEL PROYECTO: Nexcell

PROPÓSITO DEL PROYECTO: Sistema de gestión con interfaz gráfica de escritorio y control de acceso por roles (Administrador, Gerente, Vendedor), utilizando persistencia de datos con Hibernate y MySQL.

VERSIÓN o FECHA: Septiembre 2026

CÓMO INICIAR ESTE PROYECTO:

1-Carga del proyecto: Al abrir el proyecto en su IDE, espere unos segundos para que Maven descargue las dependencias desde el pom.xml (Hibernate, FlatLaf, el conector de MySQL, etc.).

2-Base de datos: Cree una base de datos vacía en su MySQL local (por ejemplo, nexcell_db). No hace falta correr scripts ni crear tablas a mano; el código arma todo automáticamente.

3-Configuración de la conexión: Vaya a la carpeta resources/META-INF. Haga una copia del archivo de plantilla persistence.example.xml y renómbrela a persistence.xml. Abra ese nuevo archivo y complete con su usuario, contraseña y el nombre de la base de datos que acaba de crear.

AUTORES: Gomez Agustin - Leonardo Fonteina

INSTRUCCIONES PARA EL USUARIO:

1-Carga de datos de prueba (Seeder): El sistema incluye un generador que carga datos simulados para facilitar las pruebas.

* Para la primera ejecución, verifique que en su persistence.xml la configuración de Hibernate esté en "create" ().

* Ejecute la clase Main. En la consola verá cómo se arman las tablas y se insertan usuarios, productos, clientes y un historial de ventas.

2-Cambiar a modo "update" (Importante): Una vez que el programa termine de cargar y abra la ventana de Login, ciérrelo un momento. Vuelva al persistence.xml y cambie el valor "create" por "update". Esto es fundamental para que la base de datos no se formatee y conserve los registros en sus siguientes pruebas.

3-Cuentas de acceso: Para probar los distintos niveles de permisos, el sistema ya generó estas cuentas (la contraseña para todas es 123):

*Vendedor: ingrese con el usuario "pedro" (aquí podrá comprobar que el vendedor únicamente tiene acceso a su propio historial de ventas).

*Gerente: ingrese con el usuario "ana".

*Administrador: ingrese con el usuario "juan".
