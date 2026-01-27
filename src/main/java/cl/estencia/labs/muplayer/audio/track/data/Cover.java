package cl.estencia.labs.muplayer.audio.track.data;

import cl.estencia.labs.muplayer.audio.common.enums.ImageFormat;
import cl.estencia.labs.muplayer.console.common.constants.ConsoleMessages;
import cl.estencia.labs.muplayer.console.model.ConsoleImage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@Slf4j
public record Cover(byte[] coverData, ImageFormat imageFormat) {
    public Cover(byte[] coverData, ImageFormat imageFormat) {
        this.coverData = coverData != null ? coverData : new byte[0];
        this.imageFormat = imageFormat;
    }

    public boolean hasData() {
        return coverData.length > 0;
    }

    @SneakyThrows
    public File saveInLocalStorage(File parent, String name) {
        if (!hasData()) {
            return null;
        }

        if (parent == null) {
            log.error("cover parent folder is null!" + parent);
            return null;
        }
        if (!parent.exists()) {
            log.error("cover parent folder not exists: " + parent);
            return null;
        }

        if (name == null || name.isBlank()) {
            name = String.valueOf(System.currentTimeMillis());
        }

        if (name.contains("\\.")) {
            String[] nameSplit = name.split("\\.");
            name = nameSplit.length <= 2
                    ? nameSplit[0].trim()
                    : Arrays.stream(nameSplit, 0, nameSplit.length - 1)
                    .collect(Collectors.joining("."));
            if (name.endsWith(".")) {
                name = name.substring(0, name.length() - 1);
            }
        }

        if (imageFormat != null) {
            name = name + "." + imageFormat.name();
        }

        File coverFile = new File(parent, name);
        if (!coverFile.exists()) {
            coverFile.createNewFile();
        }

        Files.write(coverFile.toPath(), coverData, TRUNCATE_EXISTING);
        return coverFile;
    }

    public File saveInLocalStorage(String parent, String name) {
        if (parent == null) {
            log.error("cover parent path is null!" + parent);
            return null;
        }

        return saveInLocalStorage(new File(parent), name);
    }

    @Override
    public String toString() {
        if (hasData()) {
            final InputStream coverStream = new ByteArrayInputStream(coverData);
            final ConsoleImage consoleImage = new ConsoleImage(coverStream);

            return consoleImage.drawString();
        } else {
            return ConsoleMessages.NO_COVER_MESSAGE;
        }
    }

}
