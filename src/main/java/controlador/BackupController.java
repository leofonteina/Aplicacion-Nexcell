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
    private final String DB_PASS = "Amir11022013"; // Aca pones tu contraseña de MYSQLWorkbench
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

            java.time.LocalDateTime fechaProgramada = java.time.LocalDateTime.of(anio, mes, dia, hora, minutos);
            java.time.LocalDateTime ahora = java.time.LocalDateTime.now();

            if (fechaProgramada.isBefore(ahora)) {
                vista.mostrarMensaje("La fecha y hora deben ser a futuro.", "Error de Fecha", 0);
                return;
            }

            String carpetaDestino = "C:\\backups_nexcell";
            new java.io.File(carpetaDestino).mkdirs();

            // 1. Crear el Script de PowerShell (.ps1) con lógica de Base de Datos y Correo
            String rutaPs1 = carpetaDestino + "\\script_backup_nexcell.ps1";
            java.io.FileWriter writerPs = new java.io.FileWriter(rutaPs1);

            writerPs.write("$fecha = Get-Date -Format 'dd-MM-yyyy_HH-mm'\n");
            writerPs.write("$carpeta = 'C:\\backups_nexcell'\n");
            writerPs.write("$archivo = \"$carpeta\\backup_auto_$fecha.sql\"\n\n");

            // Ejecuta el Backup
            writerPs.write(String.format("& \"%s\" -h 127.0.0.1 -P 3307 -u %s --password=\"%s\" %s -r $archivo\n\n",
                CMD_MYSQLDUMP, DB_USER, DB_PASS, DB_NAME));

            // Configuración del Correo SMTP
            writerPs.write("$SmtpServer = 'smtp.gmail.com'\n");
            writerPs.write("$SmtpPort = 587\n");
            writerPs.write("$Username = 'agustin552689@gmail.com' # Correo del usuario\n");
            writerPs.write("$Password = 'clewkexbrypxmldd' # Contraseña generada por google\n");
            writerPs.write("$Destino = 'agusagomez19@gmail.com' # correo del gerente\n\n");

            writerPs.write("$Message = New-Object System.Net.Mail.MailMessage\n");
            writerPs.write("$Message.From = $Username\n");
            writerPs.write("$Message.To.Add($Destino)\n");
            writerPs.write("$SMTPClient = New-Object Net.Mail.SmtpClient($SmtpServer, $SmtpPort)\n");
            writerPs.write("$SMTPClient.EnableSsl = $true\n");
            writerPs.write("$SMTPClient.Credentials = New-Object System.Net.NetworkCredential($Username, $Password)\n\n");

            // Lógica de Notificación Inteligente
            writerPs.write("if (Test-Path $archivo) {\n");
            writerPs.write("    $Message.Subject = \"Nexcell - Backup Exitoso ($fecha)\"\n");
            writerPs.write("    $Message.Body = \"El respaldo se realizo correctamente. Se adjunta el archivo de seguridad SQL.\"\n");
            writerPs.write("    $Attachment = New-Object System.Net.Mail.Attachment($archivo)\n");
            writerPs.write("    $Message.Attachments.Add($Attachment)\n");
            writerPs.write("    $SMTPClient.Send($Message)\n");
            writerPs.write("    $Attachment.Dispose()\n");
            writerPs.write("} else {\n");
            writerPs.write("    $Message.Subject = \"Nexcell - ERROR CRITICO DE BACKUP ($fecha)\"\n");
            writerPs.write("    $Message.Body = \"ATENCION: Fallo la comunicacion con MySQL. No se pudo crear el archivo de respaldo.\"\n");
            writerPs.write("    $SMTPClient.Send($Message)\n");
            writerPs.write("}\n");
            writerPs.close();

            // 2. Crear el XML de configuración para el Sistema Operativo
            String rutaXml = carpetaDestino + "\\tarea_backup.xml";
            java.io.FileWriter writerXml = new java.io.FileWriter(rutaXml);
            String startBoundary = String.format("%04d-%02d-%02dT%02d:%02d:00", anio, mes, dia, hora, minutos);

            writerXml.write("<Task version=\"1.2\" xmlns=\"http://schemas.microsoft.com/windows/2004/02/mit/task\">\n");
            writerXml.write("  <Triggers>\n    <TimeTrigger>\n      <StartBoundary>" + startBoundary + "</StartBoundary>\n      <Enabled>true</Enabled>\n    </TimeTrigger>\n  </Triggers>\n");
            writerXml.write("  <Settings>\n    <StartWhenAvailable>true</StartWhenAvailable>\n    <WakeToRun>true</WakeToRun>\n  </Settings>\n");
            writerXml.write("  <Actions>\n    <Exec>\n");
            writerXml.write("      <Command>powershell.exe</Command>\n");
            // Ejecuta PowerShell de forma totalmente invisible para el usuario
            writerXml.write("      <Arguments>-ExecutionPolicy Bypass -WindowStyle Hidden -File \"" + rutaPs1 + "\"</Arguments>\n");
            writerXml.write("    </Exec>\n  </Actions>\n");
            writerXml.write("</Task>\n");
            writerXml.close();

            // 3. Registrar la tarea en Windows
            String comandoSchtasks = String.format("schtasks /create /tn \"Nexcell_Backup_Auto\" /xml \"%s\" /f", rutaXml);
            Process proceso = Runtime.getRuntime().exec(comandoSchtasks);
            int exitCode = proceso.waitFor();

            if (exitCode == 0) {
                String fechaVisual = String.format("%02d/%02d/%04d %02d:%02d", dia, mes, anio, hora, minutos);
                vista.mostrarMensaje("Backup Empresarial Configurado: " + fechaVisual +
                    "\n\nSe realizará la copia local y se enviará por correo automáticamente al finalizar.", "Éxito", 1);
            } else {
                vista.mostrarMensaje("Windows rechazó la orden. Ejecute su IDE como Administrador.", "Error de Permisos", 0);
            }

        } catch (Exception ex) {
            vista.mostrarMensaje("Error crítico: " + ex.getMessage(), "Error", 0);
            ex.printStackTrace();
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
