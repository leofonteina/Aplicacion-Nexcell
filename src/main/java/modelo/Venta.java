package modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "cliente_dni", nullable = false)
    private Cliente cliente;

    private double total;

    // CascadeType.ALL hace que al guardar la Venta, se guarden todos sus detalles automáticamente
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    private List<DetalleVenta> detalles = new ArrayList<>();

    public Venta() {}

    public Long getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public Cliente getCliente() { return cliente; }
    public double getTotal() { return total; }
    public List<DetalleVenta> getDetalles() { return detalles; }

    public void setId(Long id) { this.id = id; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public void setTotal(double total) { this.total = total; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }

    // Método auxiliar para agregar detalles fácilmente
    public void agregarDetalle(DetalleVenta detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }
}
