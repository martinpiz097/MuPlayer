package cl.estencia.labs.muplayer.core.aucom.util;

import javax.sound.sampled.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public class AudioSystemManager {
    private static final String LINE_IS_CLOSED = "Line is closed: ";
    private final cl.estencia.labs.muplayer.core.aucom.util.VolumeConverter volumeConverter;
    private final List<Mixer> mixersList;

    /**
     * Constructor por defecto que inicializa el sistema de audio
     */
    public AudioSystemManager() {
        this.volumeConverter = new VolumeConverter();
        this.mixersList = new ArrayList<>();
        loadMixers();
    }

    /**
     * Carga todos los mixers disponibles en el sistema
     */
    private void loadMixers() {
        Mixer.Info[] mixersInfo = AudioSystem.getMixerInfo();
        Mixer mixer;
        for (Mixer.Info mixerInfo : mixersInfo) {
            mixer = AudioSystem.getMixer(mixerInfo);
            /*mixer.addLineListener(event -> {
                System.out.println("Event: " + event.getLine().getLineInfo());
            });*/

            mixersList.add(mixer);
        }
    }

    //---------------------- MIXERS MANAGEMENT ----------------------//

    /**
     * Obtiene todos los mixers disponibles en el sistema
     */
    public List<Mixer> getMixers() {
        return mixersList;
    }

    /**
     * Obtiene un mixer por su nombre
     */
    public Mixer getMixer(String name) {
        return mixersList.parallelStream()
                .filter(mixer -> mixer.getMixerInfo().getName().equals(name))
                .findFirst().orElse(null);
    }

    /**
     * Obtiene un mapa con todos los mixers y sus líneas asociadas
     */
    public Map<Mixer, List<Line>> getCompleteMixers() {
        Map<Mixer, List<Line>> mapMixers = new HashMap<>();
        mixersList.forEach(mixer -> mapMixers.put(mixer, getMixerLines(mixer)));
        return mapMixers;
    }

    //---------------------- LINE MANAGEMENT ----------------------//

    /**
     * Intenta abrir una línea si está cerrada
     * @return true si la línea se abrió con éxito o ya estaba abierta
     */
    public boolean open(Line line) {
        boolean successful;
        try {
            if (line.isOpen()) {
                return false;
            }

            line.open();
            successful = true;
        } catch (Exception ex) {
            successful = false;
        }

        return successful;
    }

    /**
     * Obtiene todas las líneas disponibles para un mixer
     */
    public List<Line> getMixerLines(Mixer mixer) {
        return getMixerLines(mixer.getMixerInfo().getName());
    }

    /**
     * Obtiene todas las líneas disponibles para un mixer por nombre
     */
    public List<Line> getMixerLines(String mixerName) {
        Mixer mixer = getMixer(mixerName);
        List<Line> listLines = new ArrayList<>();

        if (mixer != null) {
            listLines.addAll(getMixerTargetLines(mixerName));
            listLines.addAll(getMixerSourceLines(mixerName));
            listLines.addAll(getMixerPorts(mixerName));
            listLines.addAll(getMixerClips(mixerName));
        }

        return listLines;
    }

    /**
     * Obtiene todas las líneas de salida (source) para un mixer
     */
    public List<SourceDataLine> getMixerSourceLines(Mixer mixer) {
        return getMixerSourceLines(mixer.getMixerInfo().getName());
    }

    /**
     * Obtiene todas las líneas de salida (source) para un mixer por nombre
     */
    public List<SourceDataLine> getMixerSourceLines(String mixerName) {
        Mixer mixer = getMixer(mixerName);
        List<SourceDataLine> listLines = new ArrayList<>();

        if (mixer != null) {
            Line.Info[] lineInfos = findLines(mixer, SourceDataLine.class);
            if (lineInfos != null) {
                for (Line.Info lineInfo : lineInfos) {
                    try {
                        listLines.add((SourceDataLine) mixer.getLine(lineInfo));
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return listLines;
    }

    /**
     * Obtiene todas las líneas de entrada (target) para un mixer
     */
    public List<TargetDataLine> getMixerTargetLines(Mixer mixer) {
        return getMixerTargetLines(mixer.getMixerInfo().getName());
    }

    /**
     * Obtiene todas las líneas de entrada (target) para un mixer por nombre
     */
    public List<TargetDataLine> getMixerTargetLines(String mixerName) {
        Mixer mixer = getMixer(mixerName);
        List<TargetDataLine> listLines = new ArrayList<>();

        if (mixer != null) {
            Line.Info[] lineInfos = findLines(mixer, TargetDataLine.class);
            if (lineInfos != null) {
                for (Line.Info lineInfo : lineInfos) {
                    try {
                        listLines.add((TargetDataLine) mixer.getLine(lineInfo));
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return listLines;
    }

    /**
     * Obtiene todos los puertos para un mixer
     */
    public List<Port> getMixerPorts(Mixer mixer) {
        return getMixerPorts(mixer.getMixerInfo().getName());
    }

    /**
     * Obtiene todos los puertos para un mixer por nombre
     */
    public List<Port> getMixerPorts(String mixerName) {
        Mixer mixer = getMixer(mixerName);
        List<Port> listLines = new ArrayList<>();

        if (mixer != null) {
            Line.Info[] lineInfos = findLines(mixer, Port.class);
            if (lineInfos != null) {
                for (Line.Info lineInfo : lineInfos) {
                    try {
                        listLines.add((Port) mixer.getLine(lineInfo));
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return listLines;
    }

    /**
     * Obtiene todos los clips para un mixer
     */
    public List<Clip> getMixerClips(Mixer mixer) {
        return getMixerClips(mixer.getMixerInfo().getName());
    }

    /**
     * Obtiene todos los clips para un mixer por nombre
     */
    public List<Clip> getMixerClips(String mixerName) {
        Mixer mixer = getMixer(mixerName);
        List<Clip> listLines = new ArrayList<>();

        if (mixer != null) {
            Line.Info[] lineInfos = findLines(mixer, Clip.class);
            if (lineInfos != null) {
                for (Line.Info lineInfo : lineInfos) {
                    try {
                        listLines.add((Clip) mixer.getLine(lineInfo));
                    } catch (LineUnavailableException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return listLines;
    }

    /**
     * Verifica si una línea es soportada por un mixer
     */
    private boolean isLineSupported(Mixer mixer, Class<? extends Line> infoClass) {
        return mixer.isLineSupported(new Line.Info(infoClass));
    }

    /**
     * Encuentra líneas de un tipo específico en un mixer
     */
    private Line.Info[] findLines(Mixer mixer, Class<? extends Line> infoClass) {
        if (isLineSupported(mixer, infoClass)) {
            Line.Info[] sourceLineInfo = mixer.getSourceLineInfo(new Line.Info(infoClass));
            Line.Info[] targetLineInfo = mixer.getTargetLineInfo(new Line.Info(infoClass));

            Line.Info[] result = new Line.Info[sourceLineInfo.length + targetLineInfo.length];

            int linesCount = 0;
            for (Line.Info info : sourceLineInfo) {
                result[linesCount++] = info;
            }
            for (Line.Info info : targetLineInfo) {
                result[linesCount++] = info;
            }

            return result;
        } else {
            return null;
        }
    }

    /**
     * Busca un puerto específico por nombre y tipo
     */
    public Port findPort(Mixer mixer, String portName, boolean input) {
        return findPort(mixer.getMixerInfo().getName(), portName, input);
    }

    /**
     * Busca un puerto específico por nombre de mixer, nombre de puerto y tipo
     */
    public Port findPort(String mixerName, String portName, boolean input) {
        Mixer mixer = getMixer(mixerName);
        if (mixer == null)
            return null;

        List<Port> mixerPorts = getMixerPorts(mixerName);
        return mixerPorts.parallelStream()
                .filter(p -> {
                    final String name = p.getLineInfo().toString().toLowerCase();
                    boolean contains = name.contains(input ? "source" : "target");
                    return contains && name.contains(portName.toLowerCase());
                })
                .findFirst().orElse(null);
    }

    /**
     * Obtiene una línea si está disponible
     */
    public static Line getLineIfAvailable(Mixer mixer, Line.Info lineInfo) {
        try {
            return mixer.getLine(lineInfo);
        } catch (LineUnavailableException ex) {
            return null;
        }
    }

    /**
     * Obtiene todas las líneas de salida disponibles para un mixer
     */
    public List<Line> getAvailableOutputLines(Mixer mixer) {
        return getAvailableLines(mixer, mixer.getTargetLineInfo());
    }

    /**
     * Obtiene todas las líneas de entrada disponibles para un mixer
     */
    public List<Line> getAvailableInputLines(Mixer mixer) {
        return getAvailableLines(mixer, mixer.getSourceLineInfo());
    }

    /**
     * Método auxiliar para obtener líneas disponibles
     */
    private static List<Line> getAvailableLines(Mixer mixer, Line.Info[] lineInfos) {
        final List<Line> lines = new ArrayList<>();
        for (Line.Info lineInfo : lineInfos) {
            Line line = getLineIfAvailable(mixer, lineInfo);
            if (line != null)
                lines.add(line);
        }

        return lines;
    }

    //---------------------- INFO METHODS ----------------------//

    /**
     * Obtiene información de todas las interfaces de salida de altavoces
     */
    public List<DataLine.Info> getAllSpeakerInfo() {
        final List<DataLine.Info> listInfo = new ArrayList<>();
        final Mixer.Info[] mixersInfo = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixersInfo) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] speakersInfo = mixer.getSourceLineInfo();
            for (Line.Info sourceLineInfo : speakersInfo) {
                if (sourceLineInfo instanceof DataLine.Info) {
                    listInfo.add((DataLine.Info) sourceLineInfo);
                }
            }
        }

        return listInfo;
    }

    /**
     * Obtiene información de todas las interfaces de entrada de micrófono
     */
    public List<DataLine.Info> getAllMicrophoneInfo() {
        final List<DataLine.Info> listInfo = new ArrayList<>();
        final Mixer.Info[] mixersInfo = AudioSystem.getMixerInfo();

        for (Mixer.Info mixerInfo : mixersInfo) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            Line.Info[] microInfo = mixer.getTargetLineInfo();
            for (Line.Info targetLineInfo : microInfo) {
                if (targetLineInfo instanceof DataLine.Info) {
                    listInfo.add((DataLine.Info) targetLineInfo);
                }
            }
        }
        return listInfo;
    }

    /**
     * Obtiene información jerárquica del sistema de audio
     */
    public String getHierarchyInfo() {
        final StringBuilder sb = new StringBuilder();
        for (Mixer mixer : getMixers()) {
            sb.append("Mixer: ").append(toString(mixer)).append("\n");

            for (Line line : getAvailableOutputLines(mixer)) {
                sb.append("  OUT: ").append(toString(line)).append("\n");
                boolean opened = open(line);
                getLineInfo(line, sb, opened);
            }

            for (Line line : getAvailableInputLines(mixer)) {
                sb.append("  IN: ").append(toString(line)).append("\n");
                boolean opened = open(line);
                getLineInfo(line, sb, opened);
            }

            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Obtiene información detallada de una línea
     */
    public void getLineInfo(Line line, StringBuilder sb, boolean opened) {
        for (Control control : line.getControls()) {
            sb.append("    Control: ").append(toString(control)).append("\n");
            if (control instanceof CompoundControl) {
                CompoundControl compoundControl = (CompoundControl) control;
                for (Control subControl : compoundControl.getMemberControls()) {
                    sb.append("      Sub-Control: ").append(toString(subControl)).append("\n");
                }
            }
        }
        if (opened)
            line.close();
    }

    //---------------------- CONTROL MANAGEMENT ----------------------//

    /**
     * Encuentra un control específico en una línea
     */
    private static Control findControl(Control.Type type, Control... controls) {
        if (controls == null) {
            return null;
        }

        for (Control control : controls) {
            if (control.getType().equals(type)) {
                return control;
            }
            if (control instanceof CompoundControl) {
                CompoundControl compoundControl = (CompoundControl) control;
                Control member = findControl(type, compoundControl.getMemberControls());
                if (member != null) {
                    return member;
                }
            }
        }

        return null;
    }

    /**
     * Obtiene todos los controles de una línea
     */
    public List<Control> getAllControls(Line line) throws LineUnavailableException {
        final List<Control> listControls = new ArrayList<>();
        boolean opened;
        if (!line.isOpen()) {
            line.open();
            opened = true;
        }
        else {
            opened = false;
        }

        findAllControls(listControls, line.getControls());
        if (opened) {
            line.close();
        }
        return listControls;
    }

    /**
     * Método auxiliar para encontrar todos los controles
     */
    public void findAllControls(List<Control> listControls, Control... controls) {
        if (controls != null && controls.length > 0) {
            for (Control control : controls) {
                if (control instanceof CompoundControl) {
                    CompoundControl compoundControl = (CompoundControl) control;
                    findAllControls(listControls, compoundControl.getMemberControls());
                } else {
                    listControls.add(control);
                }
            }
        }
    }

    /**
     * Obtiene los nombres de los controles mediante reflexión
     */
    private String[] getControlNames(Class<? extends Control.Type> clazz) throws
            NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Field[] declaredFields = clazz.getDeclaredFields();
        List<String> listNames = new ArrayList<>();

        Constructor<? extends Control.Type> constructor = clazz.getConstructor(String.class);
        constructor.setAccessible(true);

        for (Field field : declaredFields) {
            if (field.getModifiers() == Modifier.STATIC)
                listNames.add((String) field.get(null));
        }

        return listNames.toArray(new String[0]);
    }

    //---------------------- VOLUME AND MUTE CONTROL ----------------------//

    /**
     * Obtiene el control de ganancia de una línea
     */
    public FloatControl getGainControl(Line line) {
//        if (!line.isOpen()) {
//            throw new RuntimeException(LINE_IS_CLOSED + toString(line));
//        }
        return (FloatControl) findControl(FloatControl.Type.MASTER_GAIN, line.getControls());
    }

    /**
     * Obtiene el control de volumen de una línea
     */
    public FloatControl getVolumeControl(Line line) {
        //if (!line.isOpen()) {
        //    throw new RuntimeException(LINE_IS_CLOSED + toString(line));
        //}
        return (FloatControl) findControl(FloatControl.Type.VOLUME, line.getControls());
    }

    /**
     * Obtiene el control de silencio de una línea
     */
    public BooleanControl getMuteControl(Line line) {
        if (!line.isOpen())
            throw new RuntimeException(LINE_IS_CLOSED + toString(line));
        return (BooleanControl) findControl(BooleanControl.Type.MUTE, line.getControls());
    }

    /**
     * Establece el estado de silencio de una línea
     */
    public void setMuteValue(Line line, boolean mute) {
        if (!line.isOpen())
            throw new RuntimeException(LINE_IS_CLOSED + toString(line));
        final BooleanControl muteControl = (BooleanControl) findControl(BooleanControl.Type.MUTE, line.getControls());

        if (muteControl != null) {
            muteControl.setValue(mute);
        }
    }

    /**
     * Establece el estado de silencio del altavoz en uso
     */
    public void setSpeakerMuteValue(boolean mute) {
        try {
            Line speakerInUse = getSpeakerInUse();
            if (speakerInUse != null) {
                if (!speakerInUse.isOpen())
                    speakerInUse.open();
                setMuteValue(speakerInUse, mute);
                speakerInUse.close();
            }
        } catch (Exception e) {
            // Ignorar excepciones
        }
    }

    /**
     * Obtiene la línea de salida maestra
     */
    public Line getMasterOutputLine() {
        for (Mixer mixer : getMixers()) {
            for (Line line : getAvailableOutputLines(mixer)) {
                if (line.getLineInfo().toString().contains("Master"))
                    return line;
            }
        }

        return null;
    }

    /**
     * Obtiene el control de volumen listo para usar
     */
    public FloatControl getReadyVolumeControl() {
        final Line master = getMasterOutputLine();
        if (master == null) {
            return null;
        }

//        for (Mixer mixer : getMixers()) {
//            System.out.println("Mixer: " + mixer.getMixerInfo().getName());
//            List<Line> listLines = new ArrayList<>();
//            listLines.addAll(getAvailableOutputLines(mixer));
//            listLines.addAll(getAvailableInputLines(mixer));
//
//            for (Line line : listLines) {
//                open(line);
////                System.out.println("Line: " + line.getLineInfo());
////                System.out.println("Control: " + Arrays.toString(line.getControls()));
//
//                FloatControl volumeControl = getVolumeControl(line);
//                FloatControl gainControl = getGainControl(line);
//
//                if (volumeControl == null && gainControl == null) {
//                    continue;
//                }
//
//                if (volumeControl != null) {
//                    System.out.println("[Line="+line.getLineInfo()+"]  " + "Volume Control: " + volumeControl);
//                }
//
//                if (gainControl != null) {
//                    System.out.println("[Line="+line.getLineInfo()+"]  " + "Gain Control: " + gainControl);
//                }
//            }
//
//            System.out.println("--------------------------------------------------------------------------------------------------");
//            System.out.println("--------------------------------------------------------------------------------------------------");
//        }
//
//        System.exit(0);

        open(master);
        return getVolumeControl(master);
    }

    /**
     * Obtiene el altavoz actualmente en uso
     */
    public Line getSpeakerInUse() throws LineUnavailableException {
        final String headphone = "headphone";
        final String speaker = "speaker";

        final Line headphoneLine = getMixers().parallelStream().map(mixer ->
                getAvailableOutputLines(mixer).parallelStream()
                        .filter(line -> line.getLineInfo().toString()
                                .toLowerCase().contains(headphone))
                        .findFirst().orElse(null)).filter(Objects::nonNull).findFirst().orElse(null);

        final Line speakerLine = getMixers().parallelStream().map(mixer ->
                getAvailableOutputLines(mixer).parallelStream()
                        .filter(line -> line.getLineInfo().toString()
                                .toLowerCase().contains(speaker))
                        .findFirst().orElse(null)).filter(Objects::nonNull).findFirst().orElse(null);

        if (headphoneLine == null || speakerLine == null) {
            if (headphoneLine == null && speakerLine == null)
                return null;
            else
                return Objects.requireNonNullElse(headphoneLine, speakerLine);
        } else {
            headphoneLine.open();
            final BooleanControl headphoneMute = getMuteControl(headphoneLine);

            speakerLine.open();
            final BooleanControl speakerMute = getMuteControl(speakerLine);

            final Line toReturn;
            if (headphoneMute.getValue() && speakerMute.getValue())
                toReturn = getMasterOutputLine();
            else if (headphoneMute.getValue())
                toReturn = speakerLine;
            else
                toReturn = headphoneLine;

            headphoneLine.close();
            speakerLine.close();
            if (toReturn != null) {
                toReturn.open();
            }
            return toReturn;
        }
    }

    //---------------------- VOLUME MANAGEMENT ----------------------//

    /**
     * Obtiene el volumen de salida maestro
     */
    public Float getMasterOutputVolume() {
        final Line line = getMasterOutputLine();
        if (line == null)
            return null;
        final boolean opened = open(line);
        try {
            final FloatControl control = getVolumeControl(line);
            return control != null ? control.getValue() : null;
        } finally {
            if (opened)
                line.close();
        }
    }

    /**
     * Establece el volumen de salida maestro
     */
    public void setMasterOutputVolume(float value) {
        if (value < 0 || value > 1)
            throw new IllegalArgumentException(
                    "Volume can only be set to a value from 0 to 1. Given value is illegal: " + value);
        final Line line = getMasterOutputLine();
        if (line == null)
            throw new RuntimeException("Master output port not found");

        final boolean opened = open(line);
        try {
            final FloatControl control = getVolumeControl(line);
            if (control == null)
                throw new RuntimeException("Volume control not found in master port: " + toString(line));
            control.setValue(value);
        } finally {
            if (opened)
                line.close();
        }
    }

    /**
     * Obtiene el estado de silencio de la salida maestra
     */
    public Boolean getMasterOutputMute() {
        final Line line = getMasterOutputLine();
        if (line == null)
            return null;
        final boolean opened = open(line);
        try {
            final BooleanControl control = getMuteControl(line);
            return control != null ? control.getValue() : null;
        } finally {
            if (opened)
                line.close();
        }
    }

    /**
     * Establece el estado de silencio de la salida maestra
     */
    public void setMasterOutputMute(boolean value) {
        final Line line = getMasterOutputLine();
        if (line == null)
            throw new RuntimeException("Master output port not found");
        final boolean opened = open(line);
        try {
            final BooleanControl control = getMuteControl(line);
            if (control == null)
                throw new RuntimeException("Mute control not found in master port: " + toString(line));
            control.setValue(value);
        } finally {
            if (opened)
                line.close();
        }
    }

    /**
     * Obtiene el volumen maestro formateado
     */
    public float getFormattedMasterVolume() {
        final FloatControl volumeControl = getReadyVolumeControl();
        return volumeControl != null ?
                volumeConverter.dataLineVolumeToVolume(volumeControl.getValue(), volumeControl)
                : 0f;
    }

    /**
     * Establece el volumen maestro formateado
     */
    public void setFormattedMasterVolume(float volume) {
        final FloatControl volumeControl = getReadyVolumeControl();
        if (volumeControl != null) {
            volumeControl.setValue(volumeConverter.volumeToDataLineVolume(volume, volumeControl));
        }
    }

    public int getConsoleMasterVolume() {
        try {
            final String output = cl.estencia.labs.muplayer.core.aucom.util.ProcessManager.execute("pactl", "get-sink-volume", "@DEFAULT_SINK@");
            if (output == null || output.isBlank()) {
                return -1;
            }

            final String[] outputLinesSplit = output.trim().split("\n");
            final String[] outputSplitSlash = outputLinesSplit[0].split("/");
            final String volumeValueStr = outputSplitSlash[outputSplitSlash.length - 2].trim()
                    .replace("%", "");

            return Integer.parseInt(volumeValueStr.trim());
        } catch (Exception e) {
            return -1;
        }

    }

    public void setConsoleMasterVolume(int volume) {
        try {
            ProcessManager.executeLegacy("pactl set-sink-volume @DEFAULT_SINK@ "+volume+"%");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el volumen del altavoz formateado
     */
    public float getFormattedSpeakerVolume() {
        try {
            final Line speaker = getSpeakerInUse();
            if (speaker == null) return 0F;
            final FloatControl volumeControl = getVolumeControl(speaker);
            return volumeControl != null ?
                    volumeConverter.dataLineVolumeToVolume(volumeControl.getValue(), volumeControl) : 0F;
        } catch (LineUnavailableException e) {
            return 0F;
        }
    }

    /**
     * Establece el volumen del altavoz formateado
     */
    public void setFormattedSpeakerVolume(float volume) {
        try {
            final Line speaker = getSpeakerInUse();
            if (speaker != null) {
                final FloatControl volumeControl = getVolumeControl(speaker);
                if (volumeControl != null) {
                    volumeControl.setValue(volumeConverter.volumeToDataLineVolume(volume, volumeControl));
                }
            }
        } catch (LineUnavailableException e) {
            // Ignorar excepciones
        }
    }

    //---------------------- UTILITY METHODS ----------------------//

    /**
     * Convierte un control a String
     */
    public String toString(Control control) {
        return control != null
                ? control.toString() + " (" + control.getType().toString() + ")" : null;
    }

    /**
     * Convierte una línea a String
     */
    public String toString(Line line) {
        return line != null ? line.getLineInfo().toString() : null;
    }

    /**
     * Convierte un mixer a String
     */
    public String toString(Mixer mixer) {
        if (mixer == null)
            return null;
        final StringBuilder sb = new StringBuilder();
        final Mixer.Info info = mixer.getMixerInfo();
        sb.append(info.getName());
        sb.append(" (").append(info.getDescription()).append(")");
        sb.append(mixer.isOpen() ? " [open]" : " [closed]");
        return sb.toString();
    }

}