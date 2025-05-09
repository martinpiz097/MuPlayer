package cl.estencia.labs.muplayer.muplayer.util;

import cl.estencia.labs.muplayer.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.muplayer.audio.track.TrackIO;
import cl.estencia.labs.muplayer.muplayer.audio.player.Player;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TrackUtil {
    private final Map<String, Constructor<? extends Track>> mapTrackConstructors;

    public TrackUtil() {
        mapTrackConstructors = CollectionUtil.newFastMap();
    }

    public String getSongInfo(Track track) {
        final StringBuilder sbInfo = new StringBuilder();
        final String title = track.getTitle();
        final String album = track.getAlbum();
        final String artist = track.getArtist();
        final String date = track.getDate();
        final String duration = track.getFormattedDuration();
        final String hasCover = track.hasCover() ? "Yes" : "No";
        final String encoder = track.getEncoder();
        final String bitrate = track.getBitrate();

        final StringBuilder sbTabs = new StringBuilder();
        String currentLine = "Song: " + title;
        int biggerLength = currentLine.length();
        sbInfo.append(currentLine).append('\n');

        if (album != null) {
            sbTabs.append("    ");
            currentLine = sbTabs + "Album: " + album;
            if (biggerLength < currentLine.length()) {
                biggerLength = currentLine.length();
            }
            sbInfo.append(currentLine).append('\n');
        }

        if (artist != null) {
            sbTabs.append("    ");
            currentLine = sbTabs + "Artist: " + artist;
            if (biggerLength < currentLine.length()) {
                biggerLength = currentLine.length();
            }
            sbInfo.append(currentLine).append('\n');
        }

        if (date != null) {
            sbTabs.append("    ");
            currentLine = sbTabs + "Date: " + date;
            if (biggerLength < currentLine.length()) {
                biggerLength = currentLine.length();
            }
            sbInfo.append(currentLine).append('\n');
        }

        if (duration != null) {
            sbTabs.append("    ");
            currentLine = sbTabs + "Duration: " + duration;
            if (biggerLength < currentLine.length()) {
                biggerLength = currentLine.length();
            }
            sbInfo.append(currentLine).append('\n');
        }

        sbTabs.append("    ");
        currentLine = sbTabs + "Has Cover: " + hasCover;
        if (biggerLength < currentLine.length()) {
            biggerLength = currentLine.length();
        }
        sbInfo.append(currentLine).append('\n');

        if (encoder != null) {
            sbTabs.append("    ");
            currentLine = sbTabs + "Encoder: " + encoder;
            if (biggerLength < currentLine.length()) {
                biggerLength = currentLine.length();
            }
            sbInfo.append(currentLine).append('\n');
        }

        if (bitrate != null) {
            sbTabs.append("    ");
            currentLine = sbTabs + "Bitrate: " + bitrate + " kbps";
            if (biggerLength < currentLine.length()) {
                biggerLength = currentLine.length();
            }
            sbInfo.append(currentLine).append('\n');
        }

        sbTabs.delete(0, sbTabs.length());

        for (int i = 0; i < biggerLength; i++) {
            sbTabs.append('-');
        }
        sbTabs.append('\n').append(sbInfo);

        for (int i = 0; i < biggerLength; i++) {
            sbTabs.append('-');
        }
        sbTabs.append('\n');

        return sbTabs.toString();
    }

    private Class<?> getClassByPackage(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    public Set<Class<?>> findAllClassesUsingClassLoader(String packageName) {
        InputStream inputStream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClassByPackage(line, packageName))
                .collect(Collectors.toSet());
    }

    public Constructor<? extends Track> getTrackClassConstructor(String formatClass, Class<?>... paramsClasses) {
        try {
            Constructor<? extends Track> constructor = mapTrackConstructors.get(formatClass);
            if (constructor == null) {
                final Class<? extends Track> trackClass = (Class<? extends Track>)
                        Class.forName(formatClass);
                constructor = trackClass.getConstructor(paramsClasses);
                mapTrackConstructors.put(formatClass, constructor);
            }
            return constructor;
        } catch (Exception e) {
            return null;
        }
    }

    public Track getTrackFromClass(String formatClass, File dataSource, Player player) {
        try {
            final Constructor<? extends Track> constructor = getTrackClassConstructor(
                    formatClass, dataSource.getClass(), Player.class);
            return constructor != null ? constructor.newInstance(dataSource, player) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getLineInfo(Track track) {
        final TrackIO trackIO = track.getTrackIO();
        final SourceDataLine driver = trackIO.getSpeaker().getDriver();

        return new StringBuilder().append("Soporte de controles en line")
                .append("---------------")
                .append("Pan: ").append(driver.isControlSupported(FloatControl.Type.PAN))
                .append("AuxReturn: ").append(driver.isControlSupported(FloatControl.Type.AUX_RETURN))
                .append("AuxSend: ").append(driver.isControlSupported(FloatControl.Type.AUX_SEND))
                .append("Balance: ").append(driver.isControlSupported(FloatControl.Type.BALANCE))
                .append("ReverbReturn: ").append(driver.isControlSupported(FloatControl.Type.REVERB_RETURN))
                .append("ReberbSend: ").append(driver.isControlSupported(FloatControl.Type.REVERB_SEND))
                .append("Volume: ").append(driver.isControlSupported(FloatControl.Type.VOLUME))
                .append("SampleRate: ").append(driver.isControlSupported(FloatControl.Type.SAMPLE_RATE))
                .append("MasterGain: ").append(driver.isControlSupported(FloatControl.Type.MASTER_GAIN))
                .toString();
    }

}
