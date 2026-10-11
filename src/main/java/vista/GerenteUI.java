package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class GerenteUI extends JFrame {

    // Componentes de Reportes de Ventas
    private JTextField buscarVentaField;
    private JButton btnBuscarVenta;
    private JButton btnGenerarReporte;
    private JButton btnLimpiarReporte;
    private JTable tablaReportesVentas;

    // Componentes de Rendimiento de Vendedores
    private JButton btnCalcularRendimiento;
    private JTable tablaRendimiento;

    // Componentes de Productos Más Vendidos
    private JButton btnCalcularProductosVendidos;
    private JButton btnGenerarGraficoProductos;
    private JTable tablaProductosVendidos;
    private JScrollPane scrollTablaProductos;
    private JTabbedPane tabsProductos;

    // Panel para el gráfico de Productos Más Vendidos
    private JPanel panelGraficoProductos;

    // Botón general
    private JButton btnCerrarSesion;

    public GerenteUI() {
        setTitle("Panel de Gerencia - Nexcell");
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // <-- Maximizar pantalla
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane sistemaPestanas = new JTabbedPane(JTabbedPane.LEFT);
        sistemaPestanas.addTab("Reportes de Ventas", crearPanelReportesVentas());
        sistemaPestanas.addTab("Rendimiento Vendedores", crearPanelRendimiento());
        sistemaPestanas.addTab("Productos Más Vendidos", crearPanelProductosMasVendidos());

        add(sistemaPestanas, BorderLayout.CENTER);

        // Panel inferior para cerrar sesión
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setForeground(Color.RED);
        panelInferior.add(btnCerrarSesion);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelReportesVentas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelSuperior = new JPanel(new BorderLayout());

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buscarVentaField = new JTextField(15);
        btnBuscarVenta = new JButton("Buscar");
        panelBusqueda.add(new JLabel("Buscar (ID Venta o DNI): "));
        panelBusqueda.add(buscarVentaField);
        panelBusqueda.add(btnBuscarVenta);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnGenerarReporte = new JButton("Generar Reporte");
        btnLimpiarReporte = new JButton("Limpiar");

        panelFiltros.add(btnGenerarReporte);
        panelFiltros.add(btnLimpiarReporte);

        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelFiltros, BorderLayout.EAST);

        tablaReportesVentas = new JTable(new DefaultTableModel()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JScrollPane scrollTabla = new JScrollPane(tablaReportesVentas);

        JPanel panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelTotales.add(new JLabel("Total Reporte: $ 0.00"));

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);
        panel.add(panelTotales, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelRendimiento() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCalcularRendimiento = new JButton("Calcular Rendimiento por Fecha");

        panelSuperior.add(btnCalcularRendimiento);

        String[] columnas = {"Usuario Vendedor", "Cant. Ventas", "Total Facturado", "Comisión Estimada (5%)"};
        tablaRendimiento = new JTable(new DefaultTableModel(null, columnas)) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JScrollPane scrollTabla = new JScrollPane(tablaRendimiento);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelProductosMasVendidos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior dividido en Izquierda y Derecha
        JPanel panelSuperior = new JPanel(new BorderLayout());

        // Botón calcular a la izquierda
        JPanel panelIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCalcularProductosVendidos = new JButton("Calcular por Fecha");
        panelIzquierda.add(btnCalcularProductosVendidos);

        // Botón gráfico a la derecha
        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnGenerarGraficoProductos = new JButton("Generar Gráfico");
        btnGenerarGraficoProductos.setEnabled(false); // Lo deshabilitamos hasta que haya datos en la tabla
        panelDerecha.add(btnGenerarGraficoProductos);

        panelSuperior.add(panelIzquierda, BorderLayout.WEST);
        panelSuperior.add(panelDerecha, BorderLayout.EAST);
        // -------------------------------------------------------------

        String[] columnas = {"Ranking", "Producto", "Categoría", "Cant. Vendida", "Ingresos Generados"};
        tablaProductosVendidos = new JTable(new DefaultTableModel(null, columnas)) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        scrollTablaProductos = new JScrollPane(tablaProductosVendidos);

        panelGraficoProductos = new JPanel(new BorderLayout());
        panelGraficoProductos.setPreferredSize(new Dimension(800, 300));

        tabsProductos = new JTabbedPane();
        tabsProductos.addTab("Tabla de Datos", scrollTablaProductos);
        tabsProductos.addTab("Gráfico de Barras", panelGraficoProductos);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(tabsProductos, BorderLayout.CENTER); // Agregamos las pestañas en lugar del divisor

        return panel;
    }

    // Getters de Reporte de Ventas
    public JButton getBtnGenerarReporte() { return btnGenerarReporte; }
    public JButton getBtnLimpiarReporte() { return btnLimpiarReporte; }
    public JTable getTablaReportesVentas() { return tablaReportesVentas; }
    public JButton getBtnCerrarSesion() { return btnCerrarSesion; }
    public JTextField getBuscarVentaField() { return buscarVentaField; }
    public JButton getBtnBuscarVenta() { return btnBuscarVenta; }

    // Getters de Rendimiento de Vendedores
    public JButton getBtnCalcularRendimiento() { return btnCalcularRendimiento; }
    public JTable getTablaRendimiento() { return tablaRendimiento; }

    // Getters de Productos más Vendidos
    public JButton getBtnCalcularProductosVendidos() { return btnCalcularProductosVendidos; }
    public JTable getTablaProductosVendidos() { return tablaProductosVendidos; }
    public JPanel getPanelGraficoProductos() { return panelGraficoProductos; }
    public JButton getBtnGenerarGraficoProductos() { return btnGenerarGraficoProductos; }
    public JScrollPane getScrollTablaProductos() { return scrollTablaProductos; }
    public JTabbedPane getTabsProductos() { return tabsProductos; }
}
