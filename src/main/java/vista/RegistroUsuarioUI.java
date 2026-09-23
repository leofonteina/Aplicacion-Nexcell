package vista;

import javax.swing.*;
import java.awt.*;

public class RegistroUsuarioUI extends JDialog {

    // Campos de texto
    private JTextField txtNombre, txtApellido, txtDni, txtEmail, txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtCalle, txtAltura, txtCiudad;
    private JComboBox<String> cbProvincia, cbPerfil;
    private JComboBox<Integer> cbDia, cbAnio;
    private JComboBox<String> cbMes;
    private JButton btnGuardarUsuario;
    private JButton btnCancelar;

    // Etiquetas de Error (Una para cada campo a validar)
    private JLabel lblErrorNombre, lblErrorApellido, lblErrorDni, lblErrorFecha;
    private JLabel lblErrorCalle, lblErrorAltura, lblErrorCiudad;
    private JLabel lblErrorEmail, lblErrorUsername, lblErrorPassword;

    public RegistroUsuarioUI(JFrame parent) {
        super(parent, "Registrar Nuevo Usuario", true);
        setSize(480, 750); // Agrandamos un poco para dar espacio a los mensajes de error
        setLocationRelativeTo(parent);
        setResizable(false);

        // Instanciamos todas las etiquetas de error con su formato
        inicializarEtiquetasDeError();

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- DATOS PERSONALES ---
        JPanel panelPersonales = crearPanelFormulario("Datos Personales");
        agregarCampoConError(panelPersonales, "Nombre:", txtNombre = new JTextField(15), lblErrorNombre, 0);
        agregarCampoConError(panelPersonales, "Apellido:", txtApellido = new JTextField(15), lblErrorApellido, 2);
        agregarCampoConError(panelPersonales, "DNI:", txtDni = new JTextField(15), lblErrorDni, 4);

        JPanel panelFecha = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        Integer[] dias = new Integer[31]; for (int i = 0; i < 31; i++) dias[i] = i + 1;
        String[] meses = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        Integer[] anios = new Integer[100]; int anioActual = java.time.LocalDate.now().getYear();
        for (int i = 0; i < 100; i++) anios[i] = anioActual - i;
        panelFecha.add(cbDia = new JComboBox<>(dias)); panelFecha.add(new JLabel(" / "));
        panelFecha.add(cbMes = new JComboBox<>(meses)); panelFecha.add(new JLabel(" / "));
        panelFecha.add(cbAnio = new JComboBox<>(anios));
        agregarCampoConError(panelPersonales, "F. Nacimiento:", panelFecha, lblErrorFecha, 6);

        // --- DIRECCIÓN ---
        JPanel panelDireccion = crearPanelFormulario("Dirección");
        agregarCampoConError(panelDireccion, "Calle:", txtCalle = new JTextField(15), lblErrorCalle, 0);
        agregarCampoConError(panelDireccion, "Altura:", txtAltura = new JTextField(15), lblErrorAltura, 2);
        agregarCampoConError(panelDireccion, "Ciudad:", txtCiudad = new JTextField(15), lblErrorCiudad, 4);

        String[] provincias = {"Corrientes", "Chaco", "Misiones", "Formosa", "Entre Ríos", "Santa Fe", "Buenos Aires", "Otra"};

        agregarCampo(panelDireccion, "Provincia:", cbProvincia = new JComboBox<>(provincias), 6);

        // --- DATOS DE CUENTA ---
        JPanel panelCuenta = crearPanelFormulario("Datos de Cuenta");
        agregarCampoConError(panelCuenta, "Email:", txtEmail = new JTextField(15), lblErrorEmail, 0);
        agregarCampoConError(panelCuenta, "Usuario:", txtUsername = new JTextField(15), lblErrorUsername, 2);
        agregarCampoConError(panelCuenta, "Contraseña:", txtPassword = new JPasswordField(15), lblErrorPassword, 4);

        String[] roles = {"Administrador", "Gerente", "Vendedor"};
        agregarCampo(panelCuenta, "Rol del Sistema:", cbPerfil = new JComboBox<>(roles), 6);

        panelPrincipal.add(panelPersonales);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 5)));
        panelPrincipal.add(panelDireccion);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 5)));
        panelPrincipal.add(panelCuenta);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 10)));

        // Botones
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.X_AXIS));

        btnCancelar = new JButton("Cancelar");
        btnGuardarUsuario = new JButton("Guardar Usuario");

        panelBotones.add(btnCancelar);
        panelBotones.add(Box.createHorizontalGlue());
        panelBotones.add(btnGuardarUsuario);

        panelPrincipal.add(panelBotones);

        add(panelPrincipal);
    }

    // --- MÉTODOS DE DISEÑO Y ESTRUCTURA ---

    private void inicializarEtiquetasDeError() {
        lblErrorNombre = crearEtiquetaError();
        lblErrorApellido = crearEtiquetaError();
        lblErrorDni = crearEtiquetaError();
        lblErrorFecha = crearEtiquetaError();
        lblErrorCalle = crearEtiquetaError();
        lblErrorAltura = crearEtiquetaError();
        lblErrorCiudad = crearEtiquetaError();
        lblErrorEmail = crearEtiquetaError();
        lblErrorUsername = crearEtiquetaError();
        lblErrorPassword = crearEtiquetaError();
    }

    private JLabel crearEtiquetaError() {
        JLabel lbl = new JLabel("");
        lbl.setForeground(Color.RED);
        lbl.setFont(new Font("Arial", Font.BOLD, 10)); // Negrita para que resalte
        return lbl;
    }

    public void limpiarErrores() {
        lblErrorNombre.setText("");
        lblErrorApellido.setText("");
        lblErrorDni.setText("");
        lblErrorFecha.setText("");
        lblErrorCalle.setText("");
        lblErrorAltura.setText("");
        lblErrorCiudad.setText("");
        lblErrorEmail.setText("");
        lblErrorUsername.setText("");
        lblErrorPassword.setText("");
    }

    private JPanel crearPanelFormulario(String titulo) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("--- " + titulo + " ---"));
        return panel;
    }

    // Agrega el campo normal (usado para Combobox que no fallan)
    private void agregarCampo(JPanel panel, String label, JComponent componente, int fila) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = fila; panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; panel.add(componente, gbc);
    }


    private void agregarCampoConError(JPanel panel, String labelText, JComponent componente, JLabel lblError, int filaBase) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Label principal
        gbc.insets = new Insets(5, 10, 0, 10);
        gbc.gridx = 0; gbc.gridy = filaBase;
        panel.add(new JLabel(labelText), gbc);

        // Componente de ingreso (TextField)
        gbc.gridx = 1; gbc.gridy = filaBase;
        panel.add(componente, gbc);

        // Etiqueta de Error (Justo debajo del TextField)
        gbc.insets = new Insets(0, 10, 5, 10); // Sin margen superior para que se pegue a la caja
        gbc.gridx = 1; gbc.gridy = filaBase + 1;
        panel.add(lblError, gbc);
    }

    // --- GETTERS COMPONENTES ---
    public JTextField getTxtNombre() { return txtNombre; }
    public JTextField getTxtApellido() { return txtApellido; }
    public JTextField getTxtUsername() { return txtUsername; }
    public JPasswordField getTxtPassword() { return txtPassword; }
    public JTextField getTxtEmail() { return txtEmail; }
    public JTextField getTxtDni() { return txtDni; }
    public JTextField getTxtCalle() { return txtCalle; }
    public JTextField getTxtAltura() { return txtAltura; }
    public JTextField getTxtCiudad() { return txtCiudad; }
    public JComboBox<String> getCbProvincia() { return cbProvincia; }
    public JComboBox<Integer> getCbDia() { return cbDia; }
    public JComboBox<String> getCbMes() { return cbMes; }
    public JComboBox<Integer> getCbAnio() { return cbAnio; }
    public JComboBox<String> getCbPerfil() { return cbPerfil; }
    public JButton getBtnGuardarUsuario() { return btnGuardarUsuario; }
    public JButton getBtnCancelar() { return btnCancelar; }


    // --- GETTERS ETIQUETAS DE ERROR ---
    public JLabel getLblErrorNombre() { return lblErrorNombre; }
    public JLabel getLblErrorApellido() { return lblErrorApellido; }
    public JLabel getLblErrorDni() { return lblErrorDni; }
    public JLabel getLblErrorFecha() { return lblErrorFecha; }
    public JLabel getLblErrorCalle() { return lblErrorCalle; }
    public JLabel getLblErrorAltura() { return lblErrorAltura; }
    public JLabel getLblErrorCiudad() { return lblErrorCiudad; }
    public JLabel getLblErrorEmail() { return lblErrorEmail; }
    public JLabel getLblErrorUsername() { return lblErrorUsername; }
    public JLabel getLblErrorPassword() { return lblErrorPassword; }
}
