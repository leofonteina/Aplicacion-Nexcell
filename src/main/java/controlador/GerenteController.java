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
        // Leemos las fechas directamente de los combos de la pestaña Ventas (V)
        LocalDate[] fechas = obtenerFechasDesdeUI(
                vistaPrincipal.getCbDiaInicioV(), vistaPrincipal.getCbMesInicioV(), vistaPrincipal.getCbAnioInicioV(),
                vistaPrincipal.getCbDiaFinV(), vistaPrincipal.getCbMesFinV(), vistaPrincipal.getCbAnioFinV()
        );

        if (fechas == null) return;

        java.time.LocalDateTime fechaInicio = fechas[0].atStartOfDay();
        java.time.LocalDateTime fechaFin = fechas[1].atTime(23, 59, 59);

        utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES",
                "El Gerente generó el reporte de ventas desde " + fechas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " hasta " + fechas[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        java.util.List<Object[]> resultados = em.createNativeQuery("CALL reporte_ventas_detallado(:inicio, :fin)")
                .setParameter("inicio", fechaInicio)
                .setParameter("fin", fechaFin)
                .getResultList();

        String[] columnas = {"ID Venta", "Fecha", "DNI Cliente", "Vendedor", "Producto", "Total Ítem"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Object[] fila : resultados) {
            String idVenta = "V-" + fila[0].toString();
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
        // Leemos las fechas directamente de los combos de la pestaña Rendimiento (R)
        LocalDate[] fechas = obtenerFechasDesdeUI(
                vistaPrincipal.getCbDiaInicioR(), vistaPrincipal.getCbMesInicioR(), vistaPrincipal.getCbAnioInicioR(),
                vistaPrincipal.getCbDiaFinR(), vistaPrincipal.getCbMesFinR(), vistaPrincipal.getCbAnioFinR()
        );

        if (fechas == null) return;

        java.time.LocalDateTime fechaInicio = fechas[0].atStartOfDay();
        java.time.LocalDateTime fechaFin = fechas[1].atTime(23, 59, 59);

        utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES",
                "El Gerente generó el reporte de rendimiento de vendedores desde " + fechas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " hasta " + fechas[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        java.util.List<Object[]> resultados = em.createNativeQuery("CALL rendimiento_vendedores(:inicio, :fin)")
                .setParameter("inicio", fechaInicio)
                .setParameter("fin", fechaFin)
                .getResultList();

        String[] columnas = {"Usuario Vendedor", "Cant. Ventas", "Total Facturado", "Comisión Estimada (5%)"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);

        for (Object[] fila : resultados) {
            String vendedor = (String) fila[0];
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
        // Leemos las fechas directamente de los combos de la pestaña Productos (P)
        LocalDate[] fechas = obtenerFechasDesdeUI(
                vistaPrincipal.getCbDiaInicioP(), vistaPrincipal.getCbMesInicioP(), vistaPrincipal.getCbAnioInicioP(),
                vistaPrincipal.getCbDiaFinP(), vistaPrincipal.getCbMesFinP(), vistaPrincipal.getCbAnioFinP()
        );

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

    // --- MÉTODO AUXILIAR PARA EXTRAER FECHAS DE LOS COMBOBOX ---
    private LocalDate[] obtenerFechasDesdeUI(JComboBox<String> cbDiaD, JComboBox<String> cbMesD, JComboBox<String> cbAnioD,
                                             JComboBox<String> cbDiaH, JComboBox<String> cbMesH, JComboBox<String> cbAnioH) {
        try {
            int diaD = Integer.parseInt(cbDiaD.getSelectedItem().toString());
            int mesD = Integer.parseInt(cbMesD.getSelectedItem().toString());
            int anioD = Integer.parseInt(cbAnioD.getSelectedItem().toString());

            int diaH = Integer.parseInt(cbDiaH.getSelectedItem().toString());
            int mesH = Integer.parseInt(cbMesH.getSelectedItem().toString());
            int anioH = Integer.parseInt(cbAnioH.getSelectedItem().toString());

            LocalDate desde = LocalDate.of(anioD, mesD, diaD);
            LocalDate hasta = LocalDate.of(anioH, mesH, diaH);

            if (desde.isAfter(hasta)) {
                JOptionPane.showMessageDialog(vistaPrincipal, "La fecha 'Desde' no puede ser posterior a 'Hasta'.", "Rango Inválido", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            return new LocalDate[]{desde, hasta};

        } catch (DateTimeException ex) {
            JOptionPane.showMessageDialog(vistaPrincipal, "Combinación de fecha no válida (ej: 31 de febrero).", "Fecha Inexistente", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void limpiarReporte() {
        vistaPrincipal.getTablaReportesVentas().setModel(new DefaultTableModel());
    }

    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(vistaPrincipal,
                "¿Estás seguro que querés salir del panel de gerencia?", "Cerrar Sesión",
                JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
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

        JFreeChart graficoBarras = ChartFactory.createBarChart(
                "Top Productos Más Vendidos",
                "Productos",
                "Cantidad Vendida",
                datasetGraficoProductos
        );
        ChartPanel panelBarras = new ChartPanel(graficoBarras);

        vistaPrincipal.getPanelGraficoProductos().removeAll();
        vistaPrincipal.getPanelGraficoProductos().add(panelBarras, BorderLayout.CENTER);
        vistaPrincipal.getPanelGraficoProductos().revalidate();
        vistaPrincipal.getPanelGraficoProductos().repaint();

        vistaPrincipal.getTabsProductos().setSelectedIndex(1);
    }
}