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

    public VendedorController(VendedorUI vistaPrincipal, jakarta.persistence.EntityManager em) {
        this.vistaPrincipal = vistaPrincipal;
        this.em = em;

        try {
            // Cargamos las tablas apenas inicia el controlador
            cargarTablaClientes();
            cargarTablaProductos();
            cargarTablaVentas();
        } catch (Exception ex) {
            System.err.println("Aviso inicial: " + ex.getMessage());
        }

        // Activamos los botones del menú y herramientas
        this.vistaPrincipal.getBtnAbrirFormularioCliente().addActionListener(e -> abrirFormularioRegistro());
        this.vistaPrincipal.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());
        this.vistaPrincipal.getBtnAbrirFormularioVenta().addActionListener(e -> abrirFormularioVenta());
        this.vistaPrincipal.getBtnBajaCliente().addActionListener(e -> cambiarEstadoCliente(false));
        this.vistaPrincipal.getBtnAltaCliente().addActionListener(e -> cambiarEstadoCliente(true));

        // Listener para la tabla de clientes (botones de Baja/Alta)
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

    // =======================================================
    // MÓDULO DE VENTAS (CARRITO Y CONFIRMACIÓN)
    // =======================================================
    private void abrirFormularioVenta() {
        try {
            vista.RegistroVentaUI ventanaVenta = new vista.RegistroVentaUI(this.vistaPrincipal);

            // 1. Cargar productos desde la BD al ComboBox
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

            // 2. Verificar Cliente
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
                    int opcion = JOptionPane.showConfirmDialog(ventanaVenta,
                        "Cliente no encontrado o inactivo. ¿Desea registrarlo ahora?",
                        "No encontrado", JOptionPane.YES_NO_OPTION);

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

            // 3. Agregar al Carrito (Con validación de Stock)
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
                        JOptionPane.showMessageDialog(ventanaVenta,
                            "Stock insuficiente. Stock en BD: " + producto.getStock() + " (Ya tenés " + cantidadEnCarrito + " en el carrito).",
                            "Error de Stock", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    double subtotal = cantidadPedida * producto.getPrecio();
                    modeloCarrito.addRow(new Object[]{
                        producto.getId(),
                        producto.getNombre(),
                        producto.getPrecio(),
                        cantidadPedida,
                        subtotal
                    });

                    double totalVenta = 0;
                    for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
                        totalVenta += (double) modeloCarrito.getValueAt(i, 4);
                    }
                    ventanaVenta.getLblTotalVenta().setText(String.format("Total: $%.2f", totalVenta));

                    ventanaVenta.getProductoBox().setSelectedIndex(0);
                    ventanaVenta.getCantidadField().setText("");

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(ventanaVenta, "La cantidad debe ser un número entero mayor a cero.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            // 4. Confirmar Venta Completa (Guarda en BD y descuenta stock)
            ventanaVenta.getBtnConfirmarVenta().addActionListener(e -> {
                if (clienteActual[0] == null) {
                    JOptionPane.showMessageDialog(ventanaVenta, "Debe verificar y asociar un cliente antes de finalizar la venta.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                DefaultTableModel modeloCarrito = (DefaultTableModel) ventanaVenta.getTablaCarrito().getModel();
                if (modeloCarrito.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(ventanaVenta, "El carrito está vacío.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    em.getTransaction().begin();

                    modelo.Venta nuevaVenta = new modelo.Venta();
                    nuevaVenta.setCliente(clienteActual[0]);
                    nuevaVenta.setFecha(java.time.LocalDateTime.now());
                    double totalVenta = 0;

                    for (int i = 0; i < modeloCarrito.getRowCount(); i++) {
                        Long idProd = (Long) modeloCarrito.getValueAt(i, 0);
                        int cant = (int) modeloCarrito.getValueAt(i, 3);
                        double subtotal = (double) modeloCarrito.getValueAt(i, 4);

                        // Descontamos stock del producto
                        modelo.Producto p = em.find(modelo.Producto.class, idProd);
                        p.setStock(p.getStock() - cant);
                        em.merge(p);

                        // Creamos detalle
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
                    cargarTablaProductos(); // Refleja el nuevo stock

                    JOptionPane.showMessageDialog(ventanaVenta,
                        "¡Venta confirmada y facturada!\nTotal: $" + String.format("%.2f", totalVenta),
                        "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);

                    ventanaVenta.dispose();

                } catch (Exception ex) {
                    if (em.getTransaction().isActive()) em.getTransaction().rollback();
                    JOptionPane.showMessageDialog(ventanaVenta, "Error al registrar la venta en la Base de Datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            ventanaVenta.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(vistaPrincipal, "Error al abrir ventana de ventas: " + ex.getMessage(), "Error del Sistema", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =======================================================
    // MÓDULO DE CLIENTES
    // =======================================================
    private void abrirFormularioRegistro() {
        abrirFormularioRegistro("");
    }

    private void abrirFormularioRegistro(String dniPreCargado) {
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

            if (dni.isEmpty() || nombre.isEmpty() || apellido.isEmpty()) {
                JOptionPane.showMessageDialog(ventanaRegistro, "Por favor, completá al menos DNI, Nombre y Apellido.", "Campos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                repositorio.ClienteRepository repo = new repositorio.ClienteRepository(this.em);

                if (repo.existeDni(dni)) {
                    JOptionPane.showMessageDialog(ventanaRegistro, "Ya existe un cliente con el DNI: " + dni, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                modelo.Cliente nuevoCliente = new modelo.Cliente(dni, nombre, apellido, telefono, email);
                em.getTransaction().begin();
                repo.guardar(nuevoCliente);
                em.getTransaction().commit();

                cargarTablaClientes();

                JOptionPane.showMessageDialog(ventanaRegistro, "Cliente registrado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                ventanaRegistro.dispose();

            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                JOptionPane.showMessageDialog(ventanaRegistro, "Error en BD: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            String estadoVisual = c.isActivo() ? "Activo" : "Inactivo";
            modelo.addRow(new Object[]{ c.getDni(), c.getNombre(), c.getApellido(), c.getTelefono(), c.getEmail(), estadoVisual });
        }
    }

    private void cambiarEstadoCliente(boolean activar) {
        int fila = vistaPrincipal.getTablaClientes().getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(vistaPrincipal, "Seleccioná un cliente de la tabla.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dniSeleccionado = vistaPrincipal.getTablaClientes().getValueAt(fila, 0).toString();
        String accion = activar ? "Reactivar" : "Dar de Baja (Inactivar)";

        int confirmacion = JOptionPane.showConfirmDialog(vistaPrincipal, "¿Estás seguro que querés " + accion.toLowerCase() + " al cliente con DNI " + dniSeleccionado + "?", accion, JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            String nuevoEstado = activar ? "Activo" : "Inactivo";
            vistaPrincipal.getTablaClientes().setValueAt(nuevoEstado, fila, 5);
            vistaPrincipal.getTablaClientes().clearSelection();
            JOptionPane.showMessageDialog(vistaPrincipal, "El estado del cliente se actualizó a: " + nuevoEstado, "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // =======================================================
    // MÓDULOS DE PRODUCTOS Y VENTAS (HISTORIAL)
    // =======================================================
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
        repositorio.VentaRepository repo = new repositorio.VentaRepository(this.em);
        List<modelo.Venta> ventas = repo.listarTodas();
        DefaultTableModel modelo = (DefaultTableModel) vistaPrincipal.getTablaVentas().getModel();
        modelo.setRowCount(0);

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (modelo.Venta v : ventas) {
            int cantArticulos = v.getDetalles().size();
            String descProducto = cantArticulos == 1 ? v.getDetalles().get(0).getProducto().getNombre() : cantArticulos + " artículos (Varios)";
            modelo.addRow(new Object[]{ v.getId(), v.getFecha().format(formatter), v.getCliente().getDni(), descProducto, String.format("$%.2f", v.getTotal()) });
        }
    }

    // =======================================================
    // SESIÓN
    // =======================================================
    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(vistaPrincipal, "¿Estás seguro que querés salir?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            vistaPrincipal.dispose();
            LoginUI ventanaLogin = new LoginUI();
            new LoginController(ventanaLogin, em);
            ventanaLogin.setVisible(true);
        }
    }
}
