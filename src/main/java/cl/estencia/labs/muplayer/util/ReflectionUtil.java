package cl.estencia.labs.muplayer.util;

import lombok.extern.java.Log;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Log
public class ReflectionUtil {
    private Set<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        if (!directory.exists()) {
            return Set.of();
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return Set.of();
        }

        final String classFileFilter = ".class";
        Set<Class<?>> classes = new HashSet<>();
        File file;
        String className;
        for (int i = 0, filesLength = files.length; i < filesLength; i++) {
            file = files[i];
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(classFileFilter)) {
                className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }

        return classes;
    }

    private Set<Class<?>> getClasses(String packageName) throws Exception {
        final Set<Class<?>> classes = new HashSet<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        URL resource;
        File directory;
        while (resources.hasMoreElements()) {
            resource = resources.nextElement();
            directory = new File(resource.getFile());
            classes.addAll(findClasses(directory, packageName));
        }

        return classes;
    }

    public <T> Set<Class<? extends T>> findAllSubclassesOf(Class<T> parentClass) {

        try {
            String packageName = parentClass.getPackage().getName();

            return getClasses(packageName).parallelStream()
                    .filter(clazz -> parentClass.isAssignableFrom(clazz)
                            && !parentClass.equals(clazz))
                    .map(clazz -> (Class<? extends T>) clazz)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.severe(LogUtil.getExceptionMsg(e, "findAllSubclassesOf"));
            return Set.of();
        }
    }

}
