package cl.estencia.labs.muplayer.unix.dbus.mpris.interfaces;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusMemberName;
import org.freedesktop.dbus.interfaces.DBusInterface;

@DBusInterfaceName("org.mpris.MediaPlayer2")
public interface MediaPlayer2 extends DBusInterface {
    @DBusMemberName("Raise")
    void raise();

    @DBusMemberName("Quit")
    void quit();

    @DBusInterfaceName("org.mpris.MediaPlayer2.Player")
    public interface Player extends DBusInterface {

        @DBusMemberName("Play")
        void play();

        @DBusMemberName("Pause")
        void pause();

        @DBusMemberName("PlayPause")
        void playPause();

        @DBusMemberName("Next")
        void next();

        @DBusMemberName("Previous")
        void previous();

        @DBusMemberName("Stop")
        void Stop();

        @DBusMemberName("Seek")
        void seek(long offset);

        @DBusMemberName("SetPosition")
        void setPosition(String trackId, long position);

        @DBusMemberName("OpenUri")
        void openUri(String uri);
    }

    @DBusInterfaceName("org.mpris.MediaPlayer2.TrackList")
    interface TrackList {

    }

}