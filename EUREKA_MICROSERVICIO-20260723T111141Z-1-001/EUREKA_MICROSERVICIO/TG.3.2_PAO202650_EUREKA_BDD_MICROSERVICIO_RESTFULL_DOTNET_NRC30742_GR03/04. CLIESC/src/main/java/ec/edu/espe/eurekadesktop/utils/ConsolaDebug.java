package ec.edu.espe.eurekadesktop.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsolaDebug {
    private static final boolean HABILITADO = true;
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public static void log(String titulo, String mensaje) {
        if (!HABILITADO) return;
        
        String timestamp = LocalDateTime.now().format(FORMATO);
        String separator = "=".repeat(60);
        
        System.out.println();
        System.out.println(separator);
        System.out.println("[" + timestamp + "] " + titulo);
        System.out.println(separator);
        System.out.println(mensaje);
        System.out.println(separator);
    }

    public static void error(String titulo, String detalle) {
        if (!HABILITADO) return;
        
        String timestamp = LocalDateTime.now().format(FORMATO);
        String separator = "-".repeat(60);
        
        System.out.println();
        System.out.println("[ERROR] " + titulo);
        System.out.println(separator);
        System.out.println(detalle);
        System.out.println(separator);
    }

    public static void info(String mensaje) {
        if (!HABILITADO) return;
        System.out.println("[INFO] " + mensaje);
    }
}