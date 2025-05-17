package cl.estencia.labs.muplayer.event.model;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.event.listener.PlayerEventType;

import java.io.File;
import java.util.List;

public record PlayerEvent(PlayerEventType type,
                          Player player,

                          List<Track> listTracks,
                          List<File> listFolders) {}
