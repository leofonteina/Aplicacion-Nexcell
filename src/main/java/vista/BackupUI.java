package vista;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class BackupUI extends JFrame {

    private JButton btnGenerarBackup;
    private JButton btnRestaurar;
    private JButton btnProgramarBackup;

    private JComboBox<Integer> cbDia;
    private JComboBox<Integer> cbMes;
    private JComboBox<Integer> cbAnio;
    private JComboBox<Integer> cbHora;
    private JComboBox<Integer> cbMinutos;

    public BackupUI() {
        setTitle("Gestión de Respaldo y Restauración - Nexcell");
        setSize(500, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Panel Superior: Botones Manuales
        JPanel panelManual = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panelManual.setBorder(BorderFactory.createTitledBorder(null, "Acciones Inmediatas", TitledBorder.LEFT, TitledBorder.TOP));

        btnGenerarBackup = new JButton("Generar Backup Ahora");
        btnRestaurar = new JButton("Restaurar Sistema");
        btnRestaurar.setForeground(Color.RED); // Destacar que es peligroso

        panelManual.add(btnGenerarBackup);
        panelManual.add(btnRestaurar);

        // Panel Central: Backup Programado
        JPanel panelProgramado = new JPanel(new GridLayout(3, 1, 5, 5));
        panelProgramado.setBorder(BorderFactory.createTitledBorder(null, "Programar Backup Automático", TitledBorder.LEFT, TitledBorder.TOP));

        JPanel panelFecha = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelFecha.add(new JLabel("Fecha: "));
        cbDia = new JComboBox<>(generarNumeros(1, 31));
        cbMes = new JComboBox<>(generarNumeros(1, 12));
        cbAnio = new JComboBox<>(generarNumeros(2024, 2030));
        panelFecha.add(cbDia); panelFecha.add(new JLabel("/"));
        panelFecha.add(cbMes); panelFecha.add(new JLabel("/"));
        panelFecha.add(cbAnio);

        JPanel panelHora = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelHora.add(new JLabel("Hora (24h): "));
        cbHora = new JComboBox<>(generarNumeros(0, 23));
        cbMinutos = new JComboBox<>(generarNumeros(0, 59));
        panelHora.add(cbHora); panelHora.add(new JLabel(":"));
        panelHora.add(cbMinutos);

        JPanel panelBotonProg = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnProgramarBackup = new JButton("Dejar Backup Programado");
        panelBotonProg.add(btnProgramarBackup);

        panelProgramado.add(panelFecha);
        panelProgramado.add(panelHora);
        panelProgramado.add(panelBotonProg);

        add(panelManual, BorderLayout.NORTH);
        add(panelProgramado, BorderLayout.CENTER);
    }

    private Integer[] generarNumeros(int inicio, int fin) {
        Integer[] nums = new Integer[fin - inicio + 1];
        for (int i = 0; i < nums.length; i++) nums[i] = inicio + i;
        return nums;
    }

    // --- MÉTODOS VISUALES PARA QUE EL CONTROLADOR LOS USE ---

    public void mostrarMensaje(String mensaje, String titulo, int tipoMensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipoMensaje);
    }

    public int pedirConfirmacionRestauracion() {
        return JOptionPane.showConfirmDialog(this,
                "La restauración sobrescribirá TODOS los datos actuales.\nRequiere autorización de un Administrador y un Gerente.\n¿Desea continuar?",
                "Advertencia Crítica", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    }

    public String[] pedirDobleAutorizacion() {
        JTextField txtUserAdmin = new JTextField(15);
        JPasswordField passAdmin = new JPasswordField(15);
        JTextField txtUserGerente = new JTextField(15);
        JPasswordField passGerente = new JPasswordField(15);

        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        panel.add(new JLabel("--- CREDENCIALES ADMINISTRADOR ---")); panel.add(new JLabel(""));
        panel.add(new JLabel("Usuario Admin:")); panel.add(txtUserAdmin);
        panel.add(new JLabel("Contraseña:")); panel.add(passAdmin);
        panel.add(new JLabel("--- CREDENCIALES GERENTE ---")); panel.add(new JLabel(""));
        panel.add(new JLabel("Usuario Gerente:")); panel.add(txtUserGerente);
        panel.add(new JLabel("Contraseña:")); panel.add(passGerente);

        int result = JOptionPane.showConfirmDialog(this, panel, "Validación Cruzada de Seguridad", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return new String[]{
                    txtUserAdmin.getText().trim(), new String(passAdmin.getPassword()),
                    txtUserGerente.getText().trim(), new String(passGerente.getPassword())
            };
        }
        return null;
    }

    public String seleccionarDestinoGuardar(String nombreSugerido) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Backup");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos SQL (*.sql)", "sql"));
        fileChooser.setSelectedFile(new File(nombreSugerido));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String ruta = fileChooser.getSelectedFile().getAbsolutePath();
            return ruta.endsWith(".sql") ? ruta : ruta + ".sql";
        }
        return null;
    }

    public String seleccionarArchivoAbrir() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar archivo de Backup");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos SQL (*.sql)", "sql"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    // --- GETTERS ---
    public JButton getBtnGenerarBackup() { return btnGenerarBackup; }
    public JButton getBtnRestaurar() { return btnRestaurar; }
    public JButton getBtnProgramarBackup() { return btnProgramarBackup; }
    public JComboBox<Integer> getCbDia() { return cbDia; }
    public JComboBox<Integer> getCbMes() { return cbMes; }
    public JComboBox<Integer> getCbAnio() { return cbAnio; }
    public JComboBox<Integer> getCbHora() { return cbHora; }
    public JComboBox<Integer> getCbMinutos() { return cbMinutos; }
}