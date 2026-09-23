package controlador;

import vista.VendedorUI;
import vista.RegistroClienteUI;
import vista.RegistroVentaUI;
import vista.LoginUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.awt.Color;

public class VendedorController {

    private VendedorUI vistaPrincipal;
    private jakarta.persistence.EntityManager em;
    private modelo.Usuario vendedorLogueado; // <- NUEVO: Guardamos quién inició sesión

    // NUEVO: Agregamos el Usuario al constructor
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

        this.vistaPrincipal.getBtnAbrirFormularioCliente().addActionListener(e -> abrirFormularioRegistro());
        this.vistaPrincipal.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());
        this.vistaPrincipal.getBtnAbrirFormularioVenta().addActionListener(e -> abrirFormularioVenta());
        this.vistaPrincipal.getBtnBajaCliente().addActionListener(e -> cambiarEstadoCliente(false));
        this.vistaPrincipal.getBtnAltaCliente().addActionListener(e -> cambiarEstadoCliente(true));

        this.vistaPrincipal.getTablaClientes().setAutoCreateRowSorter(true);
        this.vistaPrincipal.getTablaProductos().setAutoCreateRowSorter(true);
        this.vistaPrincipal.getTablaVentas().setAutoCreateRowSorter(true);

        this.vistaPrincipal.getBtnBajaCliente().setVisible(false);
        this.vistaPrincipal.getBtnAltaCliente().setVisible(false);

        java.awt.event.MouseAdapter deseleccionador = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JTable tabla = vistaPrincipal.getTablaClientes();
                int fila = tabla.rowAtPoint(e.getPoint());
                if (fila == -1) {
                    tabla.clearSelection();
                    vistaPrincipal.getBtnBajaCliente().setVisible(false);
                    vistaPrincipal.getBtnAltaCliente().setVisible(false);
                }
            }
        };

        this.vistaPrincipal.getTablaClientes().addMouseListener(deseleccionador);
        java.awt.Container parent = this.vistaPrincipal.getTablaClientes().getParent();
        if (parent instanceof javax.swing.JViewport) {
            parent.addMouseListener(deseleccionador);
        }

        this.vistaPrincipal.getTablaClientes().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSeleccionada = this.vistaPrincipal.getTablaClientes().getSelectedRow();
                boolean haySeleccion = filaSeleccionada != -1;

                if (haySeleccion) {
                    String estadoActual = this.vistaPrincipal.getTablaClientes().getValueAt(filaSeleccionada, 5).toString();
                    if (estadoActual.equalsIgnoreCase("Activo")) {
                        this.vistaPrincipal.getBtnBajaCliente().setVisible(true);
                        this.vistaPrincipal.getBtnAltaCliente().setVisible(false);
                    } else {
                        this.vistaPrincipal.getBtnBajaCliente().setVisible(false);
                        this.vistaPrincipal.getBtnAltaCliente().setVisible(true);
                    }
                } else {
                    this.vistaPrincipal.getBtnBajaCliente().setVisible(false);
                    this.vistaPrincipal.getBtnAltaCliente().setVisible(false);
                }
            }
        });
    }

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
                    int opcion = JOptionPane.showConfirmDialog(ventanaVenta, "Cliente no encontrado o inactivo. ¿Desea registrarlo ahora?", "No encontrado", JOptionPane.YES_NO_OPTION);
                    if (opcion == JOptionPane.YES_OPTION) {
                        abrirFormularioRegistro(dni);
                        modelo.Cliente nuevoC = repoCli.buscarPorDni(dni);
                        if (nuevoC != null) {
                            clienteActual[0] = nuevoC;
                            ventanaVenta.getDniClienteField().setText(nuevoC.getDni());
                            ventanaVenta.getLblNombreCliente().setText("Cliente nuevo validado: " + nuevoC.getNombre() + " " + nuevoC.getApellido());
                            ventanaVenta.getLblNombreCliente().setForeground(new Color(40, 167, 69));
                        }
                    }
                }
            });

            ventanaVenta.getBtnAgregarProducto().addActionListener(e -> {
                if (ventanaVenta.getProductoBox().getSelectedIndex() <= 0) return;
                String itemSeleccionado = (String) ventanaVenta.getProductoBox().getSelectedItem();
                Long idProducto = Long.parseLong(itemSeleccionado.split(" - ")[0]);
                modelo.Producto producto = repoProd.buscarPorId(idProducto);
                String cantidadStr = ventanaVenta.getCantidadField().getText().trim();

                try {
                    int cantidadPedida = Integer.parseInt(cantidadStr);
                    if (cantidadPedida <= 0) throw new NumberFormatException();

                    DefaultTableModel modeloCarrito = (DefaultTableModel) ventanaVenta.getTablaCarrito().getModel();
                    int cantidadEnCarrito = 0;
                    for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
                        Long idEnTabla = (Long) modeloCarrito.getValueAt(i, 0);
                        if (idEnTabla.equals(idProducto)) {
                            cantidadEnCarrito += (int) modeloCarrito.getValueAt(i, 3);
                        }
                    }
                    if ((cantidadPedida + cantidadEnCarrito) > producto.getStock()) {
                        JOptionPane.showMessageDialog(ventanaVenta, "Stock insuficiente.", "Error de Stock", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    double subtotal = cantidadPedida * producto.getPrecio();
                    modeloCarrito.addRow(new Object[]{ producto.getId(), producto.getNombre(), producto.getPrecio(), cantidadPedida, subtotal });

                    double totalVenta = 0;
                    for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
                        totalVenta += (double) modeloCarrito.getValueAt(i, 4);
                    }
                    ventanaVenta.getLblTotalVenta().setText(String.format("Total: $%.2f", totalVenta));
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
                DefaultTableModel modeloCarrito = (DefaultTableModel) ventanaVenta.getTablaCarrito().getModel();
                if (modeloCarrito.getRowCount() == 0) return;

                try {
                    em.getTransaction().begin();
                    modelo.Venta nuevaVenta = new modelo.Venta();
                    nuevaVenta.setCliente(clienteActual[0]);

                    // NUEVO: Asociamos la venta al vendedor que inició sesión
                    nuevaVenta.setVendedor(this.vendedorLogueado);

                    nuevaVenta.setFecha(java.time.LocalDateTime.now());
                    double totalVenta = 0;

                    for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
                        Long idProd = (Long) modeloCarrito.getValueAt(i, 0);
                        int cant = (int) modeloCarrito.getValueAt(i, 3);
                        double subtotal = (double) modeloCarrito.getValueAt(i, 4);

                        modelo.Producto p = em.find(modelo.Producto.class, idProd);
                        p.setStock(p.getStock() - cant);
                        em.merge(p);

                        modelo.DetalleVenta detalle = new modelo.DetalleVenta();
                        detalle.setProducto(p);
                        detalle.setCantidad(cant);
                        detalle.setSubtotal(subtotal);

                        nuevaVenta.agregarDetalle(detalle);
                        totalVenta += subtotal;
                    }

                    nuevaVenta.setTotal(totalVenta);
                    repositorio.VentaRepository repoVenta = new repositorio.VentaRepository(em);
                    repoVenta.guardar(nuevaVenta);
                    em.getTransaction().commit();

                    cargarTablaVentas();
                    cargarTablaProductos();
                    JOptionPane.showMessageDialog(ventanaVenta, "¡Venta confirmada!\nTotal: $" + String.format("%.2f", totalVenta), "Éxito", JOptionPane.INFORMATION_MESSAGE);
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

    private void abrirFormularioRegistro() { abrirFormularioRegistro(""); }

    private void abrirFormularioRegistro(String dniPreCargado) {
        // (Método sin cambios)
        vista.RegistroClienteUI ventanaRegistro = new vista.RegistroClienteUI(this.vistaPrincipal);
        if (!dniPreCargado.isEmpty()) {
            ventanaRegistro.getDniClienteField().setText(dniPreCargado);
            ventanaRegistro.getDniClienteField().setEditable(false);
        }
        ventanaRegistro.getBtnGuardarCliente().addActionListener(e -> {
            String dni = ventanaRegistro.getDniClienteField().getText().trim();
            String nombre = ventanaRegistro.getNombreClienteField().getText().trim();
            String apellido = ventanaRegistro.getApellidoClienteField().getText().trim();
            String telefono = ventanaRegistro.getTelefonoClienteField().getText().trim();
            String email = ventanaRegistro.getEmailClienteField().getText().trim();

            if (dni.isEmpty() || nombre.isEmpty() || apellido.isEmpty()) return;
            try {
                repositorio.ClienteRepository repo = new repositorio.ClienteRepository(this.em);
                if (repo.existeDni(dni)) return;
                modelo.Cliente nuevoCliente = new modelo.Cliente(dni, nombre, apellido, telefono, email);
                em.getTransaction().begin();
                repo.guardar(nuevoCliente);
                em.getTransaction().commit();
                cargarTablaClientes();
                ventanaRegistro.dispose();
            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
            }
        });
        ventanaRegistro.setVisible(true);
    }

    private void cargarTablaClientes() {
        repositorio.ClienteRepository repo = new repositorio.ClienteRepository(this.em);
        List<modelo.Cliente> clientes = repo.listarTodos();
        DefaultTableModel modelo = (DefaultTableModel) vistaPrincipal.getTablaClientes().getModel();
        modelo.setRowCount(0);
        for (modelo.Cliente c : clientes) {
            modelo.addRow(new Object[]{ c.getDni(), c.getNombre(), c.getApellido(), c.getTelefono(), c.getEmail(), c.isActivo() ? "Activo" : "Inactivo" });
        }
    }

    private void cambiarEstadoCliente(boolean activar) {
        int fila = vistaPrincipal.getTablaClientes().getSelectedRow();
        if (fila == -1) return;
        String dni = vistaPrincipal.getTablaClientes().getValueAt(fila, 0).toString();
        String accion = activar ? "Reactivar" : "Dar de Baja";
        if (JOptionPane.showConfirmDialog(vistaPrincipal, "¿Seguro?", accion, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            vistaPrincipal.getTablaClientes().setValueAt(activar ? "Activo" : "Inactivo", fila, 5);
            vistaPrincipal.getTablaClientes().clearSelection();
        }
    }

    private void cargarTablaProductos() {
        repositorio.ProductoRepository repo = new repositorio.ProductoRepository(this.em);
        List<modelo.Producto> productos = repo.listarTodos();
        DefaultTableModel modelo = (DefaultTableModel) vistaPrincipal.getTablaProductos().getModel();
        modelo.setRowCount(0);
        for (modelo.Producto p : productos) {
            modelo.addRow(new Object[]{ p.getId(), p.getCategoria(), p.getNombre(), p.getStock(), String.format("$%.2f", p.getPrecio()) });
        }
    }

    private void cargarTablaVentas() {
        try {
            repositorio.VentaRepository repo = new repositorio.VentaRepository(this.em);

            // NUEVO: Ahora solo cargamos las ventas de ESTE vendedor logueado
            List<modelo.Venta> ventas = repo.listarPorVendedor(this.vendedorLogueado);

            DefaultTableModel modelo = (DefaultTableModel) vistaPrincipal.getTablaVentas().getModel();
            modelo.setRowCount(0);

            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (modelo.Venta v : ventas) {
                int cantArticulos = v.getDetalles().size();
                String descProducto = cantArticulos == 1 ? v.getDetalles().get(0).getProducto().getNombre() : cantArticulos + " artículos (Varios)";
                modelo.addRow(new Object[]{ v.getId(), v.getFecha().format(formatter), v.getCliente().getDni(), descProducto, String.format("$%.2f", v.getTotal()) });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cerrarSesion() {
        if (JOptionPane.showConfirmDialog(vistaPrincipal, "¿Salir?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            vistaPrincipal.dispose();
            LoginUI ventanaLogin = new LoginUI();

            new LoginController(ventanaLogin, this.em);

            ventanaLogin.setVisible(true);
        }
    }
}
