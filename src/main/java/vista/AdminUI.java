package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class AdminUI extends JFrame {

    // Componentes de Productos
    private JTextField buscarProductoField;
    private JButton btnBuscarProducto;
    private JTable tablaProductos;
    private JButton btnAbrirFormularioProducto;
    private JButton btnModificarProducto;
    private JButton btnBajaProducto;
    private JButton btnAltaProducto;

    // Componentes de Usuarios
    private JTextField buscarUsuarioField;
    private JButton btnBuscarUsuario;
    private JTable tablaUsuarios;
    private JButton btnAbrirFormularioUsuario;
    private JButton btnModificarUsuario;
    private JButton btnBajaUsuario;
    private JButton btnAltaUsuario;

    // Reportes y General
    private JComboBox<String> comboReportes;
    private JComboBox<String> cbDiaInicio, cbMesInicio, cbAnioInicio;
    private JComboBox<String> cbDiaFin, cbMesFin, cbAnioFin;
    private JButton btnGenerarReporte;
    private JButton btnLimpiarReporte;
    private JTable tablaReportes;
    private JButton btnCerrarSesion;

    public AdminUI() {
        setTitle("Panel de Administrador - Nexcell");
        setMinimumSize(new Dimension(1050, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane sistemaPestanas = new JTabbedPane(JTabbedPane.LEFT);

        sistemaPestanas.addTab("Productos ", cargarIcono("/iconos/icono_productos.png"), crearPanelProductos());
        sistemaPestanas.addTab("Usuarios ", cargarIcono("/iconos/icono_usuarios.png"), crearPanelUsuarios());
        sistemaPestanas.addTab("Reportes ", cargarIcono("/iconos/icono_reportes.png"), crearPanelReportes());

        add(sistemaPestanas, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setForeground(Color.RED);
        panelInferior.add(btnCerrarSesion);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelProductos() {
        // [Este método queda igual al que ya tenías]
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel panelSuperior = new JPanel(new BorderLayout());
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        buscarProductoField = new JTextField(15);
        buscarProductoField.setPreferredSize(new Dimension(200, 35));
        btnBuscarProducto = new JButton("Buscar");
        btnBuscarProducto.setPreferredSize(new Dimension(100, 35));
        panelBusqueda.add(new JLabel("Filtrar: "));
        panelBusqueda.add(buscarProductoField);
        panelBusqueda.add(btnBuscarProducto);
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 10));
        btnAbrirFormularioProducto = new JButton("Nuevo");
        btnAbrirFormularioProducto.setPreferredSize(new Dimension(100, 35));
        btnModificarProducto = new JButton("Modificar");
        btnModificarProducto.setPreferredSize(new Dimension(100, 35));
        btnBajaProducto = new JButton("Baja Lógica");
        btnBajaProducto.setPreferredSize(new Dimension(100, 35));
        btnAltaProducto = new JButton("Reactivar");
        btnAltaProducto.setPreferredSize(new Dimension(100, 35));
        btnModificarProducto.setVisible(false);
        btnBajaProducto.setVisible(false);
        btnAltaProducto.setVisible(false);
        panelAcciones.add(btnAbrirFormularioProducto);
        panelAcciones.add(btnModificarProducto);
        panelAcciones.add(btnBajaProducto);
        panelAcciones.add(btnAltaProducto);
        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);
        String[] columnas = {"ID", "Modelo", "Categoría", "Stock", "Precio", "Estado"};
        tablaProductos = new JTable(new DefaultTableModel(new Object[][]{}, columnas) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        JScrollPane scrollTabla = new JScrollPane(tablaProductos);
        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelUsuarios() {
        // [Este método queda igual al que ya tenías]
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel panelSuperior = new JPanel(new BorderLayout());
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        buscarUsuarioField = new JTextField(15);
        buscarUsuarioField.setPreferredSize(new Dimension(200, 35));
        btnBuscarUsuario = new JButton("Buscar");
        btnBuscarUsuario.setPreferredSize(new Dimension(100, 35));
        panelBusqueda.add(new JLabel("Usuario: "));
        panelBusqueda.add(buscarUsuarioField);
        panelBusqueda.add(btnBuscarUsuario);
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 10));
        btnAbrirFormularioUsuario = new JButton("Nuevo");
        btnAbrirFormularioUsuario.setPreferredSize(new Dimension(100, 35));
        btnModificarUsuario = new JButton("Modificar");
        btnModificarUsuario.setPreferredSize(new Dimension(100, 35));
        btnBajaUsuario = new JButton("Baja Lógica");
        btnBajaUsuario.setPreferredSize(new Dimension(100, 35));
        btnAltaUsuario = new JButton("Reactivar");
        btnAltaUsuario.setPreferredSize(new Dimension(100, 35));
        btnModificarUsuario.setVisible(false);
        btnBajaUsuario.setVisible(false);
        btnAltaUsuario.setVisible(false);
        panelAcciones.add(btnAbrirFormularioUsuario);
        panelAcciones.add(btnModificarUsuario);
        panelAcciones.add(btnBajaUsuario);
        panelAcciones.add(btnAltaUsuario);
        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);
        String[] columnas = {"Username", "Rol del Sistema", "Estado"};
        tablaUsuarios = new JTable(new DefaultTableModel(new Object[][]{}, columnas) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        JScrollPane scrollTabla = new JScrollPane(tablaUsuarios);
        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelReportes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Arreglos para fechas
        String[] dias = new String[31]; for (int i = 0; i < 31; i++) dias[i] = String.format("%02d", i + 1);
        String[] meses = new String[12]; for (int i = 0; i < 12; i++) meses[i] = String.format("%02d", i + 1);
        String[] anios = new String[5];
        int anioActual = LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) anios[i] = String.valueOf(anioActual - i);

        cbDiaInicio = new JComboBox<>(dias); cbMesInicio = new JComboBox<>(meses); cbAnioInicio = new JComboBox<>(anios);
        cbDiaFin = new JComboBox<>(dias); cbMesFin = new JComboBox<>(meses); cbAnioFin = new JComboBox<>(anios);

        LocalDate hoy = LocalDate.now();
        LocalDate haceUnMes = hoy.minusMonths(1);
        cbDiaInicio.setSelectedIndex(haceUnMes.getDayOfMonth() - 1); cbMesInicio.setSelectedIndex(haceUnMes.getMonthValue() - 1); cbAnioInicio.setSelectedItem(String.valueOf(haceUnMes.getYear()));
        cbDiaFin.setSelectedIndex(hoy.getDayOfMonth() - 1); cbMesFin.setSelectedIndex(hoy.getMonthValue() - 1); cbAnioFin.setSelectedItem(String.valueOf(hoy.getYear()));

        String[] opcionesReporte = {"Productos con Bajo Stock", "Valorización de Inventario", "Movimientos de Inventario", "Auditoría de Usuarios"};
        comboReportes = new JComboBox<>(opcionesReporte);

        btnGenerarReporte = new JButton("Generar");
        btnLimpiarReporte = new JButton("Limpiar");

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFiltros.add(new JLabel("Reporte:"));
        panelFiltros.add(comboReportes);
        panelFiltros.add(new JLabel("Desde:"));
        panelFiltros.add(crearAgrupacionFecha(cbDiaInicio, cbMesInicio, cbAnioInicio));
        panelFiltros.add(new JLabel("Hasta:"));
        panelFiltros.add(crearAgrupacionFecha(cbDiaFin, cbMesFin, cbAnioFin));
        panelFiltros.add(btnGenerarReporte);
        panelFiltros.add(btnLimpiarReporte);

        tablaReportes = new JTable(new DefaultTableModel()) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JScrollPane scrollTabla = new JScrollPane(tablaReportes);

        panel.add(panelFiltros, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearAgrupacionFecha(JComboBox<String> dia, JComboBox<String> mes, JComboBox<String> anio) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        p.add(dia); p.add(new JLabel("/")); p.add(mes); p.add(new JLabel("/")); p.add(anio);
        return p;
    }

    // Getters
    public JTable getTablaProductos() { return tablaProductos; }
    public JButton getBtnAbrirFormularioProducto() { return btnAbrirFormularioProducto; }
    public JButton getBtnModificarProducto() { return btnModificarProducto; }
    public JButton getBtnBajaProducto() { return btnBajaProducto; }
    public JButton getBtnAltaProducto() { return btnAltaProducto; }
    public JTextField getTxtBuscarUsuario() { return buscarUsuarioField; }
    public JButton getBtnBuscarUsuario() { return btnBuscarUsuario; }
    public JTable getTablaUsuarios() { return tablaUsuarios; }
    public JButton getBtnAbrirFormularioUsuario() { return btnAbrirFormularioUsuario; }
    public JButton getBtnModificarUsuario() { return btnModificarUsuario; }
    public JButton getBtnBajaUsuario() { return btnBajaUsuario; }
    public JButton getBtnAltaUsuario() { return btnAltaUsuario; }
    public JTextField getBuscarProductoField() { return buscarProductoField; }
    public JButton getBtnBuscarProducto() { return btnBuscarProducto; }

    // Nuevos Getters Reportes
    public JComboBox<String> getComboReportes() { return comboReportes; }
    public JComboBox<String> getCbDiaInicio() { return cbDiaInicio; }
    public JComboBox<String> getCbMesInicio() { return cbMesInicio; }
    public JComboBox<String> getCbAnioInicio() { return cbAnioInicio; }
    public JComboBox<String> getCbDiaFin() { return cbDiaFin; }
    public JComboBox<String> getCbMesFin() { return cbMesFin; }
    public JComboBox<String> getCbAnioFin() { return cbAnioFin; }
    public JButton getBtnGenerarReporte() { return btnGenerarReporte; }
    public JButton getBtnLimpiarReporte() { return btnLimpiarReporte; }
    public JTable getTablaReportes() { return tablaReportes; }
    public JButton getBtnCerrarSesion() { return btnCerrarSesion; }

    private ImageIcon cargarIcono(String ruta) {
        java.net.URL imgURL = getClass().getResource(ruta);
        if (imgURL != null) {
            return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        }
        return null;
    }
}
