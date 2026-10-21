package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class VendedorUI extends JFrame {

    // Componentes de Clientes
    private JTextField buscarClienteField;
    private JButton btnBuscarCliente;
    private JTable tablaClientes;
    private JButton btnAbrirFormularioCliente;
    private JButton btnBajaCliente;
    private JButton btnAltaCliente;
    private JButton btnEditarCliente;

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

    // Componentes de Reportes
    private JComboBox<String> comboTipoReporte;
    private JComboBox<String> comboCategoriasReporte;
    private JComboBox<String> cbDiaInicio, cbMesInicio, cbAnioInicio;
    private JComboBox<String> cbDiaFin, cbMesFin, cbAnioFin;
    private JButton btnGenerarReporte;
    private JButton btnExportarReporte;
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

        // Inicializar comportamientos puramente visuales de la Vista
        inicializarComportamientosVisuales();
    }

    private void inicializarComportamientosVisuales() {
        tablaClientes.setAutoCreateRowSorter(true);
        tablaProductos.setAutoCreateRowSorter(true);
        tablaVentas.setAutoCreateRowSorter(true);
        tablaReportes.setAutoCreateRowSorter(true);

        configurarFiltroBusqueda(buscarClienteField, btnBuscarCliente, tablaClientes);
        configurarFiltroBusqueda(buscarProductoField, btnBuscarProducto, tablaProductos);
        configurarFiltroBusqueda(buscarVentaField, btnBuscarVenta, tablaVentas);

        // Deseleccionar cliente al hacer clic en el espacio vacío
        java.awt.event.MouseAdapter deseleccionador = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (tablaClientes.rowAtPoint(e.getPoint()) == -1) {
                    limpiarSeleccionClientes();
                }
            }
        };
        tablaClientes.addMouseListener(deseleccionador);
        if (tablaClientes.getParent() instanceof JViewport) {
            tablaClientes.getParent().addMouseListener(deseleccionador);
        }

        // Mostrar/ocultar botones según el cliente seleccionado
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int f = tablaClientes.getSelectedRow();
                if (f != -1) {
                    boolean activo = tablaClientes.getValueAt(f, 5).toString().equalsIgnoreCase("Activo");
                    btnBajaCliente.setVisible(activo);
                    btnAltaCliente.setVisible(!activo);
                    btnEditarCliente.setVisible(true);
                } else {
                    btnBajaCliente.setVisible(false);
                    btnAltaCliente.setVisible(false);
                    btnEditarCliente.setVisible(false);
                }
            }
        });
    }

    // =========================================================================
    // MÉTODOS DE RENDERIZADO DE DATOS (Llamados por el Controlador)
    // =========================================================================

    public void mostrarClientes(List<modelo.Cliente> clientes) {
        DefaultTableModel mod = (DefaultTableModel) tablaClientes.getModel();
        mod.setRowCount(0);
        for (modelo.Cliente c : clientes) {
            mod.addRow(new Object[]{
                c.getDni(), c.getNombre(), c.getApellido(), c.getTelefono(), c.getEmail(), c.isActivo() ? "Activo" : "Inactivo"
            });
        }
    }

    public void actualizarEstadoClienteEnTabla(int filaVista, boolean activo) {
        tablaClientes.setValueAt(activo ? "Activo" : "Inactivo", filaVista, 5);
        limpiarSeleccionClientes();
    }

    public void limpiarSeleccionClientes() {
        tablaClientes.clearSelection();
        btnBajaCliente.setVisible(false);
        btnAltaCliente.setVisible(false);
        btnEditarCliente.setVisible(false);
    }

    public void mostrarProductos(List<modelo.Producto> productos) {
        DefaultTableModel mod = (DefaultTableModel) tablaProductos.getModel();
        mod.setRowCount(0);
        for (modelo.Producto p : productos) {
            mod.addRow(new Object[]{
                p.getId(), p.getCategoria(), p.getNombre(), p.getStock(), String.format("$%.2f", p.getPrecio())
            });
        }
    }

    public void mostrarVentas(List<modelo.Venta> ventas) {
        DefaultTableModel mod = (DefaultTableModel) tablaVentas.getModel();
        mod.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (modelo.Venta v : ventas) {
            int cant = v.getDetalles().size();
            String desc = cant == 1 ? v.getDetalles().get(0).getProducto().getNombre() : cant + " art. (Varios)";
            mod.addRow(new Object[]{
                v.getId(), v.getFecha().format(fmt), v.getCliente().getDni(), desc, String.format("$%.2f", v.getTotal())
            });
        }
    }

    // --- RENDERIZADO DE REPORTES EN LA VISTA ---

    public void mostrarReporteResumenDiario(List<Object[]> resultados) {
        DefaultTableModel modelo = (DefaultTableModel) tablaReportes.getModel();
        modelo.setColumnIdentifiers(new String[]{"Fecha de Operación", "Tickets Emitidos", "Monto Facturado"});
        modelo.setRowCount(0);

        double granTotal = 0;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Object[] fila : resultados) {
            java.sql.Date fechaSql = (java.sql.Date) fila[0];
            String fechaStr = fechaSql.toLocalDate().format(fmt);
            int tickets = ((Number) fila[1]).intValue();
            double total = ((Number) fila[2]).doubleValue();

            granTotal += total;
            modelo.addRow(new Object[]{fechaStr, tickets + " ventas", String.format("$%.2f", total)});
        }
        lblTotalReporte.setText(String.format("Facturación Periodo: $%.2f", granTotal));
    }

    public void mostrarReporteTopProductos(List<Object[]> resultados) {
        DefaultTableModel modelo = (DefaultTableModel) tablaReportes.getModel();
        modelo.setColumnIdentifiers(new String[]{"Producto (Modelo)", "Categoría", "Unidades Vendidas", "Ingreso Generado"});
        modelo.setRowCount(0);

        double granTotal = 0;
        for (Object[] fila : resultados) {
            String producto = (String) fila[0];
            String categoria = (String) fila[1];
            int unidades = ((Number) fila[2]).intValue();
            double total = ((Number) fila[3]).doubleValue();

            granTotal += total;
            modelo.addRow(new Object[]{producto, categoria, unidades + " u.", String.format("$%.2f", total)});
        }
        lblTotalReporte.setText(String.format("Ingreso por estos productos: $%.2f", granTotal));
    }

    public void mostrarReporteRendimientoCategoria(List<Object[]> resultados, String categoria) {
        DefaultTableModel modelo = (DefaultTableModel) tablaReportes.getModel();
        modelo.setColumnIdentifiers(new String[]{"Producto (" + categoria + ")", "Unidades Vendidas", "Ingresos Generados"});
        modelo.setRowCount(0);

        double granTotal = 0;
        for (Object[] fila : resultados) {
            double ingresos = ((Number) fila[2]).doubleValue();
            granTotal += ingresos;
            modelo.addRow(new Object[]{fila[0], fila[1], String.format("$%.2f", ingresos)});
        }
        lblTotalReporte.setText(String.format("Total en Categoría: $%.2f", granTotal));
    }

    public void mostrarReporteMejoresClientes(List<Object[]> resultados) {
        DefaultTableModel modelo = (DefaultTableModel) tablaReportes.getModel();
        modelo.setColumnIdentifiers(new String[]{"DNI Cliente", "Nombre Completo", "Compras", "Total Gastado"});
        modelo.setRowCount(0);

        for (Object[] fila : resultados) {
            String nombreCompleto = fila[1] + " " + fila[2];
            double gastado = ((Number) fila[4]).doubleValue();
            modelo.addRow(new Object[]{fila[0], nombreCompleto, fila[3], String.format("$%.2f", gastado)});
        }
        lblTotalReporte.setText("Ranking de Fidelización (Clientes del Vendedor)");
    }

    // --- SUB-VENTANA DE DETALLE DE VENTA (100% Responsabilidad de la Vista) ---

    public void mostrarDialogoDetalleVenta(modelo.Venta venta, Consumer<modelo.Venta> accionDescargarPdf) {
        JDialog dialogo = new JDialog(this, "Detalle de Venta #" + venta.getId(), true);
        dialogo.setSize(550, 450);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout(15, 15));

        JPanel panelInfo = new JPanel(new GridLayout(4, 1, 5, 5));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        panelInfo.add(new JLabel("Cliente: " + venta.getCliente().getNombre() + " " + venta.getCliente().getApellido() + " (DNI: " + venta.getCliente().getDni() + ")"));
        panelInfo.add(new JLabel("Atendido por: " + venta.getVendedor().getUsername()));
        panelInfo.add(new JLabel("Fecha de operación: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

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

    // --- UTILIDADES DE INTERACCIÓN CON EL USUARIO (Diálogos y Selectores) ---

    public void mostrarMensaje(String mensaje, String titulo, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }

    public boolean confirmarAccion(String mensaje, String titulo) {
        return JOptionPane.showConfirmDialog(this, mensaje, titulo, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public File seleccionarArchivoCSVGuardar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte Excel (CSV)");
        fileChooser.setSelectedFile(new File("Nexcell_Reporte_" + System.currentTimeMillis() + ".csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    public String getDniClienteSeleccionado() {
        int fila = tablaClientes.getSelectedRow();
        return fila != -1 ? tablaClientes.getValueAt(fila, 0).toString() : null;
    }

    public Long getIdVentaSeleccionada() {
        int fila = tablaVentas.getSelectedRow();
        return fila != -1 ? (Long) tablaVentas.getValueAt(fila, 0) : null;
    }

    public String getFechaInicioReporteStr() {
        return cbDiaInicio.getSelectedItem() + "/" + cbMesInicio.getSelectedItem() + "/" + cbAnioInicio.getSelectedItem();
    }

    public String getFechaFinReporteStr() {
        return cbDiaFin.getSelectedItem() + "/" + cbMesFin.getSelectedItem() + "/" + cbAnioFin.getSelectedItem();
    }

    // =========================================================================
    // CONSTRUCCIÓN DE PANELES INTERNOS Y FILTROS VISUALES
    // =========================================================================

    private void configurarFiltroBusqueda(JTextField campo, JButton boton, JTable tabla) {
        Runnable accionFiltrar = () -> {
            String texto = campo.getText().trim();
            TableRowSorter<?> sorter = (TableRowSorter<?>) tabla.getRowSorter();
            if (sorter != null) {
                if (texto.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
                }
            }
        };
        boton.addActionListener(e -> accionFiltrar.run());
        campo.addActionListener(e -> accionFiltrar.run());
        campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { accionFiltrar.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { accionFiltrar.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { accionFiltrar.run(); }
        });
    }

    private JPanel crearPanelClientes() {
        JPanel panel = crearPanelBase();
        buscarClienteField = new JTextField(20);
        btnBuscarCliente = crearBotonWeb("Buscar");
        JPanel panelBusqueda = crearBarraBusqueda("Buscar (DNI):", buscarClienteField, btnBuscarCliente);

        btnAbrirFormularioCliente = crearBotonWeb("Nuevo Cliente");
        btnBajaCliente = crearBotonWeb("Baja Lógica");
        btnAltaCliente = crearBotonWeb("Reactivar");
        btnEditarCliente = crearBotonWeb("Editar");

        btnBajaCliente.setVisible(false);
        btnAltaCliente.setVisible(false);
        btnEditarCliente.setVisible(false);

        JPanel panelAcciones = crearBarraAcciones(btnBajaCliente, btnAltaCliente, btnEditarCliente, btnAbrirFormularioCliente);
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
            "Resumen Diario / Arqueo",
            "Top Productos Vendidos",
            "Rendimiento por Categoría",
            "Mis Mejores Clientes"
        });

        String[] categorias = {"Smartphone", "Accesorio", "Auricular", "Parlante", "SmartWatch"};
        comboCategoriasReporte = new JComboBox<>(categorias);
        comboCategoriasReporte.setVisible(false);

        comboTipoReporte.addActionListener(e -> {
            String seleccionado = comboTipoReporte.getSelectedItem().toString();
            comboCategoriasReporte.setVisible(seleccionado.equals("Rendimiento por Categoría"));

            boolean esRango = !seleccionado.equals("Resumen Diario / Arqueo");
            cbDiaFin.setEnabled(esRango);
            cbMesFin.setEnabled(esRango);
            cbAnioFin.setEnabled(esRango);
        });

        String[] dias = new String[31];
        for (int i = 0; i < 31; i++) dias[i] = String.format("%02d", i + 1);

        String[] meses = new String[12];
        for (int i = 0; i < 12; i++) meses[i] = String.format("%02d", i + 1);

        String[] anios = new String[5];
        int anioActual = LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) anios[i] = String.valueOf(anioActual - i);

        cbDiaInicio = new JComboBox<>(dias); cbMesInicio = new JComboBox<>(meses); cbAnioInicio = new JComboBox<>(anios);
        cbDiaFin = new JComboBox<>(dias); cbMesFin = new JComboBox<>(meses); cbAnioFin = new JComboBox<>(anios);

        LocalDate hoy = LocalDate.now();
        LocalDate haceUnMes = hoy.minusMonths(1);

        cbDiaInicio.setSelectedIndex(haceUnMes.getDayOfMonth() - 1);
        cbMesInicio.setSelectedIndex(haceUnMes.getMonthValue() - 1);
        cbAnioInicio.setSelectedItem(String.valueOf(haceUnMes.getYear()));

        cbDiaFin.setSelectedIndex(hoy.getDayOfMonth() - 1);
        cbMesFin.setSelectedIndex(hoy.getMonthValue() - 1);
        cbAnioFin.setSelectedItem(String.valueOf(hoy.getYear()));

        // Por defecto arranca en "Resumen Diario", así que deshabilitamos la fecha Fin al inicio
        cbDiaFin.setEnabled(false);
        cbMesFin.setEnabled(false);
        cbAnioFin.setEnabled(false);

        btnGenerarReporte = crearBotonWeb("Generar Reporte");
        btnExportarReporte = crearBotonWeb("Exportar a Excel");

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelFiltros.add(new JLabel("Métrica:"));
        panelFiltros.add(comboTipoReporte);
        panelFiltros.add(comboCategoriasReporte);
        panelFiltros.add(new JLabel("Desde:"));
        panelFiltros.add(crearAgrupacionFecha(cbDiaInicio, cbMesInicio, cbAnioInicio));
        panelFiltros.add(new JLabel("Hasta:"));
        panelFiltros.add(crearAgrupacionFecha(cbDiaFin, cbMesFin, cbAnioFin));
        panelFiltros.add(btnGenerarReporte);
        panelFiltros.add(btnExportarReporte);

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
        p.add(dia); p.add(new JLabel("/")); p.add(mes); p.add(new JLabel("/")); p.add(anio);
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

    // Getters de componentes para eventos
    public JTable getTablaClientes() { return tablaClientes; }
    public JButton getBtnAbrirFormularioCliente() { return btnAbrirFormularioCliente; }
    public JButton getBtnBajaCliente() { return btnBajaCliente; }
    public JButton getBtnAltaCliente() { return btnAltaCliente; }
    public JButton getBtnEditarCliente() { return btnEditarCliente; }
    public JButton getBtnCerrarSesion() { return btnCerrarSesion; }
    public JButton getBtnAbrirFormularioVenta() { return btnAbrirFormularioVenta; }
    public JTable getTablaVentas() { return tablaVentas; }
    public JComboBox<String> getComboTipoReporte() { return comboTipoReporte; }
    public JComboBox<String> getComboCategoriasReporte() { return comboCategoriasReporte; }
    public JButton getBtnGenerarReporte() { return btnGenerarReporte; }
    public JButton getBtnExportarReporte() { return btnExportarReporte; }
    public JTable getTablaReportes() { return tablaReportes; }
}
