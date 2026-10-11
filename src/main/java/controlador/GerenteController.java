package controlador;

import vista.GerenteUI;
import vista.LoginUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class GerenteController {

    private GerenteUI vistaPrincipal;
    private jakarta.persistence.EntityManager em;
    private DefaultCategoryDataset datasetGraficoProductos;

    public GerenteController(GerenteUI vistaPrincipal, jakarta.persistence.EntityManager em) {
        this.vistaPrincipal = vistaPrincipal;
        this.em = em;

        this.vistaPrincipal.getBtnGenerarReporte().addActionListener(e -> generarReporteVentas());
        this.vistaPrincipal.getBtnLimpiarReporte().addActionListener(e -> limpiarReporte());
        this.vistaPrincipal.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());

        this.vistaPrincipal.getBtnCalcularRendimiento().addActionListener(e -> calcularRendimiento());
        this.vistaPrincipal.getBtnCalcularProductosVendidos().addActionListener(e -> calcularProductosMasVendidos());
        this.vistaPrincipal.getBtnGenerarGraficoProductos().addActionListener(e -> mostrarGraficoProductos());
    }

    private void generarReporteVentas() {
        // Pedimos las fechas al usuario
        LocalDate[] fechas = pedirRangoFechas("Seleccionar Rango para Reporte de Ventas");

        // Si devolvió null es porque el usuario canceló
        if (fechas == null) return;

        // Transformamos a LocalDateTime
        java.time.LocalDateTime fechaInicio = fechas[0].atStartOfDay();
        java.time.LocalDateTime fechaFin = fechas[1].atTime(23, 59, 59);

        // --- MANTENEMOS EL LOG ---
        utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES",
                "El Gerente generó el reporte de ventas desde " + fechas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " hasta " + fechas[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // --- CONSULTA REAL A LA BASE DE DATOS ---
        java.util.List<Object[]> resultados = em.createNativeQuery("CALL reporte_ventas_detallado(:inicio, :fin)")
                .setParameter("inicio", fechaInicio)
                .setParameter("fin", fechaFin)
                .getResultList();

        String[] columnas = {"ID Venta", "Fecha", "DNI Cliente", "Vendedor", "Producto", "Total Ítem"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Iteramos los datos REALES
        for (Object[] fila : resultados) {
            // Mantenemos el formato estético "V-ID"
            String idVenta = "V-" + fila[0].toString();

            // MySQL nos devuelve un Timestamp, lo convertimos a LocalDateTime de Java para formatearlo
            java.time.LocalDateTime fechaVenta = ((java.sql.Timestamp) fila[1]).toLocalDateTime();

            String dniCliente = (String) fila[2];
            String vendedor = (String) fila[3];
            String producto = (String) fila[4];
            double subtotal = ((Number) fila[5]).doubleValue();

            modelo.addRow(new Object[]{
                    idVenta,
                    fechaVenta.format(fmt),
                    dniCliente,
                    vendedor,
                    producto,
                    String.format("$%.2f", subtotal)
            });
        }

        vistaPrincipal.getTablaReportesVentas().setModel(modelo);
    }

    private void calcularRendimiento() {
        // Pedimos las fechas al usuario
        LocalDate[] fechas = pedirRangoFechas("Seleccionar Rango para Evaluar Rendimiento");

        // Si devolvió null es porque el usuario canceló
        if (fechas == null) return;

        // Transformamos a LocalDateTime para abarcar el día completo (00:00:00 a 23:59:59)
        java.time.LocalDateTime fechaInicio = fechas[0].atStartOfDay();
        java.time.LocalDateTime fechaFin = fechas[1].atTime(23, 59, 59);

        // --- MANTENEMOS EL REGISTRO DE LOG ---
        utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES",
                "El Gerente generó el reporte de rendimiento de vendedores desde " + fechas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " hasta " + fechas[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // --- CONSULTA REAL A LA BASE DE DATOS ---
        // Llamamos al Procedimiento Almacenado pasándole las fechas
        java.util.List<Object[]> resultados = em.createNativeQuery("CALL rendimiento_vendedores(:inicio, :fin)")
                .setParameter("inicio", fechaInicio)
                .setParameter("fin", fechaFin)
                .getResultList();

        String[] columnas = {"Usuario Vendedor", "Cant. Ventas", "Total Facturado", "Comisión Estimada (5%)"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);

        // Iteramos los datos REALES que nos devolvió MySQL
        for (Object[] fila : resultados) {
            String vendedor = (String) fila[0];
            // Usamos Number para evitar problemas de casteo (BigInteger/BigDecimal) que suele dar Hibernate
            int cantVentas = ((Number) fila[1]).intValue();
            double totalFacturado = ((Number) fila[2]).doubleValue();
            double comision = ((Number) fila[3]).doubleValue();

            modelo.addRow(new Object[]{
                    vendedor,
                    cantVentas,
                    String.format("$%.2f", totalFacturado),
                    String.format("$%.2f", comision)
            });
        }

        vistaPrincipal.getTablaRendimiento().setModel(modelo);
    }

    private void calcularProductosMasVendidos() {
        LocalDate[] fechas = pedirRangoFechas("Rango para Productos Más Vendidos");
        if (fechas == null) return;

        java.time.LocalDateTime fechaInicio = fechas[0].atStartOfDay();
        java.time.LocalDateTime fechaFin = fechas[1].atTime(23, 59, 59);

        utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES",
                "El Gerente generó el reporte de productos más vendidos...");

        java.util.List<Object[]> resultados = em.createNativeQuery("CALL top_productos_mas_vendidos(:inicio, :fin)")
                .setParameter("inicio", fechaInicio)
                .setParameter("fin", fechaFin)
                .getResultList();

        String[] columnas = {"Ranking", "Producto", "Categoría", "Cant. Vendida", "Ingresos Generados"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);

        // Reiniciamos el contenedor de datos para el Gráfico
        datasetGraficoProductos = new DefaultCategoryDataset();

        int ranking = 1;

        for (Object[] fila : resultados) {
            String producto = (String) fila[0];
            String categoria = (String) fila[1];
            int cantidad = ((Number) fila[2]).intValue();
            double ingresos = ((Number) fila[3]).doubleValue();

            modelo.addRow(new Object[]{ ranking++, producto, categoria, cantidad, String.format("$%.2f", ingresos) });
            datasetGraficoProductos.addValue(cantidad, "Unidades Vendidas", producto);
        }

        vistaPrincipal.getTablaProductosVendidos().setModel(modelo);

        vistaPrincipal.getScrollTablaProductos().setVisible(true);
        vistaPrincipal.getBtnGenerarGraficoProductos().setEnabled(true);
        vistaPrincipal.getTabsProductos().setSelectedIndex(0);
        vistaPrincipal.getPanelGraficoProductos().removeAll();
        vistaPrincipal.getPanelGraficoProductos().revalidate();
        vistaPrincipal.getPanelGraficoProductos().repaint();
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
            // --- NUEVO: REGISTRO DE LOG ---
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "SEGURIDAD",
                "El Gerente cerró sesión.");
            vistaPrincipal.dispose();
            LoginUI ventanaLogin = new LoginUI();
            new LoginController(ventanaLogin, em);
            ventanaLogin.setVisible(true);
        }
    }

    private void mostrarGraficoProductos() {
        if (datasetGraficoProductos == null || datasetGraficoProductos.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(vistaPrincipal, "No hay datos para graficar. Calcule el reporte primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- 1. ARMAMOS EL GRÁFICO DE BARRAS ---
        JFreeChart graficoBarras = ChartFactory.createBarChart(
                "Top Productos Más Vendidos",
                "Productos",
                "Cantidad Vendida",
                datasetGraficoProductos
        );
        ChartPanel panelBarras = new ChartPanel(graficoBarras);

        // --- 2. INYECTAMOS A LA PESTAÑA OCULTA ---
        vistaPrincipal.getPanelGraficoProductos().removeAll();
        vistaPrincipal.getPanelGraficoProductos().add(panelBarras, BorderLayout.CENTER);
        vistaPrincipal.getPanelGraficoProductos().revalidate();
        vistaPrincipal.getPanelGraficoProductos().repaint();

        // --- 3. MAGIA: SALTAMOS A LA PESTAÑA DEL GRÁFICO ---
        vistaPrincipal.getTabsProductos().setSelectedIndex(1); // Índice 1 es el "Gráfico de Barras"
    }
}
