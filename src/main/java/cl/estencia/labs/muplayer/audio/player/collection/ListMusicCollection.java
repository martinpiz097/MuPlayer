/*package org.muplayer.audio.player.collection;

import org.muplayer.audio.track.Track;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ListMusicCollection extends MusicCollection<ArrayList<Track>> {
    private final List<File> listFolders;

    private static final int DEFAULT_INITIAL_LIST_CAPACITY = 500;

    public ListMusicCollection() {
        this.collection = new ArrayList<>(DEFAULT_INITIAL_LIST_CAPACITY);
        this.listFolders = new ArrayList<>(DEFAULT_INITIAL_LIST_CAPACITY);
    }

    @Override
    public void sortTracks() {

    }

    @Override
    public void addTrack(Track track) {
        collection.add(track);
    }

    @Override
    public File findFolder(String name) {
        return null;
    }

    private int getFolderIndex() {
        final File dataSource = current.getDataSourceAsFile();
        final File currentParent = current != null ? dataSource.getParentFile() : null;
        return currentParent != null ? listFolders.indexOf(currentParent) : -1;
    }
}
*/