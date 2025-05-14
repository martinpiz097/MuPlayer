package cl.estencia.labs.muplayer.model;

import lombok.Data;
import cl.estencia.labs.muplayer.interfaces.TrackData;
import cl.estencia.labs.muplayer.util.CollectionUtil;

import java.util.List;

@Data
public class Album {
    private final String name;
    private final List<TrackData> listTracks;
    private final byte[] coverData;

    public Album(String name) {
        this.name = name;
        this.listTracks = CollectionUtil.newLinkedList();
        this.coverData = getCover();
    }

    private byte[] getCover() {
        return listTracks.parallelStream()
                .filter(TrackData::hasCover)
                .findFirst().map(TrackData::getCoverData)
                .orElse(null);
    }

    public void addTrack(TrackData trackInfo) {
        listTracks.add(trackInfo);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Album && (this == obj || name.equals(((Album) obj).getName()));
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
