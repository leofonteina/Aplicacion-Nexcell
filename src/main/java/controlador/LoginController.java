package controlador;

import modelo.Usuario;
import vista.LoginUI;
import jakarta.persistence.EntityManager;
import javax.swing.*;
import java.util.List;

public class LoginController {

    private LoginUI vista;
    private EntityManager em;

    public LoginController(LoginUI vista, EntityManager em) {
        this.vista = vista;
        this.em = em;

        // Ejecutamos la autenticación al hacer clic
        this.vista.getLoginButton().addActionListener(e -> iniciarAutenticacion());
    }

    private void iniciarAutenticacion() {
        String user = vista.getUserField().getText();
        String pass = new String(vista.getPassField().getPassword());

        vista.getLoginButton().setEnabled(false);
        vista.getLoginButton().setText("Conectando...");


        SwingWorker<Usuario, Void> worker = new SwingWorker<Usuario, Void>() {

            @Override
            protected Usuario doInBackground() throws Exception {
                // Todo lo que está acá ocurre sin trabar la pantalla
                return consultarBaseDeDatos(user, pass);
            }

            @Override
            protected void done() {
                try {
                    Usuario usuarioAutenticado = get(); // Recibimos la respuesta de doInBackground

                    if (usuarioAutenticado != null) {
                        usuarioAutenticado.mostrarInterfaz(em);
                        vista.dispose();
                    } else {
                        JOptionPane.showMessageDialog(vista, "Credenciales incorrectas", "Error de Acceso", JOptionPane.ERROR_MESSAGE);
                        restaurarBoton();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(vista, "Error de conexión con la base de datos.", "Error Fatal", JOptionPane.ERROR_MESSAGE);
                    System.err.println("Error en hilo de login: " + ex.getMessage());
                    restaurarBoton();
                }
            }
        };

        worker.execute();
    }

    private void restaurarBoton() {
        vista.getLoginButton().setEnabled(true);
        vista.getLoginButton().setText("Ingresar al Sistema");
    }

    private Usuario consultarBaseDeDatos(String user, String pass) {
        try {
            // Limpiamos transacciones pendientes y vaciamos la memoria caché
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.clear();

            List<Usuario> resultados = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.username = :user AND u.password = :pass", Usuario.class)
                .setParameter("user", user)
                .setParameter("pass", pass)
                .getResultList();

            if (!resultados.isEmpty()) {
                return resultados.get(0);
            }
        } catch (Exception e) {
            System.err.println("Error al consultar la BD: " + e.getMessage());
            throw e;
        }

        return null;
    }


}
