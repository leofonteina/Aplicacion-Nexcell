package controlador;

import modelo.Admin;
import modelo.Gerente;
import modelo.Usuario;
import repositorio.UsuarioRepository;
import vista.BackupUI;

import jakarta.persistence.EntityManager;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupController {

    private BackupUI vista;
    private EntityManager em;
    private ScheduledExecutorService planificador;

    // ==============================================================
    // CONFIGURACIÓN DE BASE DE DATOS (COMPLETA ESTOS 3 DATOS)
    // ==============================================================
    private final String DB_USER = "root";
    private final String DB_PASS = "MessiElMasMejor123"; // Aca pones tu contraseña de MYSQLWorkbench
    private final String DB_NAME = "nexcell_db";

    private final String CMD_MYSQLDUMP = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe"; // Cambiar dependiendo de la direccion de tu SGBD
    private final String CMD_MYSQL = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe";
    // ==============================================================

    public BackupController(BackupUI vista, EntityManager em) {
        this.vista = vista;
        this.em = em;
        this.planificador = Executors.newScheduledThreadPool(1);

        this.vista.getBtnGenerarBackup().addActionListener(e -> generarBackupManual());
        this.vista.getBtnProgramarBackup().addActionListener(e -> programarBackup());
        this.vista.getBtnRestaurar().addActionListener(e -> iniciarRestauracionSegura());
    }

    private void generarBackupManual() {
        String nombreSugerido = "nexcell_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm")) + ".sql";
        String rutaDestino = vista.seleccionarDestinoGuardar(nombreSugerido);

        if (rutaDestino != null) {
            ejecutarBackup(rutaDestino, true);
        }
    }

    private void programarBackup() {
        try {
            int dia = (int) vista.getCbDia().getSelectedItem();
            int mes = (int) vista.getCbMes().getSelectedItem();
            int anio = (int) vista.getCbAnio().getSelectedItem();
            int hora = (int) vista.getCbHora().getSelectedItem();
            int minutos = (int) vista.getCbMinutos().getSelectedItem();

            LocalDateTime fechaProgramada = LocalDateTime.of(anio, mes, dia, hora, minutos);
            LocalDateTime ahora = LocalDateTime.now();

            if (fechaProgramada.isBefore(ahora)) {
                vista.mostrarMensaje("La fecha y hora deben ser a futuro.", "Error de Fecha", 0); // 0 = ERROR_MESSAGE
                return;
            }

            long minutosDeRetraso = ChronoUnit.MINUTES.between(ahora, fechaProgramada);
            String rutaAutomatica = "C:\\backups_nexcell\\backup_automatico.sql";

            new File("C:\\backups_nexcell").mkdirs();

            planificador.schedule(() -> ejecutarBackup(rutaAutomatica, false), minutosDeRetraso, TimeUnit.MINUTES);

            vista.mostrarMensaje("Backup programado correctamente para el " + fechaProgramada.toString(), "Éxito", 1); // 1 = INFORMATION_MESSAGE

        } catch (Exception ex) {
            vista.mostrarMensaje("Error al programar el backup. Revise las fechas.", "Error", 0);
        }
    }

    private void ejecutarBackup(String ruta, boolean mostrarMensaje) {
        try {
            // Usamos una lista para evitar mandar espacios vacíos si no hay contraseña
            java.util.List<String> comando = new java.util.ArrayList<>();
            comando.add(CMD_MYSQLDUMP);

            // Forzamos la conexión por TCP/IP para evitar el error 2059 ---
            comando.add("-h");
            comando.add("127.0.0.1");
            comando.add("-P");
            comando.add("3307"); // Aca pones tu puerto en concreto

            comando.add("-u");
            comando.add(DB_USER);

            // Forma más segura de pasar la contraseña
            if (!DB_PASS.isEmpty()) {
                comando.add("--password=" + DB_PASS);
            }

            comando.add(DB_NAME);
            comando.add("-r");
            comando.add(ruta);

            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectErrorStream(true); // Une los errores con la salida normal para poder leerlos
            Process proceso = pb.start();

            // LECTOR: Captura exactamente qué está diciendo MySQL por detrás
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(proceso.getInputStream()));
            String linea;
            StringBuilder errorReal = new StringBuilder();
            while ((linea = reader.readLine()) != null) {
                errorReal.append(linea).append("\n");
                System.out.println("MySQL dice: " + linea); // También lo imprime en la consola de tu IDE
            }

            int exitCode = proceso.waitFor();

            if (exitCode == 0) {
                if (mostrarMensaje) vista.mostrarMensaje("Backup completado y guardado en:\n" + ruta, "Éxito", 1);
            } else {
                // Ahora la ventanita nos va a mostrar el error exacto
                if (mostrarMensaje) vista.mostrarMensaje("MySQL rechazó la orden. Detalle:\n" + errorReal.toString(), "Error de MySQL", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mostrarMensaje) vista.mostrarMensaje("Error crítico de sistema.", "Error", 0);
        }
    }

    private void iniciarRestauracionSegura() {
        // 0 = YES_OPTION en JOptionPane
        if (vista.pedirConfirmacionRestauracion() != 0) return;

        // Validar doble seguridad
        String[] credenciales = vista.pedirDobleAutorizacion();

        if (credenciales == null || !validarCredenciales(credenciales[0], credenciales[1], credenciales[2], credenciales[3])) {
            vista.mostrarMensaje("Autorización denegada. Credenciales incorrectas o insuficientes.", "Acceso Denegado", 0);
            return;
        }

        // Si es válido, pedir archivo y restaurar
        String rutaArchivo = vista.seleccionarArchivoAbrir();
        if (rutaArchivo != null) {
            ejecutarRestore(rutaArchivo);
        }
    }

    private boolean validarCredenciales(String uAdmin, String pAdmin, String uGerente, String pGerente) {
        UsuarioRepository repo = new UsuarioRepository(this.em);
        Usuario cuentaAdmin = repo.buscarPorUsername(uAdmin);
        Usuario cuentaGerente = repo.buscarPorUsername(uGerente);

        boolean adminValido = cuentaAdmin != null && cuentaAdmin.getPassword().equals(pAdmin) && cuentaAdmin instanceof Admin;
        boolean gerenteValido = cuentaGerente != null && cuentaGerente.getPassword().equals(pGerente) && cuentaGerente instanceof Gerente;

        return adminValido && gerenteValido;
    }

    // Método que permite la ejecución del Restore de la BD
    private void ejecutarRestore(String ruta) {
        try {
            // Usamos la misma estructura exacta que te funcionó en el backup
            java.util.List<String> comando = new java.util.ArrayList<>();
            comando.add(CMD_MYSQL);

            // Forzamos la conexión por TCP/IP usando TU puerto
            comando.add("-h");
            comando.add("127.0.0.1");
            comando.add("-P");
            comando.add("3307"); // EL PUERTO CLAVE

            comando.add("-u");
            comando.add(DB_USER);

            // Forma más segura de pasar la contraseña, igual que en el backup
            if (!DB_PASS.isEmpty()) {
                comando.add("--password=" + DB_PASS);
            }

            comando.add(DB_NAME);

            ProcessBuilder pb = new ProcessBuilder(comando);

            // LA DIFERENCIA: En vez de "-r", hacemos que Java lea el archivo y se lo tire a MySQL
            pb.redirectInput(new java.io.File(ruta));
            pb.redirectErrorStream(true);

            Process proceso = pb.start();

            // LECTOR
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(proceso.getInputStream()));
            String linea;
            StringBuilder errorReal = new StringBuilder();
            while ((linea = reader.readLine()) != null) {
                if (!linea.contains("Using a password on the command line interface can be insecure")) {
                    errorReal.append(linea).append("\n");
                }
                System.out.println("MySQL Restore dice: " + linea);
            }

            int exitCode = proceso.waitFor();

            if (exitCode == 0) {
                vista.mostrarMensaje("Base de datos restaurada correctamente.\nSe recomienda reiniciar el sistema para refrescar las tablas.", "Éxito", 1);
            } else {
                vista.mostrarMensaje("MySQL rechazó la orden. Detalle:\n" + errorReal.toString(), "Error de MySQL", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            vista.mostrarMensaje("Error crítico de sistema.", "Error", 0);
        }
    }
}