package modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import vista.VendedorUI;
import controlador.VendedorController;
import jakarta.persistence.EntityManager;

@Entity
@DiscriminatorValue("VENDEDOR")
public class Vendedor extends Usuario {

    public Vendedor() {
        super();
    }

    public Vendedor(String username, String password) {
        super(username, password);
    }

    @Override
    public void mostrarInterfaz(EntityManager em) {
        VendedorUI vista = new VendedorUI();

        // ACÁ ESTÁ EL CAMBIO: Le pasamos 'this' (este vendedor) como tercer parámetro
        new VendedorController(vista, em, this);

        vista.setVisible(true);
    }
}
