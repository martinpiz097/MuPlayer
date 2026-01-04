package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;

public class StartedState extends TrackState {

    public StartedState(Track track) {
        super(TrackStateName.STARTED, track);
    }

    @Override
    public void handle() {
        speaker.open();

        // se captura status en variable antes porque al llamar a setVolume
        // el estado de boolean isMute de playerStatusData se cambia dependiendo del valaor
        // de volume
        boolean isMuted = trackStatusData.isMute();

        track.setVolume(trackStatusData.getVolume());
        if (isMuted) {
            track.mute();
        }

        track.play();
    }
}