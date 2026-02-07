package cl.estencia.labs.muplayer.unix.dbus.mpris;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.dbus.connections.IDisconnectCallback;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

@Getter
@Slf4j
public class MprisConnection {
    private final String busName;
    private final DBusInterface dBusInterface;
    private volatile DBusConnection dbusConnection;

    public MprisConnection(String busName, DBusInterface dBusInterface) {
        this.busName = busName;
        this.dBusInterface = dBusInterface;
    }

    private IDisconnectCallback createDisconnectCallback() {
        return new IDisconnectCallback() {
            @Override
            public void disconnectOnError(IOException _ex) {
                log.error("DBus disconnected due to error: " + _ex.getMessage());
            }

            @Override
            public void requestedDisconnect(Integer _connectionId) {
                log.trace("Requested disconnect from connection_id: " + _connectionId);
            }

            @Override
            public void clientDisconnect() {
                log.warn("DBus connection closed!");
            }

            @Override
            public void exceptionOnTerminate(IOException _ex) {
                log.error("Exception thrown when trying to disconnect dbus: " + _ex.getMessage());
            }
        };
    }

    private Consumer<DBusSignal> createUnknownSignalHandler() {
        return dBusSignal ->
                log.warn("Unknown DBus signal: " + dBusSignal.toString());
    }

    private DBusConnection createDBusConnection() throws DBusException {
        return DBusConnectionBuilder.forSessionBus()
                .withShared(true)
                .withDisconnectCallback(createDisconnectCallback())
                .withUnknownSignalHandler(createUnknownSignalHandler())
                .build();
    }

    private void checkConnection() {
        int retries = 1;
        int maxRetries = 10;
        while (!isConnected() && retries++ <= maxRetries) {
            log.trace("Connection retry " + retries);
            LockSupport.parkNanos(Duration.ofMillis(100).toNanos());
        }
    }

    public boolean isConnected() {
        return dbusConnection != null && dbusConnection.isConnected();
    }

    public synchronized void open() throws DBusException, InterruptedException {
        log.debug("Connecting with mpris...");
        dbusConnection = createDBusConnection();
        dbusConnection.requestBusName(busName);
        dbusConnection.exportObject(dBusInterface);

        if (dbusConnection.isConnected()) {
            log.debug("Connection established with mpris through the dbus name " + busName);
        } else {
            log.warn("The connection to mpris could not be established " +
                    "through the dbus name " + busName);
        }
    }

    public void close() {
        dbusConnection.disconnect();
    }

    public void sendDbusMessage(org.freedesktop.dbus.messages.Message message) {
        try {
            if (message == null) {
                log.warn("{message_type=null} " + "Dbus message is null!");
                return;
            }

//            checkConnection();
//            if (!isConnected()) {
//                log.warn("Cannot send message, not connected after retries");
//                return;
//            }

            dbusConnection.sendMessage(message);
            log.trace("{" + message.getClass().getSimpleName() + "} Dbus message sent!");
        } catch (Exception e) {
            if (message != null) {
                log.error("{" + message.getClass().getSimpleName() + "} " +
                        "Error sending dbus message: " + e.getMessage());
            } else {
                log.error("{null} " + "Error sending dbus message: " + e.getMessage());
            }
        }
    }

}
