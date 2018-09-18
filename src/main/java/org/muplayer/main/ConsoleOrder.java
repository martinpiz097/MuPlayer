package org.muplayer.main;

import java.util.HashMap;

public class ConsoleOrder {
    public static final String START = "st";
    public static final String ISSTARTED = "ist";
    public static final String PLAY = "pl"; // Puede ser para reproducir una cancion especifica
    public static final String STOP = "s";
    public static final String RESUME = "r";
    public static final String PAUSE = "ps";
    public static final String NEXT = "n";
    public static final String PREV = "p";
    public static final String JUMP = "j";
    public static final String MUTE = "m";
    public static final String UNMUTE = "um";
    public static final String LIST = "l";
    public static final String GETGAIN = "gv";
    public static final String SETGAIN = "v";
    public static final String SHUTDOWN = "sh";
    public static final String SEEK = "sk";
    public static final String SEEKFLD = "skf";
    public static final String GOTOSEC = "gt";
    public static final String GETCOVER = "cover";
    public static final String GETINFO = "info";
    public static final String GETPROGRESS = "prog";
    public static final String HELP = "h";
    public static final HashMap<String, String> HELP_MAP;

    static {
        HELP_MAP = new HashMap<>();
        HELP_MAP.put(START, "Inicia el reproductor");
        HELP_MAP.put(ISSTARTED, "Comprueba que el reproductor está iniciado");
        HELP_MAP.put(PLAY, "Inicia la reproducción");
        HELP_MAP.put(STOP, "Detiene la canción actual");
        HELP_MAP.put(RESUME, "Inicia la canción actual si está pausada o detenida");
        HELP_MAP.put(PAUSE, "Pausa la canción actual");
        HELP_MAP.put(NEXT, "Reproduce la canción siguiente");
        HELP_MAP.put(PREV, "Reproduce la canción anterior");
        HELP_MAP.put(JUMP, "Salta canciones con un valor determinado");
        HELP_MAP.put(MUTE, "Silencia el reproductor");
        HELP_MAP.put(UNMUTE, "Quita el silencio al reproductor");
        HELP_MAP.put(GETGAIN, "Obtiene el volumen actual del reproductor");
        HELP_MAP.put(SETGAIN, "Cambia el volumen del reproductor (valor entre 0 y 100 ambos incluidos)");
        HELP_MAP.put(SHUTDOWN, "Apaga el reproductor y finaliza la ejecución de MuPlayer");
        HELP_MAP.put(SEEK, "Salta segundos de la canción actual");
        HELP_MAP.put(SEEKFLD, "Salta carpetas");
        HELP_MAP.put(GOTOSEC, "Se dirige a un segundo específico de la canción actual");
        HELP_MAP.put(GETCOVER, "Obtiene la carátula de la canción actual si existe " +
                "y la guarda en la ruta seleccionada");
        HELP_MAP.put(GETINFO, "Obtiene información de la canción actual");
        HELP_MAP.put(GETPROGRESS, "Obtiene los segundos actuales reproducidos de la canción actual");
    }
}
