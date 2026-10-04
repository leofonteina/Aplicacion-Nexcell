package utilidades;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GestorLogs {

    // Aca va la ruta donde se guardan el archivo
    private static final String RUTA_CARPETA = "Auditoria";
    private static final String RUTA_ARCHIVO = RUTA_CARPETA + "\\historial_nexcell.log";

    // Las 3 categorías de gravedad para los eventos
    public enum Nivel {
        INFO,       // Para acciones normales (iniciar sesión, vender, registrar cliente)
        ALERTA,     // Para acciones de cuidado (dar de baja un cliente, poco stock)
        ERROR       // Para excepciones y fallos del sistema (errores de base de datos)
    }

    public static void registrar(Nivel nivel, String modulo, String mensaje) {
        try {
            // Se verifica que la carpeta exista y si no la crea automaticamente
            File carpeta = new File(RUTA_CARPETA);
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            // Aca formatea la hora exacta del evento
            String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            // Armamos el renglon que lleva la informacion de que se hizo ahi
            String lineaLog = String.format("[%s] [%s] [%s] - %s", fechaHora, nivel.name(), modulo.toUpperCase(), mensaje);

            // 4. Escribimos en el archivo usamos true para que no sobreescriba lo que teniamos, lo escribe abajo no mas
            try (PrintWriter out = new PrintWriter(new FileWriter(RUTA_ARCHIVO, true))) {
                out.println(lineaLog);
            }

            // Tambien lo imprimimos en la consola de del IDE asi vemos como se van agregando
            System.out.println(lineaLog);

        } catch (IOException e) {
            System.err.println("No se pudo escribir en el archivo de log: " + e.getMessage());
        }
    }
}
