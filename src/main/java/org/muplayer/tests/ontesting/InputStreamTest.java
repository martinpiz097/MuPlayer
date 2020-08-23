package org.muplayer.tests.ontesting;

import org.muplayer.audio.Track;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class InputStreamTest {

    public static void main(String[] args) throws IOException {
        String url = "http://www.lindberg.no/hires/test/2L-125_stereo-44k-16b_04.flac";
        InputStream inputStream = new URL(url).openStream();

        Track.getTrack(new BufferedInputStream(inputStream)).start();
    }
}
