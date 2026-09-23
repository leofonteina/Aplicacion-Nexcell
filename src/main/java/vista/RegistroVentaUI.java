package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RegistroVentaUI extends JDialog {

    private JTextField dniClienteField;
    private JButton btnVerificarCliente;
    private JLabel lblNombreCliente;

    private JComboBox<String> productoBox;
    private JTextField cantidadField;
    private JButton btnAgregarProducto;

    private JTable tablaCarrito;
    private JLabel lblTotalVenta;
    private JButton btnConfirmarVenta;

    public RegistroVentaUI(JFrame parent) {
        super(parent, "Registrar Nueva Venta", true);
        setSize(650, 600); // Un poco más ancho
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- Título ---
        JLabel lblTitulo = new JLabel("Registrar Venta", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // --- Panel Superior
        JPanel panelForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font fuenteLabel = new Font("Segoe UI", Font.PLAIN, 14);

        // DNI y Verificar
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblDni = new JLabel("DNI del Cliente:");
        lblDni.setFont(fuenteLabel);
        panelForm.add(lblDni, gbc);

        gbc.gridx = 1;
        dniClienteField = new JTextField(15);
        panelForm.add(dniClienteField, gbc);

        gbc.gridx = 2;
        btnVerificarCliente = new JButton("Verificar");
        btnVerificarCliente.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelForm.add(btnVerificarCliente, gbc);

        // Nombre del Cliente Verificado
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        lblNombreCliente = new JLabel("Cliente no verificado.");
        lblNombreCliente.setForeground(Color.GRAY);
        lblNombreCliente.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        panelForm.add(lblNombreCliente, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        panelForm.add(new JSeparator(), gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        JLabel lblProd = new JLabel("Producto:");
        lblProd.setFont(fuenteLabel);
        panelForm.add(lblProd, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        productoBox = new JComboBox<>(new String[]{"Cargando productos..."});
        panelForm.add(productoBox, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        JLabel lblCant = new JLabel("Cantidad:");
        lblCant.setFont(fuenteLabel);
        panelForm.add(lblCant, gbc);

        gbc.gridx = 1;
        cantidadField = new JTextField(5);
        panelForm.add(cantidadField, gbc);

        gbc.gridx = 2;
        btnAgregarProducto = new JButton("Agregar a la lista");
        btnAgregarProducto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelForm.add(btnAgregarProducto, gbc);

        JPanel panelCentro = new JPanel(new BorderLayout(0, 15));
        panelCentro.add(panelForm, BorderLayout.NORTH);

        // --- Tabla Carrito ---
        String[] columnasCarrito = {"ID", "Producto", "Precio Unit.", "Cantidad", "Subtotal"};
        DefaultTableModel modeloCarrito = new DefaultTableModel(null, columnasCarrito) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setRowHeight(30);

        // Ajustamos los anchos de la tabla
        tablaCarrito.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        tablaCarrito.getColumnModel().getColumn(1).setPreferredWidth(200); // Producto

        JScrollPane scrollCarrito = new JScrollPane(tablaCarrito);
        panelCentro.add(scrollCarrito, BorderLayout.CENTER);

        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        JPanel panelBoton = new JPanel(new BorderLayout());

        lblTotalVenta = new JLabel("Total: $0.00");
        lblTotalVenta.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panelBoton.add(lblTotalVenta, BorderLayout.WEST);

        btnConfirmarVenta = new JButton("Confirmar Venta Completa");
        btnConfirmarVenta.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmarVenta.setMargin(new Insets(10, 20, 10, 20));
        panelBoton.add(btnConfirmarVenta, BorderLayout.EAST);

        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    // --- Getters ---
    public JTextField getDniClienteField() { return dniClienteField; }
    public JButton getBtnVerificarCliente() { return btnVerificarCliente; }
    public JLabel getLblNombreCliente() { return lblNombreCliente; }
    public JComboBox<String> getProductoBox() { return productoBox; }
    public JTextField getCantidadField() { return cantidadField; }
    public JButton getBtnAgregarProducto() { return btnAgregarProducto; }
    public JTable getTablaCarrito() { return tablaCarrito; }
    public JLabel getLblTotalVenta() { return lblTotalVenta; }
    public JButton getBtnConfirmarVenta() { return btnConfirmarVenta; }
}
