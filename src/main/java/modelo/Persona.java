package modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDate;

@MappedSuperclass
public abstract class Persona {

    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    @Column(name = "fecha_registro", updatable = false)
    private java.time.LocalDate fechaRegistro;

    @Embedded
    private Direccion direccion;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    public Persona() {
        // Cada vez que se instancie un objeto, se guarda la fecha exacta automáticamente
        this.fechaRegistro = java.time.LocalDate.now();
    }

    // --- GETTERS Y SETTERS ---
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Direccion getDireccion() { return direccion; }
    public void setDireccion(Direccion direccion) { this.direccion = direccion; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public java.time.LocalDate getFechaRegistro() { return fechaRegistro; }
}

