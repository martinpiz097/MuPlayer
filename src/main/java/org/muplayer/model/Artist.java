package org.muplayer.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class Artist {
    private String name;
    private Set<TrackInfo> tracksSet;

    public Artist() {
        tracksSet = new LinkedHashSet<>();
    }

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
