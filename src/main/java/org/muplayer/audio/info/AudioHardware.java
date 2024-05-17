package org.muplayer.audio.info;

import org.muplayer.util.AudioUtil;
import org.muplayer.util.CollectionUtil;

import javax.sound.sampled.*;
import java.util.*;
import java.util.stream.Collectors;

public class AudioHardware {
    public static List<Mixer> getMixers() {
        return Arrays.stream(AudioSystem.getMixerInfo())
                .map(AudioSystem::getMixer)
                .collect(Collectors.toList());
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

    public static List<Control> getAllControls(Line line) throws LineUnavailableException {
        final List<Control> listControls = CollectionUtil.newFastList();
        boolean opened = false;
        if (!line.isOpen()) {
            line.open();
            opened = true;
        }
        findAllControls(listControls, line.getControls());
        if (opened)
            line.close();
        return listControls;
    }

    public static void findAllControls(List<Control> listControls, Control... controls) {
        if (controls != null && controls.length > 0) {
            CompoundControl compoundControl;
            Control control;
            for (int i = 0; i < controls.length; i++) {
                control = controls[i];
                if (control instanceof CompoundControl) {
                    compoundControl = (CompoundControl) control;
                    findAllControls(listControls, compoundControl.getMemberControls());
                } else
                    listControls.add(control);
            }
        }
    }

    public static List<DataLine.Info> getAllSpeakerInfo() {
        final List<DataLine.Info> listInfo = CollectionUtil.newFastList();
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
        final List<DataLine.Info> listInfo = CollectionUtil.newFastList();
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

    public static FloatControl getReadyVolumeControl() {
        final Line master = getMasterOutputLine();
        try {
            master.open();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return getVolumeControl(master);
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

    public static Line getMasterOutputLine() {
        for (Mixer mixer : getMixers()) {
            for (Line line : getAvailableOutputLines(mixer)) {
                if (line.getLineInfo().toString().contains("Master"))
                    return line;
            }
        }
        return null;
    }

    public static Line getSpeakerInUse() throws LineUnavailableException {
        final String headphone = "Headphone";
        final String speaker = "Speaker";

        final Line headphoneLine = getMixers().parallelStream().map(mixer ->
                getAvailableOutputLines(mixer).parallelStream()
                        .filter(line -> line.getLineInfo().toString().contains(headphone))
                        .findFirst().orElse(null)).filter(Objects::nonNull).findFirst().orElse(null);

        final Line speakerLine = getMixers().parallelStream().map(mixer ->
                getAvailableOutputLines(mixer).parallelStream()
                        .filter(line -> line.getLineInfo().toString().contains(speaker))
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

            //System.out.println("HeadphoneLine: " + Arrays.toString(headphoneLine.getControls()));
            //System.out.println("SpeakerLine: " + Arrays.toString(speakerLine.getControls()));

            final Line toReturn;
            if (headphoneMute.getValue() && speakerMute.getValue())
                toReturn = getMasterOutputLine();

            else if (headphoneMute.getValue())
                toReturn = speakerLine;
            else
                toReturn = headphoneLine;

            headphoneLine.close();
            speakerLine.close();
            toReturn.open();
            return toReturn;
        }
    }

    public static FloatControl getGainControl(Line line) {
        if (!line.isOpen())
            throw new RuntimeException("Line is closed: " + toString(line));
        return (FloatControl) findControl(FloatControl.Type.MASTER_GAIN, line.getControls());
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

    public static void setMuteValue(Line line, boolean mute) {
        if (!line.isOpen())
            throw new RuntimeException("Line is closed: " + toString(line));
        final BooleanControl muteControl = (BooleanControl) findControl(BooleanControl.Type.MUTE, line.getControls());

        if (muteControl != null)
            muteControl.setValue(mute);
    }

    public static void setSpeakerMuteValue(boolean mute) {
        try {
            Line speakerInUse = getSpeakerInUse();
            if (!speakerInUse.isOpen())
                speakerInUse.open();
            setMuteValue(speakerInUse, mute);
            speakerInUse.close();
        } catch (Exception e) {

        }
    }

    public static float getFormattedMasterVolume() {
        final FloatControl volumeControl = AudioHardware.getReadyVolumeControl();
        return AudioUtil.convertLineRangeToVolRange(volumeControl.getValue(), volumeControl);
    }

    public static void setFormattedMasterVolume(float volume) {
        final FloatControl volumeControl = AudioHardware.getReadyVolumeControl();
        volumeControl.setValue(AudioUtil.convertVolRangeToLineRange(volume, volumeControl));
    }

    public static float getFormattedSpeakerVolume() {
        try {
            final Line speaker = getSpeakerInUse();
            final FloatControl volumeControl = getVolumeControl(speaker);
            return AudioUtil.convertLineRangeToVolRange(volumeControl.getValue(), volumeControl);
        } catch (LineUnavailableException e) {
            return 0F;
        }
    }

    public static void setFormattedSpeakerVolume(float volume) {
        try {
            final Line speaker = getSpeakerInUse();
            final FloatControl volumeControl = getVolumeControl(speaker);
            volumeControl.setValue(AudioUtil.convertVolRangeToLineRange(volume, volumeControl));
        } catch (LineUnavailableException e) {
        }
    }

    public static List<Line> getAvailableOutputLines(Mixer mixer) {
        return getAvailableLines(mixer, mixer.getTargetLineInfo());
    }

    public static List<Line> getAvailableInputLines(Mixer mixer) {
        return getAvailableLines(mixer, mixer.getSourceLineInfo());
    }

    private static List<Line> getAvailableLines(Mixer mixer, Line.Info[] lineInfos) {
        final List<Line> lines = CollectionUtil.newFastList();
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
