package cl.estencia.labs.muplayer.unix.dbus.mpris;

import cl.estencia.labs.ebot.utils.collection.CollectionUtil;
import cl.estencia.labs.ebot.utils.threads.Interruptor;
import cl.estencia.labs.muplayer.core.bus.model.PlayerInfo;
import cl.estencia.labs.muplayer.unix.dbus.mpris.common.LoopStatus;
import cl.estencia.labs.muplayer.unix.dbus.mpris.common.PlaybackStatus;
import cl.estencia.labs.muplayer.unix.dbus.mpris.interfaces.MediaPlayer2;
import cl.estencia.labs.muplayer.unix.dbus.mpris.queue.MprisPublishAction;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cl.estencia.labs.muplayer.core.util.CollectionUtil.newMap;
import static cl.estencia.labs.muplayer.unix.dbus.mpris.common.MprisConstants.*;

@Getter
@Slf4j
public class MprisPublisher extends Thread {
    private final MprisConnection connection;
    private final AtomicReference<PlayerInfo> playerInfoRef;
    private final AtomicReference<String> trackIdRef;
    private final AtomicReference<Map<String, Variant<?>>> metadataRef;
    private final AtomicReference<PlaybackStatus> playbackStatusRef;
    private final AtomicReference<LoopStatus> loopStatusRef;

    private final Deque<MprisPublishAction> publishActionsQueue;
    private final Interruptor interruptor;

    public MprisPublisher(MprisConnection connection) {
        this.connection = connection;
        this.playerInfoRef = new AtomicReference<>(null);
        this.trackIdRef = new AtomicReference<>(UNKNOWN_TRACK_ID);
        this.metadataRef = new AtomicReference<>(newMap());
        this.playbackStatusRef = new AtomicReference<>(PlaybackStatus.Stopped);
        this.loopStatusRef = new AtomicReference<>(LoopStatus.None);

        this.publishActionsQueue = CollectionUtil.newThreadSafeQueue();
        this.interruptor = Interruptor.manual(this);
        setName("mpris-publisher");
    }

    public void sendSeekedSignal(long position) {
        enqueueAction(() -> {
            try {
                sendDbusMessage(new MediaPlayer2.Player.Seeked(DBUS_OBJECT_PATH, position));
            } catch (DBusException e) {
                log.error("Error emitiendo Seeked: " + e.getMessage());
            }
        });
    }

    public void sendPropertiesChangedEvent(Map<String, Variant<?>> propertiesChanged) {
        enqueueAction(() -> {
            try {
                sendDbusMessage(new Properties.PropertiesChanged(
                        DBUS_OBJECT_PATH,
                        DBUS_PLAYER_INTERFACE,
                        propertiesChanged,
                        List.of()
                ));
            } catch (DBusException e) {
                log.error("Error emitiendo PropertiesChanged: " + e.getMessage());
            }
        });
    }

    public void sendPropertiesChangedEvent(String name, Object value) {
        sendPropertiesChangedEvent(Map.of(name, new Variant<>(value)));
    }

    public void sendPropertiesChangedEvent(String name, Object value, String sig) {
        sendPropertiesChangedEvent(Map.of(name, new Variant<>(value, sig)));
    }

    public void shutdown() {
        publishActionsQueue.clear();

        interrupt();
        interruptor.switchOn();
    }

    private void sendDbusMessage(org.freedesktop.dbus.messages.Message message) {
        try {
            DBusConnection dbus = connection.getDbus();
            if (!dbus.isConnected()) {
                return;
            }

            dbus.sendMessage(message);
        } catch (Exception e) {
            log.error("Error sending dbus message: " + e.getMessage());
        }
    }

    private void enqueueAction(MprisPublishAction action) {
        publishActionsQueue.add(action);
        interruptor.assignNecessaryPermits(publishActionsQueue.size());
    }

    private void executeAction(MprisPublishAction publishAction) {
        if (publishAction == null) {
            return;
        }

        Thread.ofVirtual().start(publishAction::execute);
    }

    @Override
    public void run() {
        log.trace("Mpris publisher started!");

        // TODO ojo con los errores que pueden provocar que el hilo termine con
        // acciones pendientes en la cola
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (!publishActionsQueue.isEmpty()) {
                    executeAction(publishActionsQueue.pollFirst());
                }

                interruptor.checkSignal();
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }

    }

}
