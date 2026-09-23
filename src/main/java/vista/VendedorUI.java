package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VendedorUI extends JFrame {

    // Componentes de Clientes
    private JTextField buscarClienteField;
    private JButton btnBuscarCliente;
    private JTable tablaClientes;
    private JButton btnAbrirFormularioCliente;
    private JButton btnModificarCliente;
    private JButton btnBajaCliente;
    private JButton btnAltaCliente;

    // Componentes de Productos
    private JTextField buscarProductoField;
    private JButton btnBuscarProducto;
    private JTable tablaProductos;

    // Botón general de la ventana
    private JButton btnCerrarSesion;

    // Componentes de Ventas
    private JTable tablaVentas;
    private JButton btnAbrirFormularioVenta;
    private JTextField buscarVentaField;
    private JButton btnBuscarVenta;

    // Constantes de diseño para mantener la simetría del "Dashboard"
    private static final int PADDING_PANEL = 30;
    private static final int GAP_VERTICAL = 20;
    private static final int GAP_HORIZONTAL = 10;
    private static final int ALTO_FILA_TABLA = 35;

    public VendedorUI() {
        setTitle("Panel de Vendedor - Nexcell");
        setSize(1000, 650);
        setMinimumSize(new Dimension(850, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        // Menú lateral moderno con Íconos
        JTabbedPane sistemaPestanas = new JTabbedPane(JTabbedPane.LEFT);
        sistemaPestanas.addTab("  Gestión de Clientes  ", cargarIcono("/iconos/icono_usuarios.png"), crearPanelClientes());
        sistemaPestanas.addTab("  Catálogo de Productos  ", cargarIcono("/iconos/icono_productos.png"), crearPanelProductos());
        sistemaPestanas.addTab("  Registro de Ventas  ", cargarIcono("/iconos/icono_reportes.png"), crearPanelVentas());
        add(sistemaPestanas, BorderLayout.CENTER);

        // --- PANEL INFERIOR: CERRAR SESIÓN ---
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, PADDING_PANEL, 20, PADDING_PANEL));

        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setForeground(new Color(224, 82, 82));
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setMargin(new Insets(8, 20, 8, 20));
        panelInferior.add(btnCerrarSesion);

        add(panelInferior, BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------------
    // PANEL CLIENTES
    // ------------------------------------------------------------------
    private JPanel crearPanelClientes() {
        JPanel panel = crearPanelBase();

        buscarClienteField = new JTextField(20);
        btnBuscarCliente = crearBotonWeb("Buscar");
        JPanel panelBusqueda = crearBarraBusqueda("Buscar (DNI o Apellido):", buscarClienteField, btnBuscarCliente);

        btnAbrirFormularioCliente = crearBotonWeb("Nuevo Cliente");
        btnBajaCliente = crearBotonWeb("Baja Lógica");
        btnAltaCliente = crearBotonWeb("Reactivar");

        btnBajaCliente.setVisible(false);
        btnAltaCliente.setVisible(false);

        JPanel panelAcciones = crearBarraAcciones(btnBajaCliente, btnAltaCliente, btnAbrirFormularioCliente);

        String[] columnas = {"DNI", "Nombre", "Apellido", "Teléfono", "Email", "Estado"};
        Object[][] datosVacios = {};

        DefaultTableModel modeloTabla = new DefaultTableModel(datosVacios, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaClientes = new JTable(modeloTabla);
        configurarTabla(tablaClientes);

        JScrollPane scrollTabla = new JScrollPane(tablaClientes);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());

        panel.add(crearEncabezado("Directorio de Clientes", panelBusqueda, panelAcciones), BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------
    // PANEL PRODUCTOS
    // ------------------------------------------------------------------
    private JPanel crearPanelProductos() {
        JPanel panel = crearPanelBase();

        buscarProductoField = new JTextField(20);
        btnBuscarProducto = crearBotonWeb("Buscar");
        JPanel panelBusqueda = crearBarraBusqueda("Filtrar modelo:", buscarProductoField, btnBuscarProducto);

        String[] columnas = {"ID", "Categoría", "Modelo", "Stock", "Precio"};
        Object[][] datosVacios = {}; // Arranca vacía para la BD

        DefaultTableModel modeloTabla = new DefaultTableModel(datosVacios, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaProductos = new JTable(modeloTabla);
        configurarTabla(tablaProductos);

        JScrollPane scrollTabla = new JScrollPane(tablaProductos);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());

        // Pasamos null como panelAcciones ya que el vendedor no tiene botones aquí
        panel.add(crearEncabezado("Catálogo de Productos", panelBusqueda, null), BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------
    // PANEL VENTAS
    // ------------------------------------------------------------------
    private JPanel crearPanelVentas() {
        JPanel panel = crearPanelBase();

        buscarVentaField = new JTextField(20);
        btnBuscarVenta = crearBotonWeb("Buscar");
        JPanel panelBusqueda = crearBarraBusqueda("Buscar (ID Venta o DNI):", buscarVentaField, btnBuscarVenta);

        btnAbrirFormularioVenta = crearBotonWeb("Nueva Venta");
        JPanel panelAcciones = crearBarraAcciones(btnAbrirFormularioVenta);

        String[] columnas = {"ID Venta", "Fecha", "DNI Cliente", "Producto", "Total"};
        Object[][] datosEjemplo = {};

        DefaultTableModel modeloTabla = new DefaultTableModel(datosEjemplo, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tablaVentas = new JTable(modeloTabla);
        configurarTabla(tablaVentas);

        JScrollPane scrollTabla = new JScrollPane(tablaVentas);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());

        panel.add(crearEncabezado("Historial de Ventas", panelBusqueda, panelAcciones), BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
    }

    // ------------------------------------------------------------------
    // MÉTODOS AUXILIARES DE DISEÑO
    // ------------------------------------------------------------------

    private JPanel crearPanelBase() {
        JPanel panel = new JPanel(new BorderLayout(0, GAP_VERTICAL));
        panel.setBorder(BorderFactory.createEmptyBorder(PADDING_PANEL, PADDING_PANEL, PADDING_PANEL, PADDING_PANEL));
        return panel;
    }

    private JPanel crearEncabezado(String titulo, JPanel panelBusqueda, JPanel panelAcciones) {
        JPanel encabezado = new JPanel(new BorderLayout(0, 20));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JPanel barra = new JPanel(new BorderLayout(20, 0));
        barra.add(panelBusqueda, BorderLayout.WEST);
        if (panelAcciones != null) {
            barra.add(panelAcciones, BorderLayout.EAST);
        }

        encabezado.add(lblTitulo, BorderLayout.NORTH);
        encabezado.add(barra, BorderLayout.CENTER);
        return encabezado;
    }

    private JPanel crearBarraBusqueda(String etiqueta, JTextField campo, JButton boton) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP_HORIZONTAL, 0));
        JLabel lblEtiq = new JLabel(etiqueta);
        lblEtiq.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        barra.add(lblEtiq);
        barra.add(campo);
        barra.add(boton);
        return barra;
    }

    private JPanel crearBarraAcciones(JButton... botones) {
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP_HORIZONTAL, 0));
        for (JButton boton : botones) {
            barra.add(boton);
        }
        return barra;
    }

    private void configurarTabla(JTable tabla) {
        tabla.setRowHeight(ALTO_FILA_TABLA);
        tabla.setShowVerticalLines(false);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    private JButton crearBotonWeb(String texto) {
        JButton btn = new JButton(texto);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(6, 15, 6, 15));
        return btn;
    }

    // --- CARGADOR DE ÍCONOS ---
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

    // --- GETTERS DE CLIENTES ---
    public JTextField getBuscarClienteField() { return buscarClienteField; }
    public JButton getBtnBuscarCliente() { return btnBuscarCliente; }
    public JTable getTablaClientes() { return tablaClientes; }
    public JButton getBtnAbrirFormularioCliente() { return btnAbrirFormularioCliente; }
    public JButton getBtnBajaCliente() { return btnBajaCliente; }
    public JButton getBtnAltaCliente() { return btnAltaCliente; }

    // --- GETTERS DE PRODUCTOS ---
    public JTextField getBuscarProductoField() { return buscarProductoField; }
    public JButton getBtnBuscarProducto() { return btnBuscarProducto; }
    public JTable getTablaProductos() { return tablaProductos; }

    // --- GETTER DE CERRAR SESIÓN ---
    public JButton getBtnCerrarSesion() { return btnCerrarSesion; }

    // --- GETTERS DE VENTA ---
    public JButton getBtnAbrirFormularioVenta() { return btnAbrirFormularioVenta; }
    public JTable getTablaVentas() { return tablaVentas; }
    public JTextField getBuscarVentaField() { return buscarVentaField; }
    public JButton getBtnBuscarVenta() { return btnBuscarVenta; }
}
