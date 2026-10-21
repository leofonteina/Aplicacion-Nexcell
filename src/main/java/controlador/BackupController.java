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

    private final String DB_USER = "root";
    private final String DB_PASS = "MessiElMasMejor123";
    private final String DB_NAME = "nexcell_db";

    private final String CMD_MYSQLDUMP = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe";
    private final String CMD_MYSQL = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe";

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
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "BACKUP",
                "Se inició la generación de un backup manual.");
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

            String rutaPs1 = carpetaDestino + "\\script_backup_nexcell.ps1";
            java.io.FileWriter writerPs = new java.io.FileWriter(rutaPs1);

            writerPs.write("$fecha = Get-Date -Format 'dd-MM-yyyy_HH-mm'\n");
            writerPs.write("$carpeta = 'C:\\backups_nexcell'\n");
            writerPs.write("$archivo = \"$carpeta\\backup_auto_$fecha.sql\"\n\n");

            writerPs.write(String.format("& \"%s\" -h 127.0.0.1 -P 3307 -u %s --password=\"%s\" %s -r $archivo\n\n",
                CMD_MYSQLDUMP, DB_USER, DB_PASS, DB_NAME));

            writerPs.write("$SmtpServer = 'smtp.gmail.com'\n");
            writerPs.write("$SmtpPort = 587\n");
            writerPs.write("$Username = 'agustin552689@gmail.com'\n");
            writerPs.write("$Password = 'clewkexbrypxmldd'\n");
            writerPs.write("$Destino = 'agusagomez19@gmail.com'\n\n");

            writerPs.write("$Message = New-Object System.Net.Mail.MailMessage\n");
            writerPs.write("$Message.From = $Username\n");
            writerPs.write("$Message.To.Add($Destino)\n");
            writerPs.write("$SMTPClient = New-Object Net.Mail.SmtpClient($SmtpServer, $SmtpPort)\n");
            writerPs.write("$SMTPClient.EnableSsl = $true\n");
            writerPs.write("$SMTPClient.Credentials = New-Object System.Net.NetworkCredential($Username, $Password)\n\n");

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

            String rutaXml = carpetaDestino + "\\tarea_backup.xml";
            java.io.FileWriter writerXml = new java.io.FileWriter(rutaXml);
            String startBoundary = String.format("%04d-%02d-%02dT%02d:%02d:00", anio, mes, dia, hora, minutos);

            writerXml.write("<Task version=\"1.2\" xmlns=\"http://schemas.microsoft.com/windows/2004/02/mit/task\">\n");
            writerXml.write("  <Triggers>\n    <TimeTrigger>\n      <StartBoundary>" + startBoundary + "</StartBoundary>\n      <Enabled>true</Enabled>\n    </TimeTrigger>\n  </Triggers>\n");
            writerXml.write("  <Settings>\n    <StartWhenAvailable>true</StartWhenAvailable>\n    <WakeToRun>true</WakeToRun>\n  </Settings>\n");
            writerXml.write("  <Actions>\n    <Exec>\n");
            writerXml.write("      <Command>powershell.exe</Command>\n");
            writerXml.write("      <Arguments>-ExecutionPolicy Bypass -WindowStyle Hidden -File \"" + rutaPs1 + "\"</Arguments>\n");
            writerXml.write("    </Exec>\n  </Actions>\n");
            writerXml.write("</Task>\n");
            writerXml.close();

            String comandoSchtasks = String.format("schtasks /create /tn \"Nexcell_Backup_Auto\" /xml \"%s\" /f", rutaXml);
            Process proceso = Runtime.getRuntime().exec(comandoSchtasks);
            int exitCode = proceso.waitFor();

            if (exitCode == 0) {
                String fechaVisual = String.format("%02d/%02d/%04d %02d:%02d", dia, mes, anio, hora, minutos);
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "BACKUP",
                    "El Administrador programó un backup automático para la fecha: " + fechaVisual);
                vista.mostrarMensaje("Backup Empresarial Configurado: " + fechaVisual +
                    "\n\nSe realizará la copia local y se enviará por correo automáticamente al finalizar.", "Éxito", 1);
            } else {
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "BACKUP",
                    "Fallo al programar backup en Windows. Posible falta de permisos de Administrador.");
                vista.mostrarMensaje("Windows rechazó la orden. Ejecute su IDE como Administrador.", "Error de Permisos", 0);
            }

        } catch (Exception ex) {
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "BACKUP",
                "Error crítico al intentar programar backup: " + ex.getMessage());
            vista.mostrarMensaje("Error crítico: " + ex.getMessage(), "Error", 0);
            ex.printStackTrace();
        }
    }

    private void ejecutarBackup(String ruta, boolean mostrarMensaje) {
        try {
            java.util.List<String> comando = new java.util.ArrayList<>();
            comando.add(CMD_MYSQLDUMP);
            comando.add("-h");
            comando.add("127.0.0.1");
            comando.add("-P");
            comando.add("3307");
            comando.add("-u");
            comando.add(DB_USER);

            if (!DB_PASS.isEmpty()) {
                comando.add("--password=" + DB_PASS);
            }

            comando.add(DB_NAME);
            comando.add("-r");
            comando.add(ruta);

            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectErrorStream(true);
            Process proceso = pb.start();

            // CORRECCIÓN: Cierre automático del flujo con try-with-resources para prevenir fugas de memoria
            StringBuilder errorReal = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(proceso.getInputStream()))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    errorReal.append(linea).append("\n");
                    System.out.println("MySQL dice: " + linea);
                }
            }

            int exitCode = proceso.waitFor();

            if (exitCode == 0) {
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "BACKUP",
                    "Backup manual generado con éxito en: " + ruta);
                if (mostrarMensaje) vista.mostrarMensaje("Backup completado y guardado en:\n" + ruta, "Éxito", 1);
            } else {
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "BACKUP",
                    "MySQL rechazó el backup manual. Detalle: " + errorReal.toString());
                if (mostrarMensaje) vista.mostrarMensaje("MySQL rechazó la orden. Detalle:\n" + errorReal.toString(), "Error de MySQL", 0);
            }
        } catch (Exception e) {
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "BACKUP",
                "Error de sistema al ejecutar mysqldump: " + e.getMessage());
            e.printStackTrace();
            if (mostrarMensaje) vista.mostrarMensaje("Error crítico de sistema.", "Error", 0);
        }
    }

    private void iniciarRestauracionSegura() {
        if (vista.pedirConfirmacionRestauracion() != 0) return;

        String[] credenciales = vista.pedirDobleAutorizacion();

        if (credenciales == null || !validarCredenciales(credenciales[0], credenciales[1], credenciales[2], credenciales[3])) {
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ALERTA, "SEGURIDAD",
                "Intento fallido de restauración de base de datos. Credenciales de doble factor incorrectas.");
            vista.mostrarMensaje("Autorización denegada. Credenciales incorrectas o insuficientes.", "Acceso Denegado", 0);
            return;
        }

        String rutaArchivo = vista.seleccionarArchivoAbrir();
        if (rutaArchivo != null) {
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ALERTA, "BACKUP",
                "Se inició una restauración de base de datos desde el archivo: " + rutaArchivo);
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

    private void ejecutarRestore(String ruta) {
        try {
            java.util.List<String> comando = new java.util.ArrayList<>();
            comando.add(CMD_MYSQL);
            comando.add("-h");
            comando.add("127.0.0.1");
            comando.add("-P");
            comando.add("3307");
            comando.add("-u");
            comando.add(DB_USER);

            if (!DB_PASS.isEmpty()) {
                comando.add("--password=" + DB_PASS);
            }

            comando.add(DB_NAME);

            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectInput(new java.io.File(ruta));
            pb.redirectErrorStream(true);

            Process proceso = pb.start();

            // CORRECCIÓN: Cierre automático del flujo con try-with-resources para prevenir fugas de memoria
            StringBuilder errorReal = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(proceso.getInputStream()))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    if (!linea.contains("Using a password on the command line interface can be insecure")) {
                        errorReal.append(linea).append("\n");
                    }
                    System.out.println("MySQL Restore dice: " + linea);
                }
            }

            int exitCode = proceso.waitFor();

            if (exitCode == 0) {
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.INFO, "BACKUP",
                    "Base de datos restaurada correctamente desde el archivo: " + ruta);
                vista.mostrarMensaje("Base de datos restaurada correctamente.\nSe recomienda reiniciar el sistema para refrescar las tablas.", "Éxito", 1);
            } else {
                utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "BACKUP",
                    "Error al restaurar base de datos. Detalle: " + errorReal.toString());
                vista.mostrarMensaje("MySQL rechazó la orden. Detalle:\n" + errorReal.toString(), "Error de MySQL", 0);
            }
        } catch (Exception e) {
            utilidades.GestorLogs.registrar(utilidades.GestorLogs.Nivel.ERROR, "BACKUP",
                "Error crítico al intentar restaurar base de datos: " + e.getMessage());
            e.printStackTrace();
            vista.mostrarMensaje("Error crítico de sistema.", "Error", 0);
        }
    }
}
