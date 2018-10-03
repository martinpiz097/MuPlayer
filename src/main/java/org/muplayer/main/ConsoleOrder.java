package org.muplayer.main;

import java.util.Map;
import java.util.TreeMap;

public class ConsoleOrder {
    public static final String START = "st";
    public static final String ISSTARTED = "ist";
    public static final String PLAY = "pl"; // Puede ser para reproducir una cancion especifica
    public static final String STOP = "s";
    public static final String RESUME = "r";
    public static final String PAUSE = "ps";
    public static final String NEXT = "n";
    public static final String PREV = "p";
    //public static final String JUMP = "j";
    public static final String MUTE = "m";
    public static final String UNMUTE = "um";
    public static final String LIST1 = "l";
    public static final String LIST2 = "list";
    public static final String LISTCURRENTFOLDER = "lc";
    public static final String LISTFOLDERS = "lf";
    public static final String GETGAIN = "gv";
    public static final String SETGAIN = "v";
    public static final String SHUTDOWN = "sh";
    public static final String EXIT = "exit";
    public static final String QUIT = "quit";
    public static final String SEEK = "k";
    public static final String SEEKFLD = "skf";
    public static final String RELOAD = "u";
    public static final String GOTOSEC = "g";
    public static final String SOUNDCOUNT = "c";
    public static final String DURATION = "d";
    public static final String GETCOVER = "cover";
    public static final String GETINFO = "info";
    public static final String GETPROGRESS = "prog";
    public static final String HELP1 = "h";
    public static final String HELP2 = "help";
    public static final String FORMAT = "format";
    public static final Map<String, String> HELP_MAP;

    static {
        HELP_MAP = new TreeMap<>();
        HELP_MAP.put(START, "Inicia el reproductor");
        HELP_MAP.put(ISSTARTED, "Comprueba que el reproductor está iniciado");
        HELP_MAP.put(PLAY, "Inicia la reproducción");
        HELP_MAP.put(STOP, "Detiene la canción actual");
        HELP_MAP.put(RESUME, "Inicia la canción actual si está pausada o detenida");
        HELP_MAP.put(PAUSE, "Pausa la canción actual");
        HELP_MAP.put(NEXT, "Reproduce la canción siguiente");
        HELP_MAP.put(PREV, "Reproduce la canción anterior");
        //HELP_MAP.put(JUMP, "Salta canciones con un valor determinado");
        HELP_MAP.put(MUTE, "Silencia el reproductor");
        HELP_MAP.put(UNMUTE, "Quita el silencio al reproductor");
        HELP_MAP.put(LIST1, "Lista todas las canciones disponibles en el reproductor");
        HELP_MAP.put(LIST2, "Lista todas las canciones disponibles en el reproductor");
        HELP_MAP.put(LISTCURRENTFOLDER, "Lista todas las canciones de la carpeta actual el reproducción");
        HELP_MAP.put(LISTFOLDERS, "Lista todas las carpetas leídas por el reproductor");
        HELP_MAP.put(GETGAIN, "Obtiene el volumen actual del reproductor");
        HELP_MAP.put(SETGAIN, "Cambia el volumen del reproductor (valor entre 0 y 100 ambos incluidos)");
        HELP_MAP.put(SHUTDOWN, "Apaga el reproductor");
        HELP_MAP.put(EXIT, "Apaga el reproductor y finaliza la ejecución de MuPlayer");
        HELP_MAP.put(QUIT, "Apaga el reproductor y finaliza la ejecución de MuPlayer");
        HELP_MAP.put(SEEK, "Salta segundos de la canción actual");
        HELP_MAP.put(SEEKFLD, "Salta carpetas");
        HELP_MAP.put(RELOAD, "Vuelve a cargar las canciones nuevamente");
        HELP_MAP.put(GOTOSEC, "Se dirige a un segundo específico de la canción actual");
        HELP_MAP.put(SOUNDCOUNT, "Obtiene la cantidad de canciones cargadas");
        HELP_MAP.put(DURATION, "Obtiene la duración de la canción actual");
        HELP_MAP.put(GETCOVER, "Obtiene la carátula de la canción actual si existe " +
                "y la guarda en la ruta seleccionada");
        HELP_MAP.put(GETINFO, "Obtiene información de la canción actual");
        HELP_MAP.put(GETPROGRESS, "Obtiene los segundos actuales reproducidos de la canción actual");
        HELP_MAP.put(HELP1, "Despliega el menú de ayuda de comandos");
        HELP_MAP.put(HELP2, "Despliega el menú de ayuda de comandos");
        HELP_MAP.put(FORMAT, "Obtiene el formato de audio de la canción");
    }
}
