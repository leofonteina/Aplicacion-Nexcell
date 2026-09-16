package controlador;

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

import jakarta.persistence.EntityManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

        // Escuchadores de Búsqueda de Usuarios (CORREGIDO: Ahora están adentro del constructor)
        this.vistaPrincipal.getBtnBuscarUsuario().addActionListener(e -> filtrarTablaUsuarios());
        this.vistaPrincipal.getTxtBuscarUsuario().addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filtrarTablaUsuarios();
            }
        });

        cargarTablaUsuarios();
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
                cargarTablaProductos(); // Refrescamos la tabla

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

        // Cargar los datos actuales en el formulario
        ventana.getNombreField().setText(productoActual.getNombre());
        ventana.getDescripcionArea().setText(productoActual.getDescripcion());
        ventana.getPrecioField().setText(String.valueOf(productoActual.getPrecio()));
        ventana.getDescuentoField().setText(String.valueOf(productoActual.getDescuento()));
        ventana.getStockField().setText(String.valueOf(productoActual.getStock()));
        ventana.getCategoriaBox().setSelectedItem(productoActual.getCategoria());
        ventana.getMarcaBox().setSelectedItem(productoActual.getMarca());

        ventana.getBtnGuardarProducto().addActionListener(e -> {
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

        // Cargamos los datos
        ventanaModificacion.getTxtUsername().setText(usuario.getUsername());
        ventanaModificacion.getTxtUsername().setText(usuario.getUsername()); // Lo ideal es que el username tampoco se pueda cambiar

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

    private void generarReporte() {
        String reporte = vistaPrincipal.getComboReportes().getSelectedItem().toString();
        if (reporte.equals("Stock de productos")) reporteStock();
        else if (reporte.equals("Productos registrados")) reporteProductos();
        else if (reporte.equals("Usuarios del sistema")) reporteUsuarios();
        else reporteMovimientos();
    }

    private void limpiarReporte() {
        vistaPrincipal.getTablaReportes().setModel(new DefaultTableModel());
    }

    private Integer[] generarRango(int inicio, int fin) {
        Integer[] rango = new Integer[fin - inicio + 1];
        for (int i = 0; i < rango.length; i++) rango[i] = inicio + i;
        return rango;
    }

    private void reporteUsuarios() {
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
            "Filtro Obligatorio de Fechas (Máx 6 meses)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

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
                    return;
                }

                long mesesDiferencia = ChronoUnit.MONTHS.between(desde, hasta);
                if (mesesDiferencia > 6) {
                    JOptionPane.showMessageDialog(vistaPrincipal, "Para optimizar la base de datos, el rango máximo permitido es de 6 meses.", "Rango Excesivo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                UsuarioRepository repo = new UsuarioRepository(em);
                List<Usuario> usuarios = repo.reporteUsuariosPorFecha(desde, hasta);

                String[] columnas = {"Usuario", "Rol", "Estado", "Fecha Registro"};
                DefaultTableModel modelo = new DefaultTableModel(null, columnas);

                for (Usuario u : usuarios) {
                    String estadoVisual = (u.getEstado() != null && u.getEstado()) ? "Activo" : "Inactivo";
                    String rol = u.getClass().getSimpleName();
                    String fecha = (u.getFechaRegistro() != null) ? u.getFechaRegistro().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
                    modelo.addRow(new Object[]{u.getUsername(), rol, estadoVisual, fecha});
                }

                vistaPrincipal.getTablaReportes().setModel(modelo);

                if (usuarios.isEmpty()) {
                    JOptionPane.showMessageDialog(vistaPrincipal, "No se encontraron usuarios en ese rango de fechas.", "Reporte Vacío", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (DateTimeException ex) {
                JOptionPane.showMessageDialog(vistaPrincipal, "Combinación de fecha no válida (ej: 31 de febrero).", "Fecha Inexistente", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reporteStock() {
        String[] columnas = {"Código", "Producto", "Stock", "Precio", "Estado"};
        Object[][] datos = { {"CEL001", "Motorola Edge 60", 15, "$850.000", "Normal"} };
        vistaPrincipal.getTablaReportes().setModel(new DefaultTableModel(datos, columnas));
    }

    private void reporteProductos() {
        String[] columnas = {"Código", "Producto", "Categoría", "Precio"};
        Object[][] datos = { {"CEL001", "Motorola Edge 60", "Celulares", "$850.000"} };
        vistaPrincipal.getTablaReportes().setModel(new DefaultTableModel(datos, columnas));
    }

    private void reporteMovimientos() {
        String[] columnas = {"Fecha", "Producto", "Movimiento", "Cantidad", "Usuario"};
        Object[][] datos = { {"28/08/2026", "Motorola Edge 60", "Entrada", "+10", "admin"} };
        vistaPrincipal.getTablaReportes().setModel(new DefaultTableModel(datos, columnas));
    }
}
