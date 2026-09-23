package vista;

import javax.swing.*;
import java.awt.*;

public class LoginUI extends JFrame {

    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;

    public LoginUI() {
        setTitle("Iniciar Sesión - Nexcell");
        setSize(450, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        panelPrincipal.setOpaque(false);

        JLabel lblLogo = new JLabel("Nexcell.", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblLogo.setForeground(new Color(255, 120, 30));
        lblLogo.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        panelPrincipal.add(lblLogo, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridLayout(4, 1, 8, 8));
        panelForm.setOpaque(false);

        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        userLabel.setForeground(Color.WHITE);
        userField = new JTextField();
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JLabel passLabel = new JLabel("Contraseña:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passLabel.setForeground(Color.WHITE);
        passField = new JPasswordField();
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        panelForm.add(userLabel);
        panelForm.add(userField);
        panelForm.add(passLabel);
        panelForm.add(passField);

        panelPrincipal.add(panelForm, BorderLayout.CENTER);

        loginButton = new JButton("Ingresar al Sistema");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(200, 40));

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBoton.setOpaque(false);
        panelBoton.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));
        panelBoton.add(loginButton);

        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);

        getContentPane().setBackground(new Color(10, 25, 47));
        add(panelPrincipal);
    }

    public JTextField getUserField() { return userField; }
    public JPasswordField getPassField() { return passField; }
    public JButton getLoginButton() { return loginButton; }
}
