package vista;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RegistroVentaUI extends JDialog {

    private JTextField dniClienteField;
    private JButton btnVerificarCliente;
    private JLabel lblNombreCliente;

    // Nuevos componentes para la búsqueda
    private JTextField buscarProductoField;
    private List<modelo.Producto> listaProductosOriginal;

    private JComboBox<String> productoBox;
    private JTextField cantidadField;
    private JButton btnAgregarProducto;

    private JTable tablaCarrito;
    private JLabel lblTotalVenta;
    private JButton btnConfirmarVenta;
    private JButton btnEliminarDelCarrito;

    public RegistroVentaUI(JFrame parent) {
        super(parent, "Registrar Nueva Venta", true);
        setSize(650, 680); // Aumenté un poco el alto para que entre el nuevo campo cómodamente
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel("Registrar Venta", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        Font fuenteLabel = new Font("Segoe UI", Font.PLAIN, 14);

        // Fila 0: DNI y Verificar
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

        // Fila 1: Estado del Cliente
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        lblNombreCliente = new JLabel("Cliente no verificado.");
        lblNombreCliente.setForeground(Color.GRAY);
        lblNombreCliente.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        panelForm.add(lblNombreCliente, gbc);

        // Fila 2: Separador
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        panelForm.add(new JSeparator(), gbc);

        // Fila 3: BUSCADOR DE PRODUCTO (NUEVO)
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        JLabel lblBuscar = new JLabel("Buscar Producto:");
        lblBuscar.setFont(fuenteLabel);
        panelForm.add(lblBuscar, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        buscarProductoField = new JTextField(15);
        panelForm.add(buscarProductoField, gbc);

        // Agregamos el listener para que filtre el ComboBox mientras se escribe
        buscarProductoField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrarComboProductos(); }
            public void removeUpdate(DocumentEvent e) { filtrarComboProductos(); }
            public void changedUpdate(DocumentEvent e) { filtrarComboProductos(); }
        });

        // Fila 4: Producto (ComboBox)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        JLabel lblProd = new JLabel("Producto:");
        lblProd.setFont(fuenteLabel);
        panelForm.add(lblProd, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        productoBox = new JComboBox<>(new String[]{"Cargando productos..."});
        panelForm.add(productoBox, gbc);

        // Fila 5: Cantidad y Botón Agregar
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
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

        // Tabla del Carrito
        String[] columnasCarrito = {"ID", "Producto", "Precio Unit.", "Cantidad", "Subtotal"};
        DefaultTableModel modeloCarrito = new DefaultTableModel(null, columnasCarrito) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaCarrito = new JTable(modeloCarrito);
        tablaCarrito.setRowHeight(30);
        tablaCarrito.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaCarrito.getColumnModel().getColumn(1).setPreferredWidth(200);

        JScrollPane scrollCarrito = new JScrollPane(tablaCarrito);
        panelCentro.add(scrollCarrito, BorderLayout.CENTER);

        panelPrincipal.add(panelCentro, BorderLayout.CENTER);

        // Panel Inferior (Totales y Botón de Confirmar)
        JPanel panelBoton = new JPanel(new BorderLayout());

        JPanel panelIzquierdoInferior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblTotalVenta = new JLabel("Total: $0.00");
        lblTotalVenta.setFont(new Font("Segoe UI", Font.BOLD, 18));

        btnEliminarDelCarrito = new JButton("Quitar Producto");
        btnEliminarDelCarrito.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminarDelCarrito.setVisible(false);

        panelIzquierdoInferior.add(lblTotalVenta);
        panelIzquierdoInferior.add(Box.createHorizontalStrut(15));
        panelIzquierdoInferior.add(btnEliminarDelCarrito);

        panelBoton.add(panelIzquierdoInferior, BorderLayout.WEST);

        btnConfirmarVenta = new JButton("Confirmar Venta Completa");
        btnConfirmarVenta.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmarVenta.setMargin(new Insets(10, 20, 10, 20));
        panelBoton.add(btnConfirmarVenta, BorderLayout.EAST);

        panelPrincipal.add(panelBoton, BorderLayout.SOUTH);

        add(panelPrincipal);

        // Evento para mostrar el botón de eliminar al seleccionar una fila
        tablaCarrito.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnEliminarDelCarrito.setVisible(tablaCarrito.getSelectedRow() != -1);
            }
        });
    }

    // --- NUEVO MÉTODO PARA CARGAR Y FILTRAR PRODUCTOS ---
    public void cargarProductosDisponibles(List<modelo.Producto> productos) {
        this.listaProductosOriginal = productos;
        filtrarComboProductos(); // Inicializa el ComboBox completo la primera vez
    }

    private void filtrarComboProductos() {
        if (listaProductosOriginal == null) return;

        String textoBusqueda = buscarProductoField.getText().trim().toLowerCase();

        productoBox.removeAllItems();
        productoBox.addItem("Seleccionar...");

        int cantidadMostrada = 0; // Iniciamos el contador

        for (modelo.Producto p : listaProductosOriginal) {
            if (p.isEstado() && p.getStock() > 0) {

                // Usamos startsWith para búsqueda estricta al inicio
                if (textoBusqueda.isEmpty() ||
                    p.getNombre().toLowerCase().startsWith(textoBusqueda) ||
                    String.valueOf(p.getId()).startsWith(textoBusqueda)) {

                    productoBox.addItem(p.getId() + " - " + p.getNombre() + " (Stock: " + p.getStock() + ")");
                    cantidadMostrada++; // Sumamos 1 por cada producto agregado

                    // Si ya llegamos a 8 productos, cortamos la búsqueda
                    if (cantidadMostrada >= 8) {
                        break;
                    }
                }
            }
        }
    }
    // -----------------------------------------------------

    public void mostrarClienteValidado(String dni, String nombreCompleto, boolean esNuevo) {
        dniClienteField.setText(dni);
        String prefijo = esNuevo ? "Cliente nuevo: " : "Cliente validado: ";
        lblNombreCliente.setText(prefijo + nombreCompleto);
        lblNombreCliente.setForeground(new Color(40, 167, 69));
    }

    public Long getIdProductoSeleccionado() {
        if (productoBox.getSelectedIndex() <= 0) return null;
        String item = (String) productoBox.getSelectedItem();
        if (item == null || !item.contains(" - ")) return null;
        // Se corrige el split para asegurar que corte justo en el guion
        return Long.parseLong(item.split(" - ")[0].trim());
    }

    public int obtenerCantidadEnCarritoParaProducto(Long idProducto) {
        DefaultTableModel mod = (DefaultTableModel) tablaCarrito.getModel();
        int cantEnCarro = 0;
        for (int i = 0; i < mod.getRowCount(); i++) {
            if (((Long) mod.getValueAt(i, 0)).equals(idProducto)) {
                cantEnCarro += (int) mod.getValueAt(i, 3);
            }
        }
        return cantEnCarro;
    }

    public void agregarFilaCarrito(Long id, String nombre, double precio, int cantidad, double subtotal) {
        DefaultTableModel mod = (DefaultTableModel) tablaCarrito.getModel();
        mod.addRow(new Object[]{id, nombre, precio, cantidad, subtotal});
        recalcularYMostrarTotal();

        // Limpiamos el buscador y cantidad tras agregar
        buscarProductoField.setText("");
        productoBox.setSelectedIndex(0);
        cantidadField.setText("");
    }

    public void eliminarFilaSeleccionadaCarrito() {
        int fila = tablaCarrito.getSelectedRow();
        if (fila != -1) {
            int confirmar = JOptionPane.showConfirmDialog(this, "¿Desea quitar este producto del carrito?", "Quitar Artículo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmar == JOptionPane.YES_OPTION) {
                DefaultTableModel mod = (DefaultTableModel) tablaCarrito.getModel();
                mod.removeRow(fila);
                recalcularYMostrarTotal();
            }
        } else {
            mostrarAlerta("Seleccione un producto del carrito para eliminarlo.", "Aviso");
        }
    }

    public double recalcularYMostrarTotal() {
        DefaultTableModel mod = (DefaultTableModel) tablaCarrito.getModel();
        double tot = 0;
        for (int i = 0; i < mod.getRowCount(); i++) {
            tot += (double) mod.getValueAt(i, 4);
        }
        lblTotalVenta.setText(String.format("Total: $%.2f", tot));
        return tot;
    }

    public List<Object[]> obtenerItemsCarrito() {
        List<Object[]> items = new ArrayList<>();
        for (int i = 0; i < tablaCarrito.getRowCount(); i++) {
            items.add(new Object[]{
                tablaCarrito.getValueAt(i, 0),
                tablaCarrito.getValueAt(i, 1),
                tablaCarrito.getValueAt(i, 2),
                tablaCarrito.getValueAt(i, 3),
                tablaCarrito.getValueAt(i, 4)
            });
        }
        return items;
    }

    public boolean isCarritoVacio() {
        return tablaCarrito.getRowCount() == 0;
    }

    public void mostrarExito(String mensaje, String titulo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarAlerta(String mensaje, String titulo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.WARNING_MESSAGE);
    }

    public void mostrarError(String mensaje, String titulo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }

    public boolean confirmarPregunta(String mensaje, String titulo) {
        return JOptionPane.showConfirmDialog(this, mensaje, titulo, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // Getters
    public JTextField getDniClienteField() { return dniClienteField; }
    public JButton getBtnVerificarCliente() { return btnVerificarCliente; }
    public JTextField getCantidadField() { return cantidadField; }
    public JButton getBtnAgregarProducto() { return btnAgregarProducto; }
    public JButton getBtnConfirmarVenta() { return btnConfirmarVenta; }
    public JButton getBtnEliminarDelCarrito() { return btnEliminarDelCarrito; }
}
