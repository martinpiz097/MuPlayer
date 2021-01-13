package org.muplayer.tests;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

public class MixerTest {
    public static void main(String[] args) throws Exception {
        final Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();

        for (Mixer.Info info : mixerInfo) {
            //System.out.println(info.getName());
            VolumeTest.scanPortMixer(AudioSystem.getMixer(info));
            System.out.println("--------------------------");
        }
    }
}
