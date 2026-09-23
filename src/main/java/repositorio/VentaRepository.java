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
        // Usamos JOIN FETCH para evitar el bloqueo de Hibernate al leer los detalles en la tabla.
        // Trae la Venta unida a sus Detalles y al Cliente de forma segura.
        return em.createQuery(
            "SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.detalles LEFT JOIN FETCH v.cliente ORDER BY v.fecha DESC",
            Venta.class
        ).getResultList();
    }
    public List<Venta> listarPorVendedor(modelo.Usuario vendedor) {
        return em.createQuery(
            "SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.detalles LEFT JOIN FETCH v.cliente WHERE v.vendedor = :vendedor ORDER BY v.fecha DESC",
            Venta.class
        ).setParameter("vendedor", vendedor).getResultList();
    }
}
