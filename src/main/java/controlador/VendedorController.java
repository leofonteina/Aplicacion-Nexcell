package controlador;

import vista.VendedorUI;
import vista.RegistroClienteUI;
import vista.RegistroVentaUI;
import vista.LoginUI;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

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

        // Vinculación de Eventos de Negocio
        this.vistaPrincipal.getBtnAbrirFormularioCliente().addActionListener(e -> abrirFormularioRegistro(""));
        this.vistaPrincipal.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());
        this.vistaPrincipal.getBtnAbrirFormularioVenta().addActionListener(e -> abrirFormularioVenta());
        this.vistaPrincipal.getBtnBajaCliente().addActionListener(e -> cambiarEstadoCliente(false));
        this.vistaPrincipal.getBtnAltaCliente().addActionListener(e -> cambiarEstadoCliente(true));
        this.vistaPrincipal.getBtnEditarCliente().addActionListener(e -> abrirFormularioEdicionCliente());

        this.vistaPrincipal.getBtnGenerarReporte().addActionListener(e -> generarReporte());
        this.vistaPrincipal.getBtnExportarReporte().addActionListener(e -> exportarReporteCSV());

        // Doble clic delegado limpiamente desde la vista
        this.vistaPrincipal.alHacerDobleClicEnVenta(this::abrirDetalleVenta);
    }

    // =========================================================================
    // CONSULTAS DE REPORTES (Ejecuta en BD y delega el dibujo a VendedorUI)
    // =========================================================================

    private void generarReporte() {
        String strInicio = vistaPrincipal.getFechaInicioReporteStr();
        String strFin = vistaPrincipal.getFechaFinReporteStr();

        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fechaInicio = LocalDate.parse(strInicio, fmt);
            LocalDate fechaFin = LocalDate.parse(strFin, fmt);

            int tipoReporte = vistaPrincipal.getIndiceTipoReporte();

            if (tipoReporte == 0) {
                fechaFin = fechaInicio; // Resumen diario es de un solo día
            }

            if (fechaInicio.isAfter(fechaFin)) {
                vistaPrincipal.mostrarAlerta("La fecha de inicio no puede ser mayor a la fecha de fin.", "Fechas Inválidas");
                return;
            }

            if (fechaInicio.plusMonths(6).isBefore(fechaFin)) {
                vistaPrincipal.mostrarAlerta("El rango seleccionado supera los 6 meses. Por favor, seleccione un periodo más corto.", "Rango Excedido");
                return;
            }

            LocalDateTime inicioLDT = fechaInicio.atStartOfDay();
            LocalDateTime finLDT = fechaFin.atTime(23, 59, 59);

            if (tipoReporte == 0) {
                List<Object[]> datos = consultarProcedimiento("reporte_resumen_diario", inicioLDT, finLDT);
                vistaPrincipal.mostrarReporteResumenDiario(datos);
            } else if (tipoReporte == 1) {
                List<Object[]> datos = consultarProcedimiento("reporte_top_productos", inicioLDT, finLDT);
                vistaPrincipal.mostrarReporteTopProductos(datos);
            } else if (tipoReporte == 2) {
                String categoria = vistaPrincipal.getCategoriaReporteSeleccionada();
                List<Object[]> datos = consultarProcedimientoCategoria(inicioLDT, finLDT, categoria);
                vistaPrincipal.mostrarReporteRendimientoCategoria(datos, categoria);
            } else if (tipoReporte == 3) {
                List<Object[]> datos = consultarProcedimiento("reporte_mejores_clientes", inicioLDT, finLDT);
                vistaPrincipal.mostrarReporteMejoresClientes(datos);
            }

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

    private void exportarReporteCSV() {
        List<String[]> filas = vistaPrincipal.obtenerDatosTablaReportes();
        if (filas.isEmpty()) {
            vistaPrincipal.mostrarAlerta("No hay datos en la tabla para exportar.", "Aviso");
            return;
        }

        File archivoDestino = vistaPrincipal.seleccionarArchivoCSVGuardar();
        if (archivoDestino != null) {
            try (PrintWriter writer = new PrintWriter(archivoDestino, "UTF-8")) {
                for (int i = 0; i < filas.size(); i++) {
                    String[] fila = filas.get(i);
                    for (int j = 0; j < fila.length; j++) {
                        if (i == 0) {
                            writer.print(fila[j]); // Cabeceras
                        } else {
                            writer.print("\"" + fila[j].replace("\"", "\"\"") + "\""); // Datos
                        }
                        if (j < fila.length - 1) writer.print(";");
                    }
                    writer.println();
                }

                vistaPrincipal.mostrarExito("Reporte exportado con éxito. Ya puedes abrirlo con Excel.", "Exportación Exitosa");
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES", "El Vendedor exportó datos a formato CSV.");
            } catch (Exception ex) {
                vistaPrincipal.mostrarError("Error al guardar el archivo.", "Error Crítico");
            }
        }
    }

    // =========================================================================
    // LÓGICA DE VENTAS Y CHECKOUT
    // =========================================================================

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
                StringBuilder resumen = new StringBuilder();
                resumen.append("--- TICKET DE VENTA - NEXCELL ---\n");
                resumen.append("Fecha: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
                resumen.append("Atendido por: ").append(this.vendedorLogueado.getUsername()).append("\n");
                resumen.append("Cliente: ").append(clienteActual[0].getNombre()).append(" ").append(clienteActual[0].getApellido()).append("\n");
                resumen.append("DNI: ").append(clienteActual[0].getDni()).append("\n\n");
                resumen.append("--- DETALLE ---\n");

                double totResumen = 0;
                for (Object[] fila : itemsCarrito) {
                    String nombreProd = (String) fila[1];
                    int cantProd = (int) fila[3];
                    double subTotalProd = (double) fila[4];

                    resumen.append("• ").append(cantProd).append("x ").append(nombreProd)
                        .append(" - $").append(String.format("%.2f", subTotalProd)).append("\n");
                    totResumen += subTotalProd;
                }

                resumen.append("---------------------------------\n");
                resumen.append("TOTAL A COBRAR: $").append(String.format("%.2f", totResumen)).append("\n");
                resumen.append("---------------------------------\n");

                if (!ventanaVenta.confirmarPregunta(resumen.toString() + "\n¿Desea confirmar e impactar esta venta en el sistema?", "Revisión Final de la Venta")) {
                    return;
                }

                boolean exito = procesarCheckout(vendedorLogueado.getId(), clienteActual[0].getDni(), totResumen, itemsCarrito);

                if (exito) {
                    try (FileWriter writer = new FileWriter("Ticket_Venta_" + System.currentTimeMillis() + ".txt")) {
                        writer.write(resumen.toString());
                        ventanaVenta.mostrarExito("¡Venta registrada exitosamente!\nSe generó el comprobante físico.", "Venta Exitosa");
                    } catch (Exception exio) {
                        ventanaVenta.mostrarAlerta("La venta se guardó, pero hubo un error al crear el archivo de texto.", "Aviso");
                    }

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

    public boolean procesarCheckout(Long idVendedor, String dniCliente, double totalVenta, List<Object[]> itemsCarrito) {
        StringBuilder jsonCarrito = new StringBuilder("[");

        for (int i = 0; i < itemsCarrito.size(); i++) {
            Object[] fila = itemsCarrito.get(i);
            Long idProd = (Long) fila[0];
            double precio = (double) fila[2];
            int cantidad = (int) fila[3];

            jsonCarrito.append(String.format("{\"id\": %d, \"cantidad\": %d, \"precio\": %s}",
                idProd, cantidad, String.valueOf(precio).replace(",", ".")));

            if (i < itemsCarrito.size() - 1) {
                jsonCarrito.append(",");
            }
        }
        jsonCarrito.append("]");

        try {
            em.getTransaction().begin();

            StoredProcedureQuery query = em.createStoredProcedureQuery("procesar_venta_integral");
            query.registerStoredProcedureParameter(1, Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(3, Double.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(4, String.class, ParameterMode.IN);

            query.setParameter(1, idVendedor);
            query.setParameter(2, dniCliente);
            query.setParameter(3, totalVenta);
            query.setParameter(4, jsonCarrito.toString());

            query.execute();
            em.getTransaction().commit();

            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "VENTAS",
                "El Vendedor '" + vendedorLogueado.getUsername() + "' registró exitosamente una venta por $" + totalVenta + " al DNI: " + dniCliente);

            return true;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "VENTAS",
                "Fallo al registrar venta del Vendedor '" + vendedorLogueado.getUsername() + "'. Motivo: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // GESTIÓN DE CLIENTES (ABM)
    // =========================================================================

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


    // CARGA DE DATOS E IMPRESION DE COMPROBANTES

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
                tabla.addCell(new com.itextpdf.text.Phrase(String.format("$%.2f", d.getSubtotal()), fontNormal));
            }
            documento.add(tabla);

            com.itextpdf.text.Paragraph total = new com.itextpdf.text.Paragraph("\nTOTAL ABONADO: $" + String.format("%.2f", venta.getTotal()), fontTitulo);
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
