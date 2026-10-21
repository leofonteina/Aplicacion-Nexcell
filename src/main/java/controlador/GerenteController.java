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

        // Listeners para mostrar los diferentes reportes
        this.vistaPrincipal.getBtnGenerarReporte().addActionListener(e -> generarReporteVentas());
        this.vistaPrincipal.getBtnLimpiarReporte().addActionListener(e -> limpiarReporte());
        this.vistaPrincipal.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());

        // Listeners para calcular los diferentes reportes
        this.vistaPrincipal.getBtnCalcularRendimiento().addActionListener(e -> calcularRendimiento());
        this.vistaPrincipal.getBtnCalcularProductosVendidos().addActionListener(e -> calcularProductosMasVendidos());
        this.vistaPrincipal.getBtnGenerarGraficoProductos().addActionListener(e -> mostrarGraficoProductos());

        // Listener para abrir un detalle de venta
        this.vistaPrincipal.alHacerDobleClicEnVenta(this::abrirDetalleVenta);

        // Listeners para la busqueda de usuario por DNI
        this.vistaPrincipal.inicializarAutocompletadoDni(texto -> buscarSugerenciasDinamicas(texto, vistaPrincipal.getComboCriterioBusqueda().getSelectedItem().toString()));
        this.vistaPrincipal.getBtnBuscarVenta().addActionListener(e -> filtrarTablaVentas());
        this.vistaPrincipal.getBuscarVentaField().addActionListener(e -> filtrarTablaVentas());
    }

    private void generarReporteVentas() {
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
    }

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

    private void abrirDetalleVenta(Long idVenta) {
        modelo.Venta venta = em.find(modelo.Venta.class, idVenta);
        if (venta != null) {
            vistaPrincipal.mostrarDialogoDetalleVenta(venta, this::generarComprobantePDF);
        } else {
            JOptionPane.showMessageDialog(vistaPrincipal, "No se pudo encontrar el detalle de la venta en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generarComprobantePDF(modelo.Venta venta) {
        try {
            String nombreArchivo = "Nexcell_Comprobante_Venta_Copia_Gerencia_" + venta.getId() + ".pdf";

            com.itextpdf.text.Document documento = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));

            documento.open();

            com.itextpdf.text.Font fontTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 18);
            com.itextpdf.text.Font fontSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 12);
            com.itextpdf.text.Font fontNormal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 12);

            documento.add(new com.itextpdf.text.Paragraph("NEXCELL - TECNOLOGIA", fontTitulo));
            documento.add(new com.itextpdf.text.Paragraph("-------------------------------------------------------------------------------------------------------"));
            documento.add(new com.itextpdf.text.Paragraph("\n"));
            documento.add(new com.itextpdf.text.Paragraph("N° de Factura: " + String.format("%06d", venta.getId()), fontNormal));
            documento.add(new com.itextpdf.text.Paragraph("Fecha: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fontNormal));
            documento.add(new com.itextpdf.text.Paragraph("Vendedor: " + venta.getVendedor().getUsername(), fontNormal));
            documento.add(new com.itextpdf.text.Paragraph("Cliente: " + venta.getCliente().getNombre() + " " + venta.getCliente().getApellido() + " (DNI: " + venta.getCliente().getDni() + ")", fontNormal));
            documento.add(new com.itextpdf.text.Paragraph("\n"));

            com.itextpdf.text.pdf.PdfPTable tabla = new com.itextpdf.text.pdf.PdfPTable(3);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{50f, 20f, 30f});
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Producto", fontSubtitulo)));
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Cantidad", fontSubtitulo)));
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Subtotal", fontSubtitulo)));

            for (modelo.DetalleVenta d : venta.getDetalles()) {
                tabla.addCell(new com.itextpdf.text.Phrase(d.getProducto().getNombre(), fontNormal));
                tabla.addCell(new com.itextpdf.text.Phrase(d.getCantidad() + " u.", fontNormal));
                tabla.addCell(new com.itextpdf.text.Phrase(String.format("$%.2f", d.getSubtotal()), fontNormal));
            }
            documento.add(tabla);

            com.itextpdf.text.Paragraph total = new com.itextpdf.text.Paragraph("\nTOTAL ABONADO: $" + String.format("%.2f", venta.getTotal()), fontTitulo);
            total.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(total);
            documento.add(new com.itextpdf.text.Paragraph("\n\n*** Copia Auditada por Gerencia - Nexcell ***", fontNormal));

            documento.close();

            JOptionPane.showMessageDialog(vistaPrincipal, "Comprobante PDF generado y guardado con éxito como:\n" + nombreArchivo, "PDF Generado", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(vistaPrincipal, "Error al generar el PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private java.util.List<Object[]> buscarSugerenciasDinamicas(String parteTexto, String criterio) {
        try {
            // Cambiamos el comodín para que busque solo los que EMPIECEN con el texto (ej: "4%")
            String formatoBusqueda = parteTexto + "%";

            if (criterio.equals("DNI Cliente")) {
                return em.createQuery("SELECT c.dni, c.nombre, c.apellido, 'Ver detalles en tabla' FROM Cliente c WHERE c.dni LIKE :texto OR c.nombre LIKE :texto", Object[].class)
                        .setParameter("texto", formatoBusqueda)
                        .setMaxResults(8)
                        .getResultList();
            } else {
                return em.createQuery("SELECT u.dni, u.nombre, u.apellido, u.username FROM Usuario u WHERE u.dni LIKE :texto OR u.username LIKE :texto", Object[].class)
                        .setParameter("texto", formatoBusqueda)
                        .setMaxResults(8)
                        .getResultList();
            }
        } catch (Exception e) {
            String formatoBusqueda = parteTexto + "%";
            try {
                if (criterio.equals("DNI Cliente")) {
                    return em.createNativeQuery("SELECT dni, nombre, apellido, 'Ver detalles en tabla' FROM clientes WHERE dni LIKE :texto OR nombre LIKE :texto LIMIT 8")
                            .setParameter("texto", formatoBusqueda)
                            .getResultList();
                } else {
                    return em.createNativeQuery("SELECT dni, nombre, apellido, username FROM usuarios WHERE dni LIKE :texto OR username LIKE :texto LIMIT 8")
                            .setParameter("texto", formatoBusqueda)
                            .getResultList();
                }
            } catch (Exception ex) {
                return java.util.Collections.emptyList();
            }
        }
    }

    private void filtrarTablaVentas() {
        String textoBusqueda = vistaPrincipal.getBuscarVentaField().getText().trim();
        javax.swing.table.TableRowSorter<?> sorter = (javax.swing.table.TableRowSorter<?>) vistaPrincipal.getTablaReportesVentas().getRowSorter();

        if (sorter != null) {
            if (textoBusqueda.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                String criterio = vistaPrincipal.getComboCriterioBusqueda().getSelectedItem().toString();
                int columnaFiltro = (criterio.equals("DNI Cliente")) ? 2 : 3; // 2 es DNI, 3 es Vendedor

                sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(textoBusqueda), columnaFiltro));
            }
        }
    }

    private void mostrarGraficoProductos() {
        if (datasetGraficoProductos == null || datasetGraficoProductos.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(vistaPrincipal, "No hay datos para graficar. Calcule el reporte primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate[] fechas = obtenerFechasDesdeUI(
                vistaPrincipal.getCbDiaInicioP(), vistaPrincipal.getCbMesInicioP(), vistaPrincipal.getCbAnioInicioP(),
                vistaPrincipal.getCbDiaFinP(), vistaPrincipal.getCbMesFinP(), vistaPrincipal.getCbAnioFinP()
        );

        String tituloGrafico = "Top Productos Más Vendidos";
        if (fechas != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            tituloGrafico = "Top Productos Más Vendidos (" + fechas[0].format(fmt) + " al " + fechas[1].format(fmt) + ")";
        }

        JFreeChart graficoBarras = ChartFactory.createBarChart(
                tituloGrafico,
                "Productos",
                "Cantidad Vendida",
                datasetGraficoProductos
        );

        graficoBarras.setBackgroundPaint(java.awt.Color.WHITE);

        org.jfree.chart.plot.CategoryPlot plot = graficoBarras.getCategoryPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setOutlineVisible(false);

        plot.setRangeGridlinePaint(new java.awt.Color(170, 170, 170));
        plot.setRangeGridlineStroke(new java.awt.BasicStroke(1.2f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND, 1.0f, new float[]{6.0f, 4.0f}, 0.0f));

        org.jfree.chart.renderer.category.BarRenderer renderer = new org.jfree.chart.renderer.category.BarRenderer() {
            @Override
            public java.awt.Paint getItemPaint(int row, int column) {
                double max = 0;
                for (int i = 0; i < datasetGraficoProductos.getColumnCount(); i++) {
                    Number val = datasetGraficoProductos.getValue(0, i);
                    if (val != null && val.doubleValue() > max) {
                        max = val.doubleValue();
                    }
                }

                Number valorActual = datasetGraficoProductos.getValue(row, column);
                double val = valorActual != null ? valorActual.doubleValue() : 0;

                int alpha = 55;
                if (max > 0) {
                    alpha = (int) ((val / max) * 200) + 55;
                }
                alpha = Math.min(alpha, 255);

                java.awt.Color[] coloresBase = {
                        new java.awt.Color(204, 0, 0),
                        new java.awt.Color(0, 153, 51),
                        new java.awt.Color(0, 102, 204),
                        new java.awt.Color(255, 128, 0),
                        new java.awt.Color(102, 0, 204),
                        new java.awt.Color(0, 153, 153)
                };

                java.awt.Color colorElegido = coloresBase[column % coloresBase.length];

                return new java.awt.Color(colorElegido.getRed(), colorElegido.getGreen(), colorElegido.getBlue(), alpha);
            }
        };

        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setDrawBarOutline(false);

        plot.setRenderer(renderer);

        ChartPanel panelBarras = new ChartPanel(graficoBarras);

        vistaPrincipal.getPanelGraficoProductos().removeAll();
        vistaPrincipal.getPanelGraficoProductos().add(panelBarras, java.awt.BorderLayout.CENTER);
        vistaPrincipal.getPanelGraficoProductos().revalidate();
        vistaPrincipal.getPanelGraficoProductos().repaint();

        vistaPrincipal.getTabsProductos().setSelectedIndex(1);
    }
}