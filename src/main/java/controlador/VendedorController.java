package controlador;

import vista.VendedorUI;
import vista.RegistroClienteUI;
import vista.RegistroVentaUI;
import vista.LoginUI;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import javax.swing.JTable;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class VendedorController {

    private VendedorUI vistaPrincipal;
    private EntityManager em;
    private modelo.Usuario vendedorLogueado;

    public VendedorController(VendedorUI vistaPrincipal, EntityManager em, modelo.Usuario vendedorLogueado) {
        this.vistaPrincipal = vistaPrincipal;
        this.em = em;
        this.vendedorLogueado = vendedorLogueado;

        try {
            cargarTablaClientes();
            cargarTablaProductos();
            cargarTablaVentas();
        } catch (Exception ex) {
            System.err.println("Aviso inicial: " + ex.getMessage());
        }

        // vinculacion de eventos de la vista principal
        this.vistaPrincipal.getBtnAbrirFormularioCliente().addActionListener(e -> abrirFormularioRegistro(""));
        this.vistaPrincipal.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());
        this.vistaPrincipal.getBtnAbrirFormularioVenta().addActionListener(e -> abrirFormularioVenta());
        this.vistaPrincipal.getBtnBajaCliente().addActionListener(e -> cambiarEstadoCliente(false));
        this.vistaPrincipal.getBtnAltaCliente().addActionListener(e -> cambiarEstadoCliente(true));
        this.vistaPrincipal.getBtnEditarCliente().addActionListener(e -> abrirFormularioEdicionCliente());
        this.vistaPrincipal.getBtnGenerarReporte().addActionListener(e -> generarReporte());
        this.vistaPrincipal.getBtnExportarReporte().addActionListener(e ->
            exportarTablaAExcel(vistaPrincipal.getTablaReportes(), obtenerNombreDefectoReporte())
        );

        // Busqueda en vivo de productos estilo Gerente
        this.vistaPrincipal.inicializarAutocompletadoProductos(texto ->
            buscarSugerenciasProductos(texto, vistaPrincipal.getComboCriterioProducto().getSelectedItem().toString())
        );
        this.vistaPrincipal.getBtnBuscarProducto().addActionListener(e -> filtrarTablaProductos());
        this.vistaPrincipal.getBuscarProductoField().addActionListener(e -> filtrarTablaProductos());

        this.vistaPrincipal.alHacerDobleClicEnVenta(this::abrirDetalleVenta);
    }

    // modulo de reportes estadisticos

    private String obtenerNombreDefectoReporte() {
        switch (vistaPrincipal.getIndiceTipoReporte()) {
            case 0:
                return "Reporte_Resumen_Diario";
            case 1:
                return "Reporte_Top_Productos";
            case 2:
                return "Reporte_Categoria_" + vistaPrincipal.getCategoriaReporteSeleccionada();
            case 3:
                return "Reporte_Mejores_Clientes";
            default:
                return "Reporte_Vendedor";
        }
    }

    private void generarReporte() {
        String strInicio = vistaPrincipal.getFechaInicioReporteStr();
        String strFin = vistaPrincipal.getFechaFinReporteStr();

        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fechaInicio = LocalDate.parse(strInicio, fmt);
            LocalDate fechaFin = LocalDate.parse(strFin, fmt);

            int tipoReporte = vistaPrincipal.getIndiceTipoReporte();

            if (tipoReporte == 0) {
                fechaFin = fechaInicio;
            }

            if (fechaInicio.isAfter(fechaFin)) {
                vistaPrincipal.mostrarAlerta("La fecha de inicio no puede ser mayor a la fecha de fin.", "Fechas Inválidas");
                return;
            }

            // SE ELIMINÓ LA RESTRICCIÓN DE LOS 6 MESES

            LocalDateTime inicioLDT = fechaInicio.atStartOfDay();
            LocalDateTime finLDT = fechaFin.atTime(23, 59, 59);

            List<Object[]> datos = null;

            if (tipoReporte == 0) {
                datos = consultarProcedimiento("reporte_resumen_diario", inicioLDT, finLDT);
                vistaPrincipal.mostrarReporteResumenDiario(datos);
            } else if (tipoReporte == 1) {
                datos = consultarProcedimiento("reporte_top_productos", inicioLDT, finLDT);
                vistaPrincipal.mostrarReporteTopProductos(datos);
            } else if (tipoReporte == 2) {
                String categoria = vistaPrincipal.getCategoriaReporteSeleccionada();
                datos = consultarProcedimientoCategoria(inicioLDT, finLDT, categoria);
                vistaPrincipal.mostrarReporteRendimientoCategoria(datos, categoria);
            } else if (tipoReporte == 3) {
                datos = consultarProcedimiento("reporte_mejores_clientes", inicioLDT, finLDT);
                vistaPrincipal.mostrarReporteMejoresClientes(datos);
            }

            boolean hayDatos = datos != null && !datos.isEmpty();
            vistaPrincipal.getBtnExportarReporte().setEnabled(hayDatos);

        } catch (DateTimeParseException ex) {
            vistaPrincipal.mostrarError("La fecha seleccionada no existe en el calendario.", "Fecha Inválida");
        } catch (Exception ex) {
            vistaPrincipal.mostrarError("Ocurrió un error al generar el reporte: " + ex.getMessage(), "Error");
        }
    }

    private List<Object[]> consultarProcedimiento(String nombreSp, LocalDateTime inicio, LocalDateTime fin) {
        StoredProcedureQuery query = em.createStoredProcedureQuery(nombreSp);
        query.registerStoredProcedureParameter(1, Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(2, LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(3, LocalDateTime.class, ParameterMode.IN);

        query.setParameter(1, this.vendedorLogueado.getId());
        query.setParameter(2, inicio);
        query.setParameter(3, fin);

        return query.getResultList();
    }

    private List<Object[]> consultarProcedimientoCategoria(LocalDateTime inicio, LocalDateTime fin, String categoria) {
        StoredProcedureQuery query = em.createStoredProcedureQuery("reporte_rendimiento_categoria");
        query.registerStoredProcedureParameter(1, Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(3, LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter(4, LocalDateTime.class, ParameterMode.IN);

        query.setParameter(1, this.vendedorLogueado.getId());
        query.setParameter(2, categoria);
        query.setParameter(3, inicio);
        query.setParameter(4, fin);

        return query.getResultList();
    }

    private void exportarTablaAExcel(JTable tabla, String nombreArchivoPorDefecto) {
        if (tabla.getRowCount() == 0) {
            vistaPrincipal.mostrarAlerta("No hay datos visibles para exportar.", "Aviso");
            return;
        }

        File archivo = vistaPrincipal.solicitarArchivoParaGuardar(nombreArchivoPorDefecto);
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
            estiloMoneda.setDataFormat(workbook.createDataFormat().getFormat("$ #,##0.00"));

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
                                celda.setCellValue(parsearMoneda(texto));
                                celda.setCellStyle(estiloMoneda);
                            } catch (Exception e) {
                                celda.setCellValue(texto);
                            }
                        } else {
                            try {
                                if (texto.matches("-?\\d+")) {
                                    celda.setCellValue(Integer.parseInt(texto));
                                } else {
                                    celda.setCellValue(texto);
                                }
                            } catch (Exception ex) {
                                celda.setCellValue(texto);
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < tabla.getColumnCount(); i++) {
                sheet.autoSizeColumn(i);
            }

            try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(archivo)) {
                workbook.write(fileOut);
            }

            vistaPrincipal.mostrarExito("El reporte fue exportado a:\n" + archivo.getAbsolutePath(), "Exportación Exitosa");
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES",
                "El Vendedor '" + vendedorLogueado.getUsername() + "' exportó: " + archivo.getName());

        } catch (Exception ex) {
            vistaPrincipal.mostrarError("Error al exportar XLSX:\n" + ex.getMessage(), "Error");
        }
    }

    private String formatearMoneda(double monto) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(new Locale("es", "AR"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("$ #,##0.00", simbolos);
        return df.format(monto);
    }

    private double parsearMoneda(String texto) {
        try {
            String limpio = texto.replace("$", "").replace(" ", "").replace(".", "").replace(",", ".").trim();
            return Double.parseDouble(limpio);
        } catch (Exception e) {
            return 0.0;
        }
    }

    // modulo de ventas y checkout

    private void abrirFormularioVenta() {
        try {
            RegistroVentaUI ventanaVenta = new RegistroVentaUI(this.vistaPrincipal);
            repositorio.ProductoRepository repoProd = new repositorio.ProductoRepository(this.em);
            repositorio.ClienteRepository repoCli = new repositorio.ClienteRepository(this.em);

            ventanaVenta.cargarProductosDisponibles(repoProd.listarTodos());

            final modelo.Cliente[] clienteActual = {null};

            ventanaVenta.getBtnVerificarCliente().addActionListener(e -> {
                String dni = ventanaVenta.getDniClienteField().getText().trim();
                if (dni.isEmpty()) {
                    ventanaVenta.mostrarAlerta("Ingrese un DNI primero.", "Atención");
                    return;
                }
                modelo.Cliente c = repoCli.buscarPorDni(dni);
                if (c != null && c.isActivo()) {
                    clienteActual[0] = c;
                    ventanaVenta.mostrarClienteValidado(c.getDni(), c.getNombre() + " " + c.getApellido(), false);
                } else {
                    if (ventanaVenta.confirmarPregunta("Cliente no encontrado. ¿Registrarlo?", "No encontrado")) {
                        abrirFormularioRegistro(dni);
                        modelo.Cliente nuevoC = repoCli.buscarPorDni(dni);
                        if (nuevoC != null) {
                            clienteActual[0] = nuevoC;
                            ventanaVenta.mostrarClienteValidado(nuevoC.getDni(), nuevoC.getNombre() + " " + nuevoC.getApellido(), true);
                        }
                    }
                }
            });

            ventanaVenta.getBtnEliminarDelCarrito().addActionListener(e -> ventanaVenta.eliminarFilaSeleccionadaCarrito());

            ventanaVenta.getBtnAgregarProducto().addActionListener(e -> {
                Long idProducto = ventanaVenta.getIdProductoSeleccionado();
                if (idProducto == null) return;

                modelo.Producto producto = repoProd.buscarPorId(idProducto);

                try {
                    int pedida = Integer.parseInt(ventanaVenta.getCantidadField().getText().trim());
                    if (pedida <= 0) throw new NumberFormatException();

                    int cantEnCarro = ventanaVenta.obtenerCantidadEnCarritoParaProducto(idProducto);
                    int totalRequerido = pedida + cantEnCarro;

                    if (totalRequerido > producto.getStock()) {
                        ventanaVenta.mostrarError("Stock insuficiente. Solo cuentas con " + producto.getStock() + " unidades disponibles.", "Error de Stock");
                        return;
                    }

                    int stockRestante = producto.getStock() - totalRequerido;
                    if (stockRestante <= 5) {
                        ventanaVenta.mostrarAlerta("Atención: Al confirmar esta venta, solo quedarán " + stockRestante + " unidades de '" + producto.getNombre() + "' en inventario.", "Alerta de Stock Crítico");
                    }

                    double sub = pedida * producto.getPrecio();
                    ventanaVenta.agregarFilaCarrito(producto.getId(), producto.getNombre(), producto.getPrecio(), pedida, sub);

                } catch (NumberFormatException ex) {
                    ventanaVenta.mostrarError("Cantidad inválida.", "Error");
                }
            });

            ventanaVenta.getBtnConfirmarVenta().addActionListener(e -> {
                if (clienteActual[0] == null) {
                    ventanaVenta.mostrarAlerta("Debe asociar un cliente antes de facturar.", "Error");
                    return;
                }
                if (ventanaVenta.isCarritoVacio()) {
                    ventanaVenta.mostrarAlerta("El carrito está vacío.", "Atención");
                    return;
                }

                List<Object[]> itemsCarrito = ventanaVenta.obtenerItemsCarrito();
                double totResumen = 0;
                for (Object[] fila : itemsCarrito) {
                    totResumen += (double) fila[4];
                }

                if (!ventanaVenta.confirmarPregunta(
                    "¿Desea confirmar y registrar esta venta por un total de " + formatearMoneda(totResumen) + "?",
                    "Confirmar Venta")) {
                    return;
                }

                boolean exito = procesarCheckout(clienteActual[0], totResumen, itemsCarrito);

                if (exito) {
                    ventanaVenta.mostrarExito("¡Venta registrada exitosamente!", "Venta Exitosa");
                    cargarTablaVentas();
                    cargarTablaProductos();
                    ventanaVenta.dispose();
                } else {
                    ventanaVenta.mostrarError("Error interno en la base de datos al procesar la venta.", "Error Crítico");
                }
            });

            ventanaVenta.setVisible(true);

        } catch (Exception ex) {
            vistaPrincipal.mostrarError("Error al inicializar módulo de ventas: " + ex.getMessage(), "Error");
        }
    }

    public boolean procesarCheckout(modelo.Cliente cliente, double totalVenta, List<Object[]> itemsCarrito) {
        try {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < itemsCarrito.size(); i++) {
                Object[] fila = itemsCarrito.get(i);
                if (i > 0) json.append(",");
                json.append("{\"id\":").append(fila[0])
                    .append(",\"cantidad\":").append(fila[3])
                    .append(",\"precio\":").append(fila[2]).append("}");
            }
            json.append("]");

            StoredProcedureQuery query = em.createStoredProcedureQuery("procesar_registro_venta");
            query.registerStoredProcedureParameter(1, Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(3, Double.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(4, String.class, ParameterMode.IN);

            query.setParameter(1, this.vendedorLogueado.getId());
            query.setParameter(2, cliente.getDni());
            query.setParameter(3, totalVenta);
            query.setParameter(4, json.toString());

            query.execute();

            em.clear();

            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "VENTAS",
                "El Vendedor '" + vendedorLogueado.getUsername() + "' registró exitosamente una venta por "
                    + formatearMoneda(totalVenta) + " al DNI: " + cliente.getDni());
            return true;

        } catch (Exception e) {
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "VENTAS",
                "Fallo en la transacción de venta del Vendedor '" + vendedorLogueado.getUsername()
                    + "'. Motivo: " + e.getMessage());
            return false;
        }
    }

    // filtrado de productos estilo gerente

    private void filtrarTablaProductos() {
        String textoBusqueda = vistaPrincipal.getBuscarProductoField().getText().trim();
        javax.swing.table.TableRowSorter<?> sorter = (javax.swing.table.TableRowSorter<?>) vistaPrincipal.getTablaProductos().getRowSorter();

        if (sorter != null) {
            if (textoBusqueda.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                int columnaFiltro = vistaPrincipal.getComboCriterioProducto().getSelectedItem().toString().equals("Modelo") ? 2 : 1;
                sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(textoBusqueda), columnaFiltro));
            }
        }
    }

    private List<Object[]> buscarSugerenciasProductos(String parteTexto, String criterio) {
        try {
            String formatoBusqueda = "%" + parteTexto + "%";
            if (criterio.equals("Modelo")) {
                return em.createQuery(
                    "SELECT p.id, p.nombre, p.categoria, p.stock FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(:texto)",
                    Object[].class
                ).setParameter("texto", formatoBusqueda).setMaxResults(8).getResultList();
            } else {
                return em.createQuery(
                    "SELECT p.id, p.nombre, p.categoria, p.stock FROM Producto p WHERE LOWER(p.categoria) LIKE LOWER(:texto)",
                    Object[].class
                ).setParameter("texto", formatoBusqueda).setMaxResults(8).getResultList();
            }
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    // gestion de clientes (abm)

    private void abrirFormularioRegistro(String dniPreCargado) {
        RegistroClienteUI ven = new RegistroClienteUI(this.vistaPrincipal);
        ven.preCargarDni(dniPreCargado);
        ven.getBtnCancelar().addActionListener(e -> ven.dispose());

        ven.getBtnGuardarCliente().addActionListener(e -> {
            ven.limpiarErrores();
            boolean hayErrores = false;

            String dni = ven.getTxtDni().getText().trim();
            String nombre = ven.getTxtNombre().getText().trim();
            String apellido = ven.getTxtApellido().getText().trim();
            String tel = ven.getTxtTelefono().getText().trim();
            String email = ven.getTxtEmail().getText().trim();
            String calle = ven.getTxtCalle().getText().trim();
            String altura = ven.getTxtAltura().getText().trim();
            String ciudad = ven.getTxtCiudad().getText().trim();

            if (nombre.isEmpty()) { ven.getLblErrorNombre().setText("Requerido."); hayErrores = true; }
            if (apellido.isEmpty()) { ven.getLblErrorApellido().setText("Requerido."); hayErrores = true; }
            if (dni.isEmpty()) { ven.getLblErrorDni().setText("Requerido."); hayErrores = true; }

            LocalDate fechaNac = null;
            try {
                int d = (int) ven.getCbDia().getSelectedItem();
                int m = Integer.parseInt(ven.getCbMes().getSelectedItem().toString());
                int a = (int) ven.getCbAnio().getSelectedItem();
                fechaNac = LocalDate.of(a, m, d);
            } catch (Exception ex) {
                ven.getLblErrorFecha().setText("Fecha inválida."); hayErrores = true;
            }

            if (hayErrores) return;

            try {
                repositorio.ClienteRepository repo = new repositorio.ClienteRepository(this.em);
                if (repo.existeDni(dni)) {
                    ven.getLblErrorDni().setText("Este DNI ya existe.");
                    return;
                }

                modelo.Direccion dir = new modelo.Direccion(calle, altura, ciudad, ven.getCbProvincia().getSelectedItem().toString());

                em.getTransaction().begin();
                repo.guardar(new modelo.Cliente(dni, nombre, apellido, tel, email, fechaNac, dir));
                em.getTransaction().commit();

                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "CLIENTES",
                    "El Vendedor '" + vendedorLogueado.getUsername() + "' registró al cliente DNI: " + dni);

                cargarTablaClientes();
                ven.dispose();
                vistaPrincipal.mostrarExito("Cliente registrado con éxito.", "Éxito");

            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "CLIENTES",
                    "Error al registrar cliente: " + ex.getMessage());
            }
        });
        ven.setVisible(true);
    }

    private void abrirFormularioEdicionCliente() {
        String dni = vistaPrincipal.getDniClienteSeleccionado();
        if (dni == null) return;

        repositorio.ClienteRepository repo = new repositorio.ClienteRepository(this.em);
        modelo.Cliente cliente = repo.buscarPorDni(dni);
        if (cliente == null) return;

        RegistroClienteUI ven = new RegistroClienteUI(this.vistaPrincipal);
        ven.configurarModoEdicionDireccion(cliente);
        ven.getBtnCancelar().addActionListener(e -> ven.dispose());

        ven.getBtnGuardarCliente().addActionListener(e -> {
            ven.limpiarErrores();

            String calle = ven.getTxtCalle().getText().trim();
            String altura = ven.getTxtAltura().getText().trim();
            String ciudad = ven.getTxtCiudad().getText().trim();
            String provincia = ven.getCbProvincia().getSelectedItem().toString();

            try {
                em.getTransaction().begin();

                if (cliente.getDireccion() == null) {
                    cliente.setDireccion(new modelo.Direccion(calle, altura, ciudad, provincia));
                } else {
                    cliente.getDireccion().setCalle(calle);
                    cliente.getDireccion().setAltura(altura);
                    cliente.getDireccion().setCiudad(ciudad);
                    cliente.getDireccion().setProvincia(provincia);
                }

                em.merge(cliente);
                em.getTransaction().commit();

                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "CLIENTES",
                    "El Vendedor '" + vendedorLogueado.getUsername() + "' modificó la dirección del cliente DNI: " + dni);

                cargarTablaClientes();
                ven.dispose();
                vistaPrincipal.mostrarExito("Dirección actualizada correctamente.", "Edición Exitosa");

            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "CLIENTES", "Error al editar cliente: " + ex.getMessage());
                vistaPrincipal.mostrarError("Error al actualizar la base de datos.", "Error");
            }
        });

        ven.setVisible(true);
    }

    private void cambiarEstadoCliente(boolean act) {
        String dniCliente = vistaPrincipal.getDniClienteSeleccionado();
        if (dniCliente == null) return;

        if (vistaPrincipal.confirmarAccion("¿Seguro?", act ? "Reactivar" : "Dar Baja")) {
            try {
                repositorio.ClienteRepository repo = new repositorio.ClienteRepository(this.em);
                modelo.Cliente cliente = repo.buscarPorDni(dniCliente);

                if (cliente != null) {
                    em.getTransaction().begin();
                    cliente.setActivo(act);
                    repo.actualizar(cliente);
                    em.getTransaction().commit();

                    vistaPrincipal.actualizarEstadoClienteSeleccionado(act);

                    utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ALERTA, "CLIENTES",
                        "El Vendedor '" + vendedorLogueado.getUsername() + "' cambió el estado del cliente DNI " + dniCliente + " a " + (act ? "Activo" : "Inactivo"));
                }
            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "CLIENTES", "Error al cambiar estado: " + ex.getMessage());
            }
        }
    }

    // extraccion de datos y presentacion (impresion pdf)

    private void cargarTablaClientes() {
        List<modelo.Cliente> cls = new repositorio.ClienteRepository(this.em).listarTodos();
        vistaPrincipal.mostrarClientes(cls);
    }

    private void cargarTablaProductos() {
        List<modelo.Producto> prs = new repositorio.ProductoRepository(this.em).listarTodos();
        vistaPrincipal.mostrarProductos(prs);
    }

    private void cargarTablaVentas() {
        try {
            List<modelo.Venta> vts = new repositorio.VentaRepository(this.em).listarPorVendedor(this.vendedorLogueado);
            vistaPrincipal.mostrarVentas(vts);
        } catch (Exception e) {}
    }

    private void abrirDetalleVenta(Long idVenta) {
        modelo.Venta venta = em.find(modelo.Venta.class, idVenta);
        if (venta != null) {
            vistaPrincipal.mostrarDialogoDetalleVenta(venta, this::generarComprobantePDF);
        }
    }

    private void generarComprobantePDF(modelo.Venta venta) {
        try {
            String nombreArchivo = "Nexcell_Comprobante_Venta_" + venta.getId() + ".pdf";

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
                tabla.addCell(new com.itextpdf.text.Phrase(formatearMoneda(d.getSubtotal()), fontNormal));
            }
            documento.add(tabla);

            com.itextpdf.text.Paragraph total = new com.itextpdf.text.Paragraph("\nTOTAL ABONADO: " + formatearMoneda(venta.getTotal()), fontTitulo);
            total.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(total);
            documento.add(new com.itextpdf.text.Paragraph("\n\n*** Gracias por su compra en Nexcell ***", fontNormal));

            documento.close();

            vistaPrincipal.mostrarExito("Comprobante PDF generado y guardado con éxito como:\n" + nombreArchivo, "PDF Generado");

        } catch (Exception e) {
            vistaPrincipal.mostrarError("Error al generar el PDF: " + e.getMessage(), "Error");
        }
    }

    private void cerrarSesion() {
        if (vistaPrincipal.confirmarAccion("¿Salir?", "Cerrar")) {
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "SEGURIDAD",
                "El Vendedor '" + vendedorLogueado.getUsername() + "' cerró sesión.");
            vistaPrincipal.dispose();
            LoginUI ven = new LoginUI();
            new LoginController(ven, this.em);
            ven.setVisible(true);
        }
    }
}
