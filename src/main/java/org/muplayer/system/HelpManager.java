package org.muplayer.system;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import static org.muplayer.main.ConsoleOrder.*;

public class HelpManager {
    private File fileProps;
    private Properties properties;
    private boolean cacheMode;


    private static final HelpManager instance = new HelpManager();

    public static HelpManager getInstance() {
        return instance;
    }

    public HelpManager() {
        fileProps = new File("help.properties");
        cacheMode = false;
        try {
            checkFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkFile() throws IOException {
        properties = new Properties();
        if (fileProps.exists()) {
            loadData();
            checkProperty(START, "Inicia el reproductor");
            checkProperty(ISSTARTED, "Comprueba que el reproductor está iniciado");
            checkProperty(PLAY, "Inicia la reproducción");
            checkProperty(STOP, "Detiene la canción actual");
            checkProperty(RESUME, "Inicia la canción actual si está pausada o detenida");
            checkProperty(PAUSE, "Pausa la canción actual");
            checkProperty(NEXT, "Reproduce la canción siguiente");
            checkProperty(PREV, "Reproduce la canción anterior");
            //properties.setProperty(JUMP, "Salta canciones con un valor determinado");
            checkProperty(MUTE, "Silencia el reproductor");
            checkProperty(UNMUTE, "Quita el silencio al reproductor");
            checkProperty(LIST1, "Lista todas las canciones disponibles en el reproductor");
            checkProperty(LIST2, "Lista todas las canciones disponibles en el reproductor");
            checkProperty(LISTCURRENTFOLDER, "Lista todas las canciones de la carpeta actual el reproducción");
            checkProperty(LISTFOLDERS, "Lista todas las carpetas leídas por el reproductor");
            checkProperty(GETGAIN, "Obtiene el volumen actual del reproductor");
            checkProperty(SETGAIN, "Cambia el volumen del reproductor (valor entre 0 y 100 ambos incluidos)");
            checkProperty(GETSYSVOL, "Obtiene el volumen actual del sistema");
            checkProperty(SETSYSVOL, "Cambia el volumen del sistema (valor entre 0 y 100 ambos incluidos)");
            checkProperty(SHUTDOWN, "Apaga el reproductor");
            checkProperty(EXIT, "Apaga el reproductor y finaliza la ejecución de MuPlayer");
            checkProperty(QUIT, "Apaga el reproductor y finaliza la ejecución de MuPlayer");
            checkProperty(SEEK, "Salta segundos de la canción actual");
            checkProperty(SEEKFLD, "Salta carpetas");
            checkProperty(RELOAD, "Vuelve a cargar las canciones nuevamente");
            checkProperty(GOTOSEC, "Se dirige a un segundo específico de la canción actual");
            checkProperty(SOUNDCOUNT, "Obtiene la cantidad de canciones cargadas");
            checkProperty(DURATION, "Obtiene la duración de la canción actual");
            checkProperty(GETCOVER, "Obtiene la carátula de la canción actual si existe " +
                    "y la guarda en la ruta seleccionada");
            checkProperty(GETINFO, "Obtiene información de la canción actual");
            checkProperty(GETPROGRESS, "Obtiene los segundos actuales reproducidos de la canción actual");
            checkProperty(CLEAR1, "Limpia la pantalla");
            checkProperty(CLEAR2, "Limpia la pantalla");
            checkProperty(HELP1, "Despliega el menú de ayuda de comandos");
            checkProperty(HELP2, "Despliega el menú de ayuda de comandos");
            checkProperty(FORMAT, "Obtiene el formato de audio de la canción");
            checkProperty(TITLE, "Obtiene el titulo de la canción");
            checkProperty(NAME, "Obtiene el nombre del archivo de la canción");
            checkProperty(SYSTEM1, "Ejecuta comandos del sistema");
            checkProperty(SYSTEM2, "Ejecuta comandos del sistema");

            checkProperty(SHOW_NEXT, "Muestra información de la canción siguiente");
            checkProperty(SHOW_PREV, "Muestra información de la canción anterior");

            checkProperty(PLAY_FOLDER, "Reproduce una carpeta según el índice entregado");
            checkProperty(LOAD, "Reinicia reproductor cargando la carpeta segun la ruta indicada");
            checkProperty(LIST_ARTISTS, "Lista todos los artistas con sus respectivas canciones");
            checkProperty(LIST_ALBUMS, "Lista todos los álbums con sus respectivas canciones");

            saveData();
        }
        else {
            fileProps.createNewFile();

            properties.setProperty(START, "Inicia el reproductor");
            properties.setProperty(ISSTARTED, "Comprueba que el reproductor está iniciado");
            properties.setProperty(PLAY, "Inicia la reproducción");
            properties.setProperty(STOP, "Detiene la canción actual");
            properties.setProperty(RESUME, "Inicia la canción actual si está pausada o detenida");
            properties.setProperty(PAUSE, "Pausa la canción actual");
            properties.setProperty(NEXT, "Reproduce la canción siguiente");
            properties.setProperty(PREV, "Reproduce la canción anterior");
            //properties.setProperty(JUMP, "Salta canciones con un valor determinado");
            properties.setProperty(MUTE, "Silencia el reproductor");
            properties.setProperty(UNMUTE, "Quita el silencio al reproductor");
            properties.setProperty(LIST1, "Lista todas las canciones disponibles en el reproductor");
            properties.setProperty(LIST2, "Lista todas las canciones disponibles en el reproductor");
            properties.setProperty(LISTCURRENTFOLDER, "Lista todas las canciones de la carpeta actual el reproducción");
            properties.setProperty(LISTFOLDERS, "Lista todas las carpetas leídas por el reproductor");
            properties.setProperty(GETGAIN, "Obtiene el volumen actual del reproductor");
            properties.setProperty(SETGAIN, "Cambia el volumen del reproductor (valor entre 0 y 100 ambos incluidos)");
            properties.setProperty(GETSYSVOL, "Obtiene el volumen actual del sistema");
            properties.setProperty(SETSYSVOL, "Cambia el volumen del sistema (valor entre 0 y 100 ambos incluidos)");
            properties.setProperty(SHUTDOWN, "Apaga el reproductor");
            properties.setProperty(EXIT, "Apaga el reproductor y finaliza la ejecución de MuPlayer");
            properties.setProperty(QUIT, "Apaga el reproductor y finaliza la ejecución de MuPlayer");
            properties.setProperty(SEEK, "Salta segundos de la canción actual");
            properties.setProperty(SEEKFLD, "Salta carpetas");
            properties.setProperty(RELOAD, "Vuelve a cargar las canciones nuevamente");
            properties.setProperty(GOTOSEC, "Se dirige a un segundo específico de la canción actual");
            properties.setProperty(SOUNDCOUNT, "Obtiene la cantidad de canciones cargadas");
            properties.setProperty(DURATION, "Obtiene la duración de la canción actual");
            properties.setProperty(GETCOVER, "Obtiene la carátula de la canción actual si existe " +
                    "y la guarda en la ruta seleccionada");
            properties.setProperty(GETINFO, "Obtiene información de la canción actual");
            properties.setProperty(GETPROGRESS, "Obtiene los segundos actuales reproducidos de la canción actual");
            properties.setProperty(CLEAR1, "Limpia la pantalla");
            properties.setProperty(CLEAR2, "Limpia la pantalla");
            properties.setProperty(HELP1, "Despliega el menú de ayuda de comandos");
            properties.setProperty(HELP2, "Despliega el menú de ayuda de comandos");
            properties.setProperty(FORMAT, "Obtiene el formato de audio de la canción");
            properties.setProperty(TITLE, "Obtiene el titulo de la canción");
            properties.setProperty(NAME, "Obtiene el nombre del archivo de la canción");
            properties.setProperty(SYSTEM1, "Ejecuta comandos del sistema");
            properties.setProperty(SYSTEM2, "Ejecuta comandos del sistema");

            properties.setProperty(SHOW_NEXT, "Muestra información de la canción siguiente");
            properties.setProperty(SHOW_PREV, "Muestra información de la canción anterior");

            properties.setProperty(PLAY_FOLDER, "Reproduce una carpeta según el índice entregado");
            properties.setProperty(LOAD, "Reinicia reproductor cargando la carpeta segun la ruta indicada");
            properties.setProperty(LIST_ARTISTS, "Lista todos los artistas con sus respectivas canciones");
            properties.setProperty(LIST_ALBUMS, "Lista todos los álbums con sus respectivas canciones");

            saveData();
        }
    }

    // revisa si se han hecho cambios, si esta propiedad existe y tiene texto valido se deja tal cual
    private void checkProperty(String key, String defaultValue) {
        String property = properties.getProperty(key);
        if (property == null || property.isEmpty()) {
            properties.setProperty(key, defaultValue);
        }
    }

    private void saveData() {
        try {
            properties.store(new FileWriter(fileProps), "MuPlayer Command Help");
            properties = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try {
            properties = new Properties();
            properties.load(new FileReader(fileProps));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCacheMode(boolean cacheMode) {
        this.cacheMode = cacheMode;
        if (cacheMode && properties == null) {
            loadData();
        }
        else {
            properties = null;
            System.gc();
        }
    }

    public String getProperty(String key) {
        if (cacheMode) {
            return properties.get(key).toString();
        }
        else {
            loadData();
            String value = properties.get(key).toString();
            properties = null;
            return value;
        }
    }

    public Set<String> getPropertyNames() {
        if (cacheMode) {
            return properties.stringPropertyNames();
        }
        else {
            loadData();
            Set<String> strings = properties.stringPropertyNames();
            properties = null;
            return strings;
        }
    }

}
