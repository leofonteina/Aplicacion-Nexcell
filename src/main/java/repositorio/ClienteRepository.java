package repositorio;

import jakarta.persistence.EntityManager;
import modelo.Cliente; // Asegurate de tener tu clase Cliente creada en el paquete modelo
import java.util.List;

public class ClienteRepository {

    private EntityManager em;

    public ClienteRepository(EntityManager em) {
        this.em = em;
    }

    // CREATE: Guardar un nuevo cliente
    public void guardar(Cliente cliente) {
        em.persist(cliente);
    }

    // UPDATE: Actualizar datos de un cliente existente
    public void actualizar(Cliente cliente) {
        em.getTransaction().begin();
        em.merge(cliente); // MERGE equivale a un UPDATE
        em.getTransaction().commit();
    }

    // READ: Traer todos los clientes para llenar la tabla
    public List<Cliente> listarTodos() {
        return em.createQuery("SELECT c FROM Cliente c", Cliente.class).getResultList();
    }

    // READ: Buscar un cliente específico por su DNI (útil para validaciones)
    public Cliente buscarPorDni(String dni) {
        try {
            return em.createQuery("SELECT c FROM Cliente c WHERE c.dni = :dni", Cliente.class)
                .setParameter("dni", dni)
                .getSingleResult();
        } catch (Exception e) {
            return null; // Si no lo encuentra, devuelve null
        }
    }

    // VALIDACIÓN: Verificar si un DNI ya existe antes de registrar
    public boolean existeDni(String dni) {
        try {
            Long count = em.createQuery("SELECT COUNT(c) FROM Cliente c WHERE c.dni = :dni", Long.class)
                .setParameter("dni", dni)
                .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // BÚSQUEDA: Para la barra de búsqueda (por DNI, Nombre o Apellido)
    public List<Cliente> buscarPorTexto(String texto) {
        return em.createQuery(
                "SELECT c FROM Cliente c WHERE c.dni LIKE :texto OR c.nombre LIKE :texto OR c.apellido LIKE :texto",
                Cliente.class)
            .setParameter("texto", "%" + texto + "%")
            .getResultList();
    }
}
