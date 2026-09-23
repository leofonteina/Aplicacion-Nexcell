package repositorio;

import jakarta.persistence.EntityManager;
import modelo.Venta;
import java.util.List;

public class VentaRepository {

    private EntityManager em;

    public VentaRepository(EntityManager em) {
        this.em = em;
    }

    public void guardar(Venta venta) {
        em.persist(venta);
    }

    public List<Venta> listarTodas() {
        // Ordenamos por fecha descendente para ver las más recientes primero
        return em.createQuery("SELECT v FROM Venta v ORDER BY v.fecha DESC", Venta.class).getResultList();
    }
}
