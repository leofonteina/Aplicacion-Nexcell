package controlador;

import vista.VendedorUI;
import vista.RegistroClienteUI;
import vista.RegistroVentaUI;
import vista.LoginUI;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;
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

        // Doble clic en tabla de ventas para pedir el detalle
        this.vistaPrincipal.getTablaVentas().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && vistaPrincipal.getIdVentaSeleccionada() != null) {
                    abrirDetalleVenta();
                }
            }
        });
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

            int tipoReporte = vistaPrincipal.getComboTipoReporte().getSelectedIndex();

            if (tipoReporte == 0) {
                fechaFin = fechaInicio; // Resumen diario es de un solo día
            }

            if (fechaInicio.isAfter(fechaFin)) {
                vistaPrincipal.mostrarMensaje("La fecha de inicio no puede ser mayor a la fecha de fin.", "Fechas Inválidas", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (fechaInicio.plusMonths(6).isBefore(fechaFin)) {
                vistaPrincipal.mostrarMensaje("El rango seleccionado supera los 6 meses. Por favor, seleccione un periodo más corto.", "Rango Excedido", JOptionPane.WARNING_MESSAGE);
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
                String categoria = vistaPrincipal.getComboCategoriasReporte().getSelectedItem().toString();
                List<Object[]> datos = consultarProcedimientoCategoria(inicioLDT, finLDT, categoria);
                vistaPrincipal.mostrarReporteRendimientoCategoria(datos, categoria);
            } else if (tipoReporte == 3) {
                List<Object[]> datos = consultarProcedimiento("reporte_mejores_clientes", inicioLDT, finLDT);
                vistaPrincipal.mostrarReporteMejoresClientes(datos);
            }

        } catch (DateTimeParseException ex) {
            vistaPrincipal.mostrarMensaje("La fecha seleccionada no existe en el calendario.", "Fecha Inválida", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            vistaPrincipal.mostrarMensaje("Ocurrió un error al generar el reporte: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        JTable tabla = vistaPrincipal.getTablaReportes();
        if (tabla.getRowCount() == 0) {
            vistaPrincipal.mostrarMensaje("No hay datos en la tabla para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File archivoDestino = vistaPrincipal.seleccionarArchivoCSVGuardar();
        if (archivoDestino != null) {
            try (PrintWriter writer = new PrintWriter(archivoDestino, "UTF-8")) {
                for (int i = 0; i < tabla.getColumnCount(); i++) {
                    writer.print(tabla.getColumnName(i));
                    if (i < tabla.getColumnCount() - 1) writer.print(";");
                }
                writer.println();

                for (int i = 0; i < tabla.getRowCount(); i++) {
                    for (int j = 0; j < tabla.getColumnCount(); j++) {
                        String val = tabla.getValueAt(i, j) != null ? tabla.getValueAt(i, j).toString() : "";
                        writer.print("\"" + val.replace("\"", "\"\"") + "\"");
                        if (j < tabla.getColumnCount() - 1) writer.print(";");
                    }
                    writer.println();
                }

                vistaPrincipal.mostrarMensaje("Reporte exportado con éxito. Ya puedes abrirlo con Excel.", "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES", "El Vendedor exportó datos a formato CSV.");
            } catch (Exception ex) {
                vistaPrincipal.mostrarMensaje("Error al guardar el archivo.", "Error Crítico", JOptionPane.ERROR_MESSAGE);
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

            // La vista se encarga de cargar su propio ComboBox
            ventanaVenta.cargarProductosDisponibles(repoProd.listarTodos());

            final modelo.Cliente[] clienteActual = {null};

            ventanaVenta.getBtnVerificarCliente().addActionListener(e -> {
                String dni = ventanaVenta.getDniClienteField().getText().trim();
                if (dni.isEmpty()) {
                    ventanaVenta.mostrarMensaje("Ingrese un DNI primero.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                modelo.Cliente c = repoCli.buscarPorDni(dni);
                if (c != null && c.isActivo()) {
                    clienteActual[0] = c;
                    ventanaVenta.mostrarClienteValidado(c.getDni(), c.getNombre() + " " + c.getApellido(), false);
                } else {
                    if (ventanaVenta.confirmarAccion("Cliente no encontrado. ¿Registrarlo?", "No encontrado", JOptionPane.QUESTION_MESSAGE)) {
                        abrirFormularioRegistro(dni);
                        modelo.Cliente nuevoC = repoCli.buscarPorDni(dni);
                        if (nuevoC != null) {
                            clienteActual[0] = nuevoC;
                            ventanaVenta.mostrarClienteValidado(nuevoC.getDni(), nuevoC.getNombre() + " " + nuevoC.getApellido(), true);
                        }
                    }
                }
            });

            // La vista se encarga de eliminar la fila y recalcular su total
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
                        ventanaVenta.mostrarMensaje("Stock insuficiente. Solo cuentas con " + producto.getStock() + " unidades disponibles.", "Error de Stock", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int stockRestante = producto.getStock() - totalRequerido;
                    if (stockRestante <= 5) {
                        ventanaVenta.mostrarMensaje("Atención: Al confirmar esta venta, solo quedarán " + stockRestante + " unidades de '" + producto.getNombre() + "' en inventario.", "Alerta de Stock Crítico", JOptionPane.WARNING_MESSAGE);
                    }

                    double sub = pedida * producto.getPrecio();
                    ventanaVenta.agregarFilaCarrito(producto.getId(), producto.getNombre(), producto.getPrecio(), pedida, sub);

                } catch (NumberFormatException ex) {
                    ventanaVenta.mostrarMensaje("Cantidad inválida.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            ventanaVenta.getBtnConfirmarVenta().addActionListener(e -> {
                if (clienteActual[0] == null) {
                    ventanaVenta.mostrarMensaje("Debe asociar un cliente antes de facturar.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (ventanaVenta.isCarritoVacio()) {
                    ventanaVenta.mostrarMensaje("El carrito está vacío.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                TableModel mod = ventanaVenta.getTablaCarrito().getModel();
                StringBuilder resumen = new StringBuilder();
                resumen.append("--- TICKET DE VENTA - NEXCELL ---\n");
                resumen.append("Fecha: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
                resumen.append("Atendido por: ").append(this.vendedorLogueado.getUsername()).append("\n");
                resumen.append("Cliente: ").append(clienteActual[0].getNombre()).append(" ").append(clienteActual[0].getApellido()).append("\n");
                resumen.append("DNI: ").append(clienteActual[0].getDni()).append("\n\n");
                resumen.append("--- DETALLE ---\n");

                double totResumen = 0;
                for (int i = 0; i < mod.getRowCount(); i++) {
                    String nombreProd = (String) mod.getValueAt(i, 1);
                    int cantProd = (int) mod.getValueAt(i, 3);
                    double subTotalProd = (double) mod.getValueAt(i, 4);

                    resumen.append("• ").append(cantProd).append("x ").append(nombreProd)
                        .append(" - $").append(String.format("%.2f", subTotalProd)).append("\n");
                    totResumen += subTotalProd;
                }

                resumen.append("---------------------------------\n");
                resumen.append("TOTAL A COBRAR: $").append(String.format("%.2f", totResumen)).append("\n");
                resumen.append("---------------------------------\n");

                if (!ventanaVenta.confirmarAccion(resumen.toString() + "\n¿Desea confirmar e impactar esta venta en el sistema?", "Revisión Final de la Venta", JOptionPane.QUESTION_MESSAGE)) {
                    return;
                }

                boolean exito = procesarCheckout(vendedorLogueado.getId(), clienteActual[0].getDni(), totResumen, mod);

                if (exito) {
                    try (FileWriter writer = new FileWriter("Ticket_Venta_" + System.currentTimeMillis() + ".txt")) {
                        writer.write(resumen.toString());
                        ventanaVenta.mostrarMensaje("¡Venta registrada exitosamente!\nSe generó el comprobante físico.", "Venta Exitosa", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception exio) {
                        ventanaVenta.mostrarMensaje("La venta se guardó, pero hubo un error al crear el archivo de texto.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    }

                    cargarTablaVentas();
                    cargarTablaProductos();
                    ventanaVenta.dispose();
                } else {
                    ventanaVenta.mostrarMensaje("Error interno en la base de datos al procesar la venta.", "Error Crítico", JOptionPane.ERROR_MESSAGE);
                }
            });

            ventanaVenta.setVisible(true);

        } catch (Exception ex) {
            vistaPrincipal.mostrarMensaje("Error al inicializar módulo de ventas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean procesarCheckout(Long idVendedor, String dniCliente, double totalVenta, TableModel modeloCarrito) {
        StringBuilder jsonCarrito = new StringBuilder("[");

        for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
            Long idProd = (Long) modeloCarrito.getValueAt(i, 0);
            double precio = (double) modeloCarrito.getValueAt(i, 2);
            int cantidad = (int) modeloCarrito.getValueAt(i, 3);

            jsonCarrito.append(String.format("{\"id\": %d, \"cantidad\": %d, \"precio\": %s}",
                idProd, cantidad, String.valueOf(precio).replace(",", ".")));

            if (i < modeloCarrito.getRowCount() - 1) {
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
                vistaPrincipal.mostrarMensaje("Cliente registrado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

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
        // La Vista se encarga de pre-cargar y bloquear los campos que no corresponden a Dirección
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
                vistaPrincipal.mostrarMensaje("Dirección actualizada correctamente.", "Edición Exitosa", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "CLIENTES", "Error al editar cliente: " + ex.getMessage());
                vistaPrincipal.mostrarMensaje("Error al actualizar la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        ven.setVisible(true);
    }

    private void cambiarEstadoCliente(boolean act) {
        int fila = vistaPrincipal.getTablaClientes().getSelectedRow();
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

                    vistaPrincipal.actualizarEstadoClienteEnTabla(fila, act);

                    utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ALERTA, "CLIENTES",
                        "El Vendedor '" + vendedorLogueado.getUsername() + "' cambió el estado del cliente DNI " + dniCliente + " a " + (act ? "Activo" : "Inactivo"));
                }
            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "CLIENTES", "Error al cambiar estado: " + ex.getMessage());
            }
        }
    }

    // =========================================================================
    // CARGA DE DATOS E IMPRESIÓN DE COMPROBANTES
    // =========================================================================

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

    private void abrirDetalleVenta() {
        Long idVenta = vistaPrincipal.getIdVentaSeleccionada();
        if (idVenta == null) return;

        modelo.Venta venta = em.find(modelo.Venta.class, idVenta);
        if (venta != null) {
            // Le pasamos la venta a la Vista para que arme el diálogo, y le pasamos el método generarComprobantePDF como callback
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

            vistaPrincipal.mostrarMensaje("Comprobante PDF generado y guardado con éxito como:\n" + nombreArchivo, "PDF Generado", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            vistaPrincipal.mostrarMensaje("Error al generar el PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
