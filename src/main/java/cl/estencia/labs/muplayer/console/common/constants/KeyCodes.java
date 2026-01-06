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

    // ANSI escape sequences (multi-byte)
    // Estas teclas envían: ESC [ <código>
    // Arrow keys: ESC [ A/B/C/D
    public static final int SEQ_UP = 'A';         // Después de ESC [
    public static final int SEQ_DOWN = 'B';
    public static final int SEQ_RIGHT = 'C';
    public static final int SEQ_LEFT = 'D';
    public static final int SEQ_HOME = 'H';
    public static final int SEQ_END = 'F';

    // Function keys: ESC O P/Q/R/S (F1-F4) o ESC [ 15~ (F5+)
    public static final int SEQ_F1 = 'P';         // Después de ESC O
    public static final int SEQ_F2 = 'Q';
    public static final int SEQ_F3 = 'R';
    public static final int SEQ_F4 = 'S';

    // Extended sequences: ESC [ <num> ~
    public static final int EXT_INSERT = 2;
    public static final int EXT_DELETE = 3;
    public static final int EXT_PAGE_UP = 5;
    public static final int EXT_PAGE_DOWN = 6;
    public static final int EXT_F5 = 15;
    public static final int EXT_F6 = 17;
    public static final int EXT_F7 = 18;
    public static final int EXT_F8 = 19;
    public static final int EXT_F9 = 20;
    public static final int EXT_F10 = 21;
    public static final int EXT_F11 = 23;
    public static final int EXT_F12 = 24;

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

    public static int toDigit(int key) {
        return isDigit(key) ? key - DIGIT_0 : -1;
    }

    public static int toUppercase(int key) {
        return isLowercase(key) ? key - 32 : key;
    }

    public static int toLowercase(int key) {
        return isUppercase(key) ? key + 32 : key;
    }
}