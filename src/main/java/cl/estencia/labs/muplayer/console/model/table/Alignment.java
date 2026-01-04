package cl.estencia.labs.muplayer.console.model.table;

public enum Alignment {
    LEFT, CENTER, RIGHT;

    public static boolean isValidShortName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }

        char firstChar = name.trim().toUpperCase().charAt(0);
        return firstChar == 'L' || firstChar == 'C' || firstChar == 'R';

    }

    public static boolean isValidName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }

        try {
            if (isValidShortName(name)) {
                return true;
            }

            Alignment.fromName(name.trim().toUpperCase());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Alignment fromName(String name) {
        if (!isValidName(name)) {
            return null;
        }

        try {
            return Alignment.valueOf(name.trim().toUpperCase());
        } catch (Exception e) {
            return fromShortName(name);
        }
    }

    public static Alignment fromShortName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        char firstChar = name.trim().toUpperCase().charAt(0);
        return switch (firstChar) {
            case 'L' -> Alignment.LEFT;
            case 'C' -> Alignment.CENTER;
            case 'R' -> Alignment.RIGHT;
            default -> null;
        };
    }

}
