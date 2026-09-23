package vista;

import javax.swing.*;
import java.awt.*;

public class RegistroClienteUI extends JDialog {

    private JTextField dniClienteField;
    private JTextField nombreClienteField;
    private JTextField apellidoClienteField;
    private JTextField telefonoClienteField;
    private JTextField emailClienteField;
    private JButton btnGuardarCliente;

    public RegistroClienteUI(JFrame parent) {
        super(parent, "Registrar Nuevo Cliente", true);
        setSize(450, 400); // Un poco más grande para que respire
        setLocationRelativeTo(parent);
        setResizable(false);

        // Panel principal
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // --- Título Superior ---
        JLabel lblTitulo = new JLabel("Registrar Cliente", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // --- Panel de Formulario ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font fuenteLabel = new Font("Segoe UI", Font.PLAIN, 14);

        // Fila 0: DNI
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblDni = new JLabel("DNI:"); lblDni.setFont(fuenteLabel);
        panelForm.add(lblDni, gbc);
        gbc.gridx = 1; dniClienteField = new JTextField(15); panelForm.add(dniClienteField, gbc);

        // Fila 1: Nombre
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblNombre = new JLabel("Nombre:"); lblNombre.setFont(fuenteLabel);
        panelForm.add(lblNombre, gbc);
        gbc.gridx = 1; nombreClienteField = new JTextField(15); panelForm.add(nombreClienteField, gbc);

        // Fila 2: Apellido
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblApellido = new JLabel("Apellido:"); lblApellido.setFont(fuenteLabel);
        panelForm.add(lblApellido, gbc);
        gbc.gridx = 1; apellidoClienteField = new JTextField(15); panelForm.add(apellidoClienteField, gbc);

        // Fila 3: Teléfono
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel lblTelefono = new JLabel("Teléfono:"); lblTelefono.setFont(fuenteLabel);
        panelForm.add(lblTelefono, gbc);
        gbc.gridx = 1; telefonoClienteField = new JTextField(15); panelForm.add(telefonoClienteField, gbc);

        // Fila 4: Email
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel lblEmail = new JLabel("Email:"); lblEmail.setFont(fuenteLabel);
        panelForm.add(lblEmail, gbc);
        gbc.gridx = 1; emailClienteField = new JTextField(15); panelForm.add(emailClienteField, gbc);

        panelPrincipal.add(panelForm, BorderLayout.CENTER);

        // --- Panel Inferior ---
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnGuardarCliente = new JButton("Guardar Cliente");
        btnGuardarCliente.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardarCliente.setMargin(new Insets(8, 20, 8, 20));
        panelBoton.add(btnGuardarCliente);

        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    // --- Getters para el controlador ---
    public JTextField getDniClienteField() { return dniClienteField; }
    public JTextField getNombreClienteField() { return nombreClienteField; }
    public JTextField getApellidoClienteField() { return apellidoClienteField; }
    public JTextField getTelefonoClienteField() { return telefonoClienteField; }
    public JTextField getEmailClienteField() { return emailClienteField; }
    public JButton getBtnGuardarCliente() { return btnGuardarCliente; }
}
