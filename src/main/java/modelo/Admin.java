package modelo;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import vista.AdminUI;
import controlador.AdminController;

@Entity
@DiscriminatorValue("ADMIN") // Valor para la columna "rol"
public class Admin extends Usuario {

    public Admin() {}

    public Admin(String username, String password) {
        super(username, password);
    }

    @Override
    public void mostrarInterfaz(EntityManager em) {
        // Aquí es donde se instancia la vista y el controlador del administrador
        AdminUI ventanaAdmin = new AdminUI();
        new AdminController(ventanaAdmin, em);
        ventanaAdmin.setVisible(true);
    }
}
