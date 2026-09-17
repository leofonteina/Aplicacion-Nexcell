package controlador;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.LocalDate;
import modelo.Producto;
import modelo.Admin;
import modelo.Gerente;
import modelo.Vendedor;
import modelo.Direccion;

public class DatabaseSeeder {

    public static void inicializarDatos(EntityManager em) {

        System.out.println("Comprobando el estado de la base de datos...");

        seederProductos(em);
        seederUsuarios(em);
    }

    private static void seederProductos(EntityManager em) {
        // Revisamos si ya existen productos en la base de datos
        Long cantidadProductos = em.createQuery("SELECT COUNT(p) FROM Producto p", Long.class).getSingleResult();

        if (cantidadProductos == 0) {
            System.out.println("Ejecutando Seeder de productos...");

            try {
                em.getTransaction().begin();
                LocalDateTime ahora = LocalDateTime.now();

                // SMARTPHONES
                registrarProducto(em, "Iphone 15 Pro Max", "iPhone 15 Pro Max Apple", 50, 0, 2176900.0, "Apple", "Smartphone", true, ahora, ahora);
                registrarProducto(em, "Samsung S25 Ultra", "Samsung Galaxy S25 Ultra", 50, 0, 1771900.0, "Samsung", "Smartphone", true, ahora, ahora);
                registrarProducto(em, "Poco F7", "Xiaomi Poco F7", 50, 0, 2316100.0, "Xiaomi", "Smartphone", true, ahora, ahora);
                registrarProducto(em, "Motorola Edge 60 Pro", "Motorola Edge 60 Pro", 50, 0, 526500.0, "Motorola", "Smartphone", true, ahora, ahora);
                registrarProducto(em, "Iphone 14", "Apple Iphone 14", 50, 0, 1400000.0, "Apple", "Smartphone", true, ahora, ahora);
                registrarProducto(em, "Iphone 12", "Apple Iphone 12", 50, 0, 950000.0, "Apple", "Smartphone", true, ahora, ahora);
                registrarProducto(em, "Motorola Razr 40", "Motorola Razr 40", 50, 0, 1200000.0, "Motorola", "Smartphone", true, ahora, ahora);
                registrarProducto(em, "Motorola G56", "Motorola G56", 50, 0, 420000.0, "Motorola", "Smartphone", true, ahora, ahora);
                registrarProducto(em, "Motorola G84", "Motorola G84", 50, 0, 480000.0, "Motorola", "Smartphone", true, ahora, ahora);
                registrarProducto(em, "Samsung Galaxy S26 Ultra", "Samsung Galaxy S26 Ultra", 50, 0, 2100000.0, "Samsung", "Smartphone", true, ahora, ahora);
                registrarProducto(em, "Xiaomi Poco F8 Pro", "Xiaomi Poco F8 Pro", 50, 0, 1100000.0, "Xiaomi", "Smartphone", true, ahora, ahora);
                registrarProducto(em, "Xiaomi Redmi Note 14", "Xiaomi Redmi Note 14", 50, 0, 350000.0, "Xiaomi", "Smartphone", true, ahora, ahora);

                // ACCESORIOS Y AURICULARES
                registrarProducto(em, "Apple AirPods (3ra generacion)", "AirPods Apple", 50, 0, 45000.0, "Apple", "Auricular", true, ahora, ahora);
                registrarProducto(em, "Cargador Samsung 25W", "Cargador Samsung", 50, 0, 18000.0, "Samsung", "Accesorio", true, ahora, ahora);
                registrarProducto(em, "Funda de silicona - iPhone 17", "Funda para iPhone", 50, 0, 8500.0, "Apple", "Accesorio", true, ahora, ahora);
                registrarProducto(em, "Auriculares JBL", "Auriculares JBL vincha", 50, 0, 8500.0, "JBL", "Auricular", true, ahora, ahora);
                registrarProducto(em, "Auriculares Apple", "Auriculares Apple vincha", 50, 0, 8500.0, "Apple", "Auricular", true, ahora, ahora);

                // PARLANTES Y SMARTWATCHES
                registrarProducto(em, "Parlante JBL Charge 5", "Parlante JBL Charge 5", 50, 0, 8500.0, "JBL", "Parlante", true, ahora, ahora);
                registrarProducto(em, "Parlante JBL Party Box", "Parlante JBL Party Box", 50, 0, 8500.0, "JBL", "Parlante", true, ahora, ahora);
                registrarProducto(em, "SmartWatch Samsung", "SmartWatch Samsung", 50, 0, 8500.0, "Samsung", "SmartWatch", true, ahora, ahora);

                em.getTransaction().commit();
                System.out.println("✅ Seeder completado: 20 productos registrados exitosamente.");

            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                e.printStackTrace();
            }
        } else {
            System.out.println("✅ La base de datos ya tiene productos. Seeder omitido.");
        }
    }

    private static void seederUsuarios(EntityManager em) {
        // Revisamos si ya existen usuarios
        Long cantidadUsuarios = em.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class).getSingleResult();

        if (cantidadUsuarios == 0) {
            System.out.println("Ejecutando Seeder de usuarios...");

            try {
                em.getTransaction().begin();

                // Direcciones únicas para cada usuario
                Direccion dir1 = new Direccion("San Martin", "1050", "Corrientes", "Corrientes");
                Direccion dir2 = new Direccion("Junin", "1423", "Corrientes", "Corrientes");
                Direccion dir3 = new Direccion("Av. 3 de Abril", "850", "Corrientes", "Corrientes");
                Direccion dir4 = new Direccion("Pellegrini", "980", "Corrientes", "Corrientes");
                Direccion dir5 = new Direccion("Salta", "1210", "Corrientes", "Corrientes");
                Direccion dir6 = new Direccion("Tucuman", "540", "Corrientes", "Corrientes");
                Direccion dir7 = new Direccion("Buenos Aires", "1100", "Corrientes", "Corrientes");
                Direccion dir8 = new Direccion("San Lorenzo", "732", "Corrientes", "Corrientes");
                Direccion dir9 = new Direccion("Santa Fe", "645", "Corrientes", "Corrientes");
                Direccion dir10 = new Direccion("Catamarca", "1300", "Corrientes", "Corrientes");

                // --- 3 ADMINISTRADORES ---
                registrarUsuario(em, new Admin("juan", "123"), "Juan", "Pérez", "11111111", "juan@mail.com", dir1, LocalDate.of(1985, 5, 10));
                registrarUsuario(em, new Admin("maria", "123"), "María", "Gómez", "22222222", "maria@mail.com", dir2, LocalDate.of(1990, 8, 20));
                registrarUsuario(em, new Admin("carlos", "123"), "Carlos", "López", "33333333", "carlos@mail.com", dir3, LocalDate.of(1982, 12, 5));

                // --- 1 GERENTE ---
                registrarUsuario(em, new Gerente("ana", "123"), "Ana", "Martínez", "44444444", "ana@mail.com", dir4, LocalDate.of(1978, 3, 15));

                // --- 6 VENDEDORES ---
                registrarUsuario(em, new Vendedor("pedro", "123"), "Pedro", "Sánchez", "55555555", "pedro@mail.com", dir5, LocalDate.of(1995, 7, 25));
                registrarUsuario(em, new Vendedor("lucia", "123"), "Lucía", "Fernández", "66666666", "lucia@mail.com", dir6, LocalDate.of(1998, 1, 30));
                registrarUsuario(em, new Vendedor("diego", "123"), "Diego", "Ramírez", "77777777", "diego@mail.com", dir7, LocalDate.of(2000, 11, 11));
                registrarUsuario(em, new Vendedor("sofia", "123"), "Sofía", "Torres", "88888888", "sofia@mail.com", dir8, LocalDate.of(1996, 4, 18));
                registrarUsuario(em, new Vendedor("martin", "123"), "Martín", "Díaz", "99999999", "martin@mail.com", dir9, LocalDate.of(1993, 9, 9));
                registrarUsuario(em, new Vendedor("elena", "123"), "Elena", "Ruiz", "10101010", "elena@mail.com", dir10, LocalDate.of(1999, 2, 22));

                em.getTransaction().commit();
                System.out.println("✅ Seeder completado: 10 usuarios registrados exitosamente.");

            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                e.printStackTrace();
            }
        } else {
            System.out.println("✅ La base de datos ya tiene usuarios. Seeder omitido.");
        }
    }


    // Método auxiliar para Productos
    private static void registrarProducto(EntityManager em, String nombre, String descripcion, int stock, double descuento, double precio, String marca, String categoria, boolean estado, LocalDateTime fechaCreacion, LocalDateTime fechaModificacion) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setStock(stock);
        p.setDescuento(descuento);
        p.setPrecio(precio);
        p.setMarca(marca);
        p.setCategoria(categoria);
        p.setEstado(estado);
        p.setFechaCreacion(fechaCreacion);
        p.setFechaModificacion(fechaModificacion);
        em.persist(p);
    }

    // Método auxiliar para Usuarios
    private static void registrarUsuario(EntityManager em, modelo.Usuario usuario, String nombre, String apellido, String dni, String email, Direccion direccion, LocalDate fechaNac) {
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setDni(dni);
        usuario.setEmail(email);
        usuario.setDireccion(direccion);
        usuario.setFechaNacimiento(fechaNac);
        // El estado y la fecha de registro se configuran solos en el constructor

        em.persist(usuario);
    }
}