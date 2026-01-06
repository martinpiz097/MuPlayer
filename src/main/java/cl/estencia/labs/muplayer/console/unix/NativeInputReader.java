package cl.estencia.labs.muplayer.console.unix;

import cl.estencia.labs.muplayer.console.unix.event.KeyInputEvent;
import cl.estencia.labs.muplayer.console.unix.event.LineInputEvent;
import cl.estencia.labs.muplayer.console.unix.listener.KeyInputListener;
import cl.estencia.labs.muplayer.console.unix.listener.LineInputListener;
import cl.estencia.labs.muplayer.console.unix.listener.NativeInputListener;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.Getter;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.*;
import static cl.estencia.labs.muplayer.console.unix.InputMode.SINGLE_CHAR;
import static cl.estencia.labs.muplayer.console.unix.InputMode.WORDS;
import static cl.estencia.labs.muplayer.console.util.SystemCommandExecutor.getTerminalWidth;

public class NativeInputReader {
    @Getter
    private volatile boolean inputBlocked = true;
    private final int unlockKeyCode;
    private final StringBuilder sbInput;
    private final List<KeyInputListener> keyInputListeners;
    private final List<LineInputListener> lineInputListeners;
    private final InputModeConfig inputModeConfig;

    public NativeInputReader() {
        this(ESC);
    }

    public NativeInputReader(int unlockKeyCode) {
        this(unlockKeyCode, WORDS);
    }

    public NativeInputReader(int unlockKeyCode, InputMode inputMode) {
        this.unlockKeyCode = unlockKeyCode;
        this.inputModeConfig = new InputModeConfig(inputMode);
        this.sbInput = new StringBuilder();
        this.keyInputListeners = CollectionUtil.newFastArrayList();
        this.lineInputListeners = CollectionUtil.newFastArrayList();
    }

    private void sendInputEvent(char key) {
        sendInputEvent(new KeyInputEvent(key));
    }

    private void sendInputEvent(String line) {
        sendInputEvent(new LineInputEvent(line));
    }

    private void sendInputEvent(KeyInputEvent event) {
        if (keyInputListeners.isEmpty()) {
            return;
        }

        keyInputListeners.parallelStream()
                .forEach(inputListener -> inputListener.onInput(event));
    }

    private void sendInputEvent(LineInputEvent event) {
        if (lineInputListeners.isEmpty()) {
            return;
        }

        lineInputListeners.parallelStream()
                .forEach(inputListener -> inputListener.onInput(event));
    }

    // un relleno por cada linea del input
    private int calculateFilled() {
        String input = sbInput.toString();
        int terminalWidth = getTerminalWidth();

        if (input.contains("\n")) {
            String[] split = input.split("\n");
            return split.length * terminalWidth;
        } else {
            return terminalWidth;
        }
    }

    private void printKey(int key) {
        if (key == DELETE) {
            IO.print("\r" + (" ".repeat(getTerminalWidth())) + '\r' + sbInput.toString());
        } else {
            IO.print((char) key);
        }
    }

    private void handleInput(int key) {
        InputMode inputMode = inputModeConfig.getInputMode();

        switch (key) {
            case '\n' -> {
                switch (inputMode) {
                    case SINGLE_CHAR -> sendInputEvent(new KeyInputEvent(key));
                    case WORDS -> {
                        String line = sbInput.toString();
                        Thread.ofVirtual().start(() -> sendInputEvent(line));
                        sbInput.delete(0, sbInput.length());
                    }
                }
            }
            case DELETE -> {
                switch (inputMode) {
                    case SINGLE_CHAR -> sendInputEvent(new KeyInputEvent(key));
                    case WORDS -> {
                        if (!sbInput.isEmpty()) {
                            sbInput.deleteCharAt(sbInput.length() - 1);
                        }
                    }
                }
            }
            default -> {
                switch (inputMode) {
                    case SINGLE_CHAR -> sendInputEvent(new KeyInputEvent(key));
                    case WORDS -> sbInput.append((char)key);
                }
            }
        }

        if (inputMode == WORDS) {
            printKey(key);
        }

    }

    // es para restaurar terminal cuando el programa termina (por sea caso)
    private void restoreTerminal() {
        try {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "stty sane < /dev/tty"}).waitFor();
        } catch (Exception ignored) {}
    }

    public <L extends NativeInputListener> void addInputListener(L inputListener) {
        if (inputListener instanceof KeyInputListener) {
            keyInputListeners.add((KeyInputListener) inputListener);
        } else if (inputListener instanceof LineInputListener) {
            lineInputListeners.add((LineInputListener) inputListener);
        }
    }

    public <L extends NativeInputListener> void removeInputListener(L inputListener) {
        if (inputListener instanceof KeyInputListener) {
            keyInputListeners.remove((KeyInputListener) inputListener);
        } else if (inputListener instanceof LineInputListener) {
            lineInputListeners.remove((LineInputListener) inputListener);
        }
    }

    public void clearAllListeners() {
        keyInputListeners.clear();;
    }

    public void start() throws Exception {
        Runtime.getRuntime().exec(new String[]{"sh", "-c", "stty -echo -icanon < /dev/tty"}).waitFor();
        Runtime.getRuntime().addShutdownHook(new Thread(this::restoreTerminal));

        new Thread(() -> {
            try (var reader = new FileInputStream(FileDescriptor.in)) {
                while (true) {
                    int key = reader.read();

                    if (key == unlockKeyCode) {
                        inputBlocked = !inputBlocked;
                    } else if (!inputBlocked) {
                        if (key == inputModeConfig.getToggleModeKey()) {
                            inputModeConfig.toggleInputMode();
                            System.out.println("Changed to input mode: " + inputModeConfig.getInputMode());
                        } else {
                            handleInput(key);
                        }
                    }

                }
            } catch (IOException ignored) {}
        }).start();
    }

}