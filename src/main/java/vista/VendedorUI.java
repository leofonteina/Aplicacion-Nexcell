package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class VendedorUI extends JFrame {

    // Componentes de Clientes
    private JTextField buscarClienteField;
    private JButton btnBuscarCliente;
    private JTable tablaClientes;
    private JButton btnAbrirFormularioCliente;
    private JButton btnBajaCliente;
    private JButton btnAltaCliente;

    // Componentes de Productos
    private JTextField buscarProductoField;
    private JButton btnBuscarProducto;
    private JTable tablaProductos;

    // Botón general
    private JButton btnCerrarSesion;

    // Componentes de Ventas
    private JTable tablaVentas;
    private JButton btnAbrirFormularioVenta;
    private JTextField buscarVentaField;
    private JButton btnBuscarVenta;

    // NUEVO: Componentes de Reportes con Fechas Desplegables
    private JComboBox<String> comboTipoReporte;
    private JComboBox<String> cbDiaInicio, cbMesInicio, cbAnioInicio;
    private JComboBox<String> cbDiaFin, cbMesFin, cbAnioFin;
    private JButton btnGenerarReporte;
    private JTable tablaReportes;
    private JLabel lblTotalReporte;

    private static final int PADDING_PANEL = 30;
    private static final int GAP_VERTICAL = 20;
    private static final int GAP_HORIZONTAL = 10;
    private static final int ALTO_FILA_TABLA = 35;

    public VendedorUI() {
        setTitle("Panel de Vendedor - Nexcell");
        setMinimumSize(new Dimension(950, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ESTA LÍNEA ABRE LA VENTANA MAXIMIZADA POR DEFECTO
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane sistemaPestanas = new JTabbedPane(JTabbedPane.LEFT);
        sistemaPestanas.addTab("  Gestión de Clientes  ", cargarIcono("/iconos/icono_usuarios.png"), crearPanelClientes());
        sistemaPestanas.addTab("  Catálogo de Productos  ", cargarIcono("/iconos/icono_productos.png"), crearPanelProductos());
        sistemaPestanas.addTab("  Registro de Ventas  ", cargarIcono("/iconos/icono_ventas.png"), crearPanelVentas());
        sistemaPestanas.addTab("  Mis Reportes  ", cargarIcono("/iconos/icono_reportes.png"), crearPanelReportes());
        add(sistemaPestanas, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, PADDING_PANEL, 20, PADDING_PANEL));

        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setForeground(new Color(224, 82, 82));
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setMargin(new Insets(8, 20, 8, 20));
        panelInferior.add(btnCerrarSesion);

        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelClientes() {
        JPanel panel = crearPanelBase();
        buscarClienteField = new JTextField(20);
        btnBuscarCliente = crearBotonWeb("Buscar");
        JPanel panelBusqueda = crearBarraBusqueda("Buscar (DNI):", buscarClienteField, btnBuscarCliente);

        btnAbrirFormularioCliente = crearBotonWeb("Nuevo Cliente");
        btnBajaCliente = crearBotonWeb("Baja Lógica");
        btnAltaCliente = crearBotonWeb("Reactivar");
        btnBajaCliente.setVisible(false);
        btnAltaCliente.setVisible(false);

        JPanel panelAcciones = crearBarraAcciones(btnBajaCliente, btnAltaCliente, btnAbrirFormularioCliente);
        String[] columnas = {"DNI", "Nombre", "Apellido", "Teléfono", "Email", "Estado"};
        tablaClientes = new JTable(new DefaultTableModel(new Object[][]{}, columnas) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        });
        configurarTabla(tablaClientes);

        JScrollPane scroll = new JScrollPane(tablaClientes);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(crearEncabezado("Directorio de Clientes", panelBusqueda, panelAcciones), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelProductos() {
        JPanel panel = crearPanelBase();
        buscarProductoField = new JTextField(20);
        btnBuscarProducto = crearBotonWeb("Buscar");
        JPanel panelBusqueda = crearBarraBusqueda("Filtrar modelo:", buscarProductoField, btnBuscarProducto);

        String[] columnas = {"ID", "Categoría", "Modelo", "Stock", "Precio"};
        tablaProductos = new JTable(new DefaultTableModel(new Object[][]{}, columnas) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        });
        configurarTabla(tablaProductos);

        JScrollPane scroll = new JScrollPane(tablaProductos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(crearEncabezado("Catálogo de Productos", panelBusqueda, null), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelVentas() {
        JPanel panel = crearPanelBase();
        buscarVentaField = new JTextField(20);
        btnBuscarVenta = crearBotonWeb("Buscar");
        JPanel panelBusqueda = crearBarraBusqueda("Buscar ID Venta:", buscarVentaField, btnBuscarVenta);

        btnAbrirFormularioVenta = crearBotonWeb("Nueva Venta");
        JPanel panelAcciones = crearBarraAcciones(btnAbrirFormularioVenta);

        String[] columnas = {"ID Venta", "Fecha", "DNI Cliente", "Producto", "Total"};
        tablaVentas = new JTable(new DefaultTableModel(new Object[][]{}, columnas) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        });
        configurarTabla(tablaVentas);

        JScrollPane scroll = new JScrollPane(tablaVentas);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(crearEncabezado("Historial de Mis Ventas", panelBusqueda, panelAcciones), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelReportes() {
        JPanel panel = crearPanelBase();

        comboTipoReporte = new JComboBox<>(new String[]{
            "Resumen de ventas",
            "Productos mas vendidos"
        });

        // Configuración de arrays para los ComboBox de fechas
        String[] dias = new String[31];
        for (int i = 0; i < 31; i++) dias[i] = String.format("%02d", i + 1);

        String[] meses = new String[12];
        for (int i = 0; i < 12; i++) meses[i] = String.format("%02d", i + 1);

        String[] anios = new String[5];
        int anioActual = LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) anios[i] = String.valueOf(anioActual - i);

        // Inicializar combos
        cbDiaInicio = new JComboBox<>(dias); cbMesInicio = new JComboBox<>(meses); cbAnioInicio = new JComboBox<>(anios);
        cbDiaFin = new JComboBox<>(dias); cbMesFin = new JComboBox<>(meses); cbAnioFin = new JComboBox<>(anios);

        // Setear fecha actual por defecto para "Fin", y mes pasado para "Inicio"
        LocalDate hoy = LocalDate.now();
        LocalDate haceUnMes = hoy.minusMonths(1);

        cbDiaInicio.setSelectedIndex(haceUnMes.getDayOfMonth() - 1);
        cbMesInicio.setSelectedIndex(haceUnMes.getMonthValue() - 1);
        cbAnioInicio.setSelectedItem(String.valueOf(haceUnMes.getYear()));

        cbDiaFin.setSelectedIndex(hoy.getDayOfMonth() - 1);
        cbMesFin.setSelectedIndex(hoy.getMonthValue() - 1);
        cbAnioFin.setSelectedItem(String.valueOf(hoy.getYear()));

        btnGenerarReporte = crearBotonWeb("Generar Reporte");

        // Construir barra superior de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelFiltros.add(new JLabel("Métrica:"));
        panelFiltros.add(comboTipoReporte);
        panelFiltros.add(new JLabel("Desde:"));
        panelFiltros.add(crearAgrupacionFecha(cbDiaInicio, cbMesInicio, cbAnioInicio));
        panelFiltros.add(new JLabel("Hasta:"));
        panelFiltros.add(crearAgrupacionFecha(cbDiaFin, cbMesFin, cbAnioFin));
        panelFiltros.add(btnGenerarReporte);

        tablaReportes = new JTable(new DefaultTableModel(new Object[][]{}, new String[]{"-"})) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        configurarTabla(tablaReportes);
        JScrollPane scroll = new JScrollPane(tablaReportes);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        lblTotalReporte = new JLabel("Total Periodo: $0.00");
        lblTotalReporte.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalReporte.setForeground(new Color(40, 167, 69));

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.add(lblTotalReporte);

        panel.add(crearEncabezado("Mis Estadísticas", panelFiltros, null), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(panelInferior, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearAgrupacionFecha(JComboBox<String> dia, JComboBox<String> mes, JComboBox<String> anio) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        p.add(dia);
        p.add(new JLabel("/"));
        p.add(mes);
        p.add(new JLabel("/"));
        p.add(anio);
        return p;
    }

    private JPanel crearPanelBase() {
        JPanel panel = new JPanel(new BorderLayout(0, GAP_VERTICAL));
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING_PANEL, PADDING_PANEL, PADDING_PANEL, PADDING_PANEL));
        return panel;
    }

    private JPanel crearEncabezado(String titulo, JPanel pBusqueda, JPanel pAcciones) {
        JPanel encabezado = new JPanel(new BorderLayout(0, 20));
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JPanel barra = new JPanel(new BorderLayout(20, 0));
        barra.add(pBusqueda, BorderLayout.WEST);
        if (pAcciones != null) barra.add(pAcciones, BorderLayout.EAST);
        encabezado.add(lblTitulo, BorderLayout.NORTH);
        encabezado.add(barra, BorderLayout.CENTER);
        return encabezado;
    }

    private JPanel crearBarraBusqueda(String etiqueta, JTextField campo, JButton boton) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP_HORIZONTAL, 0));
        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        barra.add(lbl); barra.add(campo); barra.add(boton);
        return barra;
    }

    private JPanel crearBarraAcciones(JButton... botones) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP_HORIZONTAL, 0));
        for (JButton b : botones) barra.add(b);
        return barra;
    }

    private void configurarTabla(JTable tabla) {
        tabla.setRowHeight(ALTO_FILA_TABLA);
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    private JButton crearBotonWeb(String txt) {
        JButton btn = new JButton(txt);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(6, 15, 6, 15));
        return btn;
    }

    private ImageIcon cargarIcono(String ruta) {
        java.net.URL imgURL = getClass().getResource(ruta);
        if (imgURL != null) {
            return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        }
        return null;
    }

    // Getters habituales
    public JTextField getBuscarClienteField() { return buscarClienteField; }
    public JButton getBtnBuscarCliente() { return btnBuscarCliente; }
    public JTable getTablaClientes() { return tablaClientes; }
    public JButton getBtnAbrirFormularioCliente() { return btnAbrirFormularioCliente; }
    public JButton getBtnBajaCliente() { return btnBajaCliente; }
    public JButton getBtnAltaCliente() { return btnAltaCliente; }
    public JTextField getBuscarProductoField() { return buscarProductoField; }
    public JButton getBtnBuscarProducto() { return btnBuscarProducto; }
    public JTable getTablaProductos() { return tablaProductos; }
    public JButton getBtnCerrarSesion() { return btnCerrarSesion; }
    public JButton getBtnAbrirFormularioVenta() { return btnAbrirFormularioVenta; }
    public JTable getTablaVentas() { return tablaVentas; }
    public JTextField getBuscarVentaField() { return buscarVentaField; }
    public JButton getBtnBuscarVenta() { return btnBuscarVenta; }

    // Getters de Reportes
    public JComboBox<String> getComboTipoReporte() { return comboTipoReporte; }
    public JComboBox<String> getCbDiaInicio() { return cbDiaInicio; }
    public JComboBox<String> getCbMesInicio() { return cbMesInicio; }
    public JComboBox<String> getCbAnioInicio() { return cbAnioInicio; }
    public JComboBox<String> getCbDiaFin() { return cbDiaFin; }
    public JComboBox<String> getCbMesFin() { return cbMesFin; }
    public JComboBox<String> getCbAnioFin() { return cbAnioFin; }
    public JButton getBtnGenerarReporte() { return btnGenerarReporte; }
    public JTable getTablaReportes() { return tablaReportes; }
    public JLabel getLblTotalReporte() { return lblTotalReporte; }
}
