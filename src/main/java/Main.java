import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import controlador.DatabaseSeeder;
import vista.LoginUI;
import controlador.LoginController;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        // 1. Inicializamos la conexión a la base de datos (NexcellPU debe coincidir con el XML)
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("NexcellPU");
        EntityManager em = emf.createEntityManager();

        // 2. Llamamos al seeder
        DatabaseSeeder.inicializarDatos(em);

        // 3. Ejecutamos la interfaz gráfica en el hilo seguro de Swing
        SwingUtilities.invokeLater(() -> {

            // Se aplica las configuraciones hechas al FlatLaf
            vista.TemaNexcell.aplicarTema();

            LoginUI ventanaLogin = new LoginUI();
            // Le inyectamos el EntityManager al controlador para que pueda hacer consultas
            LoginController controlador = new LoginController(ventanaLogin, em);

            ventanaLogin.setVisible(true);
        });
    }
}