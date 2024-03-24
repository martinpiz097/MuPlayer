/*package org.muplayer.audio.player.collection;

import org.muplayer.audio.track.Track;

import java.io.File;
import java.util.*;

public class MapMusicCollection extends MusicCollection<HashMap<String, List<Track>>> {

    private void sortTracks() {
        listTracks.sort((o1, o2) -> {
            if (o1 == null || o2 == null)
                return 0;
            final File dataSource1 = o1.getDataSourceAsFile();
            final File dataSource2 = o2.getDataSourceAsFile();
            if (dataSource1 == null || dataSource2 == null)
                return 0;
            return dataSource1.getPath().compareTo(dataSource2.getPath());
        });
        listFolders.sort(Comparator.comparing(File::getPath));
    }

    @Override
    public void sortTracks() {
    }

    @Override
    public void addTrack(Track track) {
        String key = track.getDataSourceAsFile().getParent();
        List<Track> listTracks = collection.getOrDefault(key, new LinkedList<>());

        listTracks.add(track);
        if (listTracks.size() == 1) {
            collection.put(key, listTracks);
        }
    }

    @Override
    public File findFolder(String name) {
        return null;
    }

    @Override
    public int getFolderIndex(Track track) {
        File dataSource = track.getDataSourceAsFile();
        final String filter = dataSource.getParent();

        Set<String> keySet = collection.keySet();
        int index = 0;
        for (String key : keySet) {
            if (key.equals(filter)) {
                return index;
            }
            index++;
        }

        return -1;
    }

    @Override
    public Track getTrackBy(int index) {
        Set<String> keySet = collection.keySet();
        int i = 0;
        for (String key : keySet) {
            if (i == index) {
                return null;
            }
            index++;
        }
        return null;
    }
}
*/