package vista;

import javax.swing.*;
import java.awt.*;

public class LoginUI extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;

    public LoginUI() {
        setTitle("Iniciar Sesión - Nexcell");
        setSize(350, 300); // Un poco más alto para el nuevo diseño
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Panel principal con BorderLayout para separar el logo del formulario
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        panelPrincipal.setOpaque(false);

        // --- Título/Logo superior ---
        JLabel lblLogo = new JLabel("Nexcell.", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblLogo.setForeground(new Color(255, 120, 30)); // Naranja exacto de Nexcell
        lblLogo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panelPrincipal.add(lblLogo, BorderLayout.NORTH);

        // --- Panel del formulario ---
        JPanel panelForm = new JPanel(new GridLayout(4, 1, 5, 5));
        panelForm.setOpaque(false); // Transparente para heredar el azul oscuro

        JLabel userLabel = new JLabel("Usuario:");
        userField = new JTextField();

        JLabel passLabel = new JLabel("Contraseña:");
        passField = new JPasswordField();

        panelForm.add(userLabel);
        panelForm.add(userField);
        panelForm.add(passLabel);
        panelForm.add(passField);

        panelPrincipal.add(panelForm, BorderLayout.CENTER);

        // --- Botón inferior ---
        loginButton = new JButton("Ingresar al Sistema");
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setOpaque(false);
        panelBoton.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        panelBoton.add(loginButton);

        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);

        // Cambiamos el color de fondo de la ventana principal
        getContentPane().setBackground(new Color(10, 25, 47));
        add(panelPrincipal);
    }

    public JTextField getUserField() { return userField; }
    public JPasswordField getPassField() { return passField; }
    public JButton getLoginButton() { return loginButton; }
}