package org.muplayer.info;

import javax.sound.sampled.*;
import javax.xml.crypto.Data;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AudioHardware {
    public static List<DataLine.Info> getAllSpeakerInfo() {
        final List<DataLine.Info> listInfo = new LinkedList<>();
        final Mixer.Info[] mixersInfo = AudioSystem.getMixerInfo();

        Mixer mixer;
        Line.Info[] speakersInfo;
        Line.Info sourceLineInfo;
        for (int i = 0; i < mixersInfo.length; i++) {
            mixer = AudioSystem.getMixer(mixersInfo[i]);
            speakersInfo = mixer.getSourceLineInfo();
            for (int j = 0; j < speakersInfo.length; j++) {
                sourceLineInfo = speakersInfo[j];
                if (sourceLineInfo instanceof DataLine.Info) {
                    listInfo.add((DataLine.Info) sourceLineInfo);
                }
            }
        }

        return listInfo;
    }

    public static List<DataLine.Info> getAllMicrophoneInfo() {
        final List<DataLine.Info> listInfo = new LinkedList<>();
        final Mixer.Info[] mixersInfo = AudioSystem.getMixerInfo();

        Mixer mixer;
        Line.Info[] microInfo;
        Line.Info targetLineInfo;
        for (int i = 0; i < mixersInfo.length; i++) {
            mixer = AudioSystem.getMixer(mixersInfo[i]);
            microInfo = mixer.getTargetLineInfo();
            for (int j = 0; j < microInfo.length; j++) {
                targetLineInfo = microInfo[j];
                if (targetLineInfo instanceof DataLine.Info) {
                    listInfo.add((DataLine.Info) targetLineInfo);
                }
            }
        }
        return listInfo;
    }

    public static List<Mixer> getMixers() {
        return Arrays.stream(AudioSystem.getMixerInfo())
                .map(AudioSystem::getMixer)
                .collect(Collectors.toList());
    }

    public static void setMasterOutputVolume(float value) {
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

    public static Float getMasterOutputVolume() {
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

    public static FloatControl getReadyVolumeControl() {
        final Line master = getMasterOutputLine();
        try {
            master.open();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return getVolumeControl(master);
    }



    public static void setMasterOutputMute(boolean value) {
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

    public static Boolean getMasterOutputMute() {
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

    public static Line getMasterOutputLine() {
        for (Mixer mixer : getMixers()) {
            for (Line line : getAvailableOutputLines(mixer)) {
                if (line.getLineInfo().toString().contains("Master"))
                    return line;
            }
        }
        return null;
    }

    public static FloatControl getVolumeControl(Line line) {
        if (!line.isOpen())
            throw new RuntimeException("Line is closed: " + toString(line));
        return (FloatControl) findControl(FloatControl.Type.VOLUME, line.getControls());
    }

    public static BooleanControl getMuteControl(Line line) {
        if (!line.isOpen())
            throw new RuntimeException("Line is closed: " + toString(line));
        return (BooleanControl) findControl(BooleanControl.Type.MUTE, line.getControls());
    }

    private static Control findControl(Control.Type type, Control... controls) {
        if (controls == null || controls.length == 0)
            return null;

        CompoundControl compoundControl;
        Control member;
        for (Control control : controls) {
            if (control.getType().equals(type))
                return control;
            if (control instanceof CompoundControl) {
                compoundControl = (CompoundControl) control;
                member = findControl(type, compoundControl.getMemberControls());
                if (member != null)
                    return member;
            }
        }
        return null;
    }

    public static List<Line> getAvailableOutputLines(Mixer mixer) {
        return getAvailableLines(mixer, mixer.getTargetLineInfo());
    }

    public static List<Line> getAvailableInputLines(Mixer mixer) {
        return getAvailableLines(mixer, mixer.getSourceLineInfo());
    }

    private static List<Line> getAvailableLines(Mixer mixer, Line.Info[] lineInfos) {
        final List<Line> lines = new LinkedList<>();
        Line.Info lineInfo;
        Line line;
        for (int i = 0; i < lineInfos.length; i++) {
            lineInfo = lineInfos[i];
            line = getLineIfAvailable(mixer, lineInfo);
            if (line != null)
                lines.add(line);
        }
        return lines;
    }

    public static Line getLineIfAvailable(Mixer mixer, Line.Info lineInfo) {
        try {
            return mixer.getLine(lineInfo);
        } catch (LineUnavailableException ex) {
            return null;
        }
    }

    public static void getLineInfo(Line line, StringBuilder sb, boolean opened) {
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

    public static String getHierarchyInfo() {
        final StringBuilder sb = new StringBuilder();
        for (Mixer mixer : getMixers()) {
            sb.append("Mixer: ").append(toString(mixer)).append("\n");

            for (Line line : getAvailableOutputLines(mixer)) {
                sb.append("  OUT: ").append(toString(line)).append("\n");
                boolean opened = open(line);
                getLineInfo(line, sb, opened);
            }

            for (Line line : getAvailableOutputLines(mixer)) {
                sb.append("  IN: ").append(toString(line)).append("\n");
                boolean opened = open(line);
                getLineInfo(line, sb, opened);
            }

            sb.append("\n");
        }
        return sb.toString();
    }

    public static boolean open(Line line) {
        if (line.isOpen())
            return false;
        try {
            line.open();
        } catch (LineUnavailableException ex) {
            return false;
        }
        return true;
    }

    public static String toString(Control control) {
        return control != null
                ? control.toString() + " (" + control.getType().toString() + ")" : null;
    }

    public static String toString(Line line) {
        return line != null ? line.getLineInfo().toString() : null;
    }

    public static String toString(Mixer mixer) {
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
