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

    private JButton btnCancelar;
    private JButton btnGuardarProducto;

    public RegistroProductoUI(JFrame parent) {
        super(parent, "Registrar Nuevo Producto", true);
        setMinimumSize(new Dimension(420, 380));
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Fila 0: Nombre
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; nombreField = new JTextField(18); panel.add(nombreField, gbc);

        // Fila 1: Descripción
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Descripción:"), gbc);
        gbc.gridx = 1;
        descripcionArea = new JTextArea(3, 18);
        descripcionArea.setLineWrap(true);
        descripcionArea.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(descripcionArea);
        panel.add(scrollDesc, gbc);

        // Fila 2: Precio y Descuento
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Precio / Desc. (%):"), gbc);
        gbc.gridx = 1;
        JPanel panelPrecios = new JPanel(new GridLayout(1, 2, 8, 0));
        precioField = new JTextField();
        descuentoField = new JTextField("0");
        panelPrecios.add(precioField);
        panelPrecios.add(descuentoField);
        panel.add(panelPrecios, gbc);

        // Fila 3: Stock
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Stock:"), gbc);
        gbc.gridx = 1; stockField = new JTextField(18); panel.add(stockField, gbc);

        // Fila 4: Categoría
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        String[] categorias = {"Smartphone", "Accesorio", "SmartWatch", "Parlante", "Auricular"};
        categoriaBox = new JComboBox<>(categorias);
        panel.add(categoriaBox, gbc);

        // Fila 5: Marca
        gbc.gridx = 0; gbc.gridy = 5; panel.add(new JLabel("Marca:"), gbc);
        gbc.gridx = 1;
        String[] marcas = {"Samsung", "Motorola", "Apple", "Xiaomi", "JBL"};
        marcaBox = new JComboBox<>(marcas);
        panel.add(marcaBox, gbc);

        // Fila 6: Botones
        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 5, 10);

        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.X_AXIS));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnGuardarProducto = new JButton("Guardar Producto");
        btnGuardarProducto.setCursor(new Cursor(Cursor.HAND_CURSOR));

        panelBotones.add(btnCancelar);
        panelBotones.add(Box.createHorizontalGlue());
        panelBotones.add(btnGuardarProducto);

        panel.add(panelBotones, gbc);

        add(panel);
        getRootPane().setDefaultButton(btnGuardarProducto);
        pack(); // Ajusta el tamaño automáticamente sin cortar componentes
        setLocationRelativeTo(parent);
    }

    public JTextField getNombreField() { return nombreField; }
    public JTextArea getDescripcionArea() { return descripcionArea; }
    public JTextField getPrecioField() { return precioField; }
    public JTextField getDescuentoField() { return descuentoField; }
    public JTextField getStockField() { return stockField; }
    public JComboBox<String> getCategoriaBox() { return categoriaBox; }
    public JComboBox<String> getMarcaBox() { return marcaBox; }
    public JButton getBtnGuardarProducto() { return btnGuardarProducto; }
    public JButton getBtnCancelar() { return btnCancelar; }
}
