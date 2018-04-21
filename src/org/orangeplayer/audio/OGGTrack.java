package org.orangeplayer.audio;

import java.io.File;

public class OGGTrack extends Track {

    public OGGTrack(File ftrack) {
        super(ftrack);
    }

    @Override
    protected void getAudioStream() throws Exception {

    }

    @Override
    public void seek(int seconds) throws Exception {

    }
}
