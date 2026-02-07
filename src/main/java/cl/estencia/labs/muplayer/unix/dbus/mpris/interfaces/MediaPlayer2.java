package cl.estencia.labs.muplayer.unix.dbus.mpris.interfaces;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.annotations.DBusMemberName;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;

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
        void stop();

        @DBusMemberName("Seek")
        void seek(long offset);

        @DBusMemberName("SetPosition")
        void setPosition(String trackId, long position);

        @DBusMemberName("OpenUri")
        void openUri(String uri);

        class Seeked extends DBusSignal {
            private final long position;

            public Seeked(String path, long position) throws DBusException {
                super(path, position);
                this.position = position;
            }

            public long getPosition() { return position; }
        }
    }

    @DBusInterfaceName("org.mpris.MediaPlayer2.TrackList")
    interface TrackList {

    }

}