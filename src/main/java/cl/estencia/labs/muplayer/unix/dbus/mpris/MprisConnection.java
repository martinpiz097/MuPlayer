package cl.estencia.labs.muplayer.unix.dbus.mpris;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;

@Getter
@Slf4j
public class MprisConnection {
    private final String busName;
    private final DBusConnection dbus;

    public MprisConnection(String busName) throws DBusException {
        this.busName = busName;
        this.dbus = DBusConnectionBuilder.forSessionBus().build();
    }

    public void start(DBusInterface dBusInterface) throws DBusException {
        log.debug("Connecting with mpris...");
        dbus.requestBusName(busName);
        dbus.exportObject(dBusInterface);

        if (dbus.isConnected()) {
            log.debug("Connection established with mpris through the dbus name " + busName);
        } else {
            log.warn("The connection to mpris could not be established " +
                    "through the dbus name " + busName);
        }
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
