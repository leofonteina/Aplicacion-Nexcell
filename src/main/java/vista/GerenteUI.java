package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

public class GerenteUI extends JFrame {

    // Componentes de Reportes de Ventas
    private JTextField buscarVentaField;
    private JButton btnBuscarVenta;
    private JButton btnGenerarReporte;
    private JButton btnLimpiarReporte;
    private JTable tablaReportesVentas;
    private JLabel lblTotalReporteVentas; // CORREGIDO: Ahora es accesible por el controlador
    private JComboBox<String> cbDiaInicioV, cbMesInicioV, cbAnioInicioV;
    private JComboBox<String> cbDiaFinV, cbMesFinV, cbAnioFinV;

    // Componentes de Rendimiento de Vendedores
    private JButton btnCalcularRendimiento;
    private JTable tablaRendimiento;
    private JComboBox<String> cbDiaInicioR, cbMesInicioR, cbAnioInicioR;
    private JComboBox<String> cbDiaFinR, cbMesFinR, cbAnioFinR;

    // Componentes de Productos Más Vendidos
    private JButton btnCalcularProductosVendidos;
    private JButton btnGenerarGraficoProductos;
    private JTable tablaProductosVendidos;
    private JScrollPane scrollTablaProductos;
    private JTabbedPane tabsProductos;
    private JPanel panelGraficoProductos;
    private JComboBox<String> cbDiaInicioP, cbMesInicioP, cbAnioInicioP;
    private JComboBox<String> cbDiaFinP, cbMesFinP, cbAnioFinP;

    private JButton btnCerrarSesion;

    private static final int ALTO_FILA_TABLA = 35;

    public GerenteUI() {
        setTitle("Panel de Gerencia - Nexcell");
        setMinimumSize(new Dimension(1000, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane sistemaPestanas = new JTabbedPane(JTabbedPane.LEFT);
        sistemaPestanas.addTab("  Reportes de Ventas  ", cargarIcono("/iconos/icono_ventas.png"), crearPanelReportesVentas());
        sistemaPestanas.addTab("  Rendimiento Vendedores  ", cargarIcono("/iconos/icono_usuarios.png"), crearPanelRendimiento());
        sistemaPestanas.addTab("  Productos Más Vendidos  ", cargarIcono("/iconos/icono_reportes.png"), crearPanelProductosMasVendidos());

        add(sistemaPestanas, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));
        btnCerrarSesion = crearBotonEstilizado("Cerrar Sesión");
        btnCerrarSesion.setForeground(new Color(224, 82, 82)); // Color coral consistente con VendedorUI
        panelInferior.add(btnCerrarSesion);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelReportesVentas() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buscarVentaField = new JTextField(15);
        btnBuscarVenta = crearBotonEstilizado("Buscar");
        panelBusqueda.add(new JLabel("ID o DNI:"));
        panelBusqueda.add(buscarVentaField);
        panelBusqueda.add(btnBuscarVenta);

        cbDiaInicioV = new JComboBox<>(obtenerDias()); cbMesInicioV = new JComboBox<>(obtenerMeses()); cbAnioInicioV = new JComboBox<>(obtenerAnios());
        cbDiaFinV = new JComboBox<>(obtenerDias()); cbMesFinV = new JComboBox<>(obtenerMeses()); cbAnioFinV = new JComboBox<>(obtenerAnios());
        setearFechasPorDefecto(cbDiaInicioV, cbMesInicioV, cbAnioInicioV, cbDiaFinV, cbMesFinV, cbAnioFinV);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGenerarReporte = crearBotonEstilizado("Generar Reporte");
        btnLimpiarReporte = crearBotonEstilizado("Limpiar");

        panelFiltros.add(new JLabel("Desde:"));
        panelFiltros.add(crearAgrupacionFecha(cbDiaInicioV, cbMesInicioV, cbAnioInicioV));
        panelFiltros.add(new JLabel("Hasta:"));
        panelFiltros.add(crearAgrupacionFecha(cbDiaFinV, cbMesFinV, cbAnioFinV));
        panelFiltros.add(btnGenerarReporte);
        panelFiltros.add(btnLimpiarReporte);

        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelFiltros, BorderLayout.EAST);

        tablaReportesVentas = new JTable(new DefaultTableModel()) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        configurarTabla(tablaReportesVentas);
        JScrollPane scrollTabla = new JScrollPane(tablaReportesVentas);

        JPanel panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotalReporteVentas = new JLabel("Total Reporte: $0.00");
        lblTotalReporteVentas.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalReporteVentas.setForeground(new Color(40, 167, 69));
        panelTotales.add(lblTotalReporteVentas);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);
        panel.add(panelTotales, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelRendimiento() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        cbDiaInicioR = new JComboBox<>(obtenerDias()); cbMesInicioR = new JComboBox<>(obtenerMeses()); cbAnioInicioR = new JComboBox<>(obtenerAnios());
        cbDiaFinR = new JComboBox<>(obtenerDias()); cbMesFinR = new JComboBox<>(obtenerMeses()); cbAnioFinR = new JComboBox<>(obtenerAnios());
        setearFechasPorDefecto(cbDiaInicioR, cbMesInicioR, cbAnioInicioR, cbDiaFinR, cbMesFinR, cbAnioFinR);

        btnCalcularRendimiento = crearBotonEstilizado("Calcular Rendimiento por Fecha");

        panelSuperior.add(new JLabel("Desde:"));
        panelSuperior.add(crearAgrupacionFecha(cbDiaInicioR, cbMesInicioR, cbAnioInicioR));
        panelSuperior.add(new JLabel("Hasta:"));
        panelSuperior.add(crearAgrupacionFecha(cbDiaFinR, cbMesFinR, cbAnioFinR));
        panelSuperior.add(btnCalcularRendimiento);

        String[] columnas = {"Usuario Vendedor", "Cant. Ventas", "Total Facturado", "Comisión Estimada (5%)"};
        tablaRendimiento = new JTable(new DefaultTableModel(null, columnas)) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        configurarTabla(tablaRendimiento);
        JScrollPane scrollTabla = new JScrollPane(tablaRendimiento);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelProductosMasVendidos() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panelSuperior = new JPanel(new BorderLayout());
        JPanel panelIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        cbDiaInicioP = new JComboBox<>(obtenerDias()); cbMesInicioP = new JComboBox<>(obtenerMeses()); cbAnioInicioP = new JComboBox<>(obtenerAnios());
        cbDiaFinP = new JComboBox<>(obtenerDias()); cbMesFinP = new JComboBox<>(obtenerMeses()); cbAnioFinP = new JComboBox<>(obtenerAnios());
        setearFechasPorDefecto(cbDiaInicioP, cbMesInicioP, cbAnioInicioP, cbDiaFinP, cbMesFinP, cbAnioFinP);

        btnCalcularProductosVendidos = crearBotonEstilizado("Calcular por Fecha");

        panelIzquierda.add(new JLabel("Desde:"));
        panelIzquierda.add(crearAgrupacionFecha(cbDiaInicioP, cbMesInicioP, cbAnioInicioP));
        panelIzquierda.add(new JLabel("Hasta:"));
        panelIzquierda.add(crearAgrupacionFecha(cbDiaFinP, cbMesFinP, cbAnioFinP));
        panelIzquierda.add(btnCalcularProductosVendidos);

        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnGenerarGraficoProductos = crearBotonEstilizado("Generar Gráfico");
        btnGenerarGraficoProductos.setEnabled(false);
        panelDerecha.add(btnGenerarGraficoProductos);

        panelSuperior.add(panelIzquierda, BorderLayout.WEST);
        panelSuperior.add(panelDerecha, BorderLayout.EAST);

        String[] columnas = {"Ranking", "Producto", "Categoría", "Cant. Vendida", "Ingresos Generados"};
        tablaProductosVendidos = new JTable(new DefaultTableModel(null, columnas)) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        configurarTabla(tablaProductosVendidos);
        scrollTablaProductos = new JScrollPane(tablaProductosVendidos);

        panelGraficoProductos = new JPanel(new BorderLayout());
        panelGraficoProductos.setPreferredSize(new Dimension(800, 300));

        tabsProductos = new JTabbedPane();
        tabsProductos.addTab("Tabla de Datos", scrollTablaProductos);
        tabsProductos.addTab("Gráfico de Barras", panelGraficoProductos);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(tabsProductos, BorderLayout.CENTER);

        return panel;
    }

    private void configurarTabla(JTable tabla) {
        tabla.setRowHeight(ALTO_FILA_TABLA);
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setAutoCreateRowSorter(true);
    }

    private JButton crearBotonEstilizado(String texto) {
        JButton btn = new JButton(texto);
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

    private void setearFechasPorDefecto(JComboBox<String> dI, JComboBox<String> mI, JComboBox<String> aI,
                                        JComboBox<String> dF, JComboBox<String> mF, JComboBox<String> aF) {
        LocalDate hoy = LocalDate.now();
        LocalDate haceUnMes = hoy.minusMonths(1);

        dI.setSelectedIndex(haceUnMes.getDayOfMonth() - 1);
        mI.setSelectedIndex(haceUnMes.getMonthValue() - 1);
        aI.setSelectedItem(String.valueOf(haceUnMes.getYear()));

        dF.setSelectedIndex(hoy.getDayOfMonth() - 1);
        mF.setSelectedIndex(hoy.getMonthValue() - 1);
        aF.setSelectedItem(String.valueOf(hoy.getYear()));
    }

    // Getters
    public JButton getBtnGenerarReporte() { return btnGenerarReporte; }
    public JButton getBtnLimpiarReporte() { return btnLimpiarReporte; }
    public JTable getTablaReportesVentas() { return tablaReportesVentas; }
    public JLabel getLblTotalReporteVentas() { return lblTotalReporteVentas; }
    public JButton getBtnCerrarSesion() { return btnCerrarSesion; }
    public JTextField getBuscarVentaField() { return buscarVentaField; }
    public JButton getBtnBuscarVenta() { return btnBuscarVenta; }

    public JButton getBtnCalcularRendimiento() { return btnCalcularRendimiento; }
    public JTable getTablaRendimiento() { return tablaRendimiento; }

    public JButton getBtnCalcularProductosVendidos() { return btnCalcularProductosVendidos; }
    public JTable getTablaProductosVendidos() { return tablaProductosVendidos; }
    public JPanel getPanelGraficoProductos() { return panelGraficoProductos; }
    public JButton getBtnGenerarGraficoProductos() { return btnGenerarGraficoProductos; }
    public JScrollPane getScrollTablaProductos() { return scrollTablaProductos; }
    public JTabbedPane getTabsProductos() { return tabsProductos; }

    public JComboBox<String> getCbDiaInicioV() { return cbDiaInicioV; }
    public JComboBox<String> getCbMesInicioV() { return cbMesInicioV; }
    public JComboBox<String> getCbAnioInicioV() { return cbAnioInicioV; }
    public JComboBox<String> getCbDiaFinV() { return cbDiaFinV; }
    public JComboBox<String> getCbMesFinV() { return cbMesFinV; }
    public JComboBox<String> getCbAnioFinV() { return cbAnioFinV; }

    public JComboBox<String> getCbDiaInicioR() { return cbDiaInicioR; }
    public JComboBox<String> getCbMesInicioR() { return cbMesInicioR; }
    public JComboBox<String> getCbAnioInicioR() { return cbAnioInicioR; }
    public JComboBox<String> getCbDiaFinR() { return cbDiaFinR; }
    public JComboBox<String> getCbMesFinR() { return cbMesFinR; }
    public JComboBox<String> getCbAnioFinR() { return cbAnioFinR; }

    public JComboBox<String> getCbDiaInicioP() { return cbDiaInicioP; }
    public JComboBox<String> getCbMesInicioP() { return cbMesInicioP; }
    public JComboBox<String> getCbAnioInicioP() { return cbAnioInicioP; }
    public JComboBox<String> getCbDiaFinP() { return cbDiaFinP; }
    public JComboBox<String> getCbMesFinP() { return cbMesFinP; }
    public JComboBox<String> getCbAnioFinP() { return cbAnioFinP; }
}
