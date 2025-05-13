package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.aucom.audio.device.Speaker;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.TrackStatusData;
import cl.estencia.labs.muplayer.audio.track.TrackIO;
import cl.estencia.labs.muplayer.listener.TrackEvent;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class TrackState {
    protected final Player player;
    protected final Track track;
    protected final TrackStatusData trackData;
    protected final TrackIO trackIO;
    protected final Speaker speaker;

    protected final List<TrackEvent> listInternalEvents;

    @Getter protected final TrackStateName name;

    public TrackState(Player player, Track track,
                      TrackStateName name, List<TrackEvent> listInternalEvents) {
        this.player = player;
        this.track = track;
        this.trackData = track.getTrackStatusData();
        this.trackIO = track.getTrackIO();
        this.speaker = track.getSpeaker();
        this.listInternalEvents = listInternalEvents;
        this.name = name;
    }

    protected void getNotifyAction(TrackEvent trackEvent) {
        trackEvent.onStateChange(track, name);
    }

    protected CompletableFuture<Void> notifyState() {
        var internalEventsTask = CompletableFuture.runAsync(() ->
                listInternalEvents.parallelStream().forEach(this::getNotifyAction));

        var userEventsTask = CompletableFuture.runAsync(() ->
                track.getListUserEvents().parallelStream().forEach(this::getNotifyAction));

        return CompletableFuture.allOf(internalEventsTask, userEventsTask);
    }

    protected abstract void handle();

    public void execute() {
        notifyState();
        handle();
    }
}