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

        this.vistaPrincipal.getTablaClientes().setAutoCreateRowSorter(true);
        this.vistaPrincipal.getTablaProductos().setAutoCreateRowSorter(true);
        this.vistaPrincipal.getTablaVentas().setAutoCreateRowSorter(true);
        this.vistaPrincipal.getTablaReportes().setAutoCreateRowSorter(true); // Ordenar reportes

        configurarBusqueda(this.vistaPrincipal.getBuscarClienteField(), this.vistaPrincipal.getBtnBuscarCliente(), this.vistaPrincipal.getTablaClientes());
        configurarBusqueda(this.vistaPrincipal.getBuscarProductoField(), this.vistaPrincipal.getBtnBuscarProducto(), this.vistaPrincipal.getTablaProductos());
        configurarBusqueda(this.vistaPrincipal.getBuscarVentaField(), this.vistaPrincipal.getBtnBuscarVenta(), this.vistaPrincipal.getTablaVentas());

        this.vistaPrincipal.getBtnBajaCliente().setVisible(false);
        this.vistaPrincipal.getBtnAltaCliente().setVisible(false);
        this.vistaPrincipal.getBtnBajaCliente().setVisible(false);
        this.vistaPrincipal.getBtnAltaCliente().setVisible(false);


        java.awt.event.MouseAdapter deseleccionador = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JTable tabla = vistaPrincipal.getTablaClientes();
                if (tabla.rowAtPoint(e.getPoint()) == -1) {
                    tabla.clearSelection();
                    vistaPrincipal.getBtnBajaCliente().setVisible(false);
                    vistaPrincipal.getBtnAltaCliente().setVisible(false);
                }
            }
        };

        this.vistaPrincipal.getTablaClientes().addMouseListener(deseleccionador);
        java.awt.Container parent = this.vistaPrincipal.getTablaClientes().getParent();
        if (parent instanceof javax.swing.JViewport) parent.addMouseListener(deseleccionador);

        this.vistaPrincipal.getTablaClientes().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int f = this.vistaPrincipal.getTablaClientes().getSelectedRow();
                if (f != -1) {
                    boolean activo = this.vistaPrincipal.getTablaClientes().getValueAt(f, 5).toString().equalsIgnoreCase("Activo");
                    this.vistaPrincipal.getBtnBajaCliente().setVisible(activo);
                    this.vistaPrincipal.getBtnAltaCliente().setVisible(!activo);
                } else {
                    this.vistaPrincipal.getBtnBajaCliente().setVisible(false);
                    this.vistaPrincipal.getBtnAltaCliente().setVisible(false);
                }
            }
        });

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
    }


    // Este metodo funciona para hacer los reportes estadisticos del vendedor
    private void generarReporte() {
        // Armamos el String de fecha obteniendo los valores seleccionados en los combobox
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
            java.time.LocalDate hoy = java.time.LocalDate.now();

            // Validación Lógica Básica
            if (fechaInicio.isAfter(fechaFin)) {
                JOptionPane.showMessageDialog(vistaPrincipal, "La fecha de inicio no puede ser mayor a la fecha de fin.", "Fechas Inválidas", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // REGLA: Máximo 6 meses de antigüedad permitida
            java.time.LocalDate limiteAntiguedad = hoy.minusMonths(6);
            if (fechaInicio.isBefore(limiteAntiguedad)) {
                JOptionPane.showMessageDialog(vistaPrincipal, "Por políticas del sistema, solo puedes generar reportes con un máximo de 6 meses de antigüedad.\nLímite permitido: " + limiteAntiguedad.format(fmt), "Límite Excedido", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Traemos las ventas DIRECTAMENTE DE LA BD (Consistente con lo que ve el Vendedor)
            java.time.LocalDateTime inicioLDT = fechaInicio.atStartOfDay();
            java.time.LocalDateTime finLDT = fechaFin.atTime(23, 59, 59);

            List<modelo.Venta> ventas = em.createQuery(
                    "SELECT DISTINCT v FROM Venta v LEFT JOIN FETCH v.detalles WHERE v.vendedor = :vend AND v.fecha >= :ini AND v.fecha <= :fin ORDER BY v.fecha ASC", modelo.Venta.class)
                .setParameter("vend", this.vendedorLogueado)
                .setParameter("ini", inicioLDT)
                .setParameter("fin", finLDT)
                .getResultList();

            int tipoReporte = vistaPrincipal.getComboTipoReporte().getSelectedIndex();
            DefaultTableModel modeloTabla = (DefaultTableModel) vistaPrincipal.getTablaReportes().getModel();
            modeloTabla.setRowCount(0); // Limpiar tabla

            double granTotal = 0;

            if (tipoReporte == 0) {
                // --- 1. REPORTE DE FACTURACIÓN DIARIA ---
                modeloTabla.setColumnIdentifiers(new String[]{"Fecha de Operación", "Tickets Emitidos", "Monto Facturado"});

                java.util.Map<java.time.LocalDate, Double> facturacionPorDia = new java.util.TreeMap<>();
                java.util.Map<java.time.LocalDate, Integer> ventasPorDia = new java.util.HashMap<>();

                for (modelo.Venta v : ventas) {
                    java.time.LocalDate dia = v.getFecha().toLocalDate();
                    facturacionPorDia.put(dia, facturacionPorDia.getOrDefault(dia, 0.0) + v.getTotal());
                    ventasPorDia.put(dia, ventasPorDia.getOrDefault(dia, 0) + 1);
                    granTotal += v.getTotal();
                }

                for (java.util.Map.Entry<java.time.LocalDate, Double> entry : facturacionPorDia.entrySet()) {
                    modeloTabla.addRow(new Object[]{
                        entry.getKey().format(fmt),
                        ventasPorDia.get(entry.getKey()) + " ventas",
                        String.format("$%.2f", entry.getValue())
                    });
                }
                vistaPrincipal.getLblTotalReporte().setText(String.format("Facturación Periodo: $%.2f", granTotal));

            } else {
                // --- 2. REPORTE TOP PRODUCTOS MÁS VENDIDOS ---
                modeloTabla.setColumnIdentifiers(new String[]{"Producto (Modelo)", "Unidades Vendidas", "Ingreso Generado"});

                java.util.Map<String, Integer> cantPorProd = new java.util.HashMap<>();
                java.util.Map<String, Double> montoPorProd = new java.util.HashMap<>();

                for (modelo.Venta v : ventas) {
                    for (modelo.DetalleVenta det : v.getDetalles()) {
                        String nombreProd = det.getProducto().getNombre();
                        cantPorProd.put(nombreProd, cantPorProd.getOrDefault(nombreProd, 0) + det.getCantidad());
                        montoPorProd.put(nombreProd, montoPorProd.getOrDefault(nombreProd, 0.0) + det.getSubtotal());
                        granTotal += det.getSubtotal();
                    }
                }

                java.util.List<java.util.Map.Entry<String, Integer>> listaTop = new java.util.ArrayList<>(cantPorProd.entrySet());
                listaTop.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                for (java.util.Map.Entry<String, Integer> entry : listaTop) {
                    modeloTabla.addRow(new Object[]{
                        entry.getKey(),
                        entry.getValue() + " u.",
                        String.format("$%.2f", montoPorProd.get(entry.getKey()))
                    });
                }
                vistaPrincipal.getLblTotalReporte().setText(String.format("Ingreso por estos productos: $%.2f", granTotal));
            }

        } catch (java.time.format.DateTimeParseException ex) {
            // Este catch atrapa combinaciones imposibles (Ej: 31 de Febrero)
            JOptionPane.showMessageDialog(vistaPrincipal, "La fecha seleccionada no existe en el calendario. Verifica los días.", "Fecha Inválida", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vistaPrincipal, "Ocurrió un error al generar el reporte: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
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

            // ACA SE ENCUENTRA LA ALERTA DE STOCK AL AGREGAR PRODUCTO
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

                    // Validación principal: Supera el stock total
                    if (totalRequerido > producto.getStock()) {
                        JOptionPane.showMessageDialog(ventanaVenta, "Stock insuficiente. Solo cuentas con " + producto.getStock() + " unidades disponibles.", "Error de Stock", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Alerta secundaria: Quedan pocas unidades que serian 5
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

            // ACA SE HACE LA DOBLE VALIDACIÓN Y EXPORTACIÓN DE TICKET
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

                // ACA SE ENCUENTRA EL TICKET :D
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

                // Lanzamos la ventana emergente de validación
                int confirmacion = JOptionPane.showConfirmDialog(ventanaVenta, resumen.toString() + "\n¿Desea confirmar e impactar esta venta en el sistema?", "Revisión Final de la Venta", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (confirmacion != JOptionPane.YES_OPTION) {
                    return;
                }

                // --- NUEVA LÓGICA DE GUARDADO DELEGADA A MYSQL ---
                boolean exito = procesarCheckout(vendedorLogueado.getId(), clienteActual[0].getDni(), totResumen, mod);

                if (exito) {
                    // Generación del archivo físico (.txt)
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
                JOptionPane.showMessageDialog(ventanaVenta, "Error interno en la base de datos al procesar la venta. Se revirtieron los cambios.", "Error Crítico", JOptionPane.ERROR_MESSAGE);
            }
        });

        ventanaVenta.setVisible(true);

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(vistaPrincipal, "Error al inicializar módulo de ventas: " + ex.getMessage());
    }
}

    private void abrirFormularioRegistro(String dniPreCargado) {
        vista.RegistroClienteUI ven = new vista.RegistroClienteUI(this.vistaPrincipal);
        if (!dniPreCargado.isEmpty()) {
            ven.getDniClienteField().setText(dniPreCargado);
            ven.getDniClienteField().setEditable(false);
        }
        ven.getBtnGuardarCliente().addActionListener(e -> {
            String d = ven.getDniClienteField().getText().trim();
            String n = ven.getNombreClienteField().getText().trim();
            String a = ven.getApellidoClienteField().getText().trim();
            if (d.isEmpty() || n.isEmpty() || a.isEmpty()) return;
            try {
                repositorio.ClienteRepository repo = new repositorio.ClienteRepository(this.em);
                if (repo.existeDni(d)) return;
                em.getTransaction().begin();
                repo.guardar(new modelo.Cliente(d, n, a, ven.getTelefonoClienteField().getText().trim(), ven.getEmailClienteField().getText().trim()));
                em.getTransaction().commit();

                // ACA SE REGISTRA EL NUEVO LOG
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "CLIENTES",
                    "El Vendedor '" + vendedorLogueado.getUsername() + "' registró un nuevo cliente con DNI: " + d);

                cargarTablaClientes();
                ven.dispose();
            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                // ACA SE REGISTRA EL NUEVO LOG
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "CLIENTES",
                    "Error al registrar cliente DNI " + d + " por el Vendedor '" + vendedorLogueado.getUsername() + "'.");
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

            // Obtenemos el DNI de la fila seleccionada
            String dniCliente = vistaPrincipal.getTablaClientes().getValueAt(f, 0).toString();

            try {
                // Buscamos y actualizamos al cliente en la base de datos
                repositorio.ClienteRepository repo = new repositorio.ClienteRepository(this.em);
                modelo.Cliente cliente = repo.buscarPorDni(dniCliente);

                if (cliente != null) {
                    em.getTransaction().begin();
                    cliente.setActivo(act);
                    repo.actualizar(cliente);
                    em.getTransaction().commit();

                    // ctualizamos la pantalla
                    vistaPrincipal.getTablaClientes().setValueAt(act ? "Activo" : "Inactivo", f, 5);
                    vistaPrincipal.getTablaClientes().clearSelection();

                    //Aca se registra el nuevo LOG
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

            // ACA SE REGISTRA EL NUEVO LOG
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "SEGURIDAD",
                "El Vendedor '" + vendedorLogueado.getUsername() + "' cerró sesión.");

            vistaPrincipal.dispose();
            LoginUI ven = new LoginUI();
            new LoginController(ven, this.em);
            ven.setVisible(true);
        }
    }

    private void configurarBusqueda(JTextField campo, JButton boton, JTable tabla) {
        // Buscar al hacer clic en el botón
        boton.addActionListener(e -> aplicarFiltro(campo, tabla));

        // Buscar al presionar Enter en el teclado
        campo.addActionListener(e -> aplicarFiltro(campo, tabla));

        // Buscar en tiempo real mientras el usuario escribe
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
                sorter.setRowFilter(null); // Quita el filtro si está vacío
            } else {
                // (?i) permite buscar sin distinguir mayúsculas de minúsculas
                // Pattern.quote evita errores si el usuario ingresa caracteres raros
                sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
            }
        }
    }
    // =======================================================
    // VISUALIZADOR DE VENTAS Y EXPORTACIÓN PDF
    // =======================================================
    private void abrirDetalleVenta() {
        int fila = vistaPrincipal.getTablaVentas().getSelectedRow();
        Long idVenta = (Long) vistaPrincipal.getTablaVentas().getValueAt(fila, 0);

        // Buscamos la venta completa en la BD
        modelo.Venta venta = em.find(modelo.Venta.class, idVenta);

        // Creamos una ventana emergente (Dialog)
        JDialog dialogo = new JDialog(vistaPrincipal, "Detalle de Venta #" + venta.getId(), true);
        dialogo.setSize(550, 450);
        dialogo.setLocationRelativeTo(vistaPrincipal);
        dialogo.setLayout(new BorderLayout(15, 15));

        // --- PANEL SUPERIOR: Info de la factura ---
        JPanel panelInfo = new JPanel(new GridLayout(4, 1, 5, 5));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        panelInfo.add(new JLabel("Cliente: " + venta.getCliente().getNombre() + " " + venta.getCliente().getApellido() + " (DNI: " + venta.getCliente().getDni() + ")"));
        panelInfo.add(new JLabel("Atendido por: " + venta.getVendedor().getUsername()));
        panelInfo.add(new JLabel("Fecha de operación: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        dialogo.add(panelInfo, BorderLayout.NORTH);

        // --- PANEL CENTRAL: Tabla de artículos ---
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

        // --- PANEL INFERIOR: Total y Botón PDF ---
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

            // Titulo
            com.itextpdf.text.Paragraph titulo = new com.itextpdf.text.Paragraph("NEXCELL - TECNOLOGIA", fontTitulo);
            titulo.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            documento.add(titulo);
            documento.add(new com.itextpdf.text.Paragraph("-------------------------------------------------------------------------------------------------------"));
            documento.add(new com.itextpdf.text.Paragraph("\n"));

            // Datos de la venta
            documento.add(new com.itextpdf.text.Paragraph("N° de Factura: " + String.format("%06d", venta.getId()), fontNormal));
            documento.add(new com.itextpdf.text.Paragraph("Fecha: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fontNormal));
            documento.add(new com.itextpdf.text.Paragraph("Vendedor: " + venta.getVendedor().getUsername(), fontNormal));
            documento.add(new com.itextpdf.text.Paragraph("Cliente: " + venta.getCliente().getNombre() + " " + venta.getCliente().getApellido() + " (DNI: " + venta.getCliente().getDni() + ")", fontNormal));
            documento.add(new com.itextpdf.text.Paragraph("\n"));

            // Tabla de Productos
            com.itextpdf.text.pdf.PdfPTable tabla = new com.itextpdf.text.pdf.PdfPTable(3); // 3 Columnas
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{50f, 20f, 30f}); // Proporción de ancho de columnas

            // Cabeceras de la tabla
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Producto", fontSubtitulo)));
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Cantidad", fontSubtitulo)));
            tabla.addCell(new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Phrase("Subtotal", fontSubtitulo)));

            // Rellenar con los productos reales
            for (modelo.DetalleVenta d : venta.getDetalles()) {
                tabla.addCell(new com.itextpdf.text.Phrase(d.getProducto().getNombre(), fontNormal));
                tabla.addCell(new com.itextpdf.text.Phrase(d.getCantidad() + " u.", fontNormal));
                tabla.addCell(new com.itextpdf.text.Phrase(String.format("$%.2f", d.getSubtotal()), fontNormal));
            }
            documento.add(tabla);

            // Total Final
            com.itextpdf.text.Paragraph total = new com.itextpdf.text.Paragraph("\nTOTAL ABONADO: $" + String.format("%.2f", venta.getTotal()), fontTitulo);
            total.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
            documento.add(total);
            documento.add(new com.itextpdf.text.Paragraph("\n\n*** Gracias por su compra en Nexcell ***", fontNormal));

            documento.close();

            JOptionPane.showMessageDialog(vistaPrincipal, "Comprobante PDF generado y guardado con éxito como:\n" + nombreArchivo, "PDF Generado", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(vistaPrincipal, "Error al generar el PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    // Cambiamos "Long idCliente" por "String dniCliente"
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

            // --- NUEVO: REGISTRO DE LOG ---
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "VENTAS",
                "El Vendedor '" + vendedorLogueado.getUsername() + "' registró exitosamente una venta por $" + totalVenta + " al DNI: " + dniCliente);

            return true;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            // --- NUEVO: REGISTRO DE LOG ---
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "VENTAS",
                "Fallo al registrar venta del Vendedor '" + vendedorLogueado.getUsername() + "'. Motivo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
