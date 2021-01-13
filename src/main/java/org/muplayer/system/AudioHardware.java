package org.muplayer.system;

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

    public static List<Mixer> getAllMixers() {
        return Arrays.stream(AudioSystem.getMixerInfo())
                .map(AudioSystem::getMixer)
                .collect(Collectors.toList());
    }
}
