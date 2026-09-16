package modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT") // TEXT permite descripciones más largas
    private String descripcion;

    private String categoria;
    private String marca;
    private int stock;
    private double precio;
    private double descuento;

    private boolean estado; // true = activo, false = inactivo

    private String rutaImagen;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // Hibernate exige un constructor vacío
    public Producto() {}

    // --- Getters ---
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public String getMarca() { return marca; }
    public int getStock() { return stock; }
    public double getPrecio() { return precio; }
    public double getDescuento() { return descuento; }
    public boolean isEstado() { return estado; }
    public String getRutaImagen() { return rutaImagen; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }

    // --- Setters ---
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setMarca(String marca) { this.marca = marca; }
    public void setStock(int stock) { this.stock = stock; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setDescuento(double descuento) { this.descuento = descuento; }
    public void setEstado(boolean estado) { this.estado = estado; }
    public void setRutaImagen(String rutaImagen) { this.rutaImagen = rutaImagen; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}