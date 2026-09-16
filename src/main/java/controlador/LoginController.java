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
        this.vista.getLoginButton().addActionListener(e -> autenticar());
    }

    private void autenticar() {
        String user = vista.getUserField().getText();
        String pass = new String(vista.getPassField().getPassword());

        Usuario usuarioAutenticado = consultarBaseDeDatos(user, pass);

        if (usuarioAutenticado != null) {
            usuarioAutenticado.mostrarInterfaz(em);
            vista.dispose();
        } else {
            JOptionPane.showMessageDialog(vista, "Credenciales incorrectas", "Error de Acceso", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Usuario consultarBaseDeDatos(String user, String pass) {
        try {
            List<Usuario> resultados = em.createQuery(
                            "SELECT u FROM Usuario u WHERE u.username = :user AND u.password = :pass", Usuario.class)
                    .setParameter("user", user)
                    .setParameter("pass", pass)
                    .getResultList();

            if (!resultados.isEmpty()) {
                return resultados.get(0);
            }
        } catch (Exception e) {
            System.out.println("Error al consultar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}