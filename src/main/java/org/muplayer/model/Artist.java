package org.muplayer.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class Artist {
    private String name;
    private Set<ReportableTrack> tracksSet;

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
