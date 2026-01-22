package cl.estencia.labs.muplayer.core.util;

import cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IOUtil {

    private IOUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static final int DEFAULT_BUFFER_SIZE = 1024 * 64;

    public static boolean isSystemBigEndian() {
        return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    }

    public static boolean isEqualsBuffers(byte[] b1, byte[] b2) {
        if (b1 == b2) {
            return true;
        }
        if (b1 == null || b2 == null) {
            return false;
        }
        if (b1.length != b2.length) {
            return false;
        }
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        int read;

        while ((read = inputStream.read()) != -1) {
            baos.write(read);
        }

        return baos.toByteArray();
    }

    public static String getAsciiStringFromStream(InputStream inputStream) throws IOException {
        final byte[] buffer = getBytesFromStream(inputStream);
        return new String(buffer, StandardCharsets.UTF_8);
    }

    public static InputStream getFileStream(File file) throws IOException {
        return Files.newInputStream(file.toPath(), StandardOpenOption.READ);
    }

    public static BufferedReader getFileBufferedReader(File file) throws IOException {
        return Files.newBufferedReader(file.toPath(), Charset.defaultCharset());
    }

    public static byte[] getResourceBytes(String resourcePath) throws IOException {
        return getBytesFromStream(getResourceAsStream(resourcePath));
    }

    public static BufferedInputStream getResourceAsBuffer(String path) {
        return new BufferedInputStream(getResourceAsStream(path), DEFAULT_BUFFER_SIZE);
    }

    public static InputStream getResourceAsStream(String path) {
        return IOUtil.class.getResourceAsStream(path);
    }

    public static InputStream getResourceAsArrayStream(String path) throws IOException {
        return new ByteArrayInputStream(getResourceBytes(path));
    }

    public static String extractLinesWithFilter(String str, String filter, boolean caseSensitive) {
        try {
            if (str == null) {
                return "";
            }

            BufferedReader reader = new BufferedReader(new StringReader(str));
            Predicate<? super String> filterFunction = caseSensitive
                    ? line -> line.contains(filter)
                    : line -> line.toLowerCase().contains(filter.toLowerCase());

            String filteredContent = reader.lines()
                    .filter(filterFunction)
                    .collect(Collectors.joining(ConsoleSymbols.LINE_BREAK));

            reader.close();
            return filteredContent;
        } catch (Exception e) {
            return "";
        }
    }

    public static char[] createCharArrayFromInts(int... intArray) {
        if (intArray == null || intArray.length == 0) {
            return new char[]{};
        }

        int length = intArray.length;
        char[] charArray = new char[length];
        for (int i = 0; i < length; i++) {
            charArray[i] = (char) intArray[i];
        }

        return charArray;
    }

    public static int[] createIntArrayFromChars(char... charArray) {
        if (charArray == null || charArray.length == 0) {
            return new int[]{};
        }

        int length = charArray.length;
        int[] intArray = new int[length];
        for (int i = 0; i < length; i++) {
            intArray[i] = charArray[i];
        }

        return intArray;
    }

    public static byte[] createByteArrayFromInts(int... intArray) {
        if (intArray == null || intArray.length == 0) {
            return new byte[]{};
        }

        int length = intArray.length;
        byte[] byteArray = new byte[length];
        for (int i = 0; i < length; i++) {
            byteArray[i] = (byte) intArray[i];
        }

        return byteArray;
    }

    public static int[] createIntArrayFromBytes(byte... byteArray) {
        if (byteArray == null || byteArray.length == 0) {
            return new int[]{};
        }

        int length = byteArray.length;
        int[] intArray = new int[length];
        for (int i = 0; i < length; i++) {
            intArray[i] = byteArray[i];
        }

        return intArray;
    }

}
