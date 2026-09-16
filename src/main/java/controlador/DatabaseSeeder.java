package controlador;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import modelo.Producto;

public class DatabaseSeeder {

    public static void inicializarDatos(EntityManager em) {
        // Revisamos si ya existen productos en la base de datos
        Long cantidadProductos = em.createQuery("SELECT COUNT(p) FROM Producto p", Long.class).getSingleResult();

        if (cantidadProductos == 0) {
            System.out.println("Base de datos vacía. Ejecutando Seeder de productos...");

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
            System.out.println("La base de datos ya tiene productos. Seeder omitido.");
        }
    }

    // Método auxiliar para mantener el código limpio y evitar usar constructores gigantes
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
}