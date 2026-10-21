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

    // Componentes para la busqueda de un vendedor o compras de un cliente
    private JLabel lblInfoNombre;
    private JLabel lblInfoApellido;
    private JLabel lblInfoDni;
    private JLabel lblInfoExtra; // Se adapta para ser "Usuario" o "Atendido por"

    private JPopupMenu popupBusqueda;
    private DefaultListModel<String> modeloListaBusqueda;
    private JList<String> listaBusqueda;
    private java.util.List<Object[]> datosActualesBusqueda;
    private boolean ignorarEventosBusqueda = false;
    private JComboBox<String> comboCriterioBusqueda;

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

        // Contenedor superior para apilar los elementos verticalmente
        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        // Solo dejamos las 2 opciones solicitadas
        comboCriterioBusqueda = new JComboBox<>(new String[]{"DNI Cliente", "Usuario Vendedor"});
        comboCriterioBusqueda.setCursor(new Cursor(Cursor.HAND_CURSOR));

        buscarVentaField = new JTextField(15);
        btnBuscarVenta = crearBotonEstilizado("Buscar");

        // 4 JLabels informativos
        lblInfoNombre = new JLabel("Nombre: -");
        lblInfoApellido = new JLabel("Apellido: -");
        lblInfoDni = new JLabel("DNI: -");
        lblInfoExtra = new JLabel("Atendido por: -"); // Default porque Cliente es la opción 1

        Font fontInfo = new Font("Segoe UI", Font.ITALIC, 13);
        Color colorInfo = new Color(120, 120, 120);

        lblInfoNombre.setFont(fontInfo); lblInfoNombre.setForeground(colorInfo);
        lblInfoApellido.setFont(fontInfo); lblInfoApellido.setForeground(colorInfo);
        lblInfoDni.setFont(fontInfo); lblInfoDni.setForeground(colorInfo);
        lblInfoExtra.setFont(fontInfo); lblInfoExtra.setForeground(colorInfo);

        // Si el gerente cambia el combo, se limpian los campos para no mezclar datos
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
        panelBusqueda.add(lblInfoNombre);
        panelBusqueda.add(Box.createHorizontalStrut(10));
        panelBusqueda.add(lblInfoApellido);
        panelBusqueda.add(Box.createHorizontalStrut(10));
        panelBusqueda.add(lblInfoDni);
        panelBusqueda.add(Box.createHorizontalStrut(10));
        panelBusqueda.add(lblInfoExtra);

        cbDiaInicioV = new JComboBox<>(obtenerDias()); cbMesInicioV = new JComboBox<>(obtenerMeses()); cbAnioInicioV = new JComboBox<>(obtenerAnios());
        cbDiaFinV = new JComboBox<>(obtenerDias()); cbMesFinV = new JComboBox<>(obtenerMeses()); cbAnioFinV = new JComboBox<>(obtenerAnios());
        setearFechasPorDefecto(cbDiaInicioV, cbMesInicioV, cbAnioInicioV, cbDiaFinV, cbMesFinV, cbAnioFinV);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnGenerarReporte = crearBotonEstilizado("Generar Reporte");
        btnLimpiarReporte = crearBotonEstilizado("Limpiar");

        panelFiltros.add(new JLabel("Desde:"));
        panelFiltros.add(crearAgrupacionFecha(cbDiaInicioV, cbMesInicioV, cbAnioInicioV));
        panelFiltros.add(new JLabel("Hasta:"));
        panelFiltros.add(crearAgrupacionFecha(cbDiaFinV, cbMesFinV, cbAnioFinV));
        panelFiltros.add(btnGenerarReporte);
        panelFiltros.add(btnLimpiarReporte);

        // Colocamos la búsqueda arriba (NORTH) y los filtros abajo (CENTER)
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

    public void alHacerDobleClicEnVenta(java.util.function.Consumer<Long> accion) {
        tablaReportesVentas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Long id = getIdVentaSeleccionada();
                    if (id != null) {
                        accion.accept(id);
                    }
                }
            }
        });
    }

    public Long getIdVentaSeleccionada() {
        int fila = tablaReportesVentas.getSelectedRow();
        if (fila != -1) {
            // Se obtiene el valor y se remueve el prefijo "V-" agregado en el controlador
            String idStr = tablaReportesVentas.getValueAt(fila, 0).toString().replace("V-", "");
            try {
                return Long.parseLong(idStr);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    public void mostrarDialogoDetalleVenta(modelo.Venta venta, java.util.function.Consumer<modelo.Venta> accionDescargarPdf) {
        JDialog dialogo = new JDialog(this, "Detalle de Venta #" + venta.getId(), true);
        dialogo.setSize(550, 450);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(15, 15));

        JPanel panelInfo = new JPanel(new GridLayout(4, 1, 5, 5));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        panelInfo.add(new JLabel("Cliente: " + venta.getCliente().getNombre() + " " + venta.getCliente().getApellido() + " (DNI: " + venta.getCliente().getDni() + ")"));
        panelInfo.add(new JLabel("Atendido por: " + venta.getVendedor().getUsername()));
        panelInfo.add(new JLabel("Fecha de operación: " + venta.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        dialogo.add(panelInfo, BorderLayout.NORTH);

        String[] columnas = {"Producto", "Cantidad", "Subtotal"};
        DefaultTableModel modDetalle = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        for (modelo.DetalleVenta dv : venta.getDetalles()) {
            modDetalle.addRow(new Object[]{
                    dv.getProducto().getNombre(),
                    dv.getCantidad() + " u.",
                    String.format("$%.2f", dv.getSubtotal())
            });
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
                                if (esCliente) {
                                    modeloListaBusqueda.addElement(" " + fila[0].toString() + " - " + fila[1].toString() + " " + fila[2].toString());
                                } else {
                                    modeloListaBusqueda.addElement(" " + fila[3].toString() + " - " + fila[0].toString());
                                }
                            }

                            // Calcula la altura exacta según la cantidad de resultados ---
                            int cantidadResultados = datosActualesBusqueda.size();
                            int alturaFila = 25; // Altura fija de cada celda
                            int alturaDinamica = Math.min(cantidadResultados * alturaFila + 6, 130); // Máximo de 130px de alto

                            scrollAutocompletado.setPreferredSize(new Dimension(buscarVentaField.getWidth(), alturaDinamica));
                            scrollAutocompletado.revalidate();

                            if (!popupBusqueda.isVisible() && buscarVentaField.isShowing()) {
                                popupBusqueda.show(buscarVentaField, 0, buscarVentaField.getHeight());
                                buscarVentaField.requestFocusInWindow();
                            }
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

        listaBusqueda.addMouseMotionListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int index = listaBusqueda.locationToIndex(e.getPoint());
                if (index >= 0 && datosActualesBusqueda != null && index < datosActualesBusqueda.size()) {
                    listaBusqueda.setSelectedIndex(index);
                    Object[] seleccionado = datosActualesBusqueda.get(index);

                    lblInfoDni.setText("DNI: " + seleccionado[0].toString());
                    lblInfoNombre.setText("Nombre: " + seleccionado[1].toString());
                    lblInfoApellido.setText("Apellido: " + seleccionado[2].toString());

                    if (comboCriterioBusqueda.getSelectedItem().equals("DNI Cliente")) {
                        lblInfoExtra.setText("Atendido por: " + seleccionado[3].toString());
                    } else {
                        lblInfoExtra.setText("Usuario: " + seleccionado[3].toString());
                    }
                }
            }
        });

        listaBusqueda.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int index = listaBusqueda.locationToIndex(e.getPoint());
                    if (index >= 0 && datosActualesBusqueda != null) {
                        Object[] seleccionado = datosActualesBusqueda.get(index);
                        SwingUtilities.invokeLater(() -> {
                            ignorarEventosBusqueda = true;
                            if (comboCriterioBusqueda.getSelectedItem().equals("Usuario Vendedor")) {
                                buscarVentaField.setText(seleccionado[3].toString());
                            } else {
                                buscarVentaField.setText(seleccionado[0].toString());
                            }
                            popupBusqueda.setVisible(false);
                            btnBuscarVenta.doClick();
                            ignorarEventosBusqueda = false;
                        });
                    }
                }
            }
        });
    }

    private void restablecerLabelsInfo() {
        lblInfoNombre.setText("Nombre: -");
        lblInfoApellido.setText("Apellido: -");
        lblInfoDni.setText("DNI: -");
        if (comboCriterioBusqueda != null && comboCriterioBusqueda.getSelectedItem() != null) {
            if (comboCriterioBusqueda.getSelectedItem().equals("DNI Cliente")) {
                lblInfoExtra.setText("Atendido por: -");
            } else {
                lblInfoExtra.setText("Usuario: -");
            }
        }
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
    public JComboBox<String> getComboCriterioBusqueda() { return comboCriterioBusqueda; }
}
