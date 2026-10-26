package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class GerenteUI extends JFrame {

    // Variables Globales

    // -- Reportes de Ventas --
    private JTextField buscarVentaField;
    private JButton btnBuscarVenta, btnGenerarReporte, btnLimpiarReporte, btnExportarExcelVentas;
    private JTable tablaReportesVentas;
    private JLabel lblTotalReporteVentas;
    private JComboBox<String> cbDiaInicioV, cbMesInicioV, cbAnioInicioV, cbDiaFinV, cbMesFinV, cbAnioFinV;

    // -- Rendimiento de Vendedores --
    private JButton btnCalcularRendimiento, btnLimpiarRendimiento, btnExportarExcelRendimiento;
    private JLabel lblTotalRendimiento;
    private JTable tablaRendimiento;
    private JComboBox<String> cbDiaInicioR, cbMesInicioR, cbAnioInicioR, cbDiaFinR, cbMesFinR, cbAnioFinR;

    // -- Productos Más Vendidos --
    private JButton btnCalcularProductosVendidos, btnLimpiarProductos, btnExportarExcelProductos,
                    btnGenerarGraficoProductos;
    private JLabel lblTotalProductos;
    private JTable tablaProductosVendidos;
    private JScrollPane scrollTablaProductos;
    private JTabbedPane tabsProductos;
    private JPanel panelGraficoProductos;
    private JComboBox<String> cbDiaInicioP, cbMesInicioP, cbAnioInicioP, cbDiaFinP, cbMesFinP, cbAnioFinP;

    // -- Autocompletado y Búsqueda --
    private JLabel lblInfoNombre, lblInfoApellido, lblInfoDni, lblInfoExtra;
    private JPopupMenu popupBusqueda;
    private DefaultListModel<String> modeloListaBusqueda;
    private JList<String> listaBusqueda;
    private java.util.List<Object[]> datosActualesBusqueda;
    private boolean ignorarEventosBusqueda = false;
    private JComboBox<String> comboCriterioBusqueda;

    // -- Extras --
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
        sistemaPestanas.addTab("  Reportes de Ventas  ", cargarIcono("/iconos/icono_ventas.png"),
                               crearPanelReportesVentas());
        sistemaPestanas.addTab("  Rendimiento Vendedores  ", cargarIcono("/iconos/icono_usuarios.png"),
                               crearPanelRendimiento());
        sistemaPestanas.addTab("  Productos Más Vendidos  ", cargarIcono("/iconos/icono_reportes.png"),
                               crearPanelProductosMasVendidos());

        add(sistemaPestanas, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));
        btnCerrarSesion = crearBotonEstilizado("Cerrar Sesión");
        btnCerrarSesion.setForeground(new Color(224, 82, 82));
        panelInferior.add(btnCerrarSesion);
        add(panelInferior, BorderLayout.SOUTH);
    }

    // Metodos para la construccion de paneles
    private JPanel crearPanelReportesVentas() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        comboCriterioBusqueda = new JComboBox<>(new String[]{"DNI Cliente", "Usuario Vendedor"});
        comboCriterioBusqueda.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buscarVentaField = new JTextField(15);
        btnBuscarVenta = crearBotonEstilizado("Buscar");

        lblInfoNombre = crearLabelInformativo("Nombre: -");
        lblInfoApellido = crearLabelInformativo("Apellido: -");
        lblInfoDni = crearLabelInformativo("DNI: -");
        lblInfoExtra = crearLabelInformativo("Atendido por: -");

        comboCriterioBusqueda.addActionListener(e -> {
            ignorarEventosBusqueda = true;
            buscarVentaField.setText("");
            restablecerLabelsInfo();
            ignorarEventosBusqueda = false;
        });

        panelBusqueda.add(new JLabel("Filtrar por:"));
        panelBusqueda.add(comboCriterioBusqueda);
        panelBusqueda.add(buscarVentaField);
        panelBusqueda.add(btnBuscarVenta);
        panelBusqueda.add(Box.createHorizontalStrut(15));
        panelBusqueda.add(lblInfoNombre); panelBusqueda.add(Box.createHorizontalStrut(10));
        panelBusqueda.add(lblInfoApellido); panelBusqueda.add(Box.createHorizontalStrut(10));
        panelBusqueda.add(lblInfoDni); panelBusqueda.add(Box.createHorizontalStrut(10));
        panelBusqueda.add(lblInfoExtra);

        cbDiaInicioV = new JComboBox<>(obtenerDias());
        cbMesInicioV = new JComboBox<>(obtenerMeses());
        cbAnioInicioV = new JComboBox<>(obtenerAnios());
        cbDiaFinV = new JComboBox<>(obtenerDias());
        cbMesFinV = new JComboBox<>(obtenerMeses());
        cbAnioFinV = new JComboBox<>(obtenerAnios());
        setearFechasPorDefecto(cbDiaInicioV, cbMesInicioV, cbAnioInicioV, cbDiaFinV, cbMesFinV, cbAnioFinV);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnGenerarReporte = crearBotonEstilizado("Generar Reporte");
        btnLimpiarReporte = crearBotonEstilizado("Limpiar");

        btnExportarExcelVentas = crearBotonEstilizado("Exportar a Excel");
        btnExportarExcelVentas.setBackground(new Color(33, 115, 70));
        btnExportarExcelVentas.setForeground(Color.WHITE);
        btnExportarExcelVentas.setEnabled(false);

        panelFiltros.add(new JLabel("Desde:"));
        panelFiltros.add(crearAgrupacionFecha(cbDiaInicioV, cbMesInicioV, cbAnioInicioV));

        panelFiltros.add(new JLabel("Hasta:"));
        panelFiltros.add(crearAgrupacionFecha(cbDiaFinV, cbMesFinV, cbAnioFinV));
        panelFiltros.add(btnGenerarReporte);
        panelFiltros.add(btnLimpiarReporte);
        panelFiltros.add(btnExportarExcelVentas);

        panelSuperior.add(panelBusqueda, BorderLayout.NORTH);
        panelSuperior.add(panelFiltros, BorderLayout.CENTER);

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

        cbDiaInicioR = new JComboBox<>(obtenerDias());
        cbMesInicioR = new JComboBox<>(obtenerMeses());
        cbAnioInicioR = new JComboBox<>(obtenerAnios());
        cbDiaFinR = new JComboBox<>(obtenerDias());
        cbMesFinR = new JComboBox<>(obtenerMeses());
        cbAnioFinR = new JComboBox<>(obtenerAnios());
        setearFechasPorDefecto(cbDiaInicioR, cbMesInicioR, cbAnioInicioR, cbDiaFinR, cbMesFinR, cbAnioFinR);

        btnCalcularRendimiento = crearBotonEstilizado("Calcular Rendimiento por Fecha");
        btnLimpiarRendimiento = crearBotonEstilizado("Limpiar");

        btnExportarExcelRendimiento = crearBotonEstilizado("Exportar a Excel");
        btnExportarExcelRendimiento.setBackground(new Color(33, 115, 70));
        btnExportarExcelRendimiento.setForeground(Color.WHITE);
        btnExportarExcelRendimiento.setEnabled(false);

        panelSuperior.add(new JLabel("Desde:"));
        panelSuperior.add(crearAgrupacionFecha(cbDiaInicioR, cbMesInicioR, cbAnioInicioR));
        panelSuperior.add(new JLabel("Hasta:"));
        panelSuperior.add(crearAgrupacionFecha(cbDiaFinR, cbMesFinR, cbAnioFinR));
        panelSuperior.add(btnCalcularRendimiento);
        panelSuperior.add(btnLimpiarRendimiento);
        panelSuperior.add(btnExportarExcelRendimiento);

        String[] columnas = {"Usuario Vendedor", "Cant. Ventas", "Total Facturado", "Comisión Estimada (5%)"};
        tablaRendimiento = new JTable(new DefaultTableModel(null, columnas)) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        configurarTabla(tablaRendimiento);

        JPanel panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotalRendimiento = new JLabel("Total Facturado: $0.00");
        lblTotalRendimiento.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalRendimiento.setForeground(new Color(40, 167, 69));
        panelTotales.add(lblTotalRendimiento);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaRendimiento), BorderLayout.CENTER);
        panel.add(panelTotales, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelProductosMasVendidos() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        cbDiaInicioP = new JComboBox<>(obtenerDias());
        cbMesInicioP = new JComboBox<>(obtenerMeses());
        cbAnioInicioP = new JComboBox<>(obtenerAnios());
        cbDiaFinP = new JComboBox<>(obtenerDias());
        cbMesFinP = new JComboBox<>(obtenerMeses());
        cbAnioFinP = new JComboBox<>(obtenerAnios());
        setearFechasPorDefecto(cbDiaInicioP, cbMesInicioP, cbAnioInicioP, cbDiaFinP, cbMesFinP, cbAnioFinP);

        btnCalcularProductosVendidos = crearBotonEstilizado("Calcular por Fecha");
        btnLimpiarProductos = crearBotonEstilizado("Limpiar");

        btnExportarExcelProductos = crearBotonEstilizado("Excel");
        btnExportarExcelProductos.setBackground(new Color(33, 115, 70));
        btnExportarExcelProductos.setForeground(Color.WHITE);
        btnExportarExcelProductos.setEnabled(false);

        btnGenerarGraficoProductos = crearBotonEstilizado("Generar Gráfico");
        btnGenerarGraficoProductos.setEnabled(false);

        panelSuperior.add(new JLabel("Desde:"));
        panelSuperior.add(crearAgrupacionFecha(cbDiaInicioP, cbMesInicioP, cbAnioInicioP));
        panelSuperior.add(new JLabel("Hasta:"));
        panelSuperior.add(crearAgrupacionFecha(cbDiaFinP, cbMesFinP, cbAnioFinP));
        panelSuperior.add(btnCalcularProductosVendidos);
        panelSuperior.add(btnLimpiarProductos);
        panelSuperior.add(btnExportarExcelProductos);
        panelSuperior.add(btnGenerarGraficoProductos);

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

        JPanel panelTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotalProductos = new JLabel("Total Ingresos Generados: $0.00");
        lblTotalProductos.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalProductos.setForeground(new Color(40, 167, 69));
        panelTotales.add(lblTotalProductos);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(tabsProductos, BorderLayout.CENTER);
        panel.add(panelTotales, BorderLayout.SOUTH);

        return panel;
    }

    // Metodos varios para la creacion de la vista
    public void renderizarGraficoBarras(DefaultCategoryDataset dataset, String tituloGrafico) {
        JFreeChart graficoBarras = ChartFactory.createBarChart(
                tituloGrafico, "Productos", "Cantidad Vendida", dataset
        );
        graficoBarras.setBackgroundPaint(java.awt.Color.WHITE);

        org.jfree.chart.plot.CategoryPlot plot = graficoBarras.getCategoryPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new java.awt.Color(170, 170, 170));
        plot.setRangeGridlineStroke(new java.awt.BasicStroke(
                1.2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND,
                1.0f, new float[]{6.0f, 4.0f}, 0.0f));

        org.jfree.chart.renderer.category.BarRenderer renderer = new org.jfree.chart.renderer.category.BarRenderer() {
            @Override
            public java.awt.Paint getItemPaint(int row, int column) {
                double max = 0;
                for (int i = 0; i < dataset.getColumnCount(); i++) {
                    Number val = dataset.getValue(0, i);
                    if (val != null && val.doubleValue() > max) {
                        max = val.doubleValue();
                    }
                }
                Number valorActual = dataset.getValue(row, column);
                double val = valorActual != null ? valorActual.doubleValue() : 0;
                int alpha = (max > 0) ? (int) ((val / max) * 200) + 55 : 55;
                alpha = Math.min(alpha, 255);

                java.awt.Color[] coloresBase = {
                        new java.awt.Color(204, 0, 0), new java.awt.Color(0, 153, 51),
                        new java.awt.Color(0, 102, 204), new java.awt.Color(255, 128, 0),
                        new java.awt.Color(102, 0, 204), new java.awt.Color(0, 153, 153)
                };
                java.awt.Color colorElegido = coloresBase[column % coloresBase.length];
                return new java.awt.Color(
                        colorElegido.getRed(), colorElegido.getGreen(), colorElegido.getBlue(), alpha
                );
            }
        };

        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setDrawBarOutline(false);
        plot.setRenderer(renderer);

        ChartPanel panelBarras = new ChartPanel(graficoBarras);
        panelGraficoProductos.removeAll();
        panelGraficoProductos.add(panelBarras, java.awt.BorderLayout.CENTER);
        panelGraficoProductos.revalidate();
        panelGraficoProductos.repaint();
        tabsProductos.setSelectedIndex(1);
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

    private JLabel crearLabelInformativo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        label.setForeground(new Color(120, 120, 120));
        return label;
    }

    private ImageIcon cargarIcono(String ruta) {
        java.net.URL imgURL = getClass().getResource(ruta);
        if (imgURL != null) return new ImageIcon(new ImageIcon(imgURL).getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH));
        return null;
    }

    private JPanel crearAgrupacionFecha(JComboBox<String> dia, JComboBox<String> mes, JComboBox<String> anio) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        p.add(dia); p.add(new JLabel("/")); p.add(mes); p.add(new JLabel("/")); p.add(anio);
        return p;
    }

    private void setearFechasPorDefecto(JComboBox<String> dI, JComboBox<String> mI, JComboBox<String> aI, JComboBox<String> dF, JComboBox<String> mF, JComboBox<String> aF) {
        LocalDate hoy = LocalDate.now();
        LocalDate haceUnMes = hoy.minusMonths(1);
        dI.setSelectedIndex(haceUnMes.getDayOfMonth() - 1); mI.setSelectedIndex(haceUnMes.getMonthValue() - 1); aI.setSelectedItem(String.valueOf(haceUnMes.getYear()));
        dF.setSelectedIndex(hoy.getDayOfMonth() - 1); mF.setSelectedIndex(hoy.getMonthValue() - 1); aF.setSelectedItem(String.valueOf(hoy.getYear()));
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

    // Metodos de dialogos que informan al usuario

    // Estos métodos evitan que el Controlador toque librerías Swing (JFileChooser / JOptionPane)
    public java.io.File solicitarArchivoParaGuardar(String nombrePorDefecto) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte como Excel (.xlsx)");
        fileChooser.setSelectedFile(new java.io.File(nombrePorDefecto + ".xlsx"));
        fileChooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                        "Documento de Excel (*.xlsx)", "xlsx"
                )
        );

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File archivo = fileChooser.getSelectedFile();
            if (!archivo.getName().toLowerCase().endsWith(".xlsx")) {
                return new java.io.File(archivo.getAbsolutePath() + ".xlsx");
            }
            return archivo;
        }
        return null; // El usuario canceló
    }

    public void mostrarMensaje(String mensaje, String titulo, int tipoMensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipoMensaje);
    }

    public boolean pedirConfirmacion(String mensaje, String titulo) {
        return JOptionPane.showConfirmDialog(this, mensaje, titulo, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public void mostrarDialogoDetalleVenta(modelo.Venta venta, java.util.function.Consumer<modelo.Venta> accionDescargarPdf) {
        JDialog dialogo = new JDialog(this, "Detalle de Venta #" + venta.getId(), true);
        dialogo.setSize(550, 450);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(15, 15));

        JPanel panelInfo = new JPanel(new GridLayout(4, 1, 5, 5));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        panelInfo.add(new JLabel(
                "Cliente: " + venta.getCliente().getNombre() +
                        " " + venta.getCliente().getApellido() + " (DNI: " +
                        venta.getCliente().getDni() + ")"
                )
        );
        panelInfo.add(new JLabel("Atendido por: " + venta.getVendedor().getUsername()));
        panelInfo.add(new JLabel(
                "Fecha: " +
                        venta.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                )
        );
        dialogo.add(panelInfo, BorderLayout.NORTH);

        String[] columnas = {"Producto", "Cantidad", "Subtotal"};
        DefaultTableModel modDetalle = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        for (modelo.DetalleVenta dv : venta.getDetalles()) {
            modDetalle.addRow(new Object[]{ dv.getProducto().getNombre(), dv.getCantidad() + " u.", String.format("$%.2f", dv.getSubtotal()) });
        }

        JTable tablaDetalles = new JTable(modDetalle);
        tablaDetalles.setRowHeight(30);
        JScrollPane scroll = new JScrollPane(tablaDetalles);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        dialogo.add(scroll, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JLabel lblTotal = new JLabel("TOTAL ABONADO: $" + String.format("%.2f", venta.getTotal()));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(new Color(40, 167, 69));

        JButton btnPdf = new JButton("Descargar Comprobante PDF");
        btnPdf.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPdf.addActionListener(e -> accionDescargarPdf.accept(venta));

        panelInferior.add(lblTotal, BorderLayout.WEST);
        panelInferior.add(btnPdf, BorderLayout.EAST);
        dialogo.add(panelInferior, BorderLayout.SOUTH);

        dialogo.setVisible(true);
    }

    // Listeners y logica para la barra de busqueda

    public void alHacerDobleClicEnVenta(java.util.function.Consumer<Long> accion) {
        tablaReportesVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Long id = getIdVentaSeleccionada();
                    if (id != null) accion.accept(id);
                }
            }
        });
    }

    public void inicializarAutocompletadoDni(java.util.function.Function<String, java.util.List<Object[]>> buscador) {
        popupBusqueda = new JPopupMenu();
        popupBusqueda.setFocusable(false);

        modeloListaBusqueda = new DefaultListModel<>();
        listaBusqueda = new JList<>(modeloListaBusqueda);
        listaBusqueda.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaBusqueda.setFixedCellHeight(25);
        listaBusqueda.setCursor(new Cursor(Cursor.HAND_CURSOR));
        listaBusqueda.setFocusable(false);

        JScrollPane scrollAutocompletado = new JScrollPane(listaBusqueda);
        scrollAutocompletado.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        popupBusqueda.add(scrollAutocompletado);

        buscarVentaField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarResultados(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarResultados(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarResultados(); }

            private void actualizarResultados() {
                if (ignorarEventosBusqueda) return;
                SwingUtilities.invokeLater(() -> {
                    String texto = buscarVentaField.getText().trim();
                    if (texto.length() > 0) {
                        datosActualesBusqueda = buscador.apply(texto);
                        modeloListaBusqueda.clear();

                        if (datosActualesBusqueda != null && !datosActualesBusqueda.isEmpty()) {
                            boolean esCliente = comboCriterioBusqueda.getSelectedItem().equals("DNI Cliente");
                            for (Object[] fila : datosActualesBusqueda) {
                                if (esCliente) modeloListaBusqueda.addElement(" " + fila[0] + " - " + fila[1] + " " + fila[2]);
                                else modeloListaBusqueda.addElement(" " + fila[3] + " - " + fila[0]);
                            }

                            int alturaDinamica = Math.min(datosActualesBusqueda.size() * 25 + 6, 130);
                            scrollAutocompletado.setPreferredSize(new Dimension(buscarVentaField.getWidth(), alturaDinamica));
                            scrollAutocompletado.revalidate();

                            if (!popupBusqueda.isVisible() && buscarVentaField.isShowing()) {
                                popupBusqueda.show(buscarVentaField, 0, buscarVentaField.getHeight());
                                buscarVentaField.requestFocusInWindow();
                            }
                            listaBusqueda.setSelectedIndex(0);
                            actualizarLabelsInfoDesdeLista(0);
                        } else {
                            popupBusqueda.setVisible(false);
                            restablecerLabelsInfo();
                        }
                    } else {
                        popupBusqueda.setVisible(false);
                        restablecerLabelsInfo();
                    }
                });
            }
        });

        buscarVentaField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyPressed(java.awt.event.KeyEvent e) {
                if (popupBusqueda.isVisible()) {
                    int indiceActual = listaBusqueda.getSelectedIndex();
                    int maxIndice = modeloListaBusqueda.getSize() - 1;

                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN && indiceActual < maxIndice) {
                        listaBusqueda.setSelectedIndex(indiceActual + 1);
                        listaBusqueda.ensureIndexIsVisible(indiceActual + 1);
                        actualizarLabelsInfoDesdeLista(indiceActual + 1);
                    } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP && indiceActual > 0) {
                        listaBusqueda.setSelectedIndex(indiceActual - 1);
                        listaBusqueda.ensureIndexIsVisible(indiceActual - 1);
                        actualizarLabelsInfoDesdeLista(indiceActual - 1);
                    } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && indiceActual >= 0 && datosActualesBusqueda != null) {
                        ejecutarSeleccion(indiceActual);
                    }
                }
            }
        });

        listaBusqueda.addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                int index = listaBusqueda.locationToIndex(e.getPoint());
                if (index >= 0 && datosActualesBusqueda != null && index < datosActualesBusqueda.size()) {
                    listaBusqueda.setSelectedIndex(index);
                    actualizarLabelsInfoDesdeLista(index);
                }
            }
        });

        listaBusqueda.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = listaBusqueda.locationToIndex(e.getPoint());
                    if (index >= 0 && datosActualesBusqueda != null) ejecutarSeleccion(index);
                }
            }
        });
    }

    private void actualizarLabelsInfoDesdeLista(int index) {
        if (datosActualesBusqueda != null && index < datosActualesBusqueda.size()) {
            Object[] seleccionado = datosActualesBusqueda.get(index);
            lblInfoDni.setText("DNI: " + seleccionado[0]);
            lblInfoNombre.setText("Nombre: " + seleccionado[1]);
            lblInfoApellido.setText("Apellido: " + seleccionado[2]);
            lblInfoExtra.setText((comboCriterioBusqueda.getSelectedItem().equals("DNI Cliente") ? "Atendido por: " : "Usuario: ") + seleccionado[3]);
        }
    }

    private void ejecutarSeleccion(int index) {
        Object[] seleccionado = datosActualesBusqueda.get(index);
        SwingUtilities.invokeLater(() -> {
            ignorarEventosBusqueda = true;
            buscarVentaField.setText(comboCriterioBusqueda.getSelectedItem().equals("Usuario Vendedor") ? seleccionado[3].toString() : seleccionado[0].toString());
            popupBusqueda.setVisible(false);
            btnBuscarVenta.doClick();
            ignorarEventosBusqueda = false;
        });
    }

    private void restablecerLabelsInfo() {
        lblInfoNombre.setText("Nombre: -"); lblInfoApellido.setText("Apellido: -"); lblInfoDni.setText("DNI: -");
        lblInfoExtra.setText(comboCriterioBusqueda != null && comboCriterioBusqueda.getSelectedItem().equals("DNI Cliente") ? "Atendido por: -" : "Usuario: -");
    }

    public Long getIdVentaSeleccionada() {
        int fila = tablaReportesVentas.getSelectedRow();
        if (fila != -1) {
            try {
                return Long.parseLong(tablaReportesVentas.getValueAt(fila, 0).toString().replace("V-", ""));
            } catch (NumberFormatException ex) { return null; }
        }
        return null;
    }

    // Getters

    public JButton getBtnGenerarReporte() { return btnGenerarReporte; }
    public JButton getBtnLimpiarReporte() { return btnLimpiarReporte; }
    public JTable getTablaReportesVentas() { return tablaReportesVentas; }
    public JLabel getLblTotalReporteVentas() { return lblTotalReporteVentas; }

    public JButton getBtnCalcularRendimiento() { return btnCalcularRendimiento; }
    public JTable getTablaRendimiento() { return tablaRendimiento; }
    public JButton getBtnLimpiarRendimiento() { return btnLimpiarRendimiento; }
    public JLabel getLblTotalRendimiento() { return lblTotalRendimiento; }

    public JButton getBtnCalcularProductosVendidos() { return btnCalcularProductosVendidos; }
    public JTable getTablaProductosVendidos() { return tablaProductosVendidos; }
    public JButton getBtnLimpiarProductos() { return btnLimpiarProductos; }
    public JLabel getLblTotalProductos() { return lblTotalProductos; }
    public JPanel getPanelGraficoProductos() { return panelGraficoProductos; }
    public JButton getBtnGenerarGraficoProductos() { return btnGenerarGraficoProductos; }
    public JTabbedPane getTabsProductos() { return tabsProductos; }

    public JTextField getBuscarVentaField() { return buscarVentaField; }
    public JButton getBtnBuscarVenta() { return btnBuscarVenta; }
    public JComboBox<String> getComboCriterioBusqueda() { return comboCriterioBusqueda; }

    public JButton getBtnExportarExcelVentas() { return btnExportarExcelVentas; }
    public JButton getBtnExportarExcelRendimiento() { return btnExportarExcelRendimiento; }
    public JButton getBtnExportarExcelProductos() { return btnExportarExcelProductos; }
    public JButton getBtnCerrarSesion() { return btnCerrarSesion; }

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