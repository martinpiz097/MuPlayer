package cl.estencia.labs.muplayer.util;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.listener.notifier.internal.TrackInternalEventNotifier;
import lombok.Getter;
import lombok.extern.java.Log;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
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

    public Constructor<? extends Track> getInitConstructor(Class<? extends Track> clazz,
                                                           Class<?>... parameterTypes) {
        try {
            var constructor = clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);

            return constructor;
        } catch (NoSuchMethodException e) {
            log.severe(LogUtil.getExceptionMsg(e, "getInitConstructor"));
            return null;
        }
    }

    public <T extends Track> T tryInstance(Class<? extends Track> clazz, Object... params) {
        return tryInstance(getInitConstructor(clazz), params);
    }

    public <T extends Track> T tryInstance(Constructor<? extends Track> initConstructor, Object... params) {
        if (initConstructor == null || params == null || params.length == 0) {
            return null;
        }

        try {
            Object[] anotherParameters = Arrays.copyOfRange(params, 1, params.length);

            return (T) initConstructor.newInstance(params[0], anotherParameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.severe(LogUtil.getExceptionMsg(e, "tryInstance"));
            return null;
        }
    }

}
