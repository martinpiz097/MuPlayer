//package cl.estencia.labs.muplayer.listener.runner;
//
//import cl.estencia.labs.muplayer.audio.track.Track;
//import cl.estencia.labs.muplayer.listener.ListenerMethodName;
//import cl.estencia.labs.muplayer.listener.PlayerEvent;
//
//import java.util.List;
//
//public class PlayerListenerRunner implements Runnable {
//    private final List<PlayerEvent> listListeners;
//    private final ListenerMethodName methodName;
//    private final Track track;
//
//    public PlayerListenerRunner(List<PlayerEvent> listListeners, ListenerMethodName methodName, Track track) {
//        this.listListeners = listListeners;
//        this.methodName = methodName;
//        this.track = track;
//    }
//
//    @Override
//    public void run() {
//        switch (methodName) {
//            case ON_SONG_CHANGE:
//                listListeners.parallelStream()
//                        .forEach(listener-> listener.onSongChange(track));
//                break;
//            case ON_PLAYING:
//                listListeners.parallelStream()
//                        .forEach(listener-> listener.onPlaying(track));
//                break;
//            case ON_RESUMED:
//                listListeners.parallelStream()
//                        .forEach(listener-> listener.onResumed(track));
//                break;
//            case ON_PAUSED:
//                listListeners.parallelStream()
//                        .forEach(listener-> listener.onPaused(track));
//                break;
//            case ON_STARTED:
//                listListeners.parallelStream()
//                        .forEach(PlayerEvent::onStarted);
//                break;
//            case ON_STOPPED:
//                listListeners.parallelStream()
//                        .forEach(listener-> listener.onStopped(track));
//                break;
//            case ON_SEEK:
//                listListeners.parallelStream()
//                        .forEach(listener-> listener.onSeek(track));
//                break;
//            case ON_SHUTDOWN:
//                listListeners.parallelStream()
//                        .forEach(PlayerEvent::onShutdown);
//                break;
//        }
//    }
//}
