package modelo;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @Column(length = 15)
    private String dni;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    // --- NUEVOS CAMPOS HOMOLOGADOS CON USUARIO ---
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_registro", updatable = false)
    private LocalDate fechaRegistro;

    @Embedded
    private Direccion direccion;

    private boolean activo;

    public Cliente() {
        this.fechaRegistro = LocalDate.now();
        this.activo = true;
    }

    public Cliente(String dni, String nombre, String apellido, String telefono, String email, LocalDate fechaNacimiento, Direccion direccion) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.email = email;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.fechaRegistro = LocalDate.now();
        this.activo = true;
    }

    // --- GETTERS ---
    public String getDni() { return dni; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public Direccion getDireccion() { return direccion; }
    public boolean isActivo() { return activo; }

    // --- SETTERS ---
    public void setDni(String dni) { this.dni = dni; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setEmail(String email) { this.email = email; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public void setDireccion(Direccion direccion) { this.direccion = direccion; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
