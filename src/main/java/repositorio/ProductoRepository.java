package repositorio;

import jakarta.persistence.EntityManager;
import modelo.Producto;
import java.util.List;
import utilidades.GestorLogs;

public class ProductoRepository {

    private EntityManager em;

    // Inyectamos la conexión al instanciar el repositorio
    public ProductoRepository(EntityManager em) {
        this.em = em;
    }

    public void darDeBajaProducto(Long idProducto) {
        em.getTransaction().begin();
        try {
            // 1. Ejecutamos el Procedimiento Almacenado directamente en MySQL
            em.createNativeQuery("CALL baja_logica_producto(:id)")
                    .setParameter("id", idProducto)
                    .executeUpdate();

            em.getTransaction().commit();

            // Vaciamos la memoria caché de Hibernate.
            // Esto lo obliga a leer los datos reales de MySQL en la próxima consulta.
            em.clear();

        } catch (Exception e) {
            em.getTransaction().rollback();

            // Si algo falla, también se guarda en el log
            GestorLogs.registrar(GestorLogs.Nivel.ERROR, "PRODUCTOS", "Fallo al dar de baja el producto ID: " + idProducto + ". Motivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // CREATE: Guarda un producto nuevo
    public void guardar(Producto producto) {
        em.getTransaction().begin();
        em.persist(producto);
        em.getTransaction().commit();
    }

    // UPDATE: Actualiza un producto existente en la base de datos
    public void actualizar(Producto producto) {
        em.getTransaction().begin();
        em.merge(producto); // merge reemplaza los datos viejos por los nuevos
        em.getTransaction().commit();
    }

    // READ: Busca un producto específico por su ID interno
    public Producto buscarPorId(Long id) {
        return em.find(Producto.class, id);
    }

    // READ: Trae todo el inventario de la base de datos
    public List<Producto> listarTodos() {
        // Usamos JPQL para consultar la entidad Producto
        return em.createQuery("SELECT p FROM Producto p", Producto.class).getResultList();
    }
}
