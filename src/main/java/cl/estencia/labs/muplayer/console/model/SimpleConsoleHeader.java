package cl.estencia.labs.muplayer.console.model;

import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.player.MusicPlayer;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.core.bus.model.PlayerInfo;
import lombok.SneakyThrows;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.ARROW;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.SPACE_CHAR;
import static cl.estencia.labs.muplayer.console.util.ConsolePaintUtil.*;
import static cl.estencia.labs.muplayer.core.cache.CacheManager.CACHE;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.PLAYER_INFO;

public class SimpleConsoleHeader extends ConsoleHeader {
    @SneakyThrows
    @Override
    public String draw(MusicPlayer player) {
        StringBuilder sbHeader = new StringBuilder();
        PlayerInfo playerInfo = CACHE.get(
                PLAYER_INFO, PlayerInfo.class);
        Track currentTrack = playerInfo != null
                ? playerInfo.getCurrentTrack()
                : player.getCurrentTrack().get();
        PlayerStatusData playerStatusData = playerInfo != null
                ? playerInfo.getPlayerStatusData()
                : player.getPlayerStatusData();

        var systemVolume = player.getSystemVolume();

        sbHeader.append(paintMusicPlayerIcons(player.isPlaying())).append(SPACE_CHAR).append(SPACE_CHAR);
        sbHeader.append(paintBatteryStatus()).append(SPACE_CHAR).append(SPACE_CHAR);
        sbHeader.append(paintVolumeStatus(systemVolume, playerStatusData.isMute()))
                .append(SPACE_CHAR);

        if (currentTrack != null) {
            sbHeader.append(paintCurrentFolderInfo(currentTrack)).append(SPACE_CHAR);
            sbHeader.append(paintCurrentTrackName(currentTrack)).append(SPACE_CHAR);
        }
        sbHeader.append(ARROW).append(SPACE_CHAR);

        return sbHeader.toString();
    }
}
