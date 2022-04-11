package org.muplayer.audio.model;

import org.tritonus.share.ArraySet;

import java.util.Set;

public class Album {
    private String name;
    private Set<TrackInfo> tracksSet;

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
            TrackInfo trackInfo = tracksSet.parallelStream()
                    .filter(TrackInfo::hasCover)
                    .findFirst().orElse(null);
            return trackInfo == null ? null : trackInfo.getCoverData();
        }
    }

    public void addTrack(TrackInfo trackInfo) {
        tracksSet.add(trackInfo);
    }

    public Set<TrackInfo> getTracksSet() {
        return tracksSet;
    }

    public void setTracksSet(Set<TrackInfo> tracksSet) {
        this.tracksSet = tracksSet;
    }
}
