package modelo;

import jakarta.persistence.Embeddable;

@Embeddable
public class Direccion {

    private String calle;
    private String altura;
    private String ciudad;
    private String provincia;

    public Direccion() {}

    public Direccion(String calle, String altura, String ciudad, String provincia) {
        this.calle = calle;
        this.altura = altura;
        this.ciudad = ciudad;
        this.provincia = provincia;
    }

    // --- GETTERS Y SETTERS ---
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }

    public String getAltura() { return altura; }
    public void setAltura(String altura) { this.altura = altura; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
}
