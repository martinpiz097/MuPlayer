package cl.estencia.labs.muplayer.v2.file.bus.message;

import cl.estencia.labs.ebot.bus.model.message.Message;
import cl.estencia.labs.ebot.bus.model.message.MessageType;
import cl.estencia.labs.ebot.bus.model.message.SerializationType;
import cl.estencia.labs.muplayer.core.common.enums.SeekOption;
import cl.estencia.labs.muplayer.v2.file.bus.model.SkipTrackData;

import static cl.estencia.labs.muplayer.v2.file.bus.message.MuPlayerTopic.*;

public class Messages {
    public static Message createMsg(MuPlayerTopic topic, Object data) {
        return new Message(MessageType.NO_TYPE, topic.name(),
                data, SerializationType.ORIGINAL);
    }

    public static Message createMsg(MuPlayerTopic topic) {
        return createMsg(topic, "");
    }

    public static Message start() {
        return createMsg(START);
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
    
    public static Message skipTracks(int skipCount, SeekOption seekOption) {
        return createMsg(SKIP_TRACKS, new SkipTrackData(skipCount, seekOption));
    }

    public static Message mute() {
        return createMsg(MUTE);
    }

    public static Message unmute() {
        return createMsg(UNMUTE);
    }

}
