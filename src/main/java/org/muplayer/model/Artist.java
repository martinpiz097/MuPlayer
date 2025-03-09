package org.muplayer.model;

import lombok.Data;
import org.muplayer.interfaces.ReportableTrack;
import org.muplayer.util.CollectionUtil;

import java.util.List;
import java.util.Set;

@Data
public class Artist {
    private final String name;
    private final List<ReportableTrack> listTracks;

    public Artist(String name) {
        this.name = name;
        this.listTracks = CollectionUtil.newLinkedList();
    }

    public byte[] getAnyCover() {
        return listTracks.parallelStream()
                .filter(ReportableTrack::hasCover)
                .findFirst().map(ReportableTrack::getCoverData)
                .orElse(null);
    }

    public void addTrack(ReportableTrack trackInfo) {
        listTracks.add(trackInfo);
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
