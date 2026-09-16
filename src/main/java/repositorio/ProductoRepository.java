package repositorio;

import jakarta.persistence.EntityManager;
import modelo.Producto;
import java.util.List;

public class ProductoRepository {

    private EntityManager em;

    // Inyectamos la conexión al instanciar el repositorio
    public ProductoRepository(EntityManager em) {
        this.em = em;
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
