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

    // Botón general
    private JButton btnCerrarSesion;

    public GerenteUI() {
        setTitle("Panel de Gerencia - Nexcell");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane sistemaPestanas = new JTabbedPane(JTabbedPane.LEFT);

        sistemaPestanas.addTab("Reportes de Ventas ", cargarIcono("/iconos/icono_reportes.png"), crearPanelReportesVentas());
        sistemaPestanas.addTab("Rendimiento ", cargarIcono("/iconos/icono_usuarios.png"), crearPanelRendimiento());

        add(sistemaPestanas, BorderLayout.CENTER);

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

        // Panel Izquierdo: Buscador
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        buscarVentaField = new JTextField(15);
        buscarVentaField.setPreferredSize(new Dimension(200, 35));

        btnBuscarVenta = new JButton("Buscar");
        btnBuscarVenta.setPreferredSize(new Dimension(100, 35));

        panelBusqueda.add(new JLabel("Buscar (ID Venta o DNI): "));
        panelBusqueda.add(buscarVentaField);
        panelBusqueda.add(btnBuscarVenta);

        // Panel Derecho: Botones (Se eliminó el ComboBox)
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 10));

        btnGenerarReporte = new JButton("Generar por Fecha");
        btnGenerarReporte.setPreferredSize(new Dimension(150, 35)); // Un poco más ancho para el nuevo texto

        btnLimpiarReporte = new JButton("Limpiar");
        btnLimpiarReporte.setPreferredSize(new Dimension(100, 35));

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

        // Controles superiores (Se eliminó el ComboBox)
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        btnCalcularRendimiento = new JButton("Calcular Rendimiento por Fecha");
        btnCalcularRendimiento.setPreferredSize(new Dimension(250, 35));

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

    // --- GETTERS VENTAS ---
    public JButton getBtnGenerarReporte() { return btnGenerarReporte; }
    public JButton getBtnLimpiarReporte() { return btnLimpiarReporte; }
    public JTable getTablaReportesVentas() { return tablaReportesVentas; }
    public JButton getBtnCerrarSesion() { return btnCerrarSesion; }
    public JTextField getBuscarVentaField() { return buscarVentaField; }
    public JButton getBtnBuscarVenta() { return btnBuscarVenta; }

    // --- GETTERS RENDIMIENTO ---
    public JButton getBtnCalcularRendimiento() { return btnCalcularRendimiento; }
    public JTable getTablaRendimiento() { return tablaRendimiento; }

    // --- CARGAR ÍCONOS ---
    private ImageIcon cargarIcono(String ruta) {
        java.net.URL imgURL = getClass().getResource(ruta);
        if (imgURL != null) {
            ImageIcon iconoOriginal = new ImageIcon(imgURL);
            Image imgEscalada = iconoOriginal.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            return new ImageIcon(imgEscalada);
        } else {
            System.err.println("No se encontró el ícono en: " + ruta);
            return null;
        }
    }
}