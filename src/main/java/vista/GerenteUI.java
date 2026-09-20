package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class GerenteUI extends JFrame {

    // Componentes de Reportes de Ventas
    private JTextField buscarVentaField;
    private JButton btnBuscarVenta;
    private JComboBox<String> comboFiltroVentas;
    private JButton btnGenerarReporte;
    private JButton btnLimpiarReporte;
    private JTable tablaReportesVentas;

    // Componentes de Rendimiento de Vendedores
    private JComboBox<String> comboFiltroRendimiento;
    private JButton btnCalcularRendimiento;
    private JTable tablaRendimiento;

    // Botón general
    private JButton btnCerrarSesion;

    public GerenteUI() {
        setTitle("Panel de Gerencia - Nexcell");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane sistemaPestanas = new JTabbedPane(JTabbedPane.LEFT);
        sistemaPestanas.addTab("Reportes de Ventas", crearPanelReportesVentas());
        sistemaPestanas.addTab("Rendimiento Vendedores", crearPanelRendimiento()); // NUEVA PESTAÑA

        add(sistemaPestanas, BorderLayout.CENTER);

        // Podés agregar más pestañas a futuro (ej: "Rendimiento Vendedores")
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

        // Controles superiores reorganizados
        JPanel panelSuperior = new JPanel(new BorderLayout());

        // Panel Izquierdo: Búsqueda específica
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buscarVentaField = new JTextField(15);
        btnBuscarVenta = new JButton("Buscar");
        panelBusqueda.add(new JLabel("Buscar (ID Venta o DNI): "));
        panelBusqueda.add(buscarVentaField);
        panelBusqueda.add(btnBuscarVenta);

        // Panel Derecho: Filtros generales
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        String[] filtros = {"Ventas del Día", "Ventas del Mes", "Ventas por Vendedor", "Todas las Ventas"};
        comboFiltroVentas = new JComboBox<>(filtros);
        btnGenerarReporte = new JButton("Generar");
        btnLimpiarReporte = new JButton("Limpiar");

        panelFiltros.add(new JLabel("Período: "));
        panelFiltros.add(comboFiltroVentas);
        panelFiltros.add(btnGenerarReporte);
        panelFiltros.add(btnLimpiarReporte);

        // Ensamblamos la parte superior
        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelFiltros, BorderLayout.EAST);

        // Tabla central
        tablaReportesVentas = new JTable(new DefaultTableModel()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Bloqueamos la edición de las celdas
            }
        };
        JScrollPane scrollTabla = new JScrollPane(tablaReportesVentas);

        // Panel de totales
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

        // Controles superiores
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] periodos = {"Este Mes", "Mes Anterior", "Año Actual"};
        comboFiltroRendimiento = new JComboBox<>(periodos);
        btnCalcularRendimiento = new JButton("Calcular Rendimiento");

        panelSuperior.add(new JLabel("Período a evaluar: "));
        panelSuperior.add(comboFiltroRendimiento);
        panelSuperior.add(btnCalcularRendimiento);

        // Tabla central con edición bloqueada por defecto
        String[] columnas = {"Usuario Vendedor", "Cant. Ventas", "Total Facturado", "Comisión Estimada (5%)"};
        tablaRendimiento = new JTable(new DefaultTableModel(null, columnas)) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Bloqueamos la edición
            }
        };
        JScrollPane scrollTabla = new JScrollPane(tablaRendimiento);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
    }

    // --- GETTERS ---
    public JComboBox<String> getComboFiltroVentas() { return comboFiltroVentas; }
    public JButton getBtnGenerarReporte() { return btnGenerarReporte; }
    public JButton getBtnLimpiarReporte() { return btnLimpiarReporte; }
    public JTable getTablaReportesVentas() { return tablaReportesVentas; }
    public JButton getBtnCerrarSesion() { return btnCerrarSesion; }
    public JTextField getBuscarVentaField() { return buscarVentaField; }
    public JButton getBtnBuscarVenta() { return btnBuscarVenta; }

    // --- GETTERS RENDIMIENTO ---
    public JComboBox<String> getComboFiltroRendimiento() { return comboFiltroRendimiento; }
    public JButton getBtnCalcularRendimiento() { return btnCalcularRendimiento; }
    public JTable getTablaRendimiento() { return tablaRendimiento; }
}