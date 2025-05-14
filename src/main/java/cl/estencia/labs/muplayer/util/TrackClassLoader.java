package cl.estencia.labs.muplayer.util;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Log
public class TrackClassLoader {
    private final ReflectionUtil reflectionUtil;
    private final Set<Class<? extends Track>> trackClassesSet;
    @Getter private final List<Constructor<? extends Track>> listInitConstructors;

    public TrackClassLoader() {
        this.reflectionUtil = new ReflectionUtil();
        this.trackClassesSet = findAllTrackSubclasses();
        this.listInitConstructors = new ArrayList<>();

        loadConstructors();
    }

    private void loadConstructors() {
        trackClassesSet.parallelStream()
                .map(this::getInitConstructor)
                .sequential()
                .forEach(listInitConstructors::add);
    }

    public Set<Class<? extends Track>> findAllTrackSubclasses() {
        return reflectionUtil.findAllSubclassesOf(Track.class);
    }

    public Constructor<? extends Track> getInitConstructor(Class<? extends Track> clazz) {
        try {
            var constructor = clazz.getDeclaredConstructor(File.class, Player.class);
            constructor.setAccessible(true);

            return constructor;
        } catch (NoSuchMethodException e) {
            log.severe(LogUtil.getExceptionMsg(e, "getInitConstructor"));
            return null;
        }
    }

    public <T extends Track> T tryInstance(Class<? extends Track> clazz, File firstParam, Player secondParam) {
        return tryInstance(getInitConstructor(clazz), firstParam, secondParam);
    }

    public <T extends Track> T tryInstance(Constructor<? extends Track> initConstructor, File firstParam, Player secondParam) {
        if (initConstructor == null) {
            return null;
        }

        try {
            return (T) initConstructor.newInstance(firstParam, secondParam);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.severe(LogUtil.getExceptionMsg(e, "tryInstance"));
            return null;
        }
    }

}
