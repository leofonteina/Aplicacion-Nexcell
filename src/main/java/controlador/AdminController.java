package controlador;

// Modelos y Repositorios
import modelo.Admin;
import modelo.Gerente;
import modelo.Producto;
import modelo.Usuario;
import modelo.Vendedor;
import repositorio.ProductoRepository;
import repositorio.UsuarioRepository;

// Vistas
import vista.AdminUI;
import vista.LoginUI;
import vista.RegistroProductoUI;
import vista.RegistroUsuarioUI;

// Persistencia y Swing
import jakarta.persistence.EntityManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Fechas
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

public class AdminController {

    private final AdminUI vistaPrincipal;
    private final ProductoRepository productoRepo;
    private final EntityManager em;

    // ----------------------------
    // Constructor e inicializacion
    // ----------------------------

    public AdminController(AdminUI vistaPrincipal, EntityManager em) {
        this.vistaPrincipal = vistaPrincipal;
        this.em = em;
        this.productoRepo = new ProductoRepository(em);

        // 1. Cargar datos e incializar tablas
        inicializarTablas();

        // 2. Registrar eventos por módulos
        configurarEventosProductos();
        configurarEventosUsuarios();
        configurarEventosReportes();
        configurarEventosGenerales();
    }

    private void inicializarTablas() {
        cargarTablaProductos();
        cargarTablaUsuarios();

        // Configuración visual y ordenadores automáticos
        vistaPrincipal.getTablaProductos().setFillsViewportHeight(true);
        vistaPrincipal.getTablaUsuarios().setFillsViewportHeight(true);

        vistaPrincipal.getTablaProductos().setAutoCreateRowSorter(true);
        vistaPrincipal.getTablaUsuarios().setAutoCreateRowSorter(true);
        vistaPrincipal.getTablaReportes().setAutoCreateRowSorter(true);

        // Limpiar selección al hacer clic en espacios vacíos de la tabla
        MouseAdapter deseleccionarAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JTable tabla = (JTable) e.getSource();
                if (tabla.rowAtPoint(e.getPoint()) == -1) {
                    tabla.clearSelection();
                }
            }
        };

        vistaPrincipal.getTablaProductos().addMouseListener(deseleccionarAdapter);
        vistaPrincipal.getTablaUsuarios().addMouseListener(deseleccionarAdapter);
    }

    private void configurarEventosProductos() {
        vistaPrincipal.getBtnAbrirFormularioProducto().addActionListener(e -> abrirFormularioNuevoProducto());
        vistaPrincipal.getBtnModificarProducto().addActionListener(e -> abrirFormularioModificarProducto());
        vistaPrincipal.getBtnBajaProducto().addActionListener(e -> cambiarEstadoProducto(false));
        vistaPrincipal.getBtnAltaProducto().addActionListener(e -> cambiarEstadoProducto(true));

        vistaPrincipal.getTablaProductos().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                gestionarBotonesProducto();
            }
        });

        configurarBusqueda(vistaPrincipal.getBuscarProductoField(), vistaPrincipal.getBtnBuscarProducto(), vistaPrincipal.getTablaProductos());
    }

    private void configurarEventosUsuarios() {
        vistaPrincipal.getBtnAbrirFormularioUsuario().addActionListener(e -> abrirFormularioUsuario());
        vistaPrincipal.getBtnModificarUsuario().addActionListener(e -> modificarUsuario());
        vistaPrincipal.getBtnBajaUsuario().addActionListener(e -> cambiarEstadoUsuario(false));
        vistaPrincipal.getBtnAltaUsuario().addActionListener(e -> cambiarEstadoUsuario(true));

        vistaPrincipal.getTablaUsuarios().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                gestionarBotonesUsuario();
            }
        });

        configurarBusqueda(vistaPrincipal.getTxtBuscarUsuario(), vistaPrincipal.getBtnBuscarUsuario(), vistaPrincipal.getTablaUsuarios());
    }

    private void configurarEventosReportes() {
        vistaPrincipal.getBtnGenerarReporte().addActionListener(e -> generarReporte());
        vistaPrincipal.getBtnLimpiarReporte().addActionListener(e -> limpiarReporte());
    }

    private void configurarEventosGenerales() {
        vistaPrincipal.getBtnAbrirBackup().addActionListener(e -> abrirVentanaBackup());
        vistaPrincipal.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());
    }

    // --------------------
    // Modulos de productos
    // --------------------

    private void cargarTablaProductos() {
        DefaultTableModel modelo = (DefaultTableModel) vistaPrincipal.getTablaProductos().getModel();
        modelo.setRowCount(0);

        // 1. Configuramos el formato de moneda para Argentina (Punto para miles, coma para decimales)
        java.text.DecimalFormatSymbols simbolos = new java.text.DecimalFormatSymbols(new java.util.Locale("es", "AR"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        java.text.DecimalFormat df = new java.text.DecimalFormat("$ #,##0.00", simbolos);

        List<Producto> listaProductos = productoRepo.listarTodos();
        for (Producto p : listaProductos) {
            String estadoStr = p.isEstado() ? "Activo" : "Inactivo";

            // 2. Aplicamos el formato al precio
            String precioFormateado = df.format(p.getPrecio());

            modelo.addRow(new Object[]{
                    p.getId(), p.getNombre(), p.getCategoria(), p.getStock(), precioFormateado, estadoStr
            });
        }
    }

    private void gestionarBotonesProducto() {
        int fila = vistaPrincipal.getTablaProductos().getSelectedRow();
        boolean haySeleccion = (fila != -1);

        vistaPrincipal.getBtnModificarProducto().setVisible(haySeleccion);

        if (haySeleccion) {
            String estadoActual = vistaPrincipal.getTablaProductos().getValueAt(fila, 5).toString();
            boolean esActivo = estadoActual.equalsIgnoreCase("Activo");
            vistaPrincipal.getBtnBajaProducto().setVisible(esActivo);
            vistaPrincipal.getBtnAltaProducto().setVisible(!esActivo);
        } else {
            vistaPrincipal.getBtnBajaProducto().setVisible(false);
            vistaPrincipal.getBtnAltaProducto().setVisible(false);
        }
    }

    private void abrirFormularioNuevoProducto() {
        RegistroProductoUI ventanaRegistro = new RegistroProductoUI(vistaPrincipal);
        ventanaRegistro.getBtnCancelar().addActionListener(e -> ventanaRegistro.dispose());

        ventanaRegistro.getBtnGuardarProducto().addActionListener(e -> {
            try {
                Producto nuevoProducto = new Producto();
                nuevoProducto.setNombre(ventanaRegistro.getNombreField().getText());
                nuevoProducto.setDescripcion(ventanaRegistro.getDescripcionArea().getText());
                nuevoProducto.setPrecio(Double.parseDouble(ventanaRegistro.getPrecioField().getText()));
                nuevoProducto.setDescuento(Double.parseDouble(ventanaRegistro.getDescuentoField().getText()));
                nuevoProducto.setStock(Integer.parseInt(ventanaRegistro.getStockField().getText()));
                nuevoProducto.setCategoria(ventanaRegistro.getCategoriaBox().getSelectedItem().toString());
                nuevoProducto.setMarca(ventanaRegistro.getMarcaBox().getSelectedItem().toString());
                nuevoProducto.setEstado(true);
                nuevoProducto.setFechaCreacion(LocalDateTime.now());
                nuevoProducto.setFechaModificacion(LocalDateTime.now());

                productoRepo.guardar(nuevoProducto);

                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "PRODUCTOS",
                        "El Administrador creó un nuevo producto: " + nuevoProducto.getNombre());

                JOptionPane.showMessageDialog(ventanaRegistro, "Producto guardado en la base de datos.");
                ventanaRegistro.dispose();
                cargarTablaProductos();

            } catch (NumberFormatException ex) {
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "PRODUCTOS",
                        "Error al crear producto. Formato numérico inválido.");
                JOptionPane.showMessageDialog(ventanaRegistro, "Error: Revisa que el precio, stock y descuento sean números válidos.", "Error de formato", JOptionPane.ERROR_MESSAGE);
            }
        });

        ventanaRegistro.setVisible(true);
    }

    private void abrirFormularioModificarProducto() {
        int fila = vistaPrincipal.getTablaProductos().getSelectedRow();
        if (fila == -1) return;

        Long idProducto = (Long) vistaPrincipal.getTablaProductos().getValueAt(fila, 0);
        Producto productoActual = productoRepo.buscarPorId(idProducto);

        RegistroProductoUI ventana = new RegistroProductoUI(vistaPrincipal);
        ventana.setTitle("Modificar Producto");
        ventana.getBtnGuardarProducto().setText("Actualizar");
        ventana.getBtnCancelar().addActionListener(e -> ventana.dispose());

        // Cargar datos previos
        ventana.getNombreField().setText(productoActual.getNombre());
        ventana.getDescripcionArea().setText(productoActual.getDescripcion());
        ventana.getPrecioField().setText(String.valueOf(productoActual.getPrecio()));
        ventana.getDescuentoField().setText(String.valueOf(productoActual.getDescuento()));
        ventana.getStockField().setText(String.valueOf(productoActual.getStock()));
        ventana.getCategoriaBox().setSelectedItem(productoActual.getCategoria());
        ventana.getMarcaBox().setSelectedItem(productoActual.getMarca());

        ventana.getBtnGuardarProducto().addActionListener(e -> {
            int confirmacion = JOptionPane.showConfirmDialog(ventana,
                    "¿Estás seguro que deseas sobreescribir los datos de este producto?",
                    "Confirmar Actualización", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                try {
                    productoActual.setNombre(ventana.getNombreField().getText());
                    productoActual.setDescripcion(ventana.getDescripcionArea().getText());
                    productoActual.setPrecio(Double.parseDouble(ventana.getPrecioField().getText()));
                    productoActual.setDescuento(Double.parseDouble(ventana.getDescuentoField().getText()));
                    productoActual.setStock(Integer.parseInt(ventana.getStockField().getText()));
                    productoActual.setCategoria(ventana.getCategoriaBox().getSelectedItem().toString());
                    productoActual.setMarca(ventana.getMarcaBox().getSelectedItem().toString());
                    productoActual.setFechaModificacion(LocalDateTime.now());

                    productoRepo.actualizar(productoActual);

                    utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "PRODUCTOS",
                            "El Administrador modificó el producto ID: " + productoActual.getId());

                    JOptionPane.showMessageDialog(ventana, "Producto actualizado con éxito.");
                    ventana.dispose();
                    cargarTablaProductos();

                } catch (NumberFormatException ex) {
                    utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "PRODUCTOS",
                            "Error al modificar producto ID: " + productoActual.getId() + ". Formato numérico inválido.");
                    JOptionPane.showMessageDialog(ventana, "Revisa los valores numéricos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        ventana.setVisible(true);
    }

    private void cambiarEstadoProducto(boolean activar) {
        int fila = vistaPrincipal.getTablaProductos().getSelectedRow();
        if (fila == -1) return;

        Long idProducto = (Long) vistaPrincipal.getTablaProductos().getValueAt(fila, 0);
        Producto p = productoRepo.buscarPorId(idProducto);

        String accion = activar ? "reactivar" : "dar de baja";
        int confirm = JOptionPane.showConfirmDialog(vistaPrincipal, "¿Seguro que deseas " + accion + " este producto?", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (!activar) {
                productoRepo.darDeBajaProducto(idProducto);
            } else {
                p.setEstado(true);
                p.setFechaModificacion(LocalDateTime.now());
                productoRepo.actualizar(p);
            }

            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ALERTA, "PRODUCTOS",
                    "El Administrador cambió el estado del producto ID: " + p.getId() + " a " + (activar ? "Activo" : "Inactivo"));

            cargarTablaProductos();
            gestionarBotonesProducto();
        }
    }

    // ------------------
    // Modulo de Usuarios
    // ------------------

    private void cargarTablaUsuarios() {
        UsuarioRepository repo = new UsuarioRepository(this.em);
        List<Usuario> usuarios = repo.listarTodos();

        DefaultTableModel modelo = (DefaultTableModel) vistaPrincipal.getTablaUsuarios().getModel();
        modelo.setRowCount(0);

        for (Usuario u : usuarios) {
            String estadoVisual = (u.getEstado() != null && u.getEstado()) ? "Activo" : "Inactivo";
            String rol = u.getClass().getSimpleName();
            modelo.addRow(new Object[]{u.getUsername(), rol, estadoVisual});
        }
    }

    private void gestionarBotonesUsuario() {
        int filaSeleccionada = vistaPrincipal.getTablaUsuarios().getSelectedRow();
        boolean haySeleccion = filaSeleccionada != -1;
        vistaPrincipal.getBtnModificarUsuario().setVisible(haySeleccion);

        if (haySeleccion) {
            String estadoActual = vistaPrincipal.getTablaUsuarios().getValueAt(filaSeleccionada, 2).toString();
            boolean esActivo = estadoActual.equalsIgnoreCase("Activo");
            vistaPrincipal.getBtnBajaUsuario().setVisible(esActivo);
            vistaPrincipal.getBtnAltaUsuario().setVisible(!esActivo);
        } else {
            vistaPrincipal.getBtnBajaUsuario().setVisible(false);
            vistaPrincipal.getBtnAltaUsuario().setVisible(false);
        }
    }

    private void abrirFormularioUsuario() {
        RegistroUsuarioUI ventanaRegistro = new RegistroUsuarioUI(this.vistaPrincipal);
        UsuarioRepository usuarioRepo = new UsuarioRepository(this.em);

        ventanaRegistro.getBtnCancelar().addActionListener(e -> ventanaRegistro.dispose());

        ventanaRegistro.getBtnGuardarUsuario().addActionListener(e -> {
            ventanaRegistro.limpiarErrores();
            if (!validarFormularioUsuario(ventanaRegistro, usuarioRepo, true, null)) return;

            String nombre = ventanaRegistro.getTxtNombre().getText().trim();
            String apellido = ventanaRegistro.getTxtApellido().getText().trim();
            String dni = ventanaRegistro.getTxtDni().getText().trim();
            String email = ventanaRegistro.getTxtEmail().getText().trim();
            String username = ventanaRegistro.getTxtUsername().getText().trim();
            String password = new String(ventanaRegistro.getTxtPassword().getPassword()).trim();
            String rol = ventanaRegistro.getCbPerfil().getSelectedItem().toString();

            String calle = ventanaRegistro.getTxtCalle().getText().trim();
            String altura = ventanaRegistro.getTxtAltura().getText().trim();
            String ciudad = ventanaRegistro.getTxtCiudad().getText().trim();
            String provincia = ventanaRegistro.getCbProvincia().getSelectedItem().toString();

            int dia = (int) ventanaRegistro.getCbDia().getSelectedItem();
            int mes = Integer.parseInt(ventanaRegistro.getCbMes().getSelectedItem().toString());
            int anio = (int) ventanaRegistro.getCbAnio().getSelectedItem();
            LocalDate fechaNac = LocalDate.of(anio, mes, dia);

            modelo.Direccion nuevaDireccion = new modelo.Direccion(calle, altura, ciudad, provincia);
            Usuario nuevoUsuario;
            if (rol.equals("Gerente")) nuevoUsuario = new Gerente(username, password);
            else if (rol.equals("Vendedor")) nuevoUsuario = new Vendedor(username, password);
            else nuevoUsuario = new Admin(username, password);

            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setApellido(apellido);
            nuevoUsuario.setDni(dni);
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setDireccion(nuevaDireccion);
            nuevoUsuario.setFechaNacimiento(fechaNac);

            try {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                em.getTransaction().begin();
                usuarioRepo.guardar(nuevoUsuario);
                em.getTransaction().commit();

                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "USUARIOS",
                        "El Administrador creó un nuevo usuario: " + username + " (Rol: " + rol + ")");

                JOptionPane.showMessageDialog(ventanaRegistro, "Usuario '" + username + "' registrado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                ventanaRegistro.dispose();
                cargarTablaUsuarios();
            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "USUARIOS",
                        "Error al crear usuario " + username + ": " + ex.getMessage());
                ventanaRegistro.getLblErrorUsername().setText("Este nombre de usuario ya está en uso.");
            }
        });
        ventanaRegistro.setVisible(true);
    }

    private void modificarUsuario() {
        int fila = vistaPrincipal.getTablaUsuarios().getSelectedRow();
        if (fila == -1) return;

        String userSeleccionado = vistaPrincipal.getTablaUsuarios().getValueAt(fila, 0).toString();
        UsuarioRepository repo = new UsuarioRepository(em);
        Usuario usuario = repo.buscarPorUsername(userSeleccionado);

        if (usuario == null) return;

        RegistroUsuarioUI ventanaModificacion = new RegistroUsuarioUI(this.vistaPrincipal);
        ventanaModificacion.setTitle("Modificar Usuario");
        ventanaModificacion.getBtnGuardarUsuario().setText("Actualizar Datos");
        ventanaModificacion.getBtnCancelar().addActionListener(e -> ventanaModificacion.dispose());

        // Cargar campos y bloquear claves primarias/únicas que no cambian
        ventanaModificacion.getTxtUsername().setText(usuario.getUsername());
        ventanaModificacion.getTxtUsername().setEditable(false);
        ventanaModificacion.getTxtPassword().setText(usuario.getPassword());
        ventanaModificacion.getTxtNombre().setText(usuario.getNombre());
        ventanaModificacion.getTxtApellido().setText(usuario.getApellido());
        ventanaModificacion.getTxtDni().setText(usuario.getDni());
        ventanaModificacion.getTxtDni().setEditable(false);
        ventanaModificacion.getTxtEmail().setText(usuario.getEmail());

        // Opcional: si querés que el selector de rol aparezca bloqueado/desactivado al modificar
        ventanaModificacion.getCbPerfil().setEnabled(false);

        if (usuario.getDireccion() != null) {
            ventanaModificacion.getTxtCalle().setText(usuario.getDireccion().getCalle());
            ventanaModificacion.getTxtAltura().setText(usuario.getDireccion().getAltura());
            ventanaModificacion.getTxtCiudad().setText(usuario.getDireccion().getCiudad());
            ventanaModificacion.getCbProvincia().setSelectedItem(usuario.getDireccion().getProvincia());
        }

        ventanaModificacion.getBtnGuardarUsuario().addActionListener(e -> {
            int confirmacion = JOptionPane.showConfirmDialog(ventanaModificacion,
                    "¿Estás seguro que deseas modificar los datos de " + usuario.getUsername() + "?",
                    "Confirmar Cambios", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                ventanaModificacion.limpiarErrores();
                if (!validarFormularioUsuario(ventanaModificacion, repo, false, usuario.getDni())) return;

                usuario.setNombre(ventanaModificacion.getTxtNombre().getText().trim());
                usuario.setApellido(ventanaModificacion.getTxtApellido().getText().trim());
                usuario.setEmail(ventanaModificacion.getTxtEmail().getText().trim());

                if (usuario.getDireccion() == null) usuario.setDireccion(new modelo.Direccion());
                usuario.getDireccion().setCalle(ventanaModificacion.getTxtCalle().getText().trim());
                usuario.getDireccion().setAltura(ventanaModificacion.getTxtAltura().getText().trim());
                usuario.getDireccion().setCiudad(ventanaModificacion.getTxtCiudad().getText().trim());
                usuario.getDireccion().setProvincia(ventanaModificacion.getCbProvincia().getSelectedItem().toString());

                try {
                    // Actualizamos mediante el repositorio de forma limpia y directa
                    repo.actualizar(usuario);

                    utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "USUARIOS",
                            "El Administrador modificó los datos del usuario: " + usuario.getUsername());
                    JOptionPane.showMessageDialog(ventanaModificacion, "Datos actualizados correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    ventanaModificacion.dispose();
                    cargarTablaUsuarios();
                } catch (Exception ex) {
                    utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "USUARIOS",
                            "Error al modificar usuario " + usuario.getUsername() + ": " + ex.getMessage());
                    JOptionPane.showMessageDialog(ventanaModificacion, "Error al actualizar la base de datos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        ventanaModificacion.setVisible(true);
    }
    private boolean validarFormularioUsuario(RegistroUsuarioUI v, UsuarioRepository repo, boolean esNuevo, String dniActual) {
        boolean hayErrores = false;
        String nombre = v.getTxtNombre().getText().trim();
        String apellido = v.getTxtApellido().getText().trim();
        String dni = v.getTxtDni().getText().trim();
        String email = v.getTxtEmail().getText().trim();
        String calle = v.getTxtCalle().getText().trim();
        String altura = v.getTxtAltura().getText().trim();
        String ciudad = v.getTxtCiudad().getText().trim();

        if (nombre.isEmpty() || !nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
            v.getLblErrorNombre().setText("Requerido. Solo letras permitidas.");
            hayErrores = true;
        }
        if (apellido.isEmpty() || !apellido.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
            v.getLblErrorApellido().setText("Requerido. Solo letras permitidas.");
            hayErrores = true;
        }
        if (!dni.matches("\\d{7,8}")) {
            v.getLblErrorDni().setText("Debe contener 7 u 8 números exactos.");
            hayErrores = true;
        } else if (esNuevo && repo.existeDni(dni)) {
            v.getLblErrorDni().setText("Este DNI ya se encuentra registrado.");
            hayErrores = true;
        } else if (!esNuevo && !dni.equals(dniActual) && repo.existeDni(dni)) {
            v.getLblErrorDni().setText("Este DNI ya pertenece a otro usuario.");
            hayErrores = true;
        }

        if (esNuevo) {
            try {
                int dia = (int) v.getCbDia().getSelectedItem();
                int mes = Integer.parseInt(v.getCbMes().getSelectedItem().toString());
                int anio = (int) v.getCbAnio().getSelectedItem();
                LocalDate.of(anio, mes, dia);
            } catch (DateTimeException ex) {
                v.getLblErrorFecha().setText("La fecha seleccionada no existe en el calendario.");
                hayErrores = true;
            }

            String username = v.getTxtUsername().getText().trim();
            String password = new String(v.getTxtPassword().getPassword()).trim();
            if (username.isEmpty() || username.contains(" ") || username.length() < 4) {
                v.getLblErrorUsername().setText("Mínimo 4 caracteres y sin espacios.");
                hayErrores = true;
            }
            if (password.isEmpty()) {
                v.getLblErrorPassword().setText("La contraseña es obligatoria.");
                hayErrores = true;
            }
        }

        if (calle.isEmpty()) { v.getLblErrorCalle().setText("La calle es obligatoria."); hayErrores = true; }
        if (!altura.matches("\\d+")) { v.getLblErrorAltura().setText("Debe ser un valor numérico exacto."); hayErrores = true; }
        if (ciudad.isEmpty()) { v.getLblErrorCiudad().setText("La ciudad es obligatoria."); hayErrores = true; }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) { v.getLblErrorEmail().setText("Formato inválido (ej: correo@mail.com)."); hayErrores = true; }

        return !hayErrores;
    }

    private void cambiarEstadoUsuario(boolean activar) {
        int fila = vistaPrincipal.getTablaUsuarios().getSelectedRow();
        if (fila == -1) return;

        String userSeleccionado = vistaPrincipal.getTablaUsuarios().getValueAt(fila, 0).toString();
        String accion = activar ? "Reactivar" : "Inactivar";

        int confirmacion = JOptionPane.showConfirmDialog(vistaPrincipal,
                "¿Estás seguro que querés " + accion.toLowerCase() + " al usuario " + userSeleccionado + "?",
                "Confirmar Cambio", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            UsuarioRepository repo = new UsuarioRepository(em);
            Usuario usuario = repo.buscarPorUsername(userSeleccionado);

            if (usuario != null) {
                usuario.setEstado(activar);
                repo.actualizar(usuario);
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ALERTA, "USUARIOS",
                        "El Administrador cambió el estado del usuario " + userSeleccionado + " a " + (activar ? "Activo" : "Inactivo"));
                cargarTablaUsuarios();
                JOptionPane.showMessageDialog(vistaPrincipal, "Estado actualizado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    // ------------------
    // Modulo de Reportes
    // ------------------

    private void generarReporte() {
        LocalDateTime[] fechas = obtenerFechasDesdeUI();
        if (fechas == null) return;

        String reporte = vistaPrincipal.getComboReportes().getSelectedItem().toString();
        switch (reporte) {
            case "Productos con Bajo Stock":
                reporteBajoStock();
                break;
            case "Valorización de Inventario":
                reporteValorizacion();
                break;
            case "Auditoría de Usuarios":
                reporteAuditoriaUsuarios();
                break;
            case "Movimientos de Inventario":
                reporteMovimientos(fechas[0], fechas[1]);
                break;
        }
    }

    private void limpiarReporte() {
        vistaPrincipal.getTablaReportes().setModel(new DefaultTableModel());
    }

    private void reporteBajoStock() {
        List<Producto> productos = em.createQuery(
                        "SELECT p FROM Producto p WHERE p.stock <= 5 AND p.estado = true ORDER BY p.stock ASC", Producto.class)
                .getResultList();

        String[] columnas = {"ID", "Producto", "Marca", "Stock Actual", "Precio"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);

        for (Producto p : productos) {
            modelo.addRow(new Object[]{
                    p.getId(), p.getNombre(), p.getMarca(), p.getStock(), String.format("$%.2f", p.getPrecio())
            });
        }
        vistaPrincipal.getTablaReportes().setModel(modelo);
    }

    private void reporteValorizacion() {
        List<Object[]> resultados = em.createQuery(
                        "SELECT p.categoria, SUM(p.stock), SUM(p.stock * p.precio) FROM Producto p WHERE p.estado = true GROUP BY p.categoria", Object[].class)
                .getResultList();

        String[] columnas = {"Categoría", "Cantidad Total de Ítems", "Valor Total Invertido"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);

        long totalItems = 0;
        double granTotal = 0;

        for (Object[] fila : resultados) {
            String categoria = (String) fila[0];
            Long cantidad = (Long) fila[1];
            Double valor = (Double) fila[2];

            totalItems += (cantidad != null ? cantidad : 0);
            granTotal += (valor != null ? valor : 0);

            modelo.addRow(new Object[]{categoria, cantidad, String.format("$%.2f", valor)});
        }

        modelo.addRow(new Object[]{"TOTAL GENERAL", totalItems, String.format("$%.2f", granTotal)});
        vistaPrincipal.getTablaReportes().setModel(modelo);
    }

    private void reporteMovimientos(LocalDateTime inicio, LocalDateTime fin) {
        List<modelo.Venta> ventas = em.createQuery(
                        "SELECT DISTINCT v FROM Venta v JOIN FETCH v.detalles WHERE v.fecha BETWEEN :ini AND :fin ORDER BY v.fecha DESC", modelo.Venta.class)
                .setParameter("ini", inicio)
                .setParameter("fin", fin)
                .getResultList();

        String[] columnas = {"Fecha", "Vendedor", "Producto", "Cant. Vendida", "Subtotal"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (modelo.Venta v : ventas) {
            for (modelo.DetalleVenta d : v.getDetalles()) {
                modelo.addRow(new Object[]{
                        v.getFecha().format(fmt),
                        v.getVendedor().getUsername(),
                        d.getProducto().getNombre(),
                        d.getCantidad(),
                        String.format("$%.2f", d.getSubtotal())
                });
            }
        }
        vistaPrincipal.getTablaReportes().setModel(modelo);
    }

    private void reporteAuditoriaUsuarios() {
        List<Usuario> usuarios = em.createQuery("SELECT u FROM Usuario u ORDER BY u.username ASC", Usuario.class).getResultList();

        String[] columnas = {"Username", "Nombre Completo", "DNI", "Rol", "Estado en Sistema"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);

        for (Usuario u : usuarios) {
            String rol = u.getClass().getSimpleName();
            String estado = (u.getEstado() != null && u.getEstado()) ? "Activo" : "Inactivo / Dado de baja";

            modelo.addRow(new Object[]{
                    u.getUsername(),
                    u.getNombre() + " " + u.getApellido(),
                    u.getDni(),
                    rol,
                    estado
            });
        }
        vistaPrincipal.getTablaReportes().setModel(modelo);
    }

    private LocalDateTime[] obtenerFechasDesdeUI() {
        try {
            String strInicio = vistaPrincipal.getCbDiaInicio().getSelectedItem() + "/" +
                    vistaPrincipal.getCbMesInicio().getSelectedItem() + "/" +
                    vistaPrincipal.getCbAnioInicio().getSelectedItem();

            String strFin = vistaPrincipal.getCbDiaFin().getSelectedItem() + "/" +
                    vistaPrincipal.getCbMesFin().getSelectedItem() + "/" +
                    vistaPrincipal.getCbAnioFin().getSelectedItem();

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fechaInicio = LocalDate.parse(strInicio, fmt);
            LocalDate fechaFin = LocalDate.parse(strFin, fmt);

            if (fechaInicio.isAfter(fechaFin)) {
                JOptionPane.showMessageDialog(vistaPrincipal, "La fecha 'Desde' no puede ser posterior a 'Hasta'.", "Fechas Inválidas", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            return new LocalDateTime[]{fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59)};

        } catch (DateTimeException ex) {
            JOptionPane.showMessageDialog(vistaPrincipal, "La fecha seleccionada no existe en el calendario.", "Error de Fecha", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // -----------------------------------
    // Utilidades de navegacion y busqueda
    // -----------------------------------

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
        TableRowSorter<?> sorter = (TableRowSorter<?>) tabla.getRowSorter();

        if (sorter != null) {
            if (texto.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(texto)));
            }
        }
    }

    private void abrirVentanaBackup() {
        vista.BackupUI ventanaBackup = new vista.BackupUI();
        new BackupController(ventanaBackup, this.em);
        ventanaBackup.setVisible(true);
    }

    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(vistaPrincipal,
                "¿Estás seguro que querés salir del panel de administración?", "Cerrar Sesión",
                JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "SEGURIDAD",
                    "El Administrador cerró sesión.");
            vistaPrincipal.dispose();
            LoginUI ventanaLogin = new LoginUI();
            new LoginController(ventanaLogin, em);
            ventanaLogin.setVisible(true);
        }
    }
}
