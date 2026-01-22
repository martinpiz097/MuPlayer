package cl.estencia.labs.muplayer.console.common.constants;

public final class KeyCodes {

    private KeyCodes() {}

    // Control characters (0-31)
    public static final int CTRL_AT = 0;          // Ctrl+@, NUL
    public static final int CTRL_A = 1;           // Start of heading
    public static final int CTRL_B = 2;           // Start of text
    public static final int CTRL_C = 3;           // End of text (interrupt)
    public static final int CTRL_D = 4;           // End of transmission (EOF)
    public static final int CTRL_E = 5;           // Enquiry
    public static final int CTRL_F = 6;           // Acknowledge
    public static final int CTRL_G = 7;           // Bell
    public static final int BACKSPACE = 8;        // Ctrl+H
    public static final int TAB = 9;              // Ctrl+I
    public static final int LINE_FEED = 10;       // Ctrl+J, newline
    public static final int CTRL_K = 11;          // Vertical tab
    public static final int CTRL_L = 12;          // Form feed (clear screen)
    public static final int ENTER = 13;           // Ctrl+M, carriage return
    public static final int CTRL_N = 14;          // Shift out
    public static final int CTRL_O = 15;          // Shift in
    public static final int CTRL_P = 16;          // Data link escape
    public static final int CTRL_Q = 17;          // XON
    public static final int CTRL_R = 18;          // Device control 2
    public static final int CTRL_S = 19;          // XOFF
    public static final int CTRL_T = 20;          // Device control 4
    public static final int CTRL_U = 21;          // Negative acknowledge
    public static final int CTRL_V = 22;          // Synchronous idle
    public static final int CTRL_W = 23;          // End of transmission block
    public static final int CTRL_X = 24;          // Cancel
    public static final int CTRL_Y = 25;          // End of medium
    public static final int CTRL_Z = 26;          // Substitute (suspend)
    public static final int ESC = 27;             // Escape
    public static final int CTRL_BACKSLASH = 28;  // File separator
    public static final int CTRL_BRACKET_RIGHT = 29; // Group separator
    public static final int CTRL_CARET = 30;      // Record separator
    public static final int CTRL_UNDERSCORE = 31; // Unit separator

    // Printable characters (32-126)
    public static final int SPACE = 32;
    public static final int EXCLAMATION = 33;     // !
    public static final int DOUBLE_QUOTE = 34;    // "
    public static final int HASH = 35;            // #
    public static final int DOLLAR = 36;          // $
    public static final int PERCENT = 37;         // %
    public static final int AMPERSAND = 38;       // &
    public static final int SINGLE_QUOTE = 39;    // '
    public static final int PAREN_LEFT = 40;      // (
    public static final int PAREN_RIGHT = 41;     // )
    public static final int ASTERISK = 42;        // *
    public static final int PLUS = 43;            // +
    public static final int COMMA = 44;           // ,
    public static final int MINUS = 45;           // -
    public static final int PERIOD = 46;          // .
    public static final int SLASH = 47;           // /

    // Digits
    public static final int DIGIT_0 = 48;
    public static final int DIGIT_1 = 49;
    public static final int DIGIT_2 = 50;
    public static final int DIGIT_3 = 51;
    public static final int DIGIT_4 = 52;
    public static final int DIGIT_5 = 53;
    public static final int DIGIT_6 = 54;
    public static final int DIGIT_7 = 55;
    public static final int DIGIT_8 = 56;
    public static final int DIGIT_9 = 57;
    public static final int[] DIGITS = new int[]{
            DIGIT_0, DIGIT_1, DIGIT_2, DIGIT_3, DIGIT_4,
            DIGIT_5, DIGIT_6, DIGIT_7, DIGIT_8, DIGIT_9
    };

    public static final int COLON = 58;           // :
    public static final int SEMICOLON = 59;       // ;
    public static final int LESS_THAN = 60;       // <
    public static final int EQUALS = 61;          // =
    public static final int GREATER_THAN = 62;    // >
    public static final int QUESTION = 63;        // ?
    public static final int AT = 64;              // @

    // Uppercase letters
    public static final int A = 65;
    public static final int B = 66;
    public static final int C = 67;
    public static final int D = 68;
    public static final int E = 69;
    public static final int F = 70;
    public static final int G = 71;
    public static final int H = 72;
    public static final int I = 73;
    public static final int J = 74;
    public static final int K = 75;
    public static final int L = 76;
    public static final int M = 77;
    public static final int N = 78;
    public static final int O = 79;
    public static final int P = 80;
    public static final int Q = 81;
    public static final int R = 82;
    public static final int S = 83;
    public static final int T = 84;
    public static final int U = 85;
    public static final int V = 86;
    public static final int W = 87;
    public static final int X = 88;
    public static final int Y = 89;
    public static final int Z = 90;

    public static final int BRACKET_LEFT = 91;    // [
    public static final int BACKSLASH = 92;       // \
    public static final int BRACKET_RIGHT = 93;   // ]
    public static final int CARET = 94;           // ^
    public static final int UNDERSCORE = 95;      // _
    public static final int BACKTICK = 96;        // `

    // Lowercase letters
    public static final int a = 97;
    public static final int b = 98;
    public static final int c = 99;
    public static final int d = 100;
    public static final int e = 101;
    public static final int f = 102;
    public static final int g = 103;
    public static final int h = 104;
    public static final int i = 105;
    public static final int j = 106;
    public static final int k = 107;
    public static final int l = 108;
    public static final int m = 109;
    public static final int n = 110;
    public static final int o = 111;
    public static final int p = 112;
    public static final int q = 113;
    public static final int r = 114;
    public static final int s = 115;
    public static final int t = 116;
    public static final int u = 117;
    public static final int v = 118;
    public static final int w = 119;
    public static final int x = 120;
    public static final int y = 121;
    public static final int z = 122;

    public static final int BRACE_LEFT = 123;     // {
    public static final int PIPE = 124;           // |
    public static final int BRACE_RIGHT = 125;    // }
    public static final int TILDE = 126;          // ~
    public static final int DELETE = 127;         // DEL

    // ANSI escape sequences - Arrow keys and navigation
    public static final int SEQ_UP = 1001;
    public static final int SEQ_DOWN = 1002;
    public static final int SEQ_RIGHT = 1003;
    public static final int SEQ_LEFT = 1004;
    public static final int SEQ_HOME = 1005;
    public static final int SEQ_END = 1006;

    // Function keys F1-F4 (ESC O P/Q/R/S)
    public static final int SEQ_F1 = 1011;
    public static final int SEQ_F2 = 1012;
    public static final int SEQ_F3 = 1013;
    public static final int SEQ_F4 = 1014;

    // Extended sequences (ESC [ <num> ~)
    public static final int EXT_INSERT = 1020;
    public static final int EXT_DELETE = 1021;
    public static final int EXT_PAGE_UP = 1022;
    public static final int EXT_PAGE_DOWN = 1023;
    public static final int EXT_F5 = 1015;
    public static final int EXT_F6 = 1016;
    public static final int EXT_F7 = 1017;
    public static final int EXT_F8 = 1018;
    public static final int EXT_F9 = 1019;
    public static final int EXT_F10 = 1024;
    public static final int EXT_F11 = 1025;
    public static final int EXT_F12 = 1026;

    // ALT + Lowercase letters
    public static final int[] ALT_a = new int[]{ESC, a};
    public static final int[] ALT_b = new int[]{ESC, b};
    public static final int[] ALT_c = new int[]{ESC, c};
    public static final int[] ALT_d = new int[]{ESC, d};
    public static final int[] ALT_e = new int[]{ESC, e};
    public static final int[] ALT_f = new int[]{ESC, f};
    public static final int[] ALT_g = new int[]{ESC, g};
    public static final int[] ALT_h = new int[]{ESC, h};
    public static final int[] ALT_i = new int[]{ESC, i};
    public static final int[] ALT_j = new int[]{ESC, j};
    public static final int[] ALT_k = new int[]{ESC, k};
    public static final int[] ALT_l = new int[]{ESC, l};
    public static final int[] ALT_m = new int[]{ESC, m};
    public static final int[] ALT_n = new int[]{ESC, n};
    public static final int[] ALT_o = new int[]{ESC, o};
    public static final int[] ALT_p = new int[]{ESC, p};
    public static final int[] ALT_q = new int[]{ESC, q};
    public static final int[] ALT_r = new int[]{ESC, r};
    public static final int[] ALT_s = new int[]{ESC, s};
    public static final int[] ALT_t = new int[]{ESC, t};
    public static final int[] ALT_u = new int[]{ESC, u};
    public static final int[] ALT_v = new int[]{ESC, v};
    public static final int[] ALT_w = new int[]{ESC, w};
    public static final int[] ALT_x = new int[]{ESC, x};
    public static final int[] ALT_y = new int[]{ESC, y};
    public static final int[] ALT_z = new int[]{ESC, z};

    // ALT + Uppercase letters
    public static final int[] ALT_A = new int[]{ESC, A};
    public static final int[] ALT_B = new int[]{ESC, B};
    public static final int[] ALT_C = new int[]{ESC, C};
    public static final int[] ALT_D = new int[]{ESC, D};
    public static final int[] ALT_E = new int[]{ESC, E};
    public static final int[] ALT_F = new int[]{ESC, F};
    public static final int[] ALT_G = new int[]{ESC, G};
    public static final int[] ALT_H = new int[]{ESC, H};
    public static final int[] ALT_I = new int[]{ESC, I};
    public static final int[] ALT_J = new int[]{ESC, J};
    public static final int[] ALT_K = new int[]{ESC, K};
    public static final int[] ALT_L = new int[]{ESC, L};
    public static final int[] ALT_M = new int[]{ESC, M};
    public static final int[] ALT_N = new int[]{ESC, N};
    public static final int[] ALT_O = new int[]{ESC, O};
    public static final int[] ALT_P = new int[]{ESC, P};
    public static final int[] ALT_Q = new int[]{ESC, Q};
    public static final int[] ALT_R = new int[]{ESC, R};
    public static final int[] ALT_S = new int[]{ESC, S};
    public static final int[] ALT_T = new int[]{ESC, T};
    public static final int[] ALT_U = new int[]{ESC, U};
    public static final int[] ALT_V = new int[]{ESC, V};
    public static final int[] ALT_W = new int[]{ESC, W};
    public static final int[] ALT_X = new int[]{ESC, X};
    public static final int[] ALT_Y = new int[]{ESC, Y};
    public static final int[] ALT_Z = new int[]{ESC, Z};

    // ALT + Digits
    public static final int[] ALT_0 = new int[]{ESC, DIGIT_0};
    public static final int[] ALT_1 = new int[]{ESC, DIGIT_1};
    public static final int[] ALT_2 = new int[]{ESC, DIGIT_2};
    public static final int[] ALT_3 = new int[]{ESC, DIGIT_3};
    public static final int[] ALT_4 = new int[]{ESC, DIGIT_4};
    public static final int[] ALT_5 = new int[]{ESC, DIGIT_5};
    public static final int[] ALT_6 = new int[]{ESC, DIGIT_6};
    public static final int[] ALT_7 = new int[]{ESC, DIGIT_7};
    public static final int[] ALT_8 = new int[]{ESC, DIGIT_8};
    public static final int[] ALT_9 = new int[]{ESC, DIGIT_9};

    // Utility methods
    public static boolean isDigit(int key) {
        return key >= DIGIT_0 && key <= DIGIT_9;
    }

    public static boolean isUppercase(int key) {
        return key >= A && key <= Z;
    }

    public static boolean isLowercase(int key) {
        return key >= a && key <= z;
    }

    public static boolean isLetter(int key) {
        return isUppercase(key) || isLowercase(key);
    }

    public static boolean isAlphanumeric(int key) {
        return isLetter(key) || isDigit(key);
    }

    public static boolean isPrintable(int key) {
        return key >= SPACE && key <= TILDE;
    }

    public static boolean isControl(int key) {
        return key >= 0 && key <= 31;
    }

    public static boolean isSequence(int key) {
        return key >= 1001 && key <= 1026;
    }

    public static boolean isArrowKey(int key) {
        return key >= SEQ_UP && key <= SEQ_LEFT;
    }

    public static boolean isFunctionKey(int key) {
        return (key >= SEQ_F1 && key <= SEQ_F4) || (key >= EXT_F5 && key <= EXT_F9) || (key >= EXT_F10 && key <= EXT_F12);
    }

    public static int toDigit(int key) {
        return isDigit(key) ? key - DIGIT_0 : -1;
    }

    public static int toUppercase(int key) {
        return isLowercase(key) ? key - 32 : key;
    }

    public static int toLowercase(int key) {
        return isUppercase(key) ? key + 32 : key;
    }

    /**
     * Parses a 2-byte ANSI escape sequence and returns the corresponding key code.
     * Expected format: ESC [ <code> or ESC O <code>
     *
     * @param sequence byte array of exactly 3 elements
     * @return the parsed key code, or -1 if invalid sequence
     */
    public static int parseAltSequence(byte[] sequence) {
        if (sequence == null || sequence.length < 2) {
            return -1;
        }

        if (sequence[0] != ESC) {
            return -1;
        }

        return sequence[1];
    }

    /**
     * Parses a 3-byte ANSI escape sequence and returns the corresponding key code.
     * Expected format: ESC [ <code> or ESC O <code>
     *
     * @param sequence byte array of exactly 3 elements
     * @return the parsed key code, or -1 if invalid sequence
     */
    public static int parseThreeKeysSequence(byte[] sequence) {
        if (sequence == null || sequence.length != 3) {
            return -1;
        }

        if (sequence[0] != ESC) {
            return -1;
        }

        int modifier = sequence[1];
        int code = sequence[2];

        // ESC [ <code> - Arrow keys, Home, End
        if (modifier == BRACKET_LEFT) {
            return switch (code) {
                case 'A' -> SEQ_UP;
                case 'B' -> SEQ_DOWN;
                case 'C' -> SEQ_RIGHT;
                case 'D' -> SEQ_LEFT;
                case 'H' -> SEQ_HOME;
                case 'F' -> SEQ_END;
                default -> -1;
            };
        }

        // ESC O <code> - F1-F4
        if (modifier == 'O') {
            return switch (code) {
                case 'P' -> SEQ_F1;
                case 'Q' -> SEQ_F2;
                case 'R' -> SEQ_F3;
                case 'S' -> SEQ_F4;
                default -> -1;
            };
        }

        return -1;
    }

    /**
     * Parses extended ANSI sequences (4+ bytes): ESC [ <num> ~
     *
     * @param sequence byte array representing the sequence
     * @return the parsed extended key code, or -1 if invalid
     */
    public static int parseExtendedSequence(byte[] sequence) {
        if (sequence == null || sequence.length < 4) {
            return -1;
        }

        if (sequence[0] != ESC || sequence[1] != BRACKET_LEFT) {
            return -1;
        }

        // Extraer número entre '[' y '~'
        int num = 0;
        for (int i = 2; i < sequence.length - 1; i++) {
            if (!isDigit(sequence[i])) {
                return -1;
            }
            num = num * 10 + (sequence[i] - DIGIT_0);
        }

        if (sequence[sequence.length - 1] != '~') {
            return -1;
        }

        return switch (num) {
            case 2 -> EXT_INSERT;
            case 3 -> EXT_DELETE;
            case 5 -> EXT_PAGE_UP;
            case 6 -> EXT_PAGE_DOWN;
            case 15 -> EXT_F5;
            case 17 -> EXT_F6;
            case 18 -> EXT_F7;
            case 19 -> EXT_F8;
            case 20 -> EXT_F9;
            case 21 -> EXT_F10;
            case 23 -> EXT_F11;
            case 24 -> EXT_F12;
            default -> -1;
        };
    }

    public static int parseSequence(byte[] sequence) {
        final int keysCount = sequence.length;
        return switch (keysCount) {
            case 1 -> sequence[0];
            case 2 -> parseAltSequence(sequence);
            case 3 -> parseThreeKeysSequence(sequence);
            // para 4 o mas
            default -> parseExtendedSequence(sequence);
        };
    }

    /**
     * Returns the name of the key for debugging purposes.
     *
     * @param key the key code
     * @return the name of the key
     */
    public static String getKeyName(int key) {
        return switch (key) {
            case CTRL_C -> "CTRL+C";
            case CTRL_D -> "CTRL+D";
            case CTRL_Z -> "CTRL+Z";
            case BACKSPACE -> "BACKSPACE";
            case TAB -> "TAB";
            case ENTER -> "ENTER";
            case ESC -> "ESC";
            case SPACE -> "SPACE";
            case DELETE -> "DELETE";
            case SEQ_UP -> "UP";
            case SEQ_DOWN -> "DOWN";
            case SEQ_RIGHT -> "RIGHT";
            case SEQ_LEFT -> "LEFT";
            case SEQ_HOME -> "HOME";
            case SEQ_END -> "END";
            case SEQ_F1 -> "F1";
            case SEQ_F2 -> "F2";
            case SEQ_F3 -> "F3";
            case SEQ_F4 -> "F4";
            case EXT_F5 -> "F5";
            case EXT_F6 -> "F6";
            case EXT_F7 -> "F7";
            case EXT_F8 -> "F8";
            case EXT_F9 -> "F9";
            case EXT_F10 -> "F10";
            case EXT_F11 -> "F11";
            case EXT_F12 -> "F12";
            case EXT_INSERT -> "INSERT";
            case EXT_DELETE -> "DEL";
            case EXT_PAGE_UP -> "PAGE_UP";
            case EXT_PAGE_DOWN -> "PAGE_DOWN";
            default -> isPrintable(key) ? String.valueOf((char) key) : "KEY(" + key + ")";
        };
    }

    public static char toChar(int key) {
        return (char) key;
    }

    public static char[] toAsciiChars(int number) {
        return String.valueOf(number).toCharArray();
    }

    public static byte[] toAsciiCodes(int number) {
        char[] charArray = toAsciiChars(number);
        int length = charArray.length;
        byte[] charBytes = new byte[length];

        for (int i = 0; i < length; i++) {
            charBytes[i] = (byte) charArray[i];
        }

        return charBytes;
    }

}