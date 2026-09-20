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
    private JButton btnModificarCliente; // (Opcional si ya lo tienes planeado)
    private JButton btnBajaCliente;
    private JButton btnAltaCliente;

    // Componentes de Productos
    private JTextField buscarProductoField;
    private JButton btnBuscarProducto;
    private JTable tablaProductos;

    // Botón general de la ventana
    private JButton btnCerrarSesion;

    private JTable tablaVentas;
    private JButton btnAbrirFormularioVenta;

    // Boton de busqueda para ventas
    private JTextField buscarVentaField;
    private JButton btnBuscarVenta;

    public VendedorUI() {
        setTitle("Panel de Vendedor - Nexcell");
        setSize(700, 530);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // El JFrame usa BorderLayout por defecto
        setLayout(new BorderLayout());

        JTabbedPane sistemaPestanas = new JTabbedPane(JTabbedPane.LEFT);
        sistemaPestanas.addTab("Gestión de Clientes", crearPanelClientes());
        sistemaPestanas.addTab("Catálogo de Productos", crearPanelProductos());
        sistemaPestanas.addTab("Registro de Ventas", crearPanelVentas());
        // Agregamos las pestañas al CENTRO de la ventana
        add(sistemaPestanas, BorderLayout.CENTER);

        // --- NUEVO PANEL INFERIOR PARA CERRAR SESIÓN ---
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setForeground(Color.RED);
        panelInferior.add(btnCerrarSesion);

        // Agregamos el panel inferior al SUR de la ventana
        add(panelInferior, BorderLayout.SOUTH);
    }

    // --- METODO DEL PANEL CLIENTES QUE FALTABA ---
    private JPanel crearPanelClientes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelSuperior = new JPanel(new BorderLayout());

        // Búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buscarClienteField = new JTextField(15);
        btnBuscarCliente = new JButton("Buscar");
        panelBusqueda.add(new JLabel("Buscar (DNI o Apellido): "));
        panelBusqueda.add(buscarClienteField);
        panelBusqueda.add(btnBuscarCliente);

        // Botones de Acciones (Nuevo, Baja Lógica, Reactivar)
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnAbrirFormularioCliente = new JButton("Nuevo Cliente");
        btnBajaCliente = new JButton("Baja Lógica");
        btnAltaCliente = new JButton("Reactivar");

        // OCULTAR BOTONES POR DEFECTO
        btnBajaCliente.setVisible(false);
        btnAltaCliente.setVisible(false);

        panelAcciones.add(btnAbrirFormularioCliente);
        panelAcciones.add(btnBajaCliente);
        panelAcciones.add(btnAltaCliente);

        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);

        // Agregamos la columna "Estado" para que funcione la lógica de baja lógica
        String[] columnas = {"DNI", "Nombre", "Apellido", "Teléfono", "Email", "Estado"};
        Object[][] datosEjemplo = {
                {"35123456", "Juan", "Pérez", "3794123456", "juanperez@email.com", "Activo"}
        };

        // Creamos el modelo bloqueando la edición de celdas
        DefaultTableModel modeloTabla = new DefaultTableModel(datosEjemplo, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaClientes = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaClientes);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
    }

    // --- METODO DEL PANEL PRODUCTOS QUE FALTABA ---
    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buscarProductoField = new JTextField(20);
        btnBuscarProducto = new JButton("Buscar");
        panelBusqueda.add(new JLabel("Filtrar modelo: "));
        panelBusqueda.add(buscarProductoField);
        panelBusqueda.add(btnBuscarProducto);

        String[] columnas = {"ID", "Categoría", "Modelo", "Stock", "Precio"};
        Object[][] datosEjemplo = {
            {"101", "Celulares", "Motorola Edge 60 Pro", "15", "$850.000"}
        };

        DefaultTableModel modeloTabla = new DefaultTableModel(datosEjemplo, columnas);
        tablaProductos = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaProductos);

        panel.add(panelBusqueda, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelVentas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelSuperior = new JPanel(new BorderLayout());

        // Sub-panel izquierdo: Búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buscarVentaField = new JTextField(15);
        btnBuscarVenta = new JButton("Buscar");
        panelBusqueda.add(new JLabel("Buscar (ID Venta o DNI): "));
        panelBusqueda.add(buscarVentaField);
        panelBusqueda.add(btnBuscarVenta);

        // Sub-panel derecho: Botón Nueva Venta
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnAbrirFormularioVenta = new JButton("Nueva Venta");
        panelAcciones.add(btnAbrirFormularioVenta);

        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);

        String[] columnas = {"ID Venta", "Fecha", "DNI Cliente", "Producto", "Total"};
        Object[][] datosEjemplo = {}; // Arranca vacía

        DefaultTableModel modeloTabla = new DefaultTableModel(datosEjemplo, columnas);
        tablaVentas = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaVentas);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
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

    // --- GETTERS DE VENTA
    public JButton getBtnAbrirFormularioVenta() { return btnAbrirFormularioVenta; }
    public JTable getTablaVentas() { return tablaVentas; }
    public JTextField getBuscarVentaField() { return buscarVentaField; }
    public JButton getBtnBuscarVenta() { return btnBuscarVenta; }
}
