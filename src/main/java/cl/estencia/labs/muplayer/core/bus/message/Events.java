package cl.estencia.labs.muplayer.core.bus.message;

import cl.estencia.labs.ebot.bus.model.message.Message;
import cl.estencia.labs.ebot.bus.model.message.MessageType;
import cl.estencia.labs.ebot.bus.model.message.SerializationType;
import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.core.bus.model.LoadingPlayerInfo;
import cl.estencia.labs.muplayer.core.bus.model.PlayerInfo;
import cl.estencia.labs.muplayer.core.bus.model.SkipData;
import cl.estencia.labs.muplayer.audio.common.enums.SeekOption;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cl.estencia.labs.muplayer.core.bus.message.PlayerEventTopics.*;
import static cl.estencia.labs.muplayer.core.util.CollectionUtil.newList;

public class Events {

    public static Message createMsg(PlayerEventTopics topic, Object data) {
        return new Message(MessageType.NO_TYPE, topic.name(),
                data, SerializationType.ORIGINAL);
    }

    public static Message createMsg(PlayerEventTopics topic) {
        return createMsg(topic, "");
    }

    public static Message playerInfo(PlayerInfo playerInfo) {
        return createMsg(PLAYER_RESPONSE, playerInfo);
    }

    public static Message playerInfo(AtomicReference<Track> currentTrack,
                                     List<Track> tracks,
                                     List<File> folders,
                                     PlayerStatusData playerStatusData) {
        return playerInfo(new PlayerInfo(
                currentTrack.get(),
                newList(tracks),
                newList(folders), playerStatusData));
    }

    public static Message start() {
        return createMsg(START);
    }

    public static Message loading(LoadingPlayerInfo loadingPlayerInfo) {
        return createMsg(LOADING, loadingPlayerInfo);
    }

    public static Message loading(int tracksCount) {
        return loading(new LoadingPlayerInfo(tracksCount));
    }

    public static Message reload() {
        return createMsg(RELOAD);
    }

    public static Message playNext() {
        return createMsg(PLAY_NEXT);
    }

    public static Message playPrev() {
        return createMsg(PLAY_PREVIOUS);
    }

    public static Message playIndex(int index) {
        return createMsg(PLAY_INDEX, index);
    }

    public static Message playFolder(int folderIndex) {
        return createMsg(PLAY_FOLDER, folderIndex);
    }

    public static Message play() {
        return createMsg(PLAY);
    }

    public static Message shutdown() {
        return createMsg(SHUTDOWN);
    }

    public static Message pause() {
        return createMsg(PAUSE);
    }

    public static Message resume() {
        return createMsg(RESUME);
    }

    public static Message stop() {
        return createMsg(STOP);
    }

    public static Message seekSeconds(int seconds) {
        return createMsg(SEEK_SECONDS, seconds);
    }

    public static Message skipTracks(SkipData skipData) {
        return createMsg(SKIP_TRACKS, skipData);
    }

    public static Message skipTracks(int skipCount, SeekOption seekOption) {
        return skipTracks(new SkipData(skipCount, seekOption));
    }

    public static Message seekFolder(SkipData skipData) {
        return createMsg(SEEK_FOLDER, skipData);
    }

    public static Message seekFolder(int skipCount, SeekOption seekOption) {
        return seekFolder(new SkipData(skipCount, seekOption));
    }

    public static Message gotoSeconds(int seconds) {
        return createMsg(GOTO, seconds);
    }

    public static Message mute() {
        return createMsg(MUTE);
    }

    public static Message unmute() {
        return createMsg(UNMUTE);
    }

//    public static Message getVolume() {
//        return createMsg(GET_VOLUME);
//    }

    public static Message setVolume(float volume) {
        return createMsg(SET_VOLUME, volume);
    }

}
