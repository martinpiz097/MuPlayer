package cl.estencia.labs.muplayer.unix.dbus.mpris;

import lombok.Getter;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;

@Getter
public class MprisConnection {
    private final String busName;
    private final DBusConnection dbus;

    public MprisConnection(String busName) throws DBusException {
        this.busName = busName;
        this.dbus = DBusConnectionBuilder.forSessionBus().build();
    }

    public void start(DBusInterface dBusInterface) throws DBusException {
        dbus.requestBusName(busName);
        dbus.exportObject(dBusInterface);
    }

    public void close() {
        dbus.disconnect();
    }

    @Override
    public String toString() {
        return "MprisConnection[" +
                "busName=" + busName + ", " +
                "dbus=" + dbus + ']';
    }

}
