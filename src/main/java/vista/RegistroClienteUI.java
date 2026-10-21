package vista;

import javax.swing.*;
import java.awt.*;

public class RegistroClienteUI extends JDialog {

    private JTextField txtNombre, txtApellido, txtDni, txtTelefono, txtEmail;
    private JTextField txtCalle, txtAltura, txtCiudad;
    private JComboBox<String> cbProvincia;
    private JComboBox<Integer> cbDia, cbAnio;
    private JComboBox<String> cbMes;
    private JButton btnGuardarCliente, btnCancelar;

    private JLabel lblErrorNombre, lblErrorApellido, lblErrorDni, lblErrorTelefono, lblErrorEmail;
    private JLabel lblErrorFecha, lblErrorCalle, lblErrorAltura, lblErrorCiudad;

    public RegistroClienteUI(JFrame parent) {
        super(parent, "Registrar Nuevo Cliente", true);
        setMinimumSize(new Dimension(480, 680));
        setResizable(false);

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

        // --- CONTACTO ---
        JPanel panelContacto = crearPanelFormulario("Contacto");
        agregarCampoConError(panelContacto, "Teléfono:", txtTelefono = new JTextField(15), lblErrorTelefono, 0);
        agregarCampoConError(panelContacto, "Email:", txtEmail = new JTextField(15), lblErrorEmail, 2);

        // --- DIRECCIÓN ---
        JPanel panelDireccion = crearPanelFormulario("Dirección");
        agregarCampoConError(panelDireccion, "Calle:", txtCalle = new JTextField(15), lblErrorCalle, 0);
        agregarCampoConError(panelDireccion, "Altura:", txtAltura = new JTextField(15), lblErrorAltura, 2);
        agregarCampoConError(panelDireccion, "Ciudad:", txtCiudad = new JTextField(15), lblErrorCiudad, 4);

        String[] provincias = {"Corrientes", "Chaco", "Misiones", "Formosa", "Entre Ríos", "Santa Fe", "Buenos Aires", "Otra"};
        agregarCampo(panelDireccion, "Provincia:", cbProvincia = new JComboBox<>(provincias), 6);

        panelPrincipal.add(panelPersonales);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 5)));
        panelPrincipal.add(panelContacto);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 5)));
        panelPrincipal.add(panelDireccion);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- BOTONES ---
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.X_AXIS));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardarCliente = new JButton("Guardar Cliente");
        btnGuardarCliente.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelBotones.add(btnCancelar);
        panelBotones.add(Box.createHorizontalGlue());
        panelBotones.add(btnGuardarCliente);

        panelPrincipal.add(panelBotones);
        add(panelPrincipal);
        pack();
        setLocationRelativeTo(parent);
    }

    // =========================================================================
    // MÉTODOS DE LA VISTA PARA CONFIGURAR SU PROPIO ESTADO VISUAL
    // =========================================================================

    public void preCargarDni(String dni) {
        if (dni != null && !dni.isEmpty()) {
            txtDni.setText(dni);
            txtDni.setEditable(false);
        }
    }

    public void configurarModoEdicionDireccion(modelo.Cliente cliente) {
        setTitle("Actualizando dirección de: " + cliente.getNombre() + " " + cliente.getApellido());
        btnGuardarCliente.setText("Actualizar Dirección");

        txtDni.setText(cliente.getDni());
        txtDni.setEditable(false);

        txtNombre.setText(cliente.getNombre());
        txtNombre.setEditable(false);

        txtApellido.setText(cliente.getApellido());
        txtApellido.setEditable(false);

        txtTelefono.setText(cliente.getTelefono());
        txtTelefono.setEditable(false);

        txtEmail.setText(cliente.getEmail());
        txtEmail.setEditable(false);

        if (cliente.getFechaNacimiento() != null) {
            cbDia.setSelectedItem(cliente.getFechaNacimiento().getDayOfMonth());
            cbMes.setSelectedIndex(cliente.getFechaNacimiento().getMonthValue() - 1);
            cbAnio.setSelectedItem(cliente.getFechaNacimiento().getYear());
        }
        cbDia.setEnabled(false);
        cbMes.setEnabled(false);
        cbAnio.setEnabled(false);

        if (cliente.getDireccion() != null) {
            txtCalle.setText(cliente.getDireccion().getCalle());
            txtAltura.setText(cliente.getDireccion().getAltura());
            txtCiudad.setText(cliente.getDireccion().getCiudad());
            cbProvincia.setSelectedItem(cliente.getDireccion().getProvincia());
        }
    }

    private void inicializarEtiquetasDeError() {
        lblErrorNombre = crearEtiquetaError();
        lblErrorApellido = crearEtiquetaError();
        lblErrorDni = crearEtiquetaError();
        lblErrorTelefono = crearEtiquetaError();
        lblErrorEmail = crearEtiquetaError();
        lblErrorFecha = crearEtiquetaError();
        lblErrorCalle = crearEtiquetaError();
        lblErrorAltura = crearEtiquetaError();
        lblErrorCiudad = crearEtiquetaError();
    }

    private JLabel crearEtiquetaError() {
        JLabel lbl = new JLabel("");
        lbl.setForeground(new Color(224, 82, 82));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        return lbl;
    }

    public void limpiarErrores() {
        lblErrorNombre.setText("");
        lblErrorApellido.setText("");
        lblErrorDni.setText("");
        lblErrorTelefono.setText("");
        lblErrorEmail.setText("");
        lblErrorFecha.setText("");
        lblErrorCalle.setText("");
        lblErrorAltura.setText("");
        lblErrorCiudad.setText("");
    }

    private JPanel crearPanelFormulario(String titulo) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("--- " + titulo + " ---"));
        return panel;
    }

    private void agregarCampo(JPanel panel, String label, JComponent componente, int fila) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = fila; panel.add(new JLabel(label), gbc);
        gbc.gridx = 1; panel.add(componente, gbc);
    }

    private void agregarCampoConError(JPanel panel, String labelText, JComponent componente, JLabel lblError, int filaBase) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 0, 10); gbc.gridx = 0; gbc.gridy = filaBase;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1; gbc.gridy = filaBase; panel.add(componente, gbc);
        gbc.insets = new Insets(0, 10, 5, 10); gbc.gridx = 1; gbc.gridy = filaBase + 1;
        panel.add(lblError, gbc);
    }

    // Getters
    public JTextField getTxtNombre() { return txtNombre; }
    public JTextField getTxtApellido() { return txtApellido; }
    public JTextField getTxtDni() { return txtDni; }
    public JTextField getTxtTelefono() { return txtTelefono; }
    public JTextField getTxtEmail() { return txtEmail; }
    public JTextField getTxtCalle() { return txtCalle; }
    public JTextField getTxtAltura() { return txtAltura; }
    public JTextField getTxtCiudad() { return txtCiudad; }
    public JComboBox<String> getCbProvincia() { return cbProvincia; }
    public JComboBox<Integer> getCbDia() { return cbDia; }
    public JComboBox<String> getCbMes() { return cbMes; }
    public JComboBox<Integer> getCbAnio() { return cbAnio; }
    public JButton getBtnGuardarCliente() { return btnGuardarCliente; }
    public JButton getBtnCancelar() { return btnCancelar; }

    public JLabel getLblErrorNombre() { return lblErrorNombre; }
    public JLabel getLblErrorApellido() { return lblErrorApellido; }
    public JLabel getLblErrorDni() { return lblErrorDni; }
    public JLabel getLblErrorFecha() { return lblErrorFecha; }
}
