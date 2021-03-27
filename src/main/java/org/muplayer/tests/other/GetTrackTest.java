package org.muplayer.tests.other;

import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.muplayer.audio.Track;
import org.muplayer.audio.codec.DecodeManager;
import org.muplayer.audio.format.FlacTrack;
import org.muplayer.audio.format.MP3Track;
import org.muplayer.audio.format.OGGTrack;
import org.muplayer.audio.format.PCMTrack;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

import static org.muplayer.audio.util.AudioExtensions.*;

public class GetTrackTest {
    static long ti;
    static long tf;
    static final File fSound = new File("/home/martin/AudioTesting/audio/au.mp3");
    static final File fImg = new File("/home/martin/AudioTesting/audio/folder.jpg");
    static Track result = null;

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        long oldSound, newSound, oldImg, newImg;
        //oldGetTrack(fSound);
        oldSound = tf;
        newGetTrack(fSound);
        newSound = tf;

        //oldGetTrack(fImg);
        oldImg = tf;

        newGetTrack(fImg);
        newImg = tf;

        System.err.println("OldGetTrackSoundWav: "+oldSound);
        System.err.println("NewGetTrackSoundWav: "+newSound);
        System.err.println("OldGetTrackImg: "+oldImg);
        System.err.println("NewGetTrackImg: "+newImg);
    }

    /*public static void oldGetTrack(File fSound) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        ti = System.currentTimeMillis();
        try {
            VorbisFile vorbisTest = new VorbisFile(fSound.getCanonicalPath());
            result = new OGGTrack(fSound);
            System.out.println("OGGAis: "+result.getDecodedStream());
            System.out.println("OGGFileFormat: "+result.getFileFormat());
        } catch (JOrbisException e) {
            try {
                result = new MP3Track(fSound);
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e1) {
                //System.out.println(e.getMessage());
                result = new FlacTrack(fSound);
                if (result.getDecodedStream() == null)
                    result = null;
                else
                    System.out.println("TrackStream: "+result.getDecodedStream());
            } catch (InvalidAudioFrameException e1) {
                e1.printStackTrace();
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            //System.out.println(e.getMessage());
            result = null;
        }
        tf = System.currentTimeMillis();
        tf -= ti;
    }*/

    public static void newGetTrack(File fSound) {
        String trackName = fSound.getName();
        try {
            if (trackName.endsWith(MPEG))
                result = new MP3Track(fSound);
            else if (trackName.endsWith(OGG))
                result = new OGGTrack(fSound);
            else if (trackName.endsWith(FLAC))
                result = new FlacTrack(fSound);
            else if (trackName.endsWith(WAVE))
                result = new PCMTrack(fSound);
                // Por si no tiene formato en el nombre
            else {
                AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(fSound);
                // Es ogg, aac o mp3
                if (AudioSystem.isConversionSupported(
                        AudioFormat.Encoding.PCM_SIGNED, fileFormat.getFormat())) {
                    // no es mp3
                    if (DecodeManager.isVorbis(fSound))
                        result = new OGGTrack(fSound);
                    else
                        result = new PCMTrack(fSound);
                } else
                    result = new MP3Track(fSound);

                // Ver si es mp3
                // Podria ser que por reflection revise entre todas las subclases
                // si una es compatible con el archivo en cuestion
            }
        } catch (UnsupportedAudioFileException e) {
            // Es flac
            if (DecodeManager.isFlac(fSound)) {
                try {
                    result = new FlacTrack(fSound);
                    if (!result.isValidTrack())
                        result = null;
                } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e1) {
                    e1.printStackTrace();
                }

            }
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        }
    }
}
