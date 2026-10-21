package controlador;

import vista.VendedorUI;
import vista.RegistroClienteUI;
import vista.RegistroVentaUI;
import vista.LoginUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;

public class VendedorController {

    private VendedorUI vistaPrincipal;
    private jakarta.persistence.EntityManager em;
    private modelo.Usuario vendedorLogueado;

    public VendedorController(VendedorUI vistaPrincipal, jakarta.persistence.EntityManager em, modelo.Usuario vendedorLogueado) {
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

        // Eventos Generales
        this.vistaPrincipal.getBtnAbrirFormularioCliente().addActionListener(e -> abrirFormularioRegistro(""));
        this.vistaPrincipal.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());
        this.vistaPrincipal.getBtnAbrirFormularioVenta().addActionListener(e -> abrirFormularioVenta());
        this.vistaPrincipal.getBtnBajaCliente().addActionListener(e -> cambiarEstadoCliente(false));
        this.vistaPrincipal.getBtnAltaCliente().addActionListener(e -> cambiarEstadoCliente(true));

        // Evento para editar cliente
        this.vistaPrincipal.getBtnEditarCliente().addActionListener(e -> abrirFormularioEdicionCliente());

        this.vistaPrincipal.getTablaClientes().setAutoCreateRowSorter(true);
        this.vistaPrincipal.getTablaProductos().setAutoCreateRowSorter(true);
        this.vistaPrincipal.getTablaVentas().setAutoCreateRowSorter(true);
        this.vistaPrincipal.getTablaReportes().setAutoCreateRowSorter(true);

        configurarBusqueda(this.vistaPrincipal.getBuscarClienteField(), this.vistaPrincipal.getBtnBuscarCliente(), this.vistaPrincipal.getTablaClientes());
        configurarBusqueda(this.vistaPrincipal.getBuscarProductoField(), this.vistaPrincipal.getBtnBuscarProducto(), this.vistaPrincipal.getTablaProductos());
        configurarBusqueda(this.vistaPrincipal.getBuscarVentaField(), this.vistaPrincipal.getBtnBuscarVenta(), this.vistaPrincipal.getTablaVentas());

        this.vistaPrincipal.getBtnBajaCliente().setVisible(false);
        this.vistaPrincipal.getBtnAltaCliente().setVisible(false);
        this.vistaPrincipal.getBtnEditarCliente().setVisible(false);

        // --- EXPORTAR A EXCEL MEDIANTE BOTÓN EXPLICITO ---
        this.vistaPrincipal.getBtnExportarReporte().addActionListener(e -> exportarTablaACSV(this.vistaPrincipal.getTablaReportes()));

        // Eventos de selección de tablas (Deseleccionar al hacer clic afuera)
        java.awt.event.MouseAdapter deseleccionador = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JTable tabla = vistaPrincipal.getTablaClientes();
                if (tabla.rowAtPoint(e.getPoint()) == -1) {
                    tabla.clearSelection();
                    vistaPrincipal.getBtnBajaCliente().setVisible(false);
                    vistaPrincipal.getBtnAltaCliente().setVisible(false);
                    vistaPrincipal.getBtnEditarCliente().setVisible(false);
                }
            }
        };

        this.vistaPrincipal.getTablaClientes().addMouseListener(deseleccionador);
        java.awt.Container parent = this.vistaPrincipal.getTablaClientes().getParent();
        if (parent instanceof javax.swing.JViewport) parent.addMouseListener(deseleccionador);

        // Mostrar botones de acción al seleccionar un cliente
        this.vistaPrincipal.getTablaClientes().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int f = this.vistaPrincipal.getTablaClientes().getSelectedRow();
                if (f != -1) {
                    boolean activo = this.vistaPrincipal.getTablaClientes().getValueAt(f, 5).toString().equalsIgnoreCase("Activo");
                    this.vistaPrincipal.getBtnBajaCliente().setVisible(activo);
                    this.vistaPrincipal.getBtnAltaCliente().setVisible(!activo);
                    this.vistaPrincipal.getBtnEditarCliente().setVisible(true);
                } else {
                    this.vistaPrincipal.getBtnBajaCliente().setVisible(false);
                    this.vistaPrincipal.getBtnAltaCliente().setVisible(false);
                    this.vistaPrincipal.getBtnEditarCliente().setVisible(false);
                }
            }
        });

        // Ver detalle de venta (Doble clic)
        this.vistaPrincipal.getTablaVentas().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = vistaPrincipal.getTablaVentas().getSelectedRow();
                    if (fila != -1) {
                        abrirDetalleVenta();
                    }
                }
            }
        });

        // --- EVENTO DEL BOTÓN REPORTES ---
        this.vistaPrincipal.getBtnGenerarReporte().addActionListener(e -> generarReporte());
    }

    // =======================================================
    private void generarReporte() {
        String strInicio = vistaPrincipal.getCbDiaInicio().getSelectedItem() + "/" +
                vistaPrincipal.getCbMesInicio().getSelectedItem() + "/" +
                vistaPrincipal.getCbAnioInicio().getSelectedItem();

        String strFin = vistaPrincipal.getCbDiaFin().getSelectedItem() + "/" +
                vistaPrincipal.getCbMesFin().getSelectedItem() + "/" +
                vistaPrincipal.getCbAnioFin().getSelectedItem();

        try {
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            java.time.LocalDate fechaInicio = java.time.LocalDate.parse(strInicio, fmt);
            java.time.LocalDate fechaFin = java.time.LocalDate.parse(strFin, fmt);

            int tipoReporte = vistaPrincipal.getComboTipoReporte().getSelectedIndex();

            if (tipoReporte == 0) {
                fechaFin = fechaInicio; // Si es resumen diario, inicio y fin son el mismo día
            }

            if (fechaInicio.isAfter(fechaFin)) {
                JOptionPane.showMessageDialog(vistaPrincipal, "La fecha de inicio no puede ser mayor a la fecha de fin.", "Fechas Inválidas", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // --- NUEVA LÓGICA DE VALIDACIÓN ---
            // Solo validamos que el rango (distancia entre inicio y fin) no supere los 6 meses
            // Ya no importa qué tan antigua sea la fecha de inicio.
            if (fechaInicio.plusMonths(6).isBefore(fechaFin)) {
                JOptionPane.showMessageDialog(vistaPrincipal, "El rango seleccionado supera los 6 meses. Por favor, seleccione un periodo más corto para no sobrecargar el sistema.", "Rango Excedido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.time.LocalDateTime inicioLDT = fechaInicio.atStartOfDay();
            java.time.LocalDateTime finLDT = fechaFin.atTime(23, 59, 59);

            DefaultTableModel modeloTabla = (DefaultTableModel) vistaPrincipal.getTablaReportes().getModel();
            modeloTabla.setRowCount(0);

            if (tipoReporte == 0) {
                reporteResumenDiario(inicioLDT, finLDT);
            } else if (tipoReporte == 1) {
                reporteTopProductos(inicioLDT, finLDT);
            } else if (tipoReporte == 2) {
                String categoriaSeleccionada = vistaPrincipal.getComboCategoriasReporte().getSelectedItem().toString();
                reporteRendimientoCategoria(inicioLDT, finLDT, categoriaSeleccionada);
            } else if (tipoReporte == 3) {
                reporteMejoresClientes(inicioLDT, finLDT);
            }

        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(vistaPrincipal, "La fecha seleccionada no existe en el calendario.", "Fecha Inválida", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vistaPrincipal, "Ocurrió un error al generar el reporte: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reporteRendimientoCategoria(java.time.LocalDateTime inicio, java.time.LocalDateTime fin, String categoria) {
        StoredProcedureQuery query = em.createStoredProcedureQuery("reporte_rendimiento_categoria");
        query.registerStoredProcedureParameter("p_vendedor_id", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_categoria", String.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_ini", java.time.LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_fin", java.time.LocalDateTime.class, ParameterMode.IN);

        query.setParameter("p_vendedor_id", this.vendedorLogueado.getId());
        query.setParameter("p_categoria", categoria);
        query.setParameter("p_ini", inicio);
        query.setParameter("p_fin", fin);

        List<Object[]> resultados = query.getResultList();
        DefaultTableModel modelo = (DefaultTableModel) vistaPrincipal.getTablaReportes().getModel();
        modelo.setColumnIdentifiers(new String[]{"Producto (" + categoria + ")", "Unidades Vendidas", "Ingresos Generados"});
        modelo.setRowCount(0);

        double granTotal = 0;
        for (Object[] fila : resultados) {
            double ingresos = ((Number) fila[2]).doubleValue();
            granTotal += ingresos;
            modelo.addRow(new Object[]{ fila[0], fila[1], String.format("$%.2f", ingresos) });
        }
        vistaPrincipal.getLblTotalReporte().setText(String.format("Total en Categoría: $%.2f", granTotal));
    }
    // EXPORTACIÓN EXCEL / CSV
    // =======================================================

    private void exportarTablaACSV(JTable tabla) {
        if (tabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(vistaPrincipal, "No hay datos en la tabla para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Reporte Excel (CSV)");
        fileChooser.setSelectedFile(new java.io.File("Nexcell_Reporte_" + System.currentTimeMillis() + ".csv"));

        if (fileChooser.showSaveDialog(vistaPrincipal) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile(), "UTF-8")) {
                // Escribir cabeceras
                for (int i = 0; i < tabla.getColumnCount(); i++) {
                    writer.print(tabla.getColumnName(i));
                    if (i < tabla.getColumnCount() - 1) writer.print(";");
                }
                writer.println();

                // Escribir datos
                for (int i = 0; i < tabla.getRowCount(); i++) {
                    for (int j = 0; j < tabla.getColumnCount(); j++) {
                        String val = tabla.getValueAt(i, j) != null ? tabla.getValueAt(i, j).toString() : "";
                        writer.print("\"" + val.replace("\"", "\"\"") + "\"");
                        if (j < tabla.getColumnCount() - 1) writer.print(";");
                    }
                    writer.println();
                }

                JOptionPane.showMessageDialog(vistaPrincipal, "Reporte exportado con éxito. Ya puedes abrirlo con Excel.", "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE);
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "REPORTES", "El Vendedor exportó datos a formato CSV.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(vistaPrincipal, "Error al guardar el archivo.", "Error Crítico", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    // =======================================================
    // REPORTES ESTADÍSTICOS
    // =======================================================

    private void reporteResumenDiario(java.time.LocalDateTime inicio, java.time.LocalDateTime fin) {
        StoredProcedureQuery query = em.createStoredProcedureQuery("reporte_resumen_diario");
        query.registerStoredProcedureParameter("p_vendedor_id", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_ini", java.time.LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_fin", java.time.LocalDateTime.class, ParameterMode.IN);

        query.setParameter("p_vendedor_id", this.vendedorLogueado.getId());
        query.setParameter("p_ini", inicio);
        query.setParameter("p_fin", fin);

        List<Object[]> resultados = query.getResultList();
        DefaultTableModel modelo = (DefaultTableModel) vistaPrincipal.getTablaReportes().getModel();
        modelo.setColumnIdentifiers(new String[]{"Fecha de Operación", "Tickets Emitidos", "Monto Facturado"});
        modelo.setRowCount(0);

        double granTotal = 0;
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Object[] fila : resultados) {
            java.sql.Date fechaSql = (java.sql.Date) fila[0];
            String fechaStr = fechaSql.toLocalDate().format(fmt);
            int tickets = ((Number) fila[1]).intValue();
            double total = ((Number) fila[2]).doubleValue();

            granTotal += total;
            modelo.addRow(new Object[]{fechaStr, tickets + " ventas", String.format("$%.2f", total)});
        }
        vistaPrincipal.getLblTotalReporte().setText(String.format("Facturación Periodo: $%.2f", granTotal));
    }

    private void reporteTopProductos(java.time.LocalDateTime inicio, java.time.LocalDateTime fin) {
        StoredProcedureQuery query = em.createStoredProcedureQuery("reporte_top_productos");
        query.registerStoredProcedureParameter("p_vendedor_id", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_ini", java.time.LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_fin", java.time.LocalDateTime.class, ParameterMode.IN);

        query.setParameter("p_vendedor_id", this.vendedorLogueado.getId());
        query.setParameter("p_ini", inicio);
        query.setParameter("p_fin", fin);

        List<Object[]> resultados = query.getResultList();
        DefaultTableModel modelo = (DefaultTableModel) vistaPrincipal.getTablaReportes().getModel();
        modelo.setColumnIdentifiers(new String[]{"Producto (Modelo)", "Categoría", "Unidades Vendidas", "Ingreso Generado"});
        modelo.setRowCount(0);

        double granTotal = 0;
        for (Object[] fila : resultados) {
            String producto = (String) fila[0];
            String categoria = (String) fila[1];
            int unidades = ((Number) fila[2]).intValue();
            double total = ((Number) fila[3]).doubleValue();

            granTotal += total;
            modelo.addRow(new Object[]{producto, categoria, unidades + " u.", String.format("$%.2f", total)});
        }
        vistaPrincipal.getLblTotalReporte().setText(String.format("Ingreso por estos productos: $%.2f", granTotal));
    }

    private void reporteMejoresClientes(java.time.LocalDateTime inicio, java.time.LocalDateTime fin) {
        StoredProcedureQuery query = em.createStoredProcedureQuery("reporte_mejores_clientes");
        query.registerStoredProcedureParameter("p_vendedor_id", Long.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_ini", java.time.LocalDateTime.class, ParameterMode.IN);
        query.registerStoredProcedureParameter("p_fin", java.time.LocalDateTime.class, ParameterMode.IN);

        query.setParameter("p_vendedor_id", this.vendedorLogueado.getId());
        query.setParameter("p_ini", inicio);
        query.setParameter("p_fin", fin);

        List<Object[]> resultados = query.getResultList();
        DefaultTableModel modelo = (DefaultTableModel) vistaPrincipal.getTablaReportes().getModel();
        modelo.setColumnIdentifiers(new String[]{"DNI Cliente", "Nombre Completo", "Compras", "Total Gastado"});
        modelo.setRowCount(0);

        for (Object[] fila : resultados) {
            String nombreCompleto = fila[1] + " " + fila[2];
            double gastado = ((Number) fila[4]).doubleValue();
            modelo.addRow(new Object[]{ fila[0], nombreCompleto, fila[3], String.format("$%.2f", gastado) });
        }
        vistaPrincipal.getLblTotalReporte().setText("Ranking de Fidelización (Clientes del Vendedor)");
    }


    // =======================================================
    // MÓDULO DE VENTAS (CARRITO Y CONFIRMACIÓN)
    // =======================================================
    private void abrirFormularioVenta() {
        try {
            vista.RegistroVentaUI ventanaVenta = new vista.RegistroVentaUI(this.vistaPrincipal);
            repositorio.ProductoRepository repoProd = new repositorio.ProductoRepository(this.em);
            List<modelo.Producto> productosDB = repoProd.listarTodos();

            ventanaVenta.getProductoBox().removeAllItems();
            ventanaVenta.getProductoBox().addItem("Seleccionar...");
            for (modelo.Producto p : productosDB) {
                if (p.isEstado() && p.getStock() > 0) {
                    ventanaVenta.getProductoBox().addItem(p.getId() + " - " + p.getNombre() + " (Stock: " + p.getStock() + ")");
                }
            }

            final modelo.Cliente[] clienteActual = {null};
            repositorio.ClienteRepository repoCli = new repositorio.ClienteRepository(this.em);

            ventanaVenta.getBtnVerificarCliente().addActionListener(e -> {
                String dni = ventanaVenta.getDniClienteField().getText().trim();
                if (dni.isEmpty()) {
                    JOptionPane.showMessageDialog(ventanaVenta, "Ingrese un DNI primero.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                modelo.Cliente c = repoCli.buscarPorDni(dni);
                if (c != null && c.isActivo()) {
                    clienteActual[0] = c;
                    ventanaVenta.getLblNombreCliente().setText("Cliente validado: " + c.getNombre() + " " + c.getApellido());
                    ventanaVenta.getLblNombreCliente().setForeground(new Color(40, 167, 69));
                } else {
                    if (JOptionPane.showConfirmDialog(ventanaVenta, "Cliente no encontrado. ¿Registrarlo?", "No encontrado", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        abrirFormularioRegistro(dni);
                        modelo.Cliente nuevoC = repoCli.buscarPorDni(dni);
                        if (nuevoC != null) {
                            clienteActual[0] = nuevoC;
                            ventanaVenta.getDniClienteField().setText(nuevoC.getDni());
                            ventanaVenta.getLblNombreCliente().setText("Cliente nuevo: " + nuevoC.getNombre() + " " + nuevoC.getApellido());
                            ventanaVenta.getLblNombreCliente().setForeground(new Color(40, 167, 69));
                        }
                    }
                }
            });

            // Control de visibilidad del botón eliminar producto en el carrito
            ventanaVenta.getTablaCarrito().getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    boolean hayFilaSeleccionada = ventanaVenta.getTablaCarrito().getSelectedRow() != -1;
                    ventanaVenta.getBtnEliminarDelCarrito().setVisible(hayFilaSeleccionada);
                }
            });

            // Eliminar producto con botón
            ventanaVenta.getBtnEliminarDelCarrito().addActionListener(e -> {
                int fila = ventanaVenta.getTablaCarrito().getSelectedRow();
                if (fila != -1) {
                    int confirmar = JOptionPane.showConfirmDialog(ventanaVenta, "¿Desea quitar este producto del carrito?", "Quitar Artículo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (confirmar == JOptionPane.YES_OPTION) {
                        DefaultTableModel mod = (DefaultTableModel) ventanaVenta.getTablaCarrito().getModel();
                        mod.removeRow(fila);

                        // Recalcular total
                        double tot = 0;
                        for (int i = 0; i < mod.getRowCount(); i++) tot += (double) mod.getValueAt(i, 4);
                        ventanaVenta.getLblTotalVenta().setText(String.format("Total: $%.2f", tot));
                    }
                } else {
                    JOptionPane.showMessageDialog(ventanaVenta, "Seleccione un producto del carrito para eliminarlo.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            });

            ventanaVenta.getBtnAgregarProducto().addActionListener(e -> {
                if (ventanaVenta.getProductoBox().getSelectedIndex() <= 0) return;
                String item = (String) ventanaVenta.getProductoBox().getSelectedItem();
                Long idProducto = Long.parseLong(item.split(" - ")[0]);
                modelo.Producto producto = repoProd.buscarPorId(idProducto);

                try {
                    int pedida = Integer.parseInt(ventanaVenta.getCantidadField().getText().trim());
                    if (pedida <= 0) throw new NumberFormatException();

                    DefaultTableModel mod = (DefaultTableModel) ventanaVenta.getTablaCarrito().getModel();
                    int cantEnCarro = 0;
                    for (int i = 0; i < mod.getRowCount(); i++) {
                        if (((Long) mod.getValueAt(i, 0)).equals(idProducto)) cantEnCarro += (int) mod.getValueAt(i, 3);
                    }

                    int totalRequerido = pedida + cantEnCarro;

                    if (totalRequerido > producto.getStock()) {
                        JOptionPane.showMessageDialog(ventanaVenta, "Stock insuficiente. Solo cuentas con " + producto.getStock() + " unidades disponibles.", "Error de Stock", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int stockRestante = producto.getStock() - totalRequerido;
                    if (stockRestante <= 5) {
                        JOptionPane.showMessageDialog(ventanaVenta, "Atención: Al confirmar esta venta, solo quedarán " + stockRestante + " unidades de '" + producto.getNombre() + "' en inventario.", "Alerta de Stock Crítico", JOptionPane.WARNING_MESSAGE);
                    }

                    double sub = pedida * producto.getPrecio();
                    mod.addRow(new Object[]{ producto.getId(), producto.getNombre(), producto.getPrecio(), pedida, sub });

                    double tot = 0;
                    for (int i = 0; i < mod.getRowCount(); i++) tot += (double) mod.getValueAt(i, 4);

                    ventanaVenta.getLblTotalVenta().setText(String.format("Total: $%.2f", tot));
                    ventanaVenta.getProductoBox().setSelectedIndex(0);
                    ventanaVenta.getCantidadField().setText("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ventanaVenta, "Cantidad inválida.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            ventanaVenta.getBtnConfirmarVenta().addActionListener(e -> {
                if (clienteActual[0] == null) {
                    JOptionPane.showMessageDialog(ventanaVenta, "Debe asociar un cliente antes de facturar.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                DefaultTableModel mod = (DefaultTableModel) ventanaVenta.getTablaCarrito().getModel();
                if (mod.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(ventanaVenta, "El carrito está vacío.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                StringBuilder resumen = new StringBuilder();
                resumen.append("--- TICKET DE VENTA - NEXCELL ---\n");
                resumen.append("Fecha: ").append(java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
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

                int confirmacion = JOptionPane.showConfirmDialog(ventanaVenta, resumen.toString() + "\n¿Desea confirmar e impactar esta venta en el sistema?", "Revisión Final de la Venta", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (confirmacion != JOptionPane.YES_OPTION) {
                    return;
                }

                boolean exito = procesarCheckout(vendedorLogueado.getId(), clienteActual[0].getDni(), totResumen, mod);

                if (exito) {
                    try {
                        String nombreArchivo = "Ticket_Venta_" + System.currentTimeMillis() + ".txt";
                        java.io.FileWriter writer = new java.io.FileWriter(nombreArchivo);
                        writer.write(resumen.toString());
                        writer.close();
                        JOptionPane.showMessageDialog(ventanaVenta, "¡Venta registrada exitosamente en milisegundos!\nSe generó el comprobante: " + nombreArchivo, "Venta Exitosa", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception exio) {
                        JOptionPane.showMessageDialog(ventanaVenta, "La venta se guardó, pero hubo un error al crear el archivo de texto.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    }

                    cargarTablaVentas();
                    cargarTablaProductos();
                    ventanaVenta.dispose();
                }  else {
                    JOptionPane.showMessageDialog(ventanaVenta, "Error interno en la base de datos al procesar la venta.", "Error Crítico", JOptionPane.ERROR_MESSAGE);
                }
            });

            ventanaVenta.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vistaPrincipal, "Error al inicializar módulo de ventas: " + ex.getMessage());
        }
    }

    private void abrirFormularioRegistro(String dniPreCargado) {
        vista.RegistroClienteUI ven = new vista.RegistroClienteUI(this.vistaPrincipal);

        ven.getBtnCancelar().addActionListener(e -> ven.dispose());

        if (!dniPreCargado.isEmpty()) {
            ven.getTxtDni().setText(dniPreCargado);
            ven.getTxtDni().setEditable(false);
        }

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

            java.time.LocalDate fechaNac = null;
            try {
                int d = (int) ven.getCbDia().getSelectedItem();
                int m = Integer.parseInt(ven.getCbMes().getSelectedItem().toString());
                int a = (int) ven.getCbAnio().getSelectedItem();
                fechaNac = java.time.LocalDate.of(a, m, d);
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
                JOptionPane.showMessageDialog(vistaPrincipal, "Cliente registrado con éxito.");

            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "CLIENTES",
                    "Error al registrar cliente: " + ex.getMessage());
            }
        });
        ven.setVisible(true);
    }

    // =======================================================
    // MÓDULO DE EDICIÓN DE CLIENTES
    // =======================================================
    // =======================================================
    // MÓDULO DE EDICIÓN DE CLIENTES
    // =======================================================
    private void abrirFormularioEdicionCliente() {
        int fila = vistaPrincipal.getTablaClientes().getSelectedRow();
        if (fila == -1) return;

        // Extraemos el DNI de la fila seleccionada y buscamos el cliente real en la Base de Datos
        String dni = vistaPrincipal.getTablaClientes().getValueAt(fila, 0).toString();
        modelo.Cliente cliente = em.find(modelo.Cliente.class, dni);
        if (cliente == null) return;

        // Reciclamos la UI de Registro
        vista.RegistroClienteUI ven = new vista.RegistroClienteUI(this.vistaPrincipal);
        ven.setTitle("Actualizando dirección de: " + cliente.getNombre() + " " + cliente.getApellido());
        ven.getBtnGuardarCliente().setText("Actualizar Dirección");

        // --- PRE-CARGAMOS Y BLOQUEAMOS LOS DATOS NO EDITABLES ---
        ven.getTxtDni().setText(cliente.getDni());
        ven.getTxtDni().setEditable(false);

        ven.getTxtNombre().setText(cliente.getNombre());
        ven.getTxtNombre().setEditable(false);

        ven.getTxtApellido().setText(cliente.getApellido());
        ven.getTxtApellido().setEditable(false);

        ven.getTxtTelefono().setText(cliente.getTelefono());
        ven.getTxtTelefono().setEditable(false);

        ven.getTxtEmail().setText(cliente.getEmail());
        ven.getTxtEmail().setEditable(false);

        if (cliente.getFechaNacimiento() != null) {
            ven.getCbDia().setSelectedItem(cliente.getFechaNacimiento().getDayOfMonth());
            ven.getCbMes().setSelectedIndex(cliente.getFechaNacimiento().getMonthValue() - 1);
            ven.getCbAnio().setSelectedItem(cliente.getFechaNacimiento().getYear());
        }
        // Bloqueamos los combos de la fecha
        ven.getCbDia().setEnabled(false);
        ven.getCbMes().setEnabled(false);
        ven.getCbAnio().setEnabled(false);

        // --- PRE-CARGAMOS LA DIRECCIÓN (ÚNICOS CAMPOS EDITABLES) ---
        if (cliente.getDireccion() != null) {
            ven.getTxtCalle().setText(cliente.getDireccion().getCalle());
            ven.getTxtAltura().setText(cliente.getDireccion().getAltura());
            ven.getTxtCiudad().setText(cliente.getDireccion().getCiudad());
            ven.getCbProvincia().setSelectedItem(cliente.getDireccion().getProvincia());
        }

        ven.getBtnCancelar().addActionListener(e -> ven.dispose());

        // --- LÓGICA DE ACTUALIZACIÓN (SÓLO DIRECCIÓN) ---
        ven.getBtnGuardarCliente().addActionListener(e -> {
            ven.limpiarErrores();

            String calle = ven.getTxtCalle().getText().trim();
            String altura = ven.getTxtAltura().getText().trim();
            String ciudad = ven.getTxtCiudad().getText().trim();

            try {
                em.getTransaction().begin();

                // Solo afectamos el objeto Dirección del cliente
                if (cliente.getDireccion() == null) {
                    cliente.setDireccion(new modelo.Direccion(calle, altura, ciudad, ven.getCbProvincia().getSelectedItem().toString()));
                } else {
                    cliente.getDireccion().setCalle(calle);
                    cliente.getDireccion().setAltura(altura);
                    cliente.getDireccion().setCiudad(ciudad);
                    cliente.getDireccion().setProvincia(ven.getCbProvincia().getSelectedItem().toString());
                }

                // Impactamos los cambios en MySQL
                em.merge(cliente);
                em.getTransaction().commit();

                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "CLIENTES",
                    "El Vendedor '" + vendedorLogueado.getUsername() + "' modificó la dirección del cliente DNI: " + dni);

                cargarTablaClientes();
                ven.dispose();
                JOptionPane.showMessageDialog(vistaPrincipal, "Dirección actualizada correctamente.", "Edición Exitosa", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "CLIENTES", "Error al editar cliente: " + ex.getMessage());
                JOptionPane.showMessageDialog(vistaPrincipal, "Error al actualizar la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        ven.setVisible(true);
    }

    private void cargarTablaClientes() {
        List<modelo.Cliente> cls = new repositorio.ClienteRepository(this.em).listarTodos();
        DefaultTableModel mod = (DefaultTableModel) vistaPrincipal.getTablaClientes().getModel();
        mod.setRowCount(0);
        for (modelo.Cliente c : cls) mod.addRow(new Object[]{ c.getDni(), c.getNombre(), c.getApellido(), c.getTelefono(), c.getEmail(), c.isActivo() ? "Activo" : "Inactivo" });
    }

    private void cambiarEstadoCliente(boolean act) {
        int f = vistaPrincipal.getTablaClientes().getSelectedRow();
        if (f == -1) return;
        if (JOptionPane.showConfirmDialog(vistaPrincipal, "¿Seguro?", act ? "Reactivar" : "Dar Baja", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            String dniCliente = vistaPrincipal.getTablaClientes().getValueAt(f, 0).toString();

            try {
                repositorio.ClienteRepository repo = new repositorio.ClienteRepository(this.em);
                modelo.Cliente cliente = repo.buscarPorDni(dniCliente);

                if (cliente != null) {
                    em.getTransaction().begin();
                    cliente.setActivo(act);
                    repo.actualizar(cliente);
                    em.getTransaction().commit();

                    vistaPrincipal.getTablaClientes().setValueAt(act ? "Activo" : "Inactivo", f, 5);
                    vistaPrincipal.getTablaClientes().clearSelection();

                    utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ALERTA, "CLIENTES",
                        "El Vendedor '" + vendedorLogueado.getUsername() + "' cambió el estado del cliente DNI " + dniCliente + " a " + (act ? "Activo" : "Inactivo"));
                }
            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "CLIENTES", "Error al cambiar estado: " + ex.getMessage());
            }
        }
    }

    private void cargarTablaProductos() {
        List<modelo.Producto> prs = new repositorio.ProductoRepository(this.em).listarTodos();
        DefaultTableModel mod = (DefaultTableModel) vistaPrincipal.getTablaProductos().getModel();
        mod.setRowCount(0);
        for (modelo.Producto p : prs) mod.addRow(new Object[]{ p.getId(), p.getCategoria(), p.getNombre(), p.getStock(), String.format("$%.2f", p.getPrecio()) });
    }

    private void cargarTablaVentas() {
        try {
            List<modelo.Venta> vts = new repositorio.VentaRepository(this.em).listarPorVendedor(this.vendedorLogueado);
            DefaultTableModel mod = (DefaultTableModel) vistaPrincipal.getTablaVentas().getModel();
            mod.setRowCount(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (modelo.Venta v : vts) {
                int cant = v.getDetalles().size();
                String desc = cant == 1 ? v.getDetalles().get(0).getProducto().getNombre() : cant + " art. (Varios)";
                mod.addRow(new Object[]{ v.getId(), v.getFecha().format(fmt), v.getCliente().getDni(), desc, String.format("$%.2f", v.getTotal()) });
            }
        } catch (Exception e) {}
    }

    private void cerrarSesion() {
        if (JOptionPane.showConfirmDialog(vistaPrincipal, "¿Salir?", "Cerrar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "SEGURIDAD",
                "El Vendedor '" + vendedorLogueado.getUsername() + "' cerró sesión.");
            vistaPrincipal.dispose();
            LoginUI ven = new LoginUI();
            new LoginController(ven, this.em);
            ven.setVisible(true);
        }
    }

    private void configurarBusqueda(JTextField campo, JButton boton, JTable tabla) {
        boton.addActionListener(e -> aplicarFiltro(campo, tabla));
        campo.addActionListener(e -> aplicarFiltro(campo, tabla));
        campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(campo, tabla); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(campo, tabla); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(campo, tabla); }
        });
    }

    private void aplicarFiltro(JTextField campo, JTable tabla) {
        String texto = campo.getText().trim();
        javax.swing.table.TableRowSorter<?> sorter = (javax.swing.table.TableRowSorter<?>) tabla.getRowSorter();
        if (sorter != null) {
            if (texto.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
            }
        }
    }

    private void abrirDetalleVenta() {
        int fila = vistaPrincipal.getTablaVentas().getSelectedRow();
        Long idVenta = (Long) vistaPrincipal.getTablaVentas().getValueAt(fila, 0);

        modelo.Venta venta = em.find(modelo.Venta.class, idVenta);

        JDialog dialogo = new JDialog(vistaPrincipal, "Detalle de Venta #" + venta.getId(), true);
        dialogo.setSize(550, 450);
        dialogo.setLocationRelativeTo(vistaPrincipal);
        dialogo.setLayout(new BorderLayout(15, 15));

        JPanel panelInfo = new JPanel(new GridLayout(4, 1, 5, 5));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        panelInfo.add(new JLabel("Cliente: " + venta.getCliente().getNombre() + " " + venta.getCliente().getApellido() + " (DNI: " + venta.getCliente().getDni() + ")"));
        panelInfo.add(new JLabel("Atendido por: " + venta.getVendedor().getUsername()));
        panelInfo.add(new JLabel("Fecha de operación: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        dialogo.add(panelInfo, BorderLayout.NORTH);

        String[] columnas = {"Producto", "Cantidad", "Subtotal"};
        DefaultTableModel modDetalle = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        for (modelo.DetalleVenta dv : venta.getDetalles()) {
            modDetalle.addRow(new Object[]{
                dv.getProducto().getNombre(),
                dv.getCantidad() + " u.",
                String.format("$%.2f", dv.getSubtotal())
            });
        }

        JTable tablaDetalles = new JTable(modDetalle);
        tablaDetalles.setRowHeight(30);
        JScrollPane scroll = new JScrollPane(tablaDetalles);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        dialogo.add(scroll, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JLabel lblTotal = new JLabel("TOTAL ABONADO: $" + String.format("%.2f", venta.getTotal()));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(new Color(40, 167, 69));

        JButton btnPdf = new JButton("Descargar Comprobante PDF");
        btnPdf.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPdf.addActionListener(e -> generarComprobantePDF(venta));

        panelInferior.add(lblTotal, BorderLayout.WEST);
        panelInferior.add(btnPdf, BorderLayout.EAST);
        dialogo.add(panelInferior, BorderLayout.SOUTH);

        dialogo.setVisible(true);
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

            JOptionPane.showMessageDialog(vistaPrincipal, "Comprobante PDF generado y guardado con éxito como:\n" + nombreArchivo, "PDF Generado", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(vistaPrincipal, "Error al generar el PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean procesarCheckout(Long idVendedor, String dniCliente, double totalVenta, DefaultTableModel modeloCarrito) {
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

            query.registerStoredProcedureParameter("p_vendedor_id", Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_cliente_dni", String.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_total", Double.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("p_carrito_json", String.class, ParameterMode.IN);

            query.setParameter("p_vendedor_id", idVendedor);
            query.setParameter("p_cliente_dni", dniCliente);
            query.setParameter("p_total", totalVenta);
            query.setParameter("p_carrito_json", jsonCarrito.toString());

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
}
