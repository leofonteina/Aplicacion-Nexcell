package controlador;

import vista.GerenteUI;
import vista.LoginUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GerenteController {

    private GerenteUI vistaPrincipal;
    private jakarta.persistence.EntityManager em;

    public GerenteController(GerenteUI vistaPrincipal, jakarta.persistence.EntityManager em) {
        this.vistaPrincipal = vistaPrincipal;
        this.em = em;

        this.vistaPrincipal.getBtnGenerarReporte().addActionListener(e -> generarReporteVentas());
        this.vistaPrincipal.getBtnLimpiarReporte().addActionListener(e -> limpiarReporte());
        this.vistaPrincipal.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());

        this.vistaPrincipal.getBtnCalcularRendimiento().addActionListener(e -> calcularRendimiento());
        this.vistaPrincipal.getBtnCalcularProductosVendidos().addActionListener(e -> calcularProductosMasVendidos());
    }

    private void generarReporteVentas() {
        // Pedimos las fechas al usuario
        LocalDate[] fechas = pedirRangoFechas("Seleccionar Rango para Reporte de Ventas");

        // Si devolvió null es porque el usuario canceló o hubo un error en la fecha
        if (fechas == null) return;

        LocalDate desde = fechas[0];
        LocalDate hasta = fechas[1];

        // Simulamos la búsqueda en la Base de Datos usando las fechas seleccionadas
        String[] columnas = {"ID Venta", "Fecha", "DNI Cliente", "Vendedor", "Producto", "Total"};
        Object[][] datos = {
                {"V-9998", desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "11223344", "vendedor1", "Motorola Edge 60", "$850.000"},
                {"V-9999", hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "44556677", "vendedor2", "Funda Silicona", "$15.000"}
        };

        vistaPrincipal.getTablaReportesVentas().setModel(new DefaultTableModel(datos, columnas));
    }

    private void calcularRendimiento() {
        // Pedimos las fechas al usuario
        LocalDate[] fechas = pedirRangoFechas("Seleccionar Rango para Evaluar Rendimiento");

        // Si devolvió null es porque el usuario canceló
        if (fechas == null) return;

        // Simulamos la evaluación usando las fechas seleccionadas
        String[] columnas = {"Usuario Vendedor", "Cant. Ventas", "Total Facturado", "Comisión Estimada (5%)"};
        Object[][] datos = {
                {"vendedor1", "45", "$3.500.000", "$175.000"},
                {"vendedor2", "38", "$2.800.000", "$140.000"}
        };

        vistaPrincipal.getTablaRendimiento().setModel(new DefaultTableModel(datos, columnas));
    }

    private void calcularProductosMasVendidos() {
        // Pedimos las fechas al usuario usando tu método reutilizable
        LocalDate[] fechas = pedirRangoFechas("Rango para Productos Más Vendidos");

        // Si el usuario canceló, cortamos la ejecución
        if (fechas == null) return;

        // Simulamos la búsqueda de los productos más vendidos en la base de datos
        String[] columnas = {"Ranking", "Producto", "Categoría", "Cant. Vendida", "Ingresos Generados"};
        Object[][] datos = {
                {"1", "Motorola Edge 60", "Celulares", "120", "$102.000.000"},
                {"2", "Funda Silicona iPhone 13", "Accesorios", "85", "$1.275.000"},
                {"3", "Auriculares Bluetooth Sony", "Periféricos", "60", "$4.500.000"},
                {"4", "Samsung Galaxy S23", "Celulares", "45", "$40.500.000"},
                {"5", "Cargador Carga Rápida 20W", "Accesorios", "40", "$600.000"}
        };

        vistaPrincipal.getTablaProductosVendidos().setModel(new DefaultTableModel(datos, columnas));
    }

    // --- MÉTODO REUTILIZABLE PARA PEDIR FECHAS ---
    private LocalDate[] pedirRangoFechas(String titulo) {
        Integer[] dias = generarRango(1, 31);
        String[] meses = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        int anioActual = LocalDate.now().getYear();
        Integer[] anios = generarRango(anioActual - 5, anioActual);

        JComboBox<Integer> cbDiaDesde = new JComboBox<>(dias);
        JComboBox<String> cbMesDesde = new JComboBox<>(meses);
        JComboBox<Integer> cbAnioDesde = new JComboBox<>(anios);
        cbAnioDesde.setSelectedItem(anioActual);

        JComboBox<Integer> cbDiaHasta = new JComboBox<>(dias);
        JComboBox<String> cbMesHasta = new JComboBox<>(meses);
        JComboBox<Integer> cbAnioHasta = new JComboBox<>(anios);
        cbAnioHasta.setSelectedItem(anioActual);

        JPanel panelFechas = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel panelDesde = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelDesde.add(new JLabel("Desde:"));
        panelDesde.add(cbDiaDesde); panelDesde.add(new JLabel("/"));
        panelDesde.add(cbMesDesde); panelDesde.add(new JLabel("/"));
        panelDesde.add(cbAnioDesde);

        JPanel panelHasta = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelHasta.add(new JLabel("Hasta: "));
        panelHasta.add(cbDiaHasta); panelHasta.add(new JLabel("/"));
        panelHasta.add(cbMesHasta); panelHasta.add(new JLabel("/"));
        panelHasta.add(cbAnioHasta);

        panelFechas.add(panelDesde);
        panelFechas.add(panelHasta);

        int result = JOptionPane.showConfirmDialog(vistaPrincipal, panelFechas,
                titulo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int diaD = (int) cbDiaDesde.getSelectedItem();
                int mesD = Integer.parseInt(cbMesDesde.getSelectedItem().toString());
                int anioD = (int) cbAnioDesde.getSelectedItem();

                int diaH = (int) cbDiaHasta.getSelectedItem();
                int mesH = Integer.parseInt(cbMesHasta.getSelectedItem().toString());
                int anioH = (int) cbAnioHasta.getSelectedItem();

                LocalDate desde = LocalDate.of(anioD, mesD, diaD);
                LocalDate hasta = LocalDate.of(anioH, mesH, diaH);

                if (desde.isAfter(hasta)) {
                    JOptionPane.showMessageDialog(vistaPrincipal, "La fecha 'Desde' no puede ser posterior a 'Hasta'.", "Rango Inválido", JOptionPane.WARNING_MESSAGE);
                    return null;
                }

                return new LocalDate[]{desde, hasta}; // Devolvemos el arreglo de fechas

            } catch (DateTimeException ex) {
                JOptionPane.showMessageDialog(vistaPrincipal, "Combinación de fecha no válida (ej: 31 de febrero).", "Fecha Inexistente", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return null; // Retorna null si el usuario hizo clic en "Cancelar" o cerró la ventana
    }

    private Integer[] generarRango(int inicio, int fin) {
        Integer[] rango = new Integer[fin - inicio + 1];
        for (int i = 0; i < rango.length; i++) rango[i] = inicio + i;
        return rango;
    }

    private void limpiarReporte() {
        vistaPrincipal.getTablaReportesVentas().setModel(new DefaultTableModel());
    }

    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(vistaPrincipal,
                "¿Estás seguro que querés salir del panel de gerencia?", "Cerrar Sesión",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            vistaPrincipal.dispose();
            LoginUI ventanaLogin = new LoginUI();
            new LoginController(ventanaLogin, em);
            ventanaLogin.setVisible(true);
        }
    }
}