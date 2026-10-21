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
    private JButton btnAbrirBackup;

    private static final int ALTO_FILA_TABLA = 35;

    public AdminUI() {
        setTitle("Panel de Administrador - Nexcell");
        setMinimumSize(new Dimension(1050, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane sistemaPestanas = new JTabbedPane(JTabbedPane.LEFT);
        sistemaPestanas.addTab("  Productos  ", cargarIcono("/iconos/icono_productos.png"), crearPanelProductos());
        sistemaPestanas.addTab("  Usuarios  ", cargarIcono("/iconos/icono_usuarios.png"), crearPanelUsuarios());
        sistemaPestanas.addTab("  Reportes  ", cargarIcono("/iconos/icono_reportes.png"), crearPanelReportes());

        add(sistemaPestanas, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        JPanel panelIzq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnAbrirBackup = crearBotonEstilizado("Respaldo y Restauración (BD)");
        panelIzq.add(btnAbrirBackup);

        JPanel panelDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnCerrarSesion = crearBotonEstilizado("Cerrar Sesión");
        btnCerrarSesion.setForeground(new Color(224, 82, 82));
        panelDer.add(btnCerrarSesion);

        panelInferior.add(panelIzq, BorderLayout.WEST);
        panelInferior.add(panelDer, BorderLayout.EAST);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panelSuperior = new JPanel(new BorderLayout());
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buscarProductoField = new JTextField(20);
        btnBuscarProducto = crearBotonEstilizado("Buscar");
        panelBusqueda.add(new JLabel("Filtrar:"));
        panelBusqueda.add(buscarProductoField);
        panelBusqueda.add(btnBuscarProducto);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnAbrirFormularioProducto = crearBotonEstilizado("Nuevo Producto");
        btnModificarProducto = crearBotonEstilizado("Modificar");
        btnBajaProducto = crearBotonEstilizado("Baja Lógica");
        btnAltaProducto = crearBotonEstilizado("Reactivar");

        btnModificarProducto.setVisible(false);
        btnBajaProducto.setVisible(false);
        btnAltaProducto.setVisible(false);

        panelAcciones.add(btnModificarProducto);
        panelAcciones.add(btnBajaProducto);
        panelAcciones.add(btnAltaProducto);
        panelAcciones.add(btnAbrirFormularioProducto);

        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);

        String[] columnas = {"ID", "Modelo", "Categoría", "Stock", "Precio", "Estado"};
        tablaProductos = new JTable(new DefaultTableModel(new Object[][]{}, columnas) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        configurarTabla(tablaProductos);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panelSuperior = new JPanel(new BorderLayout());
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buscarUsuarioField = new JTextField(20);
        btnBuscarUsuario = crearBotonEstilizado("Buscar");
        panelBusqueda.add(new JLabel("Usuario:"));
        panelBusqueda.add(buscarUsuarioField);
        panelBusqueda.add(btnBuscarUsuario);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnAbrirFormularioUsuario = crearBotonEstilizado("Nuevo Usuario");
        btnModificarUsuario = crearBotonEstilizado("Modificar");
        btnBajaUsuario = crearBotonEstilizado("Baja Lógica");
        btnAltaUsuario = crearBotonEstilizado("Reactivar");

        btnModificarUsuario.setVisible(false);
        btnBajaUsuario.setVisible(false);
        btnAltaUsuario.setVisible(false);

        panelAcciones.add(btnModificarUsuario);
        panelAcciones.add(btnBajaUsuario);
        panelAcciones.add(btnAltaUsuario);
        panelAcciones.add(btnAbrirFormularioUsuario);

        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);

        String[] columnas = {"Username", "Rol del Sistema", "Estado"};
        tablaUsuarios = new JTable(new DefaultTableModel(new Object[][]{}, columnas) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        configurarTabla(tablaUsuarios);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelReportes() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        cbDiaInicio = new JComboBox<>(obtenerDias()); cbMesInicio = new JComboBox<>(obtenerMeses()); cbAnioInicio = new JComboBox<>(obtenerAnios());
        cbDiaFin = new JComboBox<>(obtenerDias()); cbMesFin = new JComboBox<>(obtenerMeses()); cbAnioFin = new JComboBox<>(obtenerAnios());

        LocalDate hoy = LocalDate.now();
        LocalDate haceUnMes = hoy.minusMonths(1);
        cbDiaInicio.setSelectedIndex(haceUnMes.getDayOfMonth() - 1); cbMesInicio.setSelectedIndex(haceUnMes.getMonthValue() - 1); cbAnioInicio.setSelectedItem(String.valueOf(haceUnMes.getYear()));
        cbDiaFin.setSelectedIndex(hoy.getDayOfMonth() - 1); cbMesFin.setSelectedIndex(hoy.getMonthValue() - 1); cbAnioFin.setSelectedItem(String.valueOf(hoy.getYear()));

        String[] opcionesReporte = {"Productos con Bajo Stock", "Valorización de Inventario", "Movimientos de Inventario", "Auditoría de Usuarios"};
        comboReportes = new JComboBox<>(opcionesReporte);

        // MEJORA UX: Solo habilitamos los selectores de fecha cuando el reporte realmente los utiliza
        habilitarCombosFecha(false);
        comboReportes.addActionListener(e -> {
            boolean usaFechas = comboReportes.getSelectedItem().toString().equals("Movimientos de Inventario");
            habilitarCombosFecha(usaFechas);
        });

        btnGenerarReporte = crearBotonEstilizado("Generar");
        btnLimpiarReporte = crearBotonEstilizado("Limpiar");

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
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
        configurarTabla(tablaReportes);

        panel.add(panelFiltros, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaReportes), BorderLayout.CENTER);

        return panel;
    }

    private void habilitarCombosFecha(boolean habilitar) {
        cbDiaInicio.setEnabled(habilitar); cbMesInicio.setEnabled(habilitar); cbAnioInicio.setEnabled(habilitar);
        cbDiaFin.setEnabled(habilitar); cbMesFin.setEnabled(habilitar); cbAnioFin.setEnabled(habilitar);
    }

    private void configurarTabla(JTable tabla) {
        tabla.setRowHeight(ALTO_FILA_TABLA);
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    private JButton crearBotonEstilizado(String texto) {
        JButton btn = new JButton(texto);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(6, 15, 6, 15));
        return btn;
    }

    private JPanel crearAgrupacionFecha(JComboBox<String> dia, JComboBox<String> mes, JComboBox<String> anio) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        p.add(dia); p.add(new JLabel("/")); p.add(mes); p.add(new JLabel("/")); p.add(anio);
        return p;
    }

    private String[] obtenerDias() {
        String[] dias = new String[31];
        for (int i = 0; i < 31; i++) dias[i] = String.format("%02d", i + 1);
        return dias;
    }

    private String[] obtenerMeses() {
        String[] meses = new String[12];
        for (int i = 0; i < 12; i++) meses[i] = String.format("%02d", i + 1);
        return meses;
    }

    private String[] obtenerAnios() {
        String[] anios = new String[5];
        int anioActual = LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) anios[i] = String.valueOf(anioActual - i);
        return anios;
    }

    private ImageIcon cargarIcono(String ruta) {
        java.net.URL imgURL = getClass().getResource(ruta);
        if (imgURL != null) {
            return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        }
        return null;
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
    public JButton getBtnAbrirBackup() { return btnAbrirBackup; }
}
