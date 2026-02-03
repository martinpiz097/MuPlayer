//package cl.estencia.labs.muplayer.unix.dbus.mpris.interfaces;
//
//import org.freedesktop.dbus.annotations.DBusInterfaceName;
//import org.freedesktop.dbus.exceptions.DBusException;
//import org.freedesktop.dbus.messages.DBusSignal;
//
//@DBusInterfaceName("org.mpris.MediaPlayer2.Player")
//public class Seeked extends DBusSignal {
//    private final long position;
//
//    public Seeked(String path, long position) throws DBusException {
//        super(path, position);
//        this.position = position;
//    }
//
//    public long getPosition() { return position; }
//}