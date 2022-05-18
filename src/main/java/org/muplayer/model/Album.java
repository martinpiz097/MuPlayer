package org.muplayer.model;

import org.muplayer.interfaces.ReportableTrack;
import org.tritonus.share.ArraySet;

import java.util.Set;

public class Album {
    private String name;
    private Set<ReportableTrack> tracksSet;

    public Album() {
        tracksSet = new ArraySet<>();
    }

    /*public Artist getArtist() {
        if (tracksSet.isEmpty())
            return null;
        else {
            Artist artist = new Artist();
            artist.setName(tracksSet.iterator().next().getArtist());
            artist.setTracksSet(tracksSet);
            return artist;
        }
    }*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getCover() {
        if (tracksSet.isEmpty())
            return null;
        else {
            ReportableTrack trackInfo = tracksSet.parallelStream()
                    .filter(ReportableTrack::hasCover)
                    .findFirst().orElse(null);
            return trackInfo == null ? null : trackInfo.getCoverData();
        }
    }

    public void addTrack(ReportableTrack trackInfo) {
        tracksSet.add(trackInfo);
    }

    public Set<ReportableTrack> getTracksSet() {
        return tracksSet;
    }

    public void setTracksSet(Set<ReportableTrack> tracksSet) {
        this.tracksSet = tracksSet;
    }
}
