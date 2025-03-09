package org.muplayer.audio.info;

import org.muplayer.util.AudioUtil;
import org.muplayer.util.CollectionUtil;

import javax.sound.sampled.*;
import java.util.*;
import java.util.stream.Collectors;

public class AudioHardware {
    private final AudioUtil audioUtil;
    
    public AudioHardware() {
        audioUtil = new AudioUtil();
    }

    private static final String LINE_IS_CLOSED = "Line is closed: ";

    public List<Mixer> getMixers() {
        return Arrays.stream(AudioSystem.getMixerInfo())
                .map(AudioSystem::getMixer)
                .collect(Collectors.toList());
    }

    public boolean open(Line line) {
        boolean successful;
        try {
            if (!line.isOpen()) {
                line.open();
                successful = true;
            }
            else {
                successful = false;
            }
        } catch (LineUnavailableException ex) {
            successful = false;
        }
        return successful;
    }

    private static Control findControl(Control.Type type, Control... controls) {
        if (controls == null || controls.length == 0) {
            return null;
        }
        CompoundControl compoundControl;
        Control member;

        Control control;
        for (int i = 0; i < controls.length; i++) {
            control = controls[i];
            if (control.getType().equals(type)) {
                return control;
            }
            if (control instanceof CompoundControl) {
                compoundControl = (CompoundControl) control;
                member = findControl(type, compoundControl.getMemberControls());
                if (member != null) {
                    return member;
                }
            }
        }
        return null;
    }

    public List<Control> getAllControls(Line line) throws LineUnavailableException {
        final List<Control> listControls = CollectionUtil.newLinkedList();
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

    public void findAllControls(List<Control> listControls, Control... controls) {
        if (controls != null && controls.length > 0) {
            CompoundControl compoundControl;
            Control control;
            for (int i = 0; i < controls.length; i++) {
                control = controls[i];
                if (control instanceof CompoundControl) {
                    compoundControl = (CompoundControl) control;
                    findAllControls(listControls, compoundControl.getMemberControls());
                } else {
                    listControls.add(control);
                }
            }
        }
    }

    public List<DataLine.Info> getAllSpeakerInfo() {
        final List<DataLine.Info> listInfo = CollectionUtil.newLinkedList();
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

    public List<DataLine.Info> getAllMicrophoneInfo() {
        final List<DataLine.Info> listInfo = CollectionUtil.newLinkedList();
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

    public FloatControl getReadyVolumeControl() {
        final Line master = getMasterOutputLine();
        try {
            master.open();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        return getVolumeControl(master);
    }


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

    public Line getMasterOutputLine() {
        for (Mixer mixer : getMixers()) {
            for (Line line : getAvailableOutputLines(mixer)) {
                if (line.getLineInfo().toString().contains("Master"))
                    return line;
            }
        }
        return null;
    }

    public Line getSpeakerInUse() throws LineUnavailableException {
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

    public FloatControl getGainControl(Line line) {
        if (!line.isOpen())
            throw new RuntimeException(LINE_IS_CLOSED + toString(line));
        return (FloatControl) findControl(FloatControl.Type.MASTER_GAIN, line.getControls());
    }

    public FloatControl getVolumeControl(Line line) {
        if (!line.isOpen())
            throw new RuntimeException(LINE_IS_CLOSED + toString(line));
        return (FloatControl) findControl(FloatControl.Type.VOLUME, line.getControls());
    }

    public BooleanControl getMuteControl(Line line) {
        if (!line.isOpen())
            throw new RuntimeException(LINE_IS_CLOSED + toString(line));
        return (BooleanControl) findControl(BooleanControl.Type.MUTE, line.getControls());
    }

    public void setMuteValue(Line line, boolean mute) {
        if (!line.isOpen())
            throw new RuntimeException(LINE_IS_CLOSED + toString(line));
        final BooleanControl muteControl = (BooleanControl) findControl(BooleanControl.Type.MUTE, line.getControls());

        if (muteControl != null)
            muteControl.setValue(mute);
    }

    public void setSpeakerMuteValue(boolean mute) {
        try {
            Line speakerInUse = getSpeakerInUse();
            if (!speakerInUse.isOpen())
                speakerInUse.open();
            setMuteValue(speakerInUse, mute);
            speakerInUse.close();
        } catch (Exception e) {

        }
    }

    public float getFormattedMasterVolume() {
        final FloatControl volumeControl = getReadyVolumeControl();
        return audioUtil.convertLineRangeToVolRange(volumeControl.getValue(), volumeControl);
    }

    public void setFormattedMasterVolume(float volume) {
        final FloatControl volumeControl = getReadyVolumeControl();
        volumeControl.setValue(audioUtil.convertVolRangeToLineRange(volume, volumeControl));
    }

    public float getFormattedSpeakerVolume() {
        try {
            final Line speaker = getSpeakerInUse();
            final FloatControl volumeControl = getVolumeControl(speaker);
            return audioUtil.convertLineRangeToVolRange(volumeControl.getValue(), volumeControl);
        } catch (LineUnavailableException e) {
            return 0F;
        }
    }

    public void setFormattedSpeakerVolume(float volume) {
        try {
            final Line speaker = getSpeakerInUse();
            final FloatControl volumeControl = getVolumeControl(speaker);
            volumeControl.setValue(audioUtil.convertVolRangeToLineRange(volume, volumeControl));
        } catch (LineUnavailableException e) {
        }
    }

    public List<Line> getAvailableOutputLines(Mixer mixer) {
        return getAvailableLines(mixer, mixer.getTargetLineInfo());
    }

    public List<Line> getAvailableInputLines(Mixer mixer) {
        return getAvailableLines(mixer, mixer.getSourceLineInfo());
    }

    private static List<Line> getAvailableLines(Mixer mixer, Line.Info[] lineInfos) {
        final List<Line> lines = CollectionUtil.newLinkedList();
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

    public String getHierarchyInfo() {
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

    public String toString(Control control) {
        return control != null
                ? control.toString() + " (" + control.getType().toString() + ")" : null;
    }

    public String toString(Line line) {
        return line != null ? line.getLineInfo().toString() : null;
    }

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
