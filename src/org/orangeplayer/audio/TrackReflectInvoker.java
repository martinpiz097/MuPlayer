package org.orangeplayer.audio;

import org.orangeplayer.audio.tracksFormats.FlacTrack;
import org.orangeplayer.audio.tracksFormats.MP3Track;
import org.orangeplayer.audio.tracksFormats.OGGTrack;

import java.util.LinkedList;

public class TrackReflectInvoker {
    private static final Class<Track> trackClass;
    private static final LinkedList<Class> listTrackClasses;

    static {
        trackClass = Track.class;
        listTrackClasses = new LinkedList<>();
        listTrackClasses.add(MP3Track.class);
        listTrackClasses.add(OGGTrack.class);
        listTrackClasses.add(FlacTrack.class);
    }



}
