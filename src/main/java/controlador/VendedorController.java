package controlador;

import vista.VendedorUI;
import vista.RegistroClienteUI;
import vista.RegistroVentaUI;
import vista.LoginUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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

        // NUEVO: Evento del botón Reportes
        this.vistaPrincipal.getBtnGenerarReporte().addActionListener(e -> generarReporte());

        // Ordenadores y clics fuera de tabla
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
                    if ((pedida + cantEnCarro) > producto.getStock()) {
                        JOptionPane.showMessageDialog(ventanaVenta, "Stock insuficiente.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
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
                    JOptionPane.showMessageDialog(ventanaVenta, "Debe asociar un cliente.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                DefaultTableModel mod = (DefaultTableModel) ventanaVenta.getTablaCarrito().getModel();
                if (mod.getRowCount() == 0) return;

                try {
                    em.getTransaction().begin();
                    modelo.Venta nueva = new modelo.Venta();
                    nueva.setCliente(clienteActual[0]);
                    nueva.setVendedor(this.vendedorLogueado);
                    nueva.setFecha(java.time.LocalDateTime.now());
                    double tot = 0;

                    for (int i = 0; i < mod.getRowCount(); i++) {
                        Long idP = (Long) mod.getValueAt(i, 0);
                        int c = (int) mod.getValueAt(i, 3);
                        double s = (double) mod.getValueAt(i, 4);

                        modelo.Producto p = em.find(modelo.Producto.class, idP);
                        p.setStock(p.getStock() - c);
                        em.merge(p);

                        modelo.DetalleVenta det = new modelo.DetalleVenta();
                        det.setProducto(p);
                        det.setCantidad(c);
                        det.setSubtotal(s);
                        nueva.agregarDetalle(det);
                        tot += s;
                    }

                    nueva.setTotal(tot);
                    new repositorio.VentaRepository(em).guardar(nueva);
                    em.getTransaction().commit();

                    cargarTablaVentas();
                    cargarTablaProductos();
                    JOptionPane.showMessageDialog(ventanaVenta, "¡Venta confirmada!\nTotal: $" + String.format("%.2f", tot));
                    ventanaVenta.dispose();
                } catch (Exception ex) {
                    if (em.getTransaction().isActive()) em.getTransaction().rollback();
                    JOptionPane.showMessageDialog(ventanaVenta, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            ventanaVenta.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vistaPrincipal, "Error: " + ex.getMessage());
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
                cargarTablaClientes();
                ven.dispose();
            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
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
            vistaPrincipal.getTablaClientes().setValueAt(act ? "Activo" : "Inactivo", f, 5);
            vistaPrincipal.getTablaClientes().clearSelection();
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
}
