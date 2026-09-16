package vista;

import javax.swing.*;
import java.awt.*;

public class RegistroProductoUI extends JDialog {

    private JTextField nombreField;
    private JTextArea descripcionArea;
    private JTextField precioField;
    private JTextField descuentoField;
    private JTextField stockField;

    private JComboBox<String> categoriaBox;
    private JComboBox<String> marcaBox;

    // Componentes para la imagen
    private JTextField rutaImagenField;
    private JButton btnSeleccionarImagen;

    private JButton btnGuardarProducto;

    public RegistroProductoUI(JFrame parent) {
        super(parent, "Registrar Nuevo Producto", true);
        setSize(450, 450);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Fila 0: Nombre
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; nombreField = new JTextField(15); panel.add(nombreField, gbc);

        // Fila 1: Descripción
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        descripcionArea = new JTextArea(3, 15);
        descripcionArea.setLineWrap(true);
        descripcionArea.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(descripcionArea);
        panel.add(scrollDesc, gbc);

        // Fila 2: Precio y Descuento
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Precio / Desc. (%):"), gbc);
        gbc.gridx = 1;
        JPanel panelPrecios = new JPanel(new GridLayout(1, 2, 5, 0));
        precioField = new JTextField();
        descuentoField = new JTextField("0");
        panelPrecios.add(precioField);
        panelPrecios.add(descuentoField);
        panel.add(panelPrecios, gbc);

        // Fila 3: Stock
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1; stockField = new JTextField(15); panel.add(stockField, gbc);

        // Fila 4: Categoría
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        String[] categorias = {"Seleccionar...", "Smartphone", "Accesorio", "SmartWatch", "Parlante", "Auricular"};
        categoriaBox = new JComboBox<>(categorias);
        panel.add(categoriaBox, gbc);

        // Fila 5: Marca
        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Marca:"), gbc);
        gbc.gridx = 1;
        String[] marcas = {"Seleccionar...", "Samsung", "Motorola", "Apple", "Xiaomi", "JBL"};
        marcaBox = new JComboBox<>(marcas);
        panel.add(marcaBox, gbc);

        // Fila 6: Imagen
        gbc.gridx = 0; gbc.gridy = 6; panel.add(new JLabel("Imagen:"), gbc);
        gbc.gridx = 1;
        JPanel panelImagen = new JPanel(new BorderLayout(5, 0));
        rutaImagenField = new JTextField();
        rutaImagenField.setEditable(false);
        btnSeleccionarImagen = new JButton("Buscar...");
        panelImagen.add(rutaImagenField, BorderLayout.CENTER);
        panelImagen.add(btnSeleccionarImagen, BorderLayout.EAST);
        panel.add(panelImagen, gbc);

        // Fila 7: Botón
        gbc.gridy = 7;
        gbc.gridx = 1;
        gbc.insets = new Insets(15, 10, 10, 10);
        btnGuardarProducto = new JButton("Guardar Producto");
        panel.add(btnGuardarProducto, gbc);

        add(panel);
    }

    // --- GETTERS ---
    public JTextField getNombreField() { return nombreField; }
    public JTextArea getDescripcionArea() { return descripcionArea; }
    public JTextField getPrecioField() { return precioField; }
    public JTextField getDescuentoField() { return descuentoField; }
    public JTextField getStockField() { return stockField; }
    public JComboBox<String> getCategoriaBox() { return categoriaBox; }
    public JComboBox<String> getMarcaBox() { return marcaBox; }

    // Getters de la imagen
    public JTextField getRutaImagenField() { return rutaImagenField; }
    public JButton getBtnSeleccionarImagen() { return btnSeleccionarImagen; }

    public JButton getBtnGuardarProducto() { return btnGuardarProducto; }
}