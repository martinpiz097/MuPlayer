package cl.estencia.labs.muplayer.listener.event;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.listener.PlayerEventType;

import java.io.File;
import java.util.List;

public record PlayerEvent(PlayerEventType type,
                          Player player,

                          List<Track> listTracks,
                          List<File> listFolders) {}
