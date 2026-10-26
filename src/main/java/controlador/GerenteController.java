package controlador;

import vista.GerenteUI;
import vista.LoginUI;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.jfree.data.category.DefaultCategoryDataset;

public class GerenteController {

    private GerenteUI vistaPrincipal;
    private jakarta.persistence.EntityManager em;
    private DefaultCategoryDataset datasetGraficoProductos;

    // Constructor con sus respectivos Listeners

    public GerenteController(GerenteUI vistaPrincipal, jakarta.persistence.EntityManager em) {
        this.vistaPrincipal = vistaPrincipal;
        this.em = em;

        // Eventos Generar Reportes
        this.vistaPrincipal.getBtnGenerarReporte().addActionListener(e -> generarReporteVentas());
        this.vistaPrincipal.getBtnCalcularRendimiento().addActionListener(e -> calcularRendimiento());
        this.vistaPrincipal.getBtnCalcularProductosVendidos().addActionListener(e -> calcularProductosMasVendidos());

        // Eventos Limpiar Reportes
        this.vistaPrincipal.getBtnLimpiarReporte().addActionListener(e -> limpiarReporteVentas());
        this.vistaPrincipal.getBtnLimpiarRendimiento().addActionListener(e -> limpiarRendimiento());
        this.vistaPrincipal.getBtnLimpiarProductos().addActionListener(e -> limpiarProductos());

        // Eventos Exportar Excel
        this.vistaPrincipal.getBtnExportarExcelVentas().addActionListener(e -> exportarTablaAExcel(vistaPrincipal.getTablaReportesVentas(), "Reporte_Ventas"));
        this.vistaPrincipal.getBtnExportarExcelRendimiento().addActionListener(e -> exportarTablaAExcel(vistaPrincipal.getTablaRendimiento(), "Rendimiento_Vendedores"));
        this.vistaPrincipal.getBtnExportarExcelProductos().addActionListener(e -> exportarTablaAExcel(vistaPrincipal.getTablaProductosVendidos(), "Top_Productos"));

        // Eventos Extras
        this.vistaPrincipal.getBtnGenerarGraficoProductos().addActionListener(e -> mostrarGraficoProductos());
        this.vistaPrincipal.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());
        this.vistaPrincipal.alHacerDobleClicEnVenta(this::abrirDetalleVenta);

        // Búsqueda en vivo
        this.vistaPrincipal.inicializarAutocompletadoDni(texto -> buscarSugerenciasDinamicas(texto, vistaPrincipal.getComboCriterioBusqueda().getSelectedItem().toString()));
        this.vistaPrincipal.getBtnBuscarVenta().addActionListener(e -> filtrarTablaVentas());
        this.vistaPrincipal.getBuscarVentaField().addActionListener(e -> filtrarTablaVentas());
    }

    // Logica de los reportes

    private void generarReporteVentas() {
        LocalDate[] fechas = obtenerFechasDesdeUI(vistaPrincipal.getCbDiaInicioV(), vistaPrincipal.getCbMesInicioV(), vistaPrincipal.getCbAnioInicioV(), vistaPrincipal.getCbDiaFinV(), vistaPrincipal.getCbMesFinV(), vistaPrincipal.getCbAnioFinV());
        if (fechas == null) return;

        java.time.LocalDateTime fechaInicio = fechas[0].atStartOfDay();
        java.time.LocalDateTime fechaFin = fechas[1].atTime(23, 59, 59);

        utilidades.GestorLogs.registrar(
                utilidades.GestorLogs.Nivel.INFO, "REPORTES",
                "El Gerente generó el reporte de ventas desde " +
                        fechas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        java.util.List<Object[]> resultados = em.createNativeQuery(
                "CALL reporte_ventas_detallado(:inicio, :fin)"
                ).setParameter("inicio", fechaInicio).setParameter("fin", fechaFin).getResultList();

        String[] columnas = {"ID Venta", "Fecha", "DNI Cliente", "Vendedor", "Producto", "Total Ítem"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Object[] fila : resultados) {
            String idVenta = "V-" + fila[0].toString();
            java.time.LocalDateTime fechaVenta = ((java.sql.Timestamp) fila[1]).toLocalDateTime();
            modelo.addRow(new Object[]{ idVenta, fechaVenta.format(fmt), (String) fila[2], (String) fila[3], (String) fila[4], String.format("$%.2f", ((Number) fila[5]).doubleValue()) });
        }

        vistaPrincipal.getTablaReportesVentas().setModel(modelo);
        vistaPrincipal.getBtnExportarExcelVentas().setEnabled(resultados != null && !resultados.isEmpty());
        actualizarTotalVentas();
    }

    private void calcularRendimiento() {
        LocalDate[] fechas = obtenerFechasDesdeUI(vistaPrincipal.getCbDiaInicioR(), vistaPrincipal.getCbMesInicioR(), vistaPrincipal.getCbAnioInicioR(), vistaPrincipal.getCbDiaFinR(), vistaPrincipal.getCbMesFinR(), vistaPrincipal.getCbAnioFinR());
        if (fechas == null) return;

        java.time.LocalDateTime fechaInicio = fechas[0].atStartOfDay();
        java.time.LocalDateTime fechaFin = fechas[1].atTime(23, 59, 59);

        utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES", "El Gerente generó el reporte de rendimiento de vendedores");

        java.util.List<Object[]> resultados = em.createNativeQuery("CALL rendimiento_vendedores(:inicio, :fin)")
                .setParameter("inicio", fechaInicio).setParameter("fin", fechaFin).getResultList();

        String[] columnas = {"Usuario Vendedor", "Cant. Ventas", "Total Facturado", "Comisión Estimada (5%)"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);

        for (Object[] fila : resultados) {
            modelo.addRow(new Object[]{ (String) fila[0], ((Number) fila[1]).intValue(), String.format("$%.2f", ((Number) fila[2]).doubleValue()), String.format("$%.2f", ((Number) fila[3]).doubleValue()) });
        }

        vistaPrincipal.getTablaRendimiento().setModel(modelo);
        vistaPrincipal.getBtnExportarExcelRendimiento().setEnabled(resultados != null && !resultados.isEmpty());
        actualizarTotalRendimiento();
    }

    private void calcularProductosMasVendidos() {
        LocalDate[] fechas = obtenerFechasDesdeUI(vistaPrincipal.getCbDiaInicioP(), vistaPrincipal.getCbMesInicioP(), vistaPrincipal.getCbAnioInicioP(), vistaPrincipal.getCbDiaFinP(), vistaPrincipal.getCbMesFinP(), vistaPrincipal.getCbAnioFinP());
        if (fechas == null) return;

        java.time.LocalDateTime fechaInicio = fechas[0].atStartOfDay();
        java.time.LocalDateTime fechaFin = fechas[1].atTime(23, 59, 59);

        utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES", "El Gerente generó el reporte de productos más vendidos");

        java.util.List<Object[]> resultados = em.createNativeQuery("CALL top_productos_mas_vendidos(:inicio, :fin)")
                .setParameter("inicio", fechaInicio).setParameter("fin", fechaFin).getResultList();

        String[] columnas = {"Ranking", "Producto", "Categoría", "Cant. Vendida", "Ingresos Generados"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);
        datasetGraficoProductos = new DefaultCategoryDataset();
        int ranking = 1;

        for (Object[] fila : resultados) {
            String producto = (String) fila[0];
            int cantidad = ((Number) fila[2]).intValue();
            modelo.addRow(new Object[]{ ranking++, producto, (String) fila[1], cantidad, String.format("$%.2f", ((Number) fila[3]).doubleValue()) });
            datasetGraficoProductos.addValue(cantidad, "Unidades Vendidas", producto);
        }

        vistaPrincipal.getTablaProductosVendidos().setModel(modelo);
        vistaPrincipal.getTabsProductos().setSelectedIndex(0);
        vistaPrincipal.getBtnGenerarGraficoProductos().setEnabled(true);
        vistaPrincipal.getBtnExportarExcelProductos().setEnabled(resultados != null && !resultados.isEmpty());
        actualizarTotalProductos();
    }

    private void mostrarGraficoProductos() {
        if (datasetGraficoProductos == null || datasetGraficoProductos.getColumnCount() == 0) {
            vistaPrincipal.mostrarMensaje("No hay datos para graficar. Calcule el reporte primero.", "Aviso", 2); // 2 = WARNING_MESSAGE
            return;
        }

        LocalDate[] fechas = obtenerFechasDesdeUI(vistaPrincipal.getCbDiaInicioP(), vistaPrincipal.getCbMesInicioP(), vistaPrincipal.getCbAnioInicioP(), vistaPrincipal.getCbDiaFinP(), vistaPrincipal.getCbMesFinP(), vistaPrincipal.getCbAnioFinP());
        String tituloGrafico = "Top Productos Más Vendidos";
        if (fechas != null) {
            tituloGrafico += " (" + fechas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al " + fechas[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";
        }

        // Delegamos todo el dibujo UI a la vista
        vistaPrincipal.renderizarGraficoBarras(datasetGraficoProductos, tituloGrafico);
    }

    // Metodos para la limpieza de los reportes

    private void limpiarReporteVentas() {
        vistaPrincipal.getTablaReportesVentas().setModel(new DefaultTableModel());
        vistaPrincipal.getBtnExportarExcelVentas().setEnabled(false);
        actualizarTotalVentas();
    }

    private void limpiarRendimiento() {
        vistaPrincipal.getTablaRendimiento().setModel(new DefaultTableModel());
        vistaPrincipal.getBtnExportarExcelRendimiento().setEnabled(false);
        actualizarTotalRendimiento();
    }

    private void limpiarProductos() {
        vistaPrincipal.getTablaProductosVendidos().setModel(new DefaultTableModel());
        vistaPrincipal.getBtnExportarExcelProductos().setEnabled(false);
        vistaPrincipal.getBtnGenerarGraficoProductos().setEnabled(false);

        vistaPrincipal.getPanelGraficoProductos().removeAll();
        vistaPrincipal.getPanelGraficoProductos().revalidate();
        vistaPrincipal.getPanelGraficoProductos().repaint();

        datasetGraficoProductos = null;
        vistaPrincipal.getTabsProductos().setSelectedIndex(0);
        actualizarTotalProductos();
    }

    // Metodos para el calculo de totales

    private double calcularTotalColumnaVisual(JTable tabla, int indiceColumna) {
        double total = 0.0;
        for (int i = 0; i < tabla.getRowCount(); i++) {
            Object valor = tabla.getValueAt(i, indiceColumna);
            if (valor != null) {
                try {
                    total += Double.parseDouble(valor.toString().replace("$", "").replace(",", "").trim());
                } catch (NumberFormatException ignored) {}
            }
        }
        return total;
    }

    private void actualizarTotalVentas() {
        double total = calcularTotalColumnaVisual(vistaPrincipal.getTablaReportesVentas(), 5);
        vistaPrincipal.getLblTotalReporteVentas().setText(String.format("Total Reporte: $%.2f", total));
    }

    private void actualizarTotalRendimiento() {
        double total = calcularTotalColumnaVisual(vistaPrincipal.getTablaRendimiento(), 2);
        vistaPrincipal.getLblTotalRendimiento().setText(String.format("Total Facturado: $%.2f", total));
    }

    private void actualizarTotalProductos() {
        double total = calcularTotalColumnaVisual(vistaPrincipal.getTablaProductosVendidos(), 4);
        vistaPrincipal.getLblTotalProductos().setText(String.format("Total Ingresos Generados: $%.2f", total));
    }

    private void filtrarTablaVentas() {
        String textoBusqueda = vistaPrincipal.getBuscarVentaField().getText().trim();
        javax.swing.table.TableRowSorter<?> sorter = (javax.swing.table.TableRowSorter<?>) vistaPrincipal.getTablaReportesVentas().getRowSorter();

        if (sorter != null) {
            if (textoBusqueda.isEmpty()) sorter.setRowFilter(null);
            else {
                int columnaFiltro = (vistaPrincipal.getComboCriterioBusqueda().getSelectedItem().toString().equals("DNI Cliente")) ? 2 : 3;
                sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(textoBusqueda), columnaFiltro));
            }
            actualizarTotalVentas();
        }
    }

    // Metodos con la logica para exportar a PDF y EXCEL

    private void exportarTablaAExcel(JTable tabla, String nombreArchivoPorDefecto) {
        if (tabla.getRowCount() == 0) {
            vistaPrincipal.mostrarMensaje("No hay datos visibles para exportar.", "Aviso", 2);
            return;
        }

        // Delegamos el JFileChooser a la vista
        java.io.File archivo = vistaPrincipal.solicitarArchivoParaGuardar(nombreArchivoPorDefecto);
        if (archivo == null) return;

        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Reporte");

            org.apache.poi.ss.usermodel.CellStyle estiloCabecera = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fuenteCabecera = workbook.createFont();
            fuenteCabecera.setBold(true);
            estiloCabecera.setFont(fuenteCabecera);
            estiloCabecera.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            estiloCabecera.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            org.apache.poi.ss.usermodel.CellStyle estiloMoneda = workbook.createCellStyle();
            estiloMoneda.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));

            org.apache.poi.ss.usermodel.Row filaCabecera = sheet.createRow(0);
            for (int i = 0; i < tabla.getColumnCount(); i++) {
                org.apache.poi.ss.usermodel.Cell celda = filaCabecera.createCell(i);
                celda.setCellValue(tabla.getColumnName(i));
                celda.setCellStyle(estiloCabecera);
            }

            for (int i = 0; i < tabla.getRowCount(); i++) {
                org.apache.poi.ss.usermodel.Row fila = sheet.createRow(i + 1);
                for (int j = 0; j < tabla.getColumnCount(); j++) {
                    org.apache.poi.ss.usermodel.Cell celda = fila.createCell(j);
                    Object valor = tabla.getValueAt(i, j);

                    if (valor != null) {
                        String texto = valor.toString();
                        if (texto.trim().startsWith("$")) {
                            try {
                                celda.setCellValue(Double.parseDouble(texto.replace("$", "").replace(",", "").trim()));
                                celda.setCellStyle(estiloMoneda);
                            } catch (NumberFormatException e) { celda.setCellValue(texto); }
                        } else {
                            try {
                                if (texto.matches("-?\\d+")) celda.setCellValue(Integer.parseInt(texto));
                                else celda.setCellValue(texto);
                            } catch (Exception ex) { celda.setCellValue(texto); }
                        }
                    }
                }
            }

            for (int i = 0; i < tabla.getColumnCount(); i++) sheet.autoSizeColumn(i);

            try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(archivo)) {
                workbook.write(fileOut);
            }

            vistaPrincipal.mostrarMensaje("El reporte fue exportado a:\n" + archivo.getAbsolutePath(), "Exportación Exitosa", 1);
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES", "El Gerente exportó: " + archivo.getName());

        } catch (Exception ex) {
            vistaPrincipal.mostrarMensaje("Error al exportar XLSX:\n" + ex.getMessage(), "Error", 0);
        }
    }

    private void generarComprobantePDF(modelo.Venta venta) {
        try {
            String nombreArchivo = "Nexcell_Comprobante_Gerencia_" + venta.getId() + ".pdf";

            com.itextpdf.text.Document documento = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));

            documento.open();
            com.itextpdf.text.Font fontTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 18);
            com.itextpdf.text.Font fontSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 12);
            com.itextpdf.text.Font fontNormal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 12);

            documento.add(new com.itextpdf.text.Paragraph("NEXCELL - TECNOLOGIA\n-----------------------------------------------------", fontTitulo));
            documento.add(new com.itextpdf.text.Paragraph("Factura: " + String.format("%06d", venta.getId()) + "\nFecha: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\nVendedor: " + venta.getVendedor().getUsername() + "\nCliente: " + venta.getCliente().getNombre() + " (DNI: " + venta.getCliente().getDni() + ")\n\n", fontNormal));

            com.itextpdf.text.pdf.PdfPTable tabla = new com.itextpdf.text.pdf.PdfPTable(3);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{50f, 20f, 30f});
            tabla.addCell(new com.itextpdf.text.Phrase("Producto", fontSubtitulo)); tabla.addCell(new com.itextpdf.text.Phrase("Cantidad", fontSubtitulo)); tabla.addCell(new com.itextpdf.text.Phrase("Subtotal", fontSubtitulo));

            for (modelo.DetalleVenta d : venta.getDetalles()) {
                tabla.addCell(new com.itextpdf.text.Phrase(d.getProducto().getNombre(), fontNormal));
                tabla.addCell(new com.itextpdf.text.Phrase(d.getCantidad() + " u.", fontNormal));
                tabla.addCell(new com.itextpdf.text.Phrase(String.format("$%.2f", d.getSubtotal()), fontNormal));
            }
            documento.add(tabla);

            com.itextpdf.text.Paragraph total = new com.itextpdf.text.Paragraph("\nTOTAL ABONADO: $" + String.format("%.2f", venta.getTotal()), fontTitulo);
            total.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(total);
            documento.add(new com.itextpdf.text.Paragraph("\n\n*** Copia Auditada por Gerencia ***", fontNormal));

            documento.close();
            vistaPrincipal.mostrarMensaje("Comprobante guardado como:\n" + nombreArchivo, "PDF Generado", 1); // 1 = INFORMATION_MESSAGE

        } catch (Exception e) {
            vistaPrincipal.mostrarMensaje("Error al generar PDF: " + e.getMessage(), "Error", 0); // 0 = ERROR_MESSAGE
        }
    }

    // Utilidades

    private void abrirDetalleVenta(Long idVenta) {
        modelo.Venta venta = em.find(modelo.Venta.class, idVenta);
        if (venta != null) vistaPrincipal.mostrarDialogoDetalleVenta(venta, this::generarComprobantePDF);
        else vistaPrincipal.mostrarMensaje("No se pudo encontrar la venta en la base de datos.", "Error", 0);
    }

    private void cerrarSesion() {
        if (vistaPrincipal.pedirConfirmacion("¿Estás seguro que querés salir del panel?", "Cerrar Sesión")) {
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "SEGURIDAD", "El Gerente cerró sesión.");
            vistaPrincipal.dispose();
            LoginUI ventanaLogin = new LoginUI();
            new LoginController(ventanaLogin, em);
            ventanaLogin.setVisible(true);
        }
    }

    private java.util.List<Object[]> buscarSugerenciasDinamicas(String parteTexto, String criterio) {
        try {
            String formatoBusqueda = parteTexto + "%";
            if (criterio.equals("DNI Cliente")) {
                return em.createQuery("SELECT c.dni, c.nombre, c.apellido, 'Ver detalles en tabla' FROM Cliente c WHERE c.dni LIKE :texto OR c.nombre LIKE :texto", Object[].class)
                        .setParameter("texto", formatoBusqueda).setMaxResults(8).getResultList();
            } else {
                return em.createQuery("SELECT u.dni, u.nombre, u.apellido, u.username FROM Usuario u WHERE (u.dni LIKE :texto OR u.username LIKE :texto) AND u.rol = 'VENDEDOR'", Object[].class)
                        .setParameter("texto", formatoBusqueda).setMaxResults(8).getResultList();
            }
        } catch (Exception e) {
            try {
                String formatoBusqueda = parteTexto + "%";
                if (criterio.equals("DNI Cliente")) return em.createNativeQuery("SELECT dni, nombre, apellido, 'Ver detalles en tabla' FROM clientes WHERE dni LIKE :texto OR nombre LIKE :texto LIMIT 8").setParameter("texto", formatoBusqueda).getResultList();
                else return em.createNativeQuery("SELECT dni, nombre, apellido, username FROM usuarios WHERE (dni LIKE :texto OR username LIKE :texto) AND rol = 'VENDEDOR' LIMIT 8").setParameter("texto", formatoBusqueda).getResultList();
            } catch (Exception ex) {
                vistaPrincipal.mostrarMensaje("Error de Base de Datos:\n" + ex.getMessage(), "Error", 0);
                return java.util.Collections.emptyList();
            }
        }
    }

    private LocalDate[] obtenerFechasDesdeUI(javax.swing.JComboBox<String> cbDiaD, javax.swing.JComboBox<String> cbMesD, javax.swing.JComboBox<String> cbAnioD, javax.swing.JComboBox<String> cbDiaH, javax.swing.JComboBox<String> cbMesH, javax.swing.JComboBox<String> cbAnioH) {
        try {
            LocalDate desde = LocalDate.of(Integer.parseInt(cbAnioD.getSelectedItem().toString()), Integer.parseInt(cbMesD.getSelectedItem().toString()), Integer.parseInt(cbDiaD.getSelectedItem().toString()));
            LocalDate hasta = LocalDate.of(Integer.parseInt(cbAnioH.getSelectedItem().toString()), Integer.parseInt(cbMesH.getSelectedItem().toString()), Integer.parseInt(cbDiaH.getSelectedItem().toString()));

            if (desde.isAfter(hasta)) {
                vistaPrincipal.mostrarMensaje("La fecha 'Desde' no puede ser posterior a 'Hasta'.", "Rango Inválido", 2);
                return null;
            }
            return new LocalDate[]{desde, hasta};
        } catch (DateTimeException ex) {
            vistaPrincipal.mostrarMensaje("Combinación de fecha no válida.", "Error", 0);
            return null;
        }
    }
}