package org.orangeplayer.audio;

import org.aucom.sound.Speaker;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MP3Track extends Track {

    public MP3Track(File ftrack) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        super(ftrack);
        getAudioStream();
        trackLine = new Speaker(speakerAis.getFormat());
        trackLine.open();
    }

    private void getAudioStream() throws IOException, UnsupportedAudioFileException {
        AudioInputStream soundAis = AudioSystem.getAudioInputStream(ftrack);
        AudioFormat baseFormat = soundAis.getFormat();
        System.out.println(baseFormat.getSampleRate());
        System.out.println(baseFormat.getFrameRate());
        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);

        speakerAis = AudioSystem.getAudioInputStream(decodedFormat, soundAis);
    }

    private void resetStream() throws IOException, UnsupportedAudioFileException {
        speakerAis.close();
        getAudioStream();
    }

    @Override
    public boolean isPlaying() {
        return state == PLAYING;
    }

    @Override
    public boolean isPaused() {
        return state == PAUSED;
    }

    @Override
    public boolean isStoped() {
        return state == STOPED;
    }

    @Override
    public boolean isFinished() {
        return state == FINISHED;
    }

    @Override
    public void play() {
        state = PLAYING;
    }

    @Override
    public void pause() {
        state = PAUSED;
    }

    @Override
    public void resume() {
        play();
    }


    @Override
    public void stop() {
        state = STOPED;
    }

    @Override
    public void finish() {
        state = FINISHED;
    }

    @Override
    public void seek(int seconds) {
        // Pendiente
    }

    @Override
    public void run() {
        try {
            byte[] audioBuffer = new byte[BUFFSIZE];
            int read;
            play();

            while (!isFinished()) {
                while (isPlaying()) {
                    read = speakerAis.read(audioBuffer);
                    trackLine.playAudio(audioBuffer);
                    if (read == -1)
                        finish();
                }
                if (isStoped()) {
                    resetStream();
                    while (isStoped()) {
                    }
                }

                System.out.print("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
        File sound = new File("/home/martin/AudioTesting/audio/au.mp3");
        MP3Track track = new MP3Track(sound);
        Thread tTrack = new Thread(track);
        tTrack.start();
        Thread.sleep(3000);
        track.pause();
        Thread.sleep(3000);
        track.resume();
    }


}
