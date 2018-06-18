/*package org.muplayer.audio;

import org.muplayer.audio.codec.DecodeManager;
import org.muplayer.audio.formats.*;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

import static org.muplayer.audio.AudioExtensions.*;
import static org.muplayer.audio.AudioExtensions.AIFC;
import static org.muplayer.audio.AudioExtensions.M4A;

public class TrackManager {
    public TrackManager() {}

    public Track newTrack(File source) {
        if (!source.exists())
            return null;
        Track result = null;
        final String trackName = source.getName();
        try {
            if (trackName.endsWith(MPEG))
                result = new MP3Track(source);
            else if (trackName.endsWith(OGG))
                result = new OGGTrack(source);
            else if (trackName.endsWith(FLAC))
                result = new FlacTrack(source);
            else if (trackName.endsWith(WAVE) || trackName.endsWith(AU)
                    || trackName.endsWith(AU) || trackName.endsWith(SND)
                    || trackName.endsWith(AIFF) || trackName.endsWith(AIFC))
                result = new PCMTrack(source);
            else if (trackName.endsWith(M4A)){
                System.out.println("Es mp4");
                result = new M4ATrack(source);
            }
            // Por si no tiene formato en el nombre
            else {
                AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(source);
                // Es ogg, aac o mp3
                if (AudioSystem.isConversionSupported(
                        AudioFormat.Encoding.PCM_SIGNED, fileFormat.getFormat())) {
                    // no es mp3
                    if (DecodeManager.isVorbis(source))
                        result = new OGGTrack(source);
                    else
                        // Tambien sirve para los mp4
                        result = new PCMTrack(source);
                } else
                    result = new MP3Track(source);

                // Ver si es mp3
                // Podria ser que por reflection revise entre todas las subclases
                // si una es compatible con el archivo en cuestion
            }
        } catch (UnsupportedAudioFileException e) {
            // Es flac
            if (DecodeManager.isFlac(source)) {
                try {
                    result = new FlacTrack(source);
                    if (!result.isValidTrack())
                        result = null;
                } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e1) {
                    e1.printStackTrace();
                }

            }
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        return result;
    }

    public Track newTrack(String sourcePath) {
        return newTrack(new File(sourcePath));
    }

    public boolean isValidTrack(File source) {
        Track result = newTrack(source);
        return result != null && result.isValidTrack();
    }

}
*/