package cl.estencia.labs.muplayer.audio.common.enums;

public enum ImageFormat {
    png("image/png"),
    jpg("image/jpeg"),
    gif("image/gif"),
    bmp("image/bmp"),
    webp("image/webp"),
    tiff("image/tiff"),
    ico("image/x-icon"),
    svg("image/svg+xml"),
    heic("image/heic"),
    heif("image/heif"),
    avif("image/avif");

    private final String mimeType;

    ImageFormat(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static ImageFormat fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return null;
        }

        mimeType = mimeType.trim();
        for (ImageFormat format : values()) {
            if (format.mimeType.equalsIgnoreCase(mimeType)) {
                return format;
            }
        }

        return null;
    }

    public static ImageFormat fromExtension(String extension) {
        String ext = extension.startsWith(".") ? extension.substring(1) : extension;
        try {
            return valueOf(ext.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}