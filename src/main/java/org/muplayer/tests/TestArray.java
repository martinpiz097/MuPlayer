package org.muplayer.tests;

import java.util.Arrays;

public class TestArray {

    public static void main(String[] args) {
        final int lenght = 100000000;
        final String[] array = getArray(lenght);
        System.out.println("Time test with "+lenght+" elements");
        printMsg("Sequential time: ", forArraySequential(array));
        printMsg("Parallel time: ", forArrayParallel(array));
    }

    private static long getCurrentTime() {return System.currentTimeMillis();}

    private static void printMsg(String msg, long time) {
        System.out.println(msg.concat(String.valueOf(time)));
    }

    private static String[] getArray(int lenght) {
        final String[] array = new String[lenght];

        for (int i = 0; i < lenght; i++)
            array[i] = "Element "+i;

        return array;
    }

    private static long forArraySequential(String[] array) {
        final long ti = getCurrentTime();
        for (int i = 0; i < array.length; i++) {}
        final long tf = getCurrentTime();
        return tf-ti;
    }

    private static long forArrayParallel(String[] array) {
        if (!Arrays.stream(array).parallel().isParallel())
            throw new RuntimeException("Error! Stream is not parallel");
        final long ti = getCurrentTime();
        Arrays.stream(array).parallel().forEach(element->{});
        final long tf = getCurrentTime();
        return tf-ti;
    }
}