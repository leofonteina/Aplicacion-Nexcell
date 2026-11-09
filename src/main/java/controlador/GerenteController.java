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

    // Datasets para los 3 gráficos
    private DefaultCategoryDataset datasetGraficoVentas;
    private DefaultCategoryDataset datasetGraficoRendimiento;
    private DefaultCategoryDataset datasetGraficoProductos;

    // Constructor en donde se "escucha" cuando el usuario hace click en un boton
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

        // Eventos Generar Gráficos
        this.vistaPrincipal.getBtnGenerarGraficoVentas().addActionListener(e -> mostrarGraficoVentas());
        this.vistaPrincipal.getBtnGenerarGraficoRendimiento().addActionListener(e -> mostrarGraficoRendimiento());
        this.vistaPrincipal.getBtnGenerarGraficoProductos().addActionListener(e -> mostrarGraficoProductos());

        // Eventos Extras
        this.vistaPrincipal.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());
        this.vistaPrincipal.alHacerDobleClicEnVenta(this::abrirDetalleVenta);

        // Búsqueda en vivo
        this.vistaPrincipal.inicializarAutocompletadoDni(texto -> buscarSugerenciasDinamicas(texto, vistaPrincipal.getComboCriterioBusqueda().getSelectedItem().toString()));
        this.vistaPrincipal.getBtnBuscarVenta().addActionListener(e -> filtrarTablaVentas());
        this.vistaPrincipal.getBuscarVentaField().addActionListener(e -> filtrarTablaVentas());
    }

    // Logica de los reportes

    private void generarReporteVentas() {
        // Obtiene el fecha desde y hasta de la interfaz de usuario
        LocalDate[] fechas = obtenerFechasDesdeUI(vistaPrincipal.getCbDiaInicioV(), vistaPrincipal.getCbMesInicioV(), vistaPrincipal.getCbAnioInicioV(), vistaPrincipal.getCbDiaFinV(), vistaPrincipal.getCbMesFinV(), vistaPrincipal.getCbAnioFinV());
        if (fechas == null) return;

        // Hace que la fecha inicio empiece desde la hora 00,
        // y que la fecha fin dure todo el dia
        java.time.LocalDateTime fechaInicio = fechas[0].atStartOfDay();
        java.time.LocalDateTime fechaFin = fechas[1].atTime(23, 59, 59);

        // Guarda el log que hizo el determinado gerente
        utilidades.GestorLogs.registrar(
                utilidades.GestorLogs.Nivel.INFO, "REPORTES",
                "El Gerente generó el reporte de ventas desde " + fechas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        );

        // Llama al SP reporte_ventas_detallado para asi guardar los datos en resultados
        java.util.List<Object[]> resultados = em.createNativeQuery(
                "CALL reporte_ventas_detallado(:inicio, :fin)"
        ).setParameter("inicio", fechaInicio).setParameter("fin", fechaFin).getResultList();

        // Crea el modelo de la tabla en la cual se van a guardar los resultados
        String[] columnas = {"ID Venta", "Fecha", "DNI Cliente", "Vendedor", "Producto", "Total Ítem"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Guarda la cantidad de dias que hay entre fecha inicio y fecha fin
        long diasDiferencia = java.time.temporal.ChronoUnit.DAYS.between(fechas[0], fechas[1]);
        boolean agruparPorDia = diasDiferencia <= 65;

        // Define el dataset en donde se guardaran los datos del grafico
        datasetGraficoVentas = new DefaultCategoryDataset();
        java.util.Map<java.time.LocalDate, Double> ventasAgrupadas = new java.util.TreeMap<>();

        // For en el cual se guardan los registros resultantes de llamar al SP reporte_ventas_detallado
        for (Object[] fila : resultados) {
            String idVenta = "V-" + fila[0].toString();
            java.time.LocalDateTime fechaVenta = ((java.sql.Timestamp) fila[1]).toLocalDateTime();
            double totalItem = ((Number) fila[5]).doubleValue();

            // Usamos la nueva herramienta formatearMoneda() para la tabla
            modelo.addRow(new Object[]{ idVenta, fechaVenta.format(fmt), (String) fila[2], (String) fila[3], (String) fila[4], formatearMoneda(totalItem) });

            java.time.LocalDate fechaClave = agruparPorDia
                    ? fechaVenta.toLocalDate()
                    : fechaVenta.toLocalDate().withDayOfMonth(1);

            ventasAgrupadas.put(fechaClave, ventasAgrupadas.getOrDefault(fechaClave, 0.0) + totalItem);
        }

        // Formatea las fechas que van a aparecer en el grafico con sus nombres en español
        java.util.Locale localeEspanol = new java.util.Locale("es", "AR");
        DateTimeFormatter fmtEjeXDia = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter fmtEjeXMes = DateTimeFormatter.ofPattern("MMM-yyyy", localeEspanol);

        for (java.util.Map.Entry<java.time.LocalDate, Double> entrada : ventasAgrupadas.entrySet()) {
            String etiquetaX;
            if (agruparPorDia) {
                etiquetaX = entrada.getKey().format(fmtEjeXDia);
            } else {
                String mesTexto = entrada.getKey().format(fmtEjeXMes);
                etiquetaX = mesTexto.substring(0, 1).toUpperCase() + mesTexto.substring(1);
            }
            datasetGraficoVentas.addValue(entrada.getValue(), "Ingresos", etiquetaX);
        }

        vistaPrincipal.getTablaReportesVentas().setModel(modelo);
        vistaPrincipal.getTabsVentas().setSelectedIndex(0);

        boolean hayDatos = resultados != null && !resultados.isEmpty();
        vistaPrincipal.getBtnExportarExcelVentas().setEnabled(hayDatos);
        vistaPrincipal.getBtnGenerarGraficoVentas().setEnabled(hayDatos);
        actualizarTotalVentas();
    }

    private void calcularRendimiento() {
        // Obtiene la fecha inicio y fecha fin
        LocalDate[] fechas = obtenerFechasDesdeUI(vistaPrincipal.getCbDiaInicioR(), vistaPrincipal.getCbMesInicioR(), vistaPrincipal.getCbAnioInicioR(), vistaPrincipal.getCbDiaFinR(), vistaPrincipal.getCbMesFinR(), vistaPrincipal.getCbAnioFinR());
        if (fechas == null) return;

        // Hace que la fecha inicio empiece desde la hora 00, y que la fecha fin tenga un rango
        // de hasta todo el dia
        java.time.LocalDateTime fechaInicio = fechas[0].atStartOfDay();
        java.time.LocalDateTime fechaFin = fechas[1].atTime(23, 59, 59);

        // Registra en auditoria el movimiento que hizo el gerente
        utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES", "El Gerente generó el reporte de rendimiento de vendedores");

        // Llama al SP de rendimiento_vendedores para que haga el trabajo mas duro de
        // cruzar tablas, y darle a java la tabla ya filtrada
        java.util.List<Object[]> resultados = em.createNativeQuery("CALL rendimiento_vendedores(:inicio, :fin)")
                .setParameter("inicio", fechaInicio).setParameter("fin", fechaFin).getResultList();

        // Define los nombres de cada columna de la tabla, y las inserta en el DefaultTableModel modelo
        String[] columnas = {"Usuario Vendedor", "Cant. Ventas", "Total Facturado", "Comisión Estimada (5%)"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);

        // Define el dataset en donde se guardaran los datos para
        // la creacion del grafico de rendimiento
        datasetGraficoRendimiento = new DefaultCategoryDataset();

        // For que agrupa todas las filas de resultantes del cruce de tablas de rendimiento_vendedores
        // Nota: Java interpreta a la tabla de SQL como un objeto, por eso el Object[]
        for (Object[] fila : resultados) {
            String vendedor = (String) fila[0];
            double totalFacturado = ((Number) fila[2]).doubleValue();
            double comision = ((Number) fila[3]).doubleValue();

            // Usamos la nueva herramienta formatearMoneda() para la tabla
            modelo.addRow(new Object[]{ vendedor, ((Number) fila[1]).intValue(), formatearMoneda(totalFacturado), formatearMoneda(comision) });

            datasetGraficoRendimiento.addValue(totalFacturado, "Facturación", vendedor);
        }

        // Envia los datos hacia la interfaz de usuario
        vistaPrincipal.getTablaRendimiento().setModel(modelo);
        vistaPrincipal.getTabsRendimiento().setSelectedIndex(0);

        // Reestablece los botones y diferentes datos de la interfaz de usuario
        boolean hayDatos = resultados != null && !resultados.isEmpty();
        vistaPrincipal.getBtnExportarExcelRendimiento().setEnabled(hayDatos);
        vistaPrincipal.getBtnGenerarGraficoRendimiento().setEnabled(hayDatos);
        actualizarTotalRendimiento();
    }

    private void calcularProductosMasVendidos() {
        // Obtiene las fechas inicio y fin
        LocalDate[] fechas = obtenerFechasDesdeUI(vistaPrincipal.getCbDiaInicioP(), vistaPrincipal.getCbMesInicioP(), vistaPrincipal.getCbAnioInicioP(), vistaPrincipal.getCbDiaFinP(), vistaPrincipal.getCbMesFinP(), vistaPrincipal.getCbAnioFinP());
        if (fechas == null) return;

        // Hace que la fecha inicio empiece desde la hora 00
        // y que la fecha fin abarque todo el dia
        java.time.LocalDateTime fechaInicio = fechas[0].atStartOfDay();
        java.time.LocalDateTime fechaFin = fechas[1].atTime(23, 59, 59);

        // Guarda en auditoria el movimiento del gerente
        utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES", "El Gerente generó el reporte de productos más vendidos");

        // Se llama al SP top_productos_mas_vendidos para el correspondiente filtro
        // de tablas mediante INNER JOIN
        java.util.List<Object[]> resultados = em.createNativeQuery("CALL top_productos_mas_vendidos(:inicio, :fin)")
                .setParameter("inicio", fechaInicio).setParameter("fin", fechaFin).getResultList();

        // Define la cantidad y nombre de las columnas que va a tener la tabla
        // y define el modelo que va a tener la tabla, con sus respectivas columnas
        String[] columnas = {"Ranking", "Producto", "Categoría", "Cant. Vendida", "Ingresos Generados"};
        DefaultTableModel modelo = new DefaultTableModel(null, columnas);

        // Define el dataset con el cual se va a crear el grafico de Top de productos
        datasetGraficoProductos = new DefaultCategoryDataset();
        int ranking = 1;

        // Guarda cada fila de registros en la tabla modelo
        for (Object[] fila : resultados) {
            String producto = (String) fila[0];
            int cantidad = ((Number) fila[2]).intValue();
            double ingresos = ((Number) fila[3]).doubleValue();

            modelo.addRow(new Object[]{ ranking++, producto, (String) fila[1], cantidad, formatearMoneda(ingresos) });

            // Define los datos que van en el grafico
            datasetGraficoProductos.addValue(cantidad, "Unidades Vendidas", producto);
        }

        // Envia la nueva tabla hacia la interfaz de usuario
        vistaPrincipal.getTablaProductosVendidos().setModel(modelo);
        vistaPrincipal.getTabsProductos().setSelectedIndex(0);

        // Actualiza todos los datos referentes a los botones, y el total de productos
        boolean hayDatos = resultados != null && !resultados.isEmpty();
        vistaPrincipal.getBtnGenerarGraficoProductos().setEnabled(hayDatos);
        vistaPrincipal.getBtnExportarExcelProductos().setEnabled(hayDatos);
        actualizarTotalProductos();
    }

    // Renderizado de graficos
    private void mostrarGraficoVentas() {
        // Verifica si hay datos en el dataset correspondiente, en caso de que no haya
        // datos, no hace nada
        if (datasetGraficoVentas == null || datasetGraficoVentas.getColumnCount() == 0) return;

        // Obtiene las fechas inicio y fin
        LocalDate[] f = obtenerFechasDesdeUI(vistaPrincipal.getCbDiaInicioV(), vistaPrincipal.getCbMesInicioV(), vistaPrincipal.getCbAnioInicioV(), vistaPrincipal.getCbDiaFinV(), vistaPrincipal.getCbMesFinV(), vistaPrincipal.getCbAnioFinV());

        // Define el titulo y la etiqueta que va a tener el valor x
        String titulo = "Evolución de Ingresos";
        String etiquetaX = "Período";

        // Si las fechas no son null entonces
        if (f != null) {
            // Obtiene los dias que hay entre fecha inicio y fecha fin
            // y define el string que va a escribir el rango de fecha en el grafico
            long diasDiferencia = java.time.temporal.ChronoUnit.DAYS.between(f[0], f[1]);
            String rangoTxt = " (" + f[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + f[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";

            // decide si mostrar los ingresos diarios o mensuales
            if (diasDiferencia <= 65) {
                titulo = "Evolución de Ingresos Diarios" + rangoTxt;
                etiquetaX = "Día";
            } else {
                titulo = "Evolución de Ingresos Mensuales" + rangoTxt;
                etiquetaX = "Mes";
            }
        }
        // Envia los datos a renderizarGraficoLineas para que se encargue de generar
        // el grafico mediante la logica ya armada
        vistaPrincipal.renderizarGraficoLineas(datasetGraficoVentas, titulo, etiquetaX, "Ingresos ($)", vistaPrincipal.getPanelGraficoVentas(), vistaPrincipal.getTabsVentas());
    }

    private void mostrarGraficoRendimiento() {
        // Verifica si el dataset correspondiente es nulo,
        // si es null, entonces no hace nada
        if (datasetGraficoRendimiento == null || datasetGraficoRendimiento.getColumnCount() == 0) return;

        // Obtiene la fecha inicio y fin
        LocalDate[] f = obtenerFechasDesdeUI(vistaPrincipal.getCbDiaInicioR(), vistaPrincipal.getCbMesInicioR(), vistaPrincipal.getCbAnioInicioR(), vistaPrincipal.getCbDiaFinR(), vistaPrincipal.getCbMesFinR(), vistaPrincipal.getCbAnioFinR());
        String titulo = (f != null) ? "Facturación por Vendedor (" + f[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + f[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")" : "Facturación por Vendedor";

        // Envia los datos obtenidos hacia renderizarGraficoBarras para que
        // se genere el grafico y aparezca en la interfaz de usuario
        vistaPrincipal.renderizarGraficoBarras(datasetGraficoRendimiento, titulo, "Usuario Vendedor", "Total Facturado ($)", vistaPrincipal.getPanelGraficoRendimiento(), vistaPrincipal.getTabsRendimiento());
    }

    private void mostrarGraficoProductos() {
        // Verifica si el dataset correspondiente es nulo
        if (datasetGraficoProductos == null || datasetGraficoProductos.getColumnCount() == 0) return;

        // Obtiene la fecha inicio y fin
        LocalDate[] f = obtenerFechasDesdeUI(vistaPrincipal.getCbDiaInicioP(), vistaPrincipal.getCbMesInicioP(), vistaPrincipal.getCbAnioInicioP(), vistaPrincipal.getCbDiaFinP(), vistaPrincipal.getCbMesFinP(), vistaPrincipal.getCbAnioFinP());
        String titulo = (f != null) ? "Top Productos (" + f[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + f[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")" : "Top Productos Más Vendidos";

        // Envia los datos obtenidos hacia renderizarGraficoBarras para generar el
        // grafico y que se muestre en la interfaz de usuario
        vistaPrincipal.renderizarGraficoBarras(datasetGraficoProductos, titulo, "Productos", "Cantidad Vendida (u)", vistaPrincipal.getPanelGraficoProductos(), vistaPrincipal.getTabsProductos());
    }

    // ------------------------------------
    // Limpieza de cada uno de los reportes
    // ------------------------------------

    private void limpiarReporteVentas() {
        // Crea un nuevo modelo de tabla sin datos
        vistaPrincipal.getTablaReportesVentas().setModel(new DefaultTableModel());

        // Establece los botones en falso para que no puedan ser usados
        vistaPrincipal.getBtnExportarExcelVentas().setEnabled(false);
        vistaPrincipal.getBtnGenerarGraficoVentas().setEnabled(false);

        // Remueve el grafico correspondiente
        vistaPrincipal.getPanelGraficoVentas().removeAll();
        vistaPrincipal.getPanelGraficoVentas().repaint();

        // Establece que el dataset sea nulo
        datasetGraficoVentas = null;
        // Setea la seccion de el grafico a 0
        vistaPrincipal.getTabsVentas().setSelectedIndex(0);
        // Actualiza el total de Ventas
        actualizarTotalVentas();
    }

    private void limpiarRendimiento() {
        // Crea un nuevo modelo de tabla sin datos
        vistaPrincipal.getTablaRendimiento().setModel(new DefaultTableModel());

        // Establece los botones en falso para que no puedan ser usados
        vistaPrincipal.getBtnExportarExcelRendimiento().setEnabled(false);
        vistaPrincipal.getBtnGenerarGraficoRendimiento().setEnabled(false);

        // Remueve el grafico correspondiente
        vistaPrincipal.getPanelGraficoRendimiento().removeAll();
        vistaPrincipal.getPanelGraficoRendimiento().repaint();

        // Establece que el dataset sea nulo
        datasetGraficoRendimiento = null;
        // Setea la seccion de el grafico a 0
        vistaPrincipal.getTabsRendimiento().setSelectedIndex(0);
        // Actualiza el total de Rendimiento
        actualizarTotalRendimiento();
    }

    private void limpiarProductos() {
        // Crea un nuevo modelo de tabla sin datos
        vistaPrincipal.getTablaProductosVendidos().setModel(new DefaultTableModel());

        // Establece los botones en falso para que no puedan ser usados
        vistaPrincipal.getBtnExportarExcelProductos().setEnabled(false);
        vistaPrincipal.getBtnGenerarGraficoProductos().setEnabled(false);

        // Remueve el grafico correspondiente
        vistaPrincipal.getPanelGraficoProductos().removeAll();
        vistaPrincipal.getPanelGraficoProductos().repaint();

        // Establece que el dataset sea nulo
        datasetGraficoProductos = null;
        // Setea la seccion de el grafico a 0
        vistaPrincipal.getTabsProductos().setSelectedIndex(0);
        // Actualiza el total de Productos
        actualizarTotalProductos();
    }

    // -----------------------------------------
    // Calculos matematicos y filtrado de tablas
    // -----------------------------------------

    // Calcula el total de cada seccion de reporte usando la tabla resultante de cuando
    // el gerente genera un nuevo reporte
    private double calcularTotalColumnaVisual(JTable tabla, int indiceColumna) {
        // Define el total
        double total = 0.0;

        // For mediante el cual se acumula el total de venta de la tabla
        for (int i = 0; i < tabla.getRowCount(); i++) {
            Object valor = tabla.getValueAt(i, indiceColumna);
            if (valor != null) {
                // parsearMoneda permite que el numero aparezca de una manera mas legible
                total += parsearMoneda(valor.toString());
            }
        }
        return total;
    }

    // Actualizan el total de ventas llamando al anterior metodo calcularTotalColumnaVisual
    private void actualizarTotalVentas() {
        double total = calcularTotalColumnaVisual(vistaPrincipal.getTablaReportesVentas(), 5);
        vistaPrincipal.getLblTotalReporteVentas().setText("Total Reporte: " + formatearMoneda(total));
    }

    private void actualizarTotalRendimiento() {
        double total = calcularTotalColumnaVisual(vistaPrincipal.getTablaRendimiento(), 2);
        vistaPrincipal.getLblTotalRendimiento().setText("Total Facturado: " + formatearMoneda(total));
    }

    private void actualizarTotalProductos() {
        double total = calcularTotalColumnaVisual(vistaPrincipal.getTablaProductosVendidos(), 4);
        vistaPrincipal.getLblTotalProductos().setText("Total Ingresos Generados: " + formatearMoneda(total));
    }

    // Metodo el cual filtra los datos de la tabla ya generada por el gerente
    private void filtrarTablaVentas() {
        // Obtiene lo que el gerente haya escrito en la barra de busqueda
        String textoBusqueda = vistaPrincipal.getBuscarVentaField().getText().trim();
        // Usamos el TableRowSorter para especificar que filas deben ser escondidas
        javax.swing.table.TableRowSorter<?> sorter = (javax.swing.table.TableRowSorter<?>) vistaPrincipal.getTablaReportesVentas().getRowSorter();

        if (sorter != null) {
            // Si la barra de busqueda esta vacia, devuelve la tabla original
            if (textoBusqueda.isEmpty()) sorter.setRowFilter(null);
            else {
                // Se decide que columna es en la que se va a buscar, dependiendo
                // si el combobox dice cliente o usuario vendedor
                int columnaFiltro = (vistaPrincipal.getComboCriterioBusqueda().getSelectedItem().toString().equals("DNI Cliente")) ? 2 : 3;
                // Aca es donde se decide que registros van a dejarse visibles
                sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(textoBusqueda), columnaFiltro));
            }
            // Actualiza el total de ventas, dejando solamente el total de las
            // ventas visibles
            actualizarTotalVentas();
        }
    }

    // -------------------------
    // Exportacion a Excel y PDF
    // -------------------------

    private void exportarTablaAExcel(JTable tabla, String nombreArchivoPorDefecto) {
        // Verifica si existen datos para exportar a Excel
        if (tabla.getRowCount() == 0) {
            vistaPrincipal.mostrarMensaje("No hay datos visibles para exportar.", "Aviso", 2);
            return;
        }

        // Abre el recuadro en donde le gerente eligira en donde se guardara el archivo
        // Excel
        java.io.File archivo = vistaPrincipal.solicitarArchivoParaGuardar(nombreArchivoPorDefecto);
        if (archivo == null) return;

        // Aca es donde se define el archivo .xlsx en el cual se guardaran
        // los datos tanto del monto de los productos, como los nombres e
        // informacion de venta correspondientes
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Reporte");

            org.apache.poi.ss.usermodel.CellStyle estiloCabecera = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font fuenteCabecera = workbook.createFont();
            fuenteCabecera.setBold(true);
            estiloCabecera.setFont(fuenteCabecera);
            estiloCabecera.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            estiloCabecera.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            // Se define el estilo con el cual van a aparecer los importes
            org.apache.poi.ss.usermodel.CellStyle estiloMoneda = workbook.createCellStyle();
            estiloMoneda.setDataFormat(workbook.createDataFormat().getFormat("$#,##0.00"));

            // Recorre la tabla visual de java, para asi insertar cada fila en
            // el archivo .xlsx, con su estilo determinado
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

                    // Determina si el registro es un importe, un nombre o un numero entero
                    if (valor != null) {
                        String texto = valor.toString();
                        if (texto.trim().startsWith("$")) {
                            try {
                                // Usamos el metodo parsearMoneda para desarmar la plata y mandarla a Excel limpia
                                celda.setCellValue(parsearMoneda(texto));
                                celda.setCellStyle(estiloMoneda);
                            } catch (Exception e) { celda.setCellValue(texto); }
                        } else {
                            try {
                                if (texto.matches("-?\\d+")) celda.setCellValue(Integer.parseInt(texto));
                                else celda.setCellValue(texto);
                            } catch (Exception ex) { celda.setCellValue(texto); }
                        }
                    }
                }
            }

            // For para que el ancho de la columna no acorte el nombre de cada registro
            for (int i = 0; i < tabla.getColumnCount(); i++) sheet.autoSizeColumn(i);

            // Permite crear el archivo .xlsx en la direccion que puso el gerente
            try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(archivo)) {
                workbook.write(fileOut);
            }

            // Muestra el mensaje de exito al momento de exportar el archivo .xlsx
            vistaPrincipal.mostrarMensaje("El reporte fue exportado a:\n" + archivo.getAbsolutePath(), "Exportación Exitosa", 1);
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES", "El Gerente exportó: " + archivo.getName());

        } catch (Exception ex) {
            vistaPrincipal.mostrarMensaje("Error al exportar XLSX:\n" + ex.getMessage(), "Error", 0);
        }
    }

    private void generarComprobantePDF(modelo.Venta venta) {
        try {
            // Define el nombre que va a tener el pdf
            String nombreArchivo = "Nexcell_Comprobante_Gerencia_" + venta.getId() + ".pdf";

            // Define la variable documento mediante itextpdf, creando un documento
            // en blanco al cual java va a escribir los datos que se le solicite
            com.itextpdf.text.Document documento = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(documento, new java.io.FileOutputStream(nombreArchivo));

            documento.open();

            // Creacion de los estilos de Titulo, Subtitulo y Normal
            com.itextpdf.text.Font fontTitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 18);
            com.itextpdf.text.Font fontSubtitulo = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 12);
            com.itextpdf.text.Font fontNormal = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 12);

            // Añade el texto que corresponde al titulo y los datos de la venta
            documento.add(new com.itextpdf.text.Paragraph("NEXCELL - TECNOLOGIA\n-----------------------------------------------------", fontTitulo));
            documento.add(new com.itextpdf.text.Paragraph("Factura: " + String.format("%06d", venta.getId()) + "\nFecha: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\nVendedor: " + venta.getVendedor().getUsername() + "\nCliente: " + venta.getCliente().getNombre() + " (DNI: " + venta.getCliente().getDni() + ")\n\n", fontNormal));

            // Define una tablapdf, y asigna los espacios que va a ocupar la tabla en el pdf
            com.itextpdf.text.pdf.PdfPTable tabla = new com.itextpdf.text.pdf.PdfPTable(3);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{50f, 20f, 30f});
            tabla.addCell(new com.itextpdf.text.Phrase("Producto", fontSubtitulo)); tabla.addCell(new com.itextpdf.text.Phrase("Cantidad", fontSubtitulo)); tabla.addCell(new com.itextpdf.text.Phrase("Subtotal", fontSubtitulo));

            // Busca todos los productos de detalle venta para insertarlos en el pdf,
            // formateando ademas el numero para que sea mas legible
            for (modelo.DetalleVenta d : venta.getDetalles()) {
                tabla.addCell(new com.itextpdf.text.Phrase(d.getProducto().getNombre(), fontNormal));
                tabla.addCell(new com.itextpdf.text.Phrase(d.getCantidad() + " u.", fontNormal));
                // Formato de moneda para los ítems del PDF
                tabla.addCell(new com.itextpdf.text.Phrase(formatearMoneda(d.getSubtotal()), fontNormal));
            }
            documento.add(tabla);

            // Formato de moneda para el total del PDF
            com.itextpdf.text.Paragraph total = new com.itextpdf.text.Paragraph("\nTOTAL ABONADO: " + formatearMoneda(venta.getTotal()), fontTitulo);
            total.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(total);
            documento.add(new com.itextpdf.text.Paragraph("\n\n*** Copia Auditada por Gerencia ***", fontNormal));

            // Se cierra el documento, si no se cierra, java queda trabado y piensa que
            // tiene que seguir escribiendo en el documento, por lo tanto genera error
            // y al momento de querer abrir el archivo, va a aparecer como que esta dañado
            documento.close();
            vistaPrincipal.mostrarMensaje("Comprobante guardado como:\n" + nombreArchivo, "PDF Generado", 1);

        } catch (Exception e) {
            vistaPrincipal.mostrarMensaje("Error al generar PDF: " + e.getMessage(), "Error", 0);
        }
    }
    // ----------
    // Utilidades
    // ----------

    // Recibe un numero en formato Double, y lo transforma a String
    private String formatearMoneda(double monto) {
        // Fuerza el formato de Argentina: punto para miles, coma para decimales
        return String.format(new java.util.Locale("es", "AR"), "$ %,.2f", monto);
    }

    // Recibe un numero en formato String, y lo transforma a Double
    private double parsearMoneda(String texto) {
        try {
            // Limpia el String "$ 20.500.000,50" -> "20500000.50" para que Java pueda sumarlo y hacer cálculos
            String limpio = texto.replace("$", "").replace(" ", "").replace(".", "").replace(",", ".").trim();
            return Double.parseDouble(limpio);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void abrirDetalleVenta(Long idVenta) {
        // Busca la venta en la base de datos
        modelo.Venta venta = em.find(modelo.Venta.class, idVenta);
        // Envia los datos de la venta hacia la interfaz de usuario, ademas de inyectar el listener
        // de generarComprobantePDF
        if (venta != null) vistaPrincipal.mostrarDialogoDetalleVenta(venta, this::generarComprobantePDF);
        else vistaPrincipal.mostrarMensaje("No se pudo encontrar la venta en la base de datos.", "Error", 0);
    }

    private void cerrarSesion() {
        // Verifica si el gerente quiere confirmar cerrar sesion
        if (vistaPrincipal.pedirConfirmacion("¿Estás seguro que querés salir del panel?", "Cerrar Sesión")) {
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "SEGURIDAD", "El Gerente cerró sesión.");
            vistaPrincipal.dispose();

            // Abre una nueva ventana de login luego de cerrar la de gerente
            LoginUI ventanaLogin = new LoginUI();
            new LoginController(ventanaLogin, em);
            ventanaLogin.setVisible(true);
        }
    }

    // Busca las coincidencias de texto que ingrese el usuario gerente
    private java.util.List<Object[]> buscarSugerenciasDinamicas(String parteTexto, String criterio) {
        try {
            // Define la variable mediante la cual se va a buscar el dato
            // en la base de datos
            String formatoBusqueda = parteTexto + "%";
            // Verifica si el filtro es por DNI Cliente o por Usuario Vendedor
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
            // Define las fechas desde y hasta con el tipo de dato LocalDate
            LocalDate desde = LocalDate.of(Integer.parseInt(cbAnioD.getSelectedItem().toString()), Integer.parseInt(cbMesD.getSelectedItem().toString()), Integer.parseInt(cbDiaD.getSelectedItem().toString()));
            LocalDate hasta = LocalDate.of(Integer.parseInt(cbAnioH.getSelectedItem().toString()), Integer.parseInt(cbMesH.getSelectedItem().toString()), Integer.parseInt(cbDiaH.getSelectedItem().toString()));

            // Si la fecha desde es despues de hasta, entonces el sistema
            // muestra el error
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