package cl.estencia.labs.muplayer.core.bus.message;

// son ActionTopics, usados para ejecutar "acciones" especificas dentro
// del reproductor, para obtener informacion del mismo se puede
// usar el listener del objeto PlayerInfo o directamente los metodos de MusicPlayer
// aunque la idea es ir orientandolo mas a eventos
public enum ActionTopics {
    PLAYER_RESPONSE,
    START,
    LOADING,
    RELOAD,
    PLAY_NEXT,
    PLAY_PREVIOUS,
    PLAY_INDEX,
    PLAY_FOLDER,
    PLAY,
    SHUTDOWN,
    PAUSE,
    RESUME,
    STOP,
    SEEK_SECONDS,
    SKIP_TRACKS,
    SEEK_FOLDER,
    GOTO,
    MUTE,
    UNMUTE,
    SET_VOLUME
}
