package org.muplayer.model;

import org.muplayer.interfaces.ReportableTrack;
import org.tritonus.share.ArraySet;

import java.util.LinkedHashSet;
import java.util.Set;

public class Artist {
    private final String name;
    private final Set<ReportableTrack> tracksSet;

    public Artist(String name) {
        this.name = name;
        tracksSet = new ArraySet<>();
    }

    public String getName() {
        return name;
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Artist && (this == obj || name.equals(((Artist) obj).getName()));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
