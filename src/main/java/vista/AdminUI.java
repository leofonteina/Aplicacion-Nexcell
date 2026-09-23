package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminUI extends JFrame {

    // Componentes de Productos
    private JTextField buscarProductoField;
    private JButton btnBuscarProducto;
    private JTable tablaProductos;
    private JButton btnAbrirFormularioProducto;
    private JButton btnModificarProducto;
    private JButton btnBajaProducto;
    private JButton btnAltaProducto;

    // Componentes de Usuarios
    private JTextField buscarUsuarioField;
    private JButton btnBuscarUsuario;
    private JTable tablaUsuarios;
    private JButton btnAbrirFormularioUsuario;
    private JButton btnModificarUsuario;
    private JButton btnBajaUsuario;
    private JButton btnAltaUsuario;

    // Reportes y General
    private JComboBox<String> comboReportes;
    private JButton btnGenerarReporte;
    private JButton btnLimpiarReporte;
    private JTable tablaReportes;
    private JButton btnCerrarSesion;

    public AdminUI() {
        setTitle("Panel de Administrador - Nexcell");
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // <-- Maximizar pantalla
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane sistemaPestanas = new JTabbedPane(JTabbedPane.LEFT);

        sistemaPestanas.addTab("Productos ", cargarIcono("/iconos/icono_productos.png"), crearPanelProductos());
        sistemaPestanas.addTab("Usuarios ", cargarIcono("/iconos/icono_usuarios.png"), crearPanelUsuarios());
        sistemaPestanas.addTab("Reportes ", cargarIcono("/iconos/icono_reportes.png"), crearPanelReportes());

        add(sistemaPestanas, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setForeground(Color.RED);
        panelInferior.add(btnCerrarSesion);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelProductos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelSuperior = new JPanel(new BorderLayout());
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        buscarProductoField = new JTextField(15);
        buscarProductoField.setPreferredSize(new Dimension(200, 35));
        btnBuscarProducto = new JButton("Buscar");
        btnBuscarProducto.setPreferredSize(new Dimension(100, 35));

        panelBusqueda.add(new JLabel("Filtrar: "));
        panelBusqueda.add(buscarProductoField);
        panelBusqueda.add(btnBuscarProducto);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 10));
        btnAbrirFormularioProducto = new JButton("Nuevo");
        btnAbrirFormularioProducto.setPreferredSize(new Dimension(100, 35));

        btnModificarProducto = new JButton("Modificar");
        btnModificarProducto.setPreferredSize(new Dimension(100, 35));

        btnBajaProducto = new JButton("Baja Lógica");
        btnBajaProducto.setPreferredSize(new Dimension(100, 35));

        btnAltaProducto = new JButton("Reactivar");
        btnAltaProducto.setPreferredSize(new Dimension(100, 35));

        btnModificarProducto.setVisible(false);
        btnBajaProducto.setVisible(false);
        btnAltaProducto.setVisible(false);

        panelAcciones.add(btnAbrirFormularioProducto);
        panelAcciones.add(btnModificarProducto);
        panelAcciones.add(btnBajaProducto);
        panelAcciones.add(btnAltaProducto);

        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);

        String[] columnas = {"ID", "Modelo", "Categoría", "Stock", "Precio", "Estado"};
        Object[][] datosEjemplo = {
            {"CEL-001", "Motorola Edge 60 Pro", "Celulares", "15", "$850.000", "Activo"},
            {"ACC-002", "Funda Silicona", "Accesorios", "30", "$15.000", "Inactivo"}
        };

        DefaultTableModel modeloTabla = new DefaultTableModel(datosEjemplo, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaProductos = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaProductos);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelSuperior = new JPanel(new BorderLayout());
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        buscarUsuarioField = new JTextField(15);
        buscarUsuarioField.setPreferredSize(new Dimension(200, 35));

        btnBuscarUsuario = new JButton("Buscar");
        btnBuscarUsuario.setPreferredSize(new Dimension(100, 35));
        panelBusqueda.add(new JLabel("Usuario: "));
        panelBusqueda.add(buscarUsuarioField);
        panelBusqueda.add(btnBuscarUsuario);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 10));
        btnAbrirFormularioUsuario = new JButton("Nuevo");
        btnAbrirFormularioUsuario.setPreferredSize(new Dimension(100, 35));

        btnModificarUsuario = new JButton("Modificar");
        btnModificarUsuario.setPreferredSize(new Dimension(100, 35));

        btnBajaUsuario = new JButton("Baja Lógica");
        btnBajaUsuario.setPreferredSize(new Dimension(100, 35));

        btnAltaUsuario = new JButton("Reactivar");
        btnAltaUsuario.setPreferredSize(new Dimension(100, 35));

        btnModificarUsuario.setVisible(false);
        btnBajaUsuario.setVisible(false);
        btnAltaUsuario.setVisible(false);

        panelAcciones.add(btnAbrirFormularioUsuario);
        panelAcciones.add(btnModificarUsuario);
        panelAcciones.add(btnBajaUsuario);
        panelAcciones.add(btnAltaUsuario);

        panelSuperior.add(panelBusqueda, BorderLayout.WEST);
        panelSuperior.add(panelAcciones, BorderLayout.EAST);

        String[] columnas = {"Username", "Rol del Sistema", "Estado"};
        Object[][] datosEjemplo = {
            {"vendedor1", "Vendedor", "Activo"},
            {"gerente_suc", "Gerente", "Activo"}
        };

        DefaultTableModel modeloTabla = new DefaultTableModel(datosEjemplo, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaUsuarios = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaUsuarios);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelReportes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        String[] opcionesReporte = {"Productos con Bajo Stock", "Valorización de Inventario", "Movimientos de Inventario", "Auditoría de Usuarios"};

        comboReportes = new JComboBox<>(opcionesReporte);
        comboReportes.setPreferredSize(new Dimension(200, 35));

        btnGenerarReporte = new JButton("Generar");
        btnGenerarReporte.setPreferredSize(new Dimension(100, 35));

        btnLimpiarReporte = new JButton("Limpiar");
        btnLimpiarReporte.setPreferredSize(new Dimension(100, 35));

        panelSuperior.add(new JLabel("Tipo de Reporte: "));
        panelSuperior.add(comboReportes);
        panelSuperior.add(btnGenerarReporte);
        panelSuperior.add(btnLimpiarReporte);

        tablaReportes = new JTable(new DefaultTableModel()) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JScrollPane scrollTabla = new JScrollPane(tablaReportes);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
    }

    public JTable getTablaProductos() { return tablaProductos; }
    public JButton getBtnAbrirFormularioProducto() { return btnAbrirFormularioProducto; }
    public JButton getBtnModificarProducto() { return btnModificarProducto; }
    public JButton getBtnBajaProducto() { return btnBajaProducto; }
    public JButton getBtnAltaProducto() { return btnAltaProducto; }

    public JTextField getTxtBuscarUsuario() { return buscarUsuarioField; }
    public JButton getBtnBuscarUsuario() { return btnBuscarUsuario; }
    public JTable getTablaUsuarios() { return tablaUsuarios; }
    public JButton getBtnAbrirFormularioUsuario() { return btnAbrirFormularioUsuario; }
    public JButton getBtnModificarUsuario() { return btnModificarUsuario; }
    public JButton getBtnBajaUsuario() { return btnBajaUsuario; }
    public JButton getBtnAltaUsuario() { return btnAltaUsuario; }

    public JComboBox<String> getComboReportes() { return comboReportes; }
    public JButton getBtnGenerarReporte() { return btnGenerarReporte; }
    public JButton getBtnLimpiarReporte() { return btnLimpiarReporte; }
    public JTable getTablaReportes() { return tablaReportes; }
    public JButton getBtnCerrarSesion() { return btnCerrarSesion; }

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
