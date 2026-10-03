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
        this.vistaPrincipal.getBtnAbrirBackup().addActionListener(e -> abrirVentanaBackup());
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

        // Activar la búsqueda en tiempo real para Productos y Usuarios
        configurarBusqueda(this.vistaPrincipal.getBuscarProductoField(), this.vistaPrincipal.getBtnBuscarProducto(), this.vistaPrincipal.getTablaProductos());
        configurarBusqueda(this.vistaPrincipal.getTxtBuscarUsuario(), this.vistaPrincipal.getBtnBuscarUsuario(), this.vistaPrincipal.getTablaUsuarios());

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
        this.vistaPrincipal.getTablaProductos().setAutoCreateRowSorter(true);
        this.vistaPrincipal.getTablaUsuarios().setAutoCreateRowSorter(true);
        this.vistaPrincipal.getTablaReportes().setAutoCreateRowSorter(true);
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
        // 1. Leemos y validamos las fechas ANTES de generar cualquier reporte
        LocalDateTime[] fechas = obtenerFechasDesdeUI();

        // Si hay algún error con la fecha o supera los 6 meses, frenamos la ejecución acá
        if (fechas == null) {
            return;
        }

        // 2. Si las fechas están bien, vemos qué reporte eligió
        String reporte = vistaPrincipal.getComboReportes().getSelectedItem().toString();

        switch (reporte) {
            case "Productos con Bajo Stock":
                reporteBajoStock(); // Muestra el stock actual
                break;
            case "Valorización de Inventario":
                reporteValorizacion(); // Muestra el valor actual
                break;
            case "Auditoría de Usuarios":
                reporteAuditoriaUsuarios(); // Muestra el estado actual
                break;
            case "Movimientos de Inventario":
                reporteMovimientos(fechas[0], fechas[1]); // Filtra los movimientos en ese rango
                break;
        }
    }

    private void limpiarReporte() {
        vistaPrincipal.getTablaReportes().setModel(new DefaultTableModel());
    }

    // 1. Reporte de Bajo Stock (Consulta Real)
    private void reporteBajoStock() {
        // Trae productos activos que tengan 5 o menos unidades en stock
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

    // 2. Reporte de Valorización (Cálculo financiero agrupado)
    private void reporteValorizacion() {
        // Agrupa por categoría y multiplica el stock por el precio usando Hibernate
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

        // Fila extra para el total general
        modelo.addRow(new Object[]{"TOTAL GENERAL", totalItems, String.format("$%.2f", granTotal)});
        vistaPrincipal.getTablaReportes().setModel(modelo);
    }

    // 3. Reporte de Movimientos (Auditoría de inventario)
    // 3. Reporte de Movimientos (Auditoría de inventario corregida)
    private void reporteMovimientos(LocalDateTime inicio, LocalDateTime fin) {
        // En lugar de buscar Detalles, buscamos Ventas y extraemos sus detalles
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

    // 4. Auditoría de Usuarios (Altas y Bajas recientes)
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

    // --- MÉTODO PARA LEER LAS FECHAS DIRECTAMENTE DE LA INTERFAZ ---
    private LocalDateTime[] obtenerFechasDesdeUI() {
        try {
            // Armamos los textos leyendo los combobox de la vista
            String strInicio = vistaPrincipal.getCbDiaInicio().getSelectedItem() + "/" +
                vistaPrincipal.getCbMesInicio().getSelectedItem() + "/" +
                vistaPrincipal.getCbAnioInicio().getSelectedItem();

            String strFin = vistaPrincipal.getCbDiaFin().getSelectedItem() + "/" +
                vistaPrincipal.getCbMesFin().getSelectedItem() + "/" +
                vistaPrincipal.getCbAnioFin().getSelectedItem();

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate fechaInicio = LocalDate.parse(strInicio, fmt);
            LocalDate fechaFin = LocalDate.parse(strFin, fmt);
            LocalDate hoy = LocalDate.now();

            if (fechaInicio.isAfter(fechaFin)) {
                JOptionPane.showMessageDialog(vistaPrincipal, "La fecha 'Desde' no puede ser posterior a 'Hasta'.", "Fechas Inválidas", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            // LÍMITE DE 6 MESES
            LocalDate limiteAntiguedad = hoy.minusMonths(6);
            if (fechaInicio.isBefore(limiteAntiguedad)) {
                JOptionPane.showMessageDialog(vistaPrincipal, "Por políticas del sistema, solo puedes generar reportes con un máximo de 6 meses de antigüedad.\nLímite permitido: " + limiteAntiguedad.format(fmt), "Límite Excedido", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            // Convertimos las fechas a LocalDateTime (Inicio a las 00:00:00 y Fin a las 23:59:59)
            return new LocalDateTime[]{fechaInicio.atStartOfDay(), fechaFin.atTime(23, 59, 59)};

        } catch (DateTimeException ex) {
            JOptionPane.showMessageDialog(vistaPrincipal, "La fecha seleccionada no existe en el calendario.", "Error de Fecha", JOptionPane.ERROR_MESSAGE);
            return null;
        }
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

    // === MÉTODOS DE BÚSQUEDA DINÁMICA ===
    private void configurarBusqueda(JTextField campo, JButton boton, JTable tabla) {
        // Buscar al hacer clic en el botón
        boton.addActionListener(e -> aplicarFiltro(campo, tabla));

        // Buscar al presionar Enter
        campo.addActionListener(e -> aplicarFiltro(campo, tabla));

        // Buscar en tiempo real mientras se escribe
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
                // Filtra ignorando mayúsculas/minúsculas
                sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto)));
            }
        }
    }

    private void abrirVentanaBackup() {
        vista.BackupUI ventanaBackup = new vista.BackupUI();
        // Le pasamos el EntityManager que este controlador ya posee
        new BackupController(ventanaBackup, this.em);
        ventanaBackup.setVisible(true);
    }
}
