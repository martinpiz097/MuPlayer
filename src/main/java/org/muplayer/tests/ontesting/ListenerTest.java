package org.muplayer.tests.ontesting;

import org.muplayer.audio.Player;
import org.muplayer.audio.Track;
import org.muplayer.audio.interfaces.PlayerListener;

import java.io.FileNotFoundException;

public class ListenerTest {
    public static void main(String[] args) throws FileNotFoundException {
        Player player = new Player("/home/martin/Escritorio");
        player.addPlayerListener(new PlayerListener() {
            @Override
            public void onSongChange(Track newTrack) {
                System.out.println("SongChanged!");
            }

            @Override
            public void onPlayed(Track track) {

            }

            @Override
            public void onPlaying(Track track) {
                System.out.println("Playing: "+track.getTitle());
                System.out.println("TrackProgress: "+track.getFormattedProgress());
                System.out.println("------------------");
            }

            @Override
            public void onResumed(Track track) {

            }

            @Override
            public void onPaused(Track track) {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onStopped(Track track) {

            }

            @Override
            public void onSeeked(Track track) {

            }

            @Override
            public void onShutdown() {

            }
        });
        player.start();
        player.mute();
    }
}
