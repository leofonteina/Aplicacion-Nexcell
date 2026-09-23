package controlador;

// Todas las clases que se utilizan en Admin Controller
import modelo.Producto;
import modelo.Usuario;
import modelo.Admin;
import modelo.Gerente;
import modelo.Vendedor;
import repositorio.ProductoRepository;
import repositorio.UsuarioRepository;
import vista.AdminUI;
import vista.RegistroProductoUI;
import vista.RegistroUsuarioUI;
import vista.LoginUI;

// Librerias para la conexión a la base de datos
import jakarta.persistence.EntityManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

// Librerias para las fechas locales
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

// Librerias para los eventos del mouse
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AdminController {

    private AdminUI vistaPrincipal;
    private ProductoRepository productoRepo;
    private EntityManager em;

    // Modificamos el constructor para recibir el EntityManager
    public AdminController(AdminUI vistaPrincipal, EntityManager em) {
        this.vistaPrincipal = vistaPrincipal;
        this.em = em;
        this.productoRepo = new ProductoRepository(em);

        // 1. Cargar los datos reales en la tabla al abrir la ventana
        cargarTablaProductos();

        // Escuchadores de Productos
        this.vistaPrincipal.getBtnAbrirFormularioProducto().addActionListener(e -> abrirFormularioNuevoProducto());
        this.vistaPrincipal.getBtnModificarProducto().addActionListener(e -> abrirFormularioModificarProducto());
        this.vistaPrincipal.getBtnBajaProducto().addActionListener(e -> cambiarEstadoProducto(false));
        this.vistaPrincipal.getBtnAltaProducto().addActionListener(e -> cambiarEstadoProducto(true));

        // Escuchador de selección de la tabla (para mostrar/ocultar botones)
        this.vistaPrincipal.getTablaProductos().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                gestionarBotonesProducto();
            }
        });

        // Escuchadores de Usuarios
        this.vistaPrincipal.getBtnAbrirFormularioUsuario().addActionListener(e -> abrirFormularioUsuario());
        this.vistaPrincipal.getBtnModificarUsuario().addActionListener(e -> modificarUsuario());
        this.vistaPrincipal.getBtnBajaUsuario().addActionListener(e -> cambiarEstadoUsuario(false));
        this.vistaPrincipal.getBtnAltaUsuario().addActionListener(e -> cambiarEstadoUsuario(true));

        this.vistaPrincipal.getTablaUsuarios().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int filaSeleccionada = this.vistaPrincipal.getTablaUsuarios().getSelectedRow();
                boolean haySeleccion = filaSeleccionada != -1;
                this.vistaPrincipal.getBtnModificarUsuario().setVisible(haySeleccion);

                if (haySeleccion) {
                    String estadoActual = this.vistaPrincipal.getTablaUsuarios().getValueAt(filaSeleccionada, 2).toString();
                    if (estadoActual.equalsIgnoreCase("Activo")) {
                        this.vistaPrincipal.getBtnBajaUsuario().setVisible(true);
                        this.vistaPrincipal.getBtnAltaUsuario().setVisible(false);
                    } else {
                        this.vistaPrincipal.getBtnBajaUsuario().setVisible(false);
                        this.vistaPrincipal.getBtnAltaUsuario().setVisible(true);
                    }
                } else {
                    this.vistaPrincipal.getBtnBajaUsuario().setVisible(false);
                    this.vistaPrincipal.getBtnAltaUsuario().setVisible(false);
                }
            }
        });

        // Escuchadores de Reportes y Sesión
        this.vistaPrincipal.getBtnGenerarReporte().addActionListener(e -> generarReporte());
        this.vistaPrincipal.getBtnLimpiarReporte().addActionListener(e -> limpiarReporte());
        this.vistaPrincipal.getBtnCerrarSesion().addActionListener(e -> cerrarSesion());

        // Escuchadores de Búsqueda de Usuarios
        this.vistaPrincipal.getBtnBuscarUsuario().addActionListener(e -> filtrarTablaUsuarios());
        this.vistaPrincipal.getTxtBuscarUsuario().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filtrarTablaUsuarios();
            }
        });

        cargarTablaUsuarios();

        // --- DESELECCIONAR AL HACER CLIC EN EL VACÍO ---

        // Para la tabla de Productos
        this.vistaPrincipal.getTablaProductos().setFillsViewportHeight(true);
        this.vistaPrincipal.getTablaProductos().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (vistaPrincipal.getTablaProductos().rowAtPoint(e.getPoint()) == -1) {
                    vistaPrincipal.getTablaProductos().clearSelection();
                }
            }
        });

        // Para la tabla de Usuarios
        this.vistaPrincipal.getTablaUsuarios().setFillsViewportHeight(true);
        this.vistaPrincipal.getTablaUsuarios().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (vistaPrincipal.getTablaUsuarios().rowAtPoint(e.getPoint()) == -1) {
                    vistaPrincipal.getTablaUsuarios().clearSelection();
                }
            }
        });

    }

// --- MÉTODOS DE BASE DE DATOS PARA PRODUCTOS ---

    private void cargarTablaProductos() {
        DefaultTableModel modelo = (DefaultTableModel) vistaPrincipal.getTablaProductos().getModel();
        modelo.setRowCount(0); // Limpiamos los datos de ejemplo

        List<Producto> listaProductos = productoRepo.listarTodos();

        for (Producto p : listaProductos) {
            String estadoStr = p.isEstado() ? "Activo" : "Inactivo";
            // Las columnas son: {"ID", "Modelo", "Categoría", "Stock", "Precio", "Estado"}
            modelo.addRow(new Object[]{
                    p.getId(),
                    p.getNombre(),
                    p.getCategoria(),
                    p.getStock(),
                    "$" + p.getPrecio(),
                    estadoStr
            });
        }
    }

    private void gestionarBotonesProducto() {
        int fila = vistaPrincipal.getTablaProductos().getSelectedRow();
        boolean haySeleccion = (fila != -1);

        vistaPrincipal.getBtnModificarProducto().setVisible(haySeleccion);

        if (haySeleccion) {
            String estadoActual = vistaPrincipal.getTablaProductos().getValueAt(fila, 5).toString();
            if (estadoActual.equalsIgnoreCase("Activo")) {
                vistaPrincipal.getBtnBajaProducto().setVisible(true);
                vistaPrincipal.getBtnAltaProducto().setVisible(false);
            } else {
                vistaPrincipal.getBtnBajaProducto().setVisible(false);
                vistaPrincipal.getBtnAltaProducto().setVisible(true);
            }
        } else {
            vistaPrincipal.getBtnBajaProducto().setVisible(false);
            vistaPrincipal.getBtnAltaProducto().setVisible(false);
        }
    }

    private void abrirFormularioNuevoProducto() {
        RegistroProductoUI ventanaRegistro = new RegistroProductoUI(vistaPrincipal);

        // Acción de Cancelar
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

                JOptionPane.showMessageDialog(ventanaRegistro, "Producto guardado en la base de datos.");
                ventanaRegistro.dispose();
                cargarTablaProductos();

            } catch (NumberFormatException ex) {
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

        // Acción de Cancelar
        ventana.getBtnCancelar().addActionListener(e -> ventana.dispose());

        ventana.getNombreField().setText(productoActual.getNombre());
        ventana.getDescripcionArea().setText(productoActual.getDescripcion());
        ventana.getPrecioField().setText(String.valueOf(productoActual.getPrecio()));
        ventana.getDescuentoField().setText(String.valueOf(productoActual.getDescuento()));
        ventana.getStockField().setText(String.valueOf(productoActual.getStock()));
        ventana.getCategoriaBox().setSelectedItem(productoActual.getCategoria());
        ventana.getMarcaBox().setSelectedItem(productoActual.getMarca());

        ventana.getBtnGuardarProducto().addActionListener(e -> {
            // Confirmación antes de actualizar
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

                    JOptionPane.showMessageDialog(ventana, "Producto actualizado con éxito.");
                    ventana.dispose();
                    cargarTablaProductos();

                } catch (NumberFormatException ex) {
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
            p.setEstado(activar);
            p.setFechaModificacion(LocalDateTime.now());
            productoRepo.actualizar(p);

            cargarTablaProductos();
            gestionarBotonesProducto();
        }
    }


    // === GESTIÓN DE USUARIOS CON VALIDACIONES INLINE ===

    private void abrirFormularioUsuario() {
        RegistroUsuarioUI ventanaRegistro = new RegistroUsuarioUI(this.vistaPrincipal);
        UsuarioRepository usuarioRepo = new UsuarioRepository(this.em);

        // Acción de Cancelar
        ventanaRegistro.getBtnCancelar().addActionListener(e -> ventanaRegistro.dispose());

        ventanaRegistro.getBtnGuardarUsuario().addActionListener(e -> {
            ventanaRegistro.limpiarErrores();
            boolean hayErrores = false;

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

            if (nombre.isEmpty() || !nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
                ventanaRegistro.getLblErrorNombre().setText("Requerido. Solo letras permitidas.");
                hayErrores = true;
            }
            if (apellido.isEmpty() || !apellido.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
                ventanaRegistro.getLblErrorApellido().setText("Requerido. Solo letras permitidas.");
                hayErrores = true;
            }
            if (!dni.matches("\\d{7,8}")) {
                ventanaRegistro.getLblErrorDni().setText("Debe contener 7 u 8 números exactos.");
                hayErrores = true;
            } else if (usuarioRepo.existeDni(dni)) {
                ventanaRegistro.getLblErrorDni().setText("Este DNI ya se encuentra registrado.");
                hayErrores = true;
            }

            int dia = (int) ventanaRegistro.getCbDia().getSelectedItem();
            int mes = Integer.parseInt(ventanaRegistro.getCbMes().getSelectedItem().toString());
            int anio = (int) ventanaRegistro.getCbAnio().getSelectedItem();
            LocalDate fechaNac = null;
            try {
                fechaNac = LocalDate.of(anio, mes, dia);
            } catch (DateTimeException ex) {
                ventanaRegistro.getLblErrorFecha().setText("La fecha seleccionada no existe en el calendario.");
                hayErrores = true;
            }

            if (calle.isEmpty()) {
                ventanaRegistro.getLblErrorCalle().setText("La calle es obligatoria.");
                hayErrores = true;
            }
            if (!altura.matches("\\d+")) {
                ventanaRegistro.getLblErrorAltura().setText("Debe ser un valor numérico exacto.");
                hayErrores = true;
            }
            if (ciudad.isEmpty()) {
                ventanaRegistro.getLblErrorCiudad().setText("La ciudad es obligatoria.");
                hayErrores = true;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                ventanaRegistro.getLblErrorEmail().setText("Formato inválido (ej: correo@mail.com).");
                hayErrores = true;
            }
            if (username.isEmpty() || username.contains(" ") || username.length() < 4) {
                ventanaRegistro.getLblErrorUsername().setText("Mínimo 4 caracteres y sin espacios.");
                hayErrores = true;
            }
            if (password.isEmpty()) {
                ventanaRegistro.getLblErrorPassword().setText("La contraseña es obligatoria.");
                hayErrores = true;
            }

            if (hayErrores) return;

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

                JOptionPane.showMessageDialog(ventanaRegistro, "Usuario '" + username + "' registrado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                ventanaRegistro.dispose();
                cargarTablaUsuarios();
            } catch (Exception ex) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
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
        modelo.Usuario usuario = repo.buscarPorUsername(userSeleccionado);

        if (usuario == null) return;

        RegistroUsuarioUI ventanaModificacion = new RegistroUsuarioUI(this.vistaPrincipal);
        ventanaModificacion.setTitle("Modificar Usuario");
        ventanaModificacion.getBtnGuardarUsuario().setText("Actualizar Datos");

        // Acción de Cancelar
        ventanaModificacion.getBtnCancelar().addActionListener(e -> ventanaModificacion.dispose());

        // Cargamos los datos
        ventanaModificacion.getTxtUsername().setText(usuario.getUsername());
        ventanaModificacion.getTxtUsername().setEditable(false); // Evita cambiar el username

        ventanaModificacion.getTxtPassword().setText(usuario.getPassword());
        ventanaModificacion.getTxtNombre().setText(usuario.getNombre());
        ventanaModificacion.getTxtApellido().setText(usuario.getApellido());

        // --- CAMBIO EN EL DNI ---
        ventanaModificacion.getTxtDni().setText(usuario.getDni()); // Mostramos el DNI actual
        ventanaModificacion.getTxtDni().setEditable(false);        // Bloqueamos para que no lo puedan editar
        // ------------------------

        ventanaModificacion.getTxtEmail().setText(usuario.getEmail());

        if (usuario.getDireccion() != null) {
            ventanaModificacion.getTxtCalle().setText(usuario.getDireccion().getCalle());
            ventanaModificacion.getTxtAltura().setText(usuario.getDireccion().getAltura());
            ventanaModificacion.getTxtCiudad().setText(usuario.getDireccion().getCiudad());
            ventanaModificacion.getCbProvincia().setSelectedItem(usuario.getDireccion().getProvincia());
        }

        ventanaModificacion.getBtnGuardarUsuario().addActionListener(e -> {

            // Confirmación antes de actualizar
            int confirmacion = JOptionPane.showConfirmDialog(ventanaModificacion,
                    "¿Estás seguro que deseas modificar los datos de " + usuario.getUsername() + "?",
                    "Confirmar Cambios", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirmacion == JOptionPane.YES_OPTION) {
                ventanaModificacion.limpiarErrores();
                boolean hayErrores = false;

                String nombre = ventanaModificacion.getTxtNombre().getText().trim();
                String apellido = ventanaModificacion.getTxtApellido().getText().trim();
                String dni = ventanaModificacion.getTxtDni().getText().trim();
                String email = ventanaModificacion.getTxtEmail().getText().trim();
                String calle = ventanaModificacion.getTxtCalle().getText().trim();
                String altura = ventanaModificacion.getTxtAltura().getText().trim();
                String ciudad = ventanaModificacion.getTxtCiudad().getText().trim();

                if (nombre.isEmpty() || !nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
                    ventanaModificacion.getLblErrorNombre().setText("Requerido. Solo letras permitidas.");
                    hayErrores = true;
                }
                if (apellido.isEmpty() || !apellido.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
                    ventanaModificacion.getLblErrorApellido().setText("Requerido. Solo letras permitidas.");
                    hayErrores = true;
                }
                if (!dni.matches("\\d{7,8}")) {
                    ventanaModificacion.getLblErrorDni().setText("Debe contener 7 u 8 números exactos.");
                    hayErrores = true;
                } else if (!dni.equals(usuario.getDni()) && repo.existeDni(dni)) {
                    ventanaModificacion.getLblErrorDni().setText("Este DNI ya pertenece a otro usuario.");
                    hayErrores = true;
                }
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    ventanaModificacion.getLblErrorEmail().setText("Formato inválido (ej: correo@mail.com).");
                    hayErrores = true;
                }
                if (calle.isEmpty()) {
                    ventanaModificacion.getLblErrorCalle().setText("La calle es obligatoria.");
                    hayErrores = true;
                }
                if (!altura.matches("\\d+")) {
                    ventanaModificacion.getLblErrorAltura().setText("Debe ser numérico.");
                    hayErrores = true;
                }
                if (ciudad.isEmpty()) {
                    ventanaModificacion.getLblErrorCiudad().setText("La ciudad es obligatoria.");
                    hayErrores = true;
                }

                if (hayErrores) return;

                usuario.setNombre(nombre);
                usuario.setApellido(apellido);
                usuario.setDni(dni);
                usuario.setEmail(email);

                if (usuario.getDireccion() == null) usuario.setDireccion(new modelo.Direccion());
                usuario.getDireccion().setCalle(calle);
                usuario.getDireccion().setAltura(altura);
                usuario.getDireccion().setCiudad(ciudad);
                usuario.getDireccion().setProvincia(ventanaModificacion.getCbProvincia().getSelectedItem().toString());

                try {
                    repo.actualizar(usuario);
                    JOptionPane.showMessageDialog(ventanaModificacion, "Datos actualizados correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    ventanaModificacion.dispose();
                    cargarTablaUsuarios();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ventanaModificacion, "Error al actualizar la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        ventanaModificacion.setVisible(true);
    }

    private void cargarTablaUsuarios() {
        repositorio.UsuarioRepository repo = new repositorio.UsuarioRepository(this.em);
        List<modelo.Usuario> usuarios = repo.listarTodos();

        DefaultTableModel modelo = (DefaultTableModel) vistaPrincipal.getTablaUsuarios().getModel();
        modelo.setRowCount(0);

        for (modelo.Usuario u : usuarios) {
            String estadoVisual = (u.getEstado() != null && u.getEstado()) ? "Activo" : "Inactivo";
            String rol = u.getClass().getSimpleName();
            modelo.addRow(new Object[]{u.getUsername(), rol, estadoVisual});
        }
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
            repositorio.UsuarioRepository repo = new repositorio.UsuarioRepository(em);
            modelo.Usuario usuario = repo.buscarPorUsername(userSeleccionado);

            if (usuario != null) {
                usuario.setEstado(activar);
                repo.actualizar(usuario);
                cargarTablaUsuarios();
                JOptionPane.showMessageDialog(vistaPrincipal, "Estado actualizado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void filtrarTablaUsuarios() {
        String textoBusqueda = vistaPrincipal.getTxtBuscarUsuario().getText().trim();
        repositorio.UsuarioRepository repo = new repositorio.UsuarioRepository(this.em);
        List<modelo.Usuario> usuarios;

        if (textoBusqueda.isEmpty()) {
            usuarios = repo.listarTodos();
        } else {
            usuarios = repo.buscarPorTexto(textoBusqueda);
        }

        DefaultTableModel modelo = (DefaultTableModel) vistaPrincipal.getTablaUsuarios().getModel();
        modelo.setRowCount(0);

        for (modelo.Usuario u : usuarios) {
            String estadoVisual = (u.getEstado() != null && u.getEstado()) ? "Activo" : "Inactivo";
            String rol = u.getClass().getSimpleName();
            modelo.addRow(new Object[]{u.getUsername(), rol, estadoVisual});
        }
    }

    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(vistaPrincipal,
            "¿Estás seguro que querés salir del panel de administración?", "Cerrar Sesión",
            JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.YES_OPTION) {
            vistaPrincipal.dispose();
            LoginUI ventanaLogin = new LoginUI();
            new LoginController(ventanaLogin, em);
            ventanaLogin.setVisible(true);
        }
    }

    // === REPORTES CON MENÚS DESPLEGABLES ===

    // === REPORTES REESTRUCTURADOS ===

    private void generarReporte() {
        String reporte = vistaPrincipal.getComboReportes().getSelectedItem().toString();

        switch (reporte) {
            case "Productos con Bajo Stock":
                reporteBajoStock();
                break;
            case "Valorización de Inventario":
                reporteValorizacion();
                break;
            case "Movimientos de Inventario":
                reporteMovimientos();
                break;
            case "Auditoría de Usuarios":
                reporteAuditoriaUsuarios();
                break;
        }
    }

    private void limpiarReporte() {
        vistaPrincipal.getTablaReportes().setModel(new DefaultTableModel());
    }

    // 1. Reporte de Bajo Stock (Alerta para compras)
    private void reporteBajoStock() {
        // Lógica futura: SELECT * FROM productos WHERE stock <= 5 AND estado = 1
        String[] columnas = {"ID", "Producto", "Marca", "Stock Actual", "Precio"};
        Object[][] datos = {
                {"ACC-045", "Funda Silicona iPhone 13", "Genérica", 2, "$15.000"},
                {"CEL-012", "Motorola Moto G24", "Motorola", 4, "$250.000"}
        };
        vistaPrincipal.getTablaReportes().setModel(new DefaultTableModel(datos, columnas));
    }

    // 2. Reporte de Valorización (Cálculo financiero agrupado)
    private void reporteValorizacion() {
        // Lógica futura: SELECT categoria, SUM(stock), SUM(stock * precio) FROM productos GROUP BY categoria
        String[] columnas = {"Categoría", "Cantidad Total de Ítems", "Valor Total Invertido"};
        Object[][] datos = {
                {"Celulares", 145, "$45.500.000"},
                {"Accesorios", 320, "$2.800.000"},
                {"Hardware", 85, "$12.300.000"}
        };

        // Fila extra para el total general
        Object[][] datosConTotal = new Object[datos.length + 1][3];
        System.arraycopy(datos, 0, datosConTotal, 0, datos.length);
        datosConTotal[datos.length] = new Object[]{"TOTAL GENERAL", 550, "$60.600.000"};

        vistaPrincipal.getTablaReportes().setModel(new DefaultTableModel(datosConTotal, columnas));
    }

    // 3. Reporte de Movimientos (Auditoría de inventario)
    private void reporteMovimientos() {
        LocalDate[] fechas = pedirRangoFechas("Rango para Movimientos de Inventario");
        if (fechas == null) return;

        // Lógica futura: Consultar tabla 'movimientos' por rango de fecha
        String[] columnas = {"Fecha", "Usuario", "Acción", "Producto", "Cant."};
        Object[][] datos = {
                {fechas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "admin_juan", "Entrada (+)", "Samsung S23", "20"},
                {fechas[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "gerente_ana", "Ajuste (-)", "Funda Silicona", "2"}
        };
        vistaPrincipal.getTablaReportes().setModel(new DefaultTableModel(datos, columnas));
    }

    // 4. Auditoría de Usuarios (Altas y Bajas recientes)
    private void reporteAuditoriaUsuarios() {
        LocalDate[] fechas = pedirRangoFechas("Rango para Auditoría de Usuarios");
        if (fechas == null) return;

        // Lógica futura: Consultar tabla 'usuarios' filtrando por fechaCreacion o fechaModificacion
        String[] columnas = {"Fecha", "Username", "Rol", "Acción Registrada"};
        Object[][] datos = {
                {fechas[0].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "vendedor_nuevo", "Vendedor", "Alta de Usuario"},
                {fechas[1].format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), "vendedor_viejo", "Vendedor", "Baja Lógica"}
        };
        vistaPrincipal.getTablaReportes().setModel(new DefaultTableModel(datos, columnas));
    }

    // --- MÉTODO REUTILIZABLE PARA PEDIR FECHAS ---
    private LocalDate[] pedirRangoFechas(String titulo) {
        Integer[] dias = generarRango(1, 31);
        String[] meses = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        int anioActual = LocalDate.now().getYear();
        Integer[] anios = generarRango(anioActual - 5, anioActual);

        JComboBox<Integer> cbDiaDesde = new JComboBox<>(dias);
        JComboBox<String> cbMesDesde = new JComboBox<>(meses);
        JComboBox<Integer> cbAnioDesde = new JComboBox<>(anios);
        cbAnioDesde.setSelectedItem(anioActual);

        JComboBox<Integer> cbDiaHasta = new JComboBox<>(dias);
        JComboBox<String> cbMesHasta = new JComboBox<>(meses);
        JComboBox<Integer> cbAnioHasta = new JComboBox<>(anios);
        cbAnioHasta.setSelectedItem(anioActual);

        JPanel panelFechas = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel panelDesde = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelDesde.add(new JLabel("Desde:"));
        panelDesde.add(cbDiaDesde); panelDesde.add(new JLabel("/"));
        panelDesde.add(cbMesDesde); panelDesde.add(new JLabel("/"));
        panelDesde.add(cbAnioDesde);

        JPanel panelHasta = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelHasta.add(new JLabel("Hasta: "));
        panelHasta.add(cbDiaHasta); panelHasta.add(new JLabel("/"));
        panelHasta.add(cbMesHasta); panelHasta.add(new JLabel("/"));
        panelHasta.add(cbAnioHasta);

        panelFechas.add(panelDesde);
        panelFechas.add(panelHasta);

        int result = JOptionPane.showConfirmDialog(vistaPrincipal, panelFechas,
                titulo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int diaD = (int) cbDiaDesde.getSelectedItem();
                int mesD = Integer.parseInt(cbMesDesde.getSelectedItem().toString());
                int anioD = (int) cbAnioDesde.getSelectedItem();

                int diaH = (int) cbDiaHasta.getSelectedItem();
                int mesH = Integer.parseInt(cbMesHasta.getSelectedItem().toString());
                int anioH = (int) cbAnioHasta.getSelectedItem();

                LocalDate desde = LocalDate.of(anioD, mesD, diaD);
                LocalDate hasta = LocalDate.of(anioH, mesH, diaH);

                if (desde.isAfter(hasta)) {
                    JOptionPane.showMessageDialog(vistaPrincipal, "La fecha 'Desde' no puede ser posterior a 'Hasta'.", "Rango Inválido", JOptionPane.WARNING_MESSAGE);
                    return null;
                }

                long mesesDiferencia = ChronoUnit.MONTHS.between(desde, hasta);
                if (mesesDiferencia > 6) {
                    JOptionPane.showMessageDialog(vistaPrincipal, "Para optimizar la base de datos, el rango máximo permitido es de 6 meses.", "Rango Excesivo", JOptionPane.WARNING_MESSAGE);
                    return null;
                }

                return new LocalDate[]{desde, hasta};

            } catch (DateTimeException ex) {
                JOptionPane.showMessageDialog(vistaPrincipal, "Combinación de fecha no válida (ej: 31 de febrero).", "Fecha Inexistente", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
        return null;
    }

    private Integer[] generarRango(int inicio, int fin) {
        Integer[] rango = new Integer[fin - inicio + 1];
        for (int i = 0; i < rango.length; i++) rango[i] = inicio + i;
        return rango;
    }
}
