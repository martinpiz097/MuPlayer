package cl.estencia.labs.muplayer.console.unix;

import cl.estencia.labs.muplayer.console.unix.event.KeyInputEvent;
import cl.estencia.labs.muplayer.console.unix.event.LineInputEvent;
import cl.estencia.labs.muplayer.console.unix.listener.KeyInputListener;
import cl.estencia.labs.muplayer.console.unix.listener.LineInputListener;
import cl.estencia.labs.muplayer.console.unix.listener.NativeInputListener;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.*;
import static cl.estencia.labs.muplayer.console.unix.InputMode.COMMANDS;
import static cl.estencia.labs.muplayer.console.util.SystemCommandExecutor.getTerminalWidth;

@Getter
public class NativeInputReader extends Thread {
    private volatile boolean inputBlocked;
    private final int unlockKeyCode;
    private final StringBuilder sbInput;

    private final List<KeyInputListener> keyInterceptors;
    private final List<KeyInputListener> keyInputListeners;
    private final List<LineInputListener> lineInputListeners;

    private final InputModeConfig inputModeConfig;
    private final ConsoleHistory consoleHistory;
    private final AtomicReference<String> lineRef;

    public NativeInputReader() {
        this(ESC);
    }

    public NativeInputReader(int unlockKeyCode) {
        this(unlockKeyCode, COMMANDS);
    }

    public NativeInputReader(int unlockKeyCode, InputMode inputMode) {
        this.inputBlocked = true;
        this.unlockKeyCode = unlockKeyCode;
        this.inputModeConfig = new InputModeConfig(inputMode, EXT_F5);
        this.sbInput = new StringBuilder();
        this.keyInterceptors = CollectionUtil.newFastArrayList();
        this.keyInputListeners = CollectionUtil.newFastArrayList();
        this.lineInputListeners = CollectionUtil.newFastArrayList();
        this.consoleHistory = new ConsoleHistory();
        this.lineRef = new AtomicReference<>();

        loadDefaultKeyInterceptors();
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

    private String cartReturn() {
        return '\r' + (" ".repeat(getTerminalWidth())) + '\r';
    }

    private void printKey(int key) {
        if (key == DELETE) {
            IO.print(cartReturn() + sbInput.toString());
        } else {
            IO.print((char) key);
        }
    }

    private void handleInput(int key, byte[] sequence) {
        InputMode inputMode = inputModeConfig.getInputMode();

        switch (key) {
            case '\n' -> {
                switch (inputMode) {
                    case SINGLE_SHORCUTS -> sendInputEvent(new KeyInputEvent(key, sequence));
                    case COMMANDS -> {
                        String line = sbInput.toString();

                        synchronized (lineRef) {
                            this.lineRef.set(line);
                        }
                        consoleHistory.addCommand(line);

                        Thread.ofVirtual().start(() -> sendInputEvent(
                                new LineInputEvent(line)));
                        sbInput.delete(0, sbInput.length());
                    }
                }
            }
            case DELETE -> {
                switch (inputMode) {
                    case SINGLE_SHORCUTS -> sendInputEvent(new KeyInputEvent(key, sequence));
                    case COMMANDS -> {
                        if (!sbInput.isEmpty()) {
                            sbInput.deleteCharAt(sbInput.length() - 1);
                        }
                    }
                }
            }
            default -> {
                switch (inputMode) {
                    case SINGLE_SHORCUTS -> sendInputEvent(new KeyInputEvent(key, sequence));
                    case COMMANDS -> sbInput.append((char) key);
                }
            }
        }

        if (inputMode == COMMANDS) {
            printKey(key);
        }

    }

    private void handleSequenceInput(int key, byte[] sequence) {
        InputMode inputMode = inputModeConfig.getInputMode();
        switch (key) {
            case '\n' -> {
                switch (inputMode) {
                    case SINGLE_SHORCUTS -> sendInputEvent(new KeyInputEvent(key, sequence));
                    case COMMANDS -> {
                        String line = sbInput.toString();

                        synchronized (lineRef) {
                            this.lineRef.set(line);
                        }
                        consoleHistory.addCommand(line);

                        Thread.ofVirtual().start(() -> sendInputEvent(
                                new LineInputEvent(line)));
                        sbInput.delete(0, sbInput.length());
                    }
                }
            }
            case DELETE -> {
                switch (inputMode) {
                    case SINGLE_SHORCUTS -> sendInputEvent(new KeyInputEvent(key, sequence));
                    case COMMANDS -> {
                        if (!sbInput.isEmpty()) {
                            sbInput.deleteCharAt(sbInput.length() - 1);
                        }
                    }
                }
            }
            default -> {
                consoleHistory.addCommand(String.valueOf((char) key));

                switch (inputMode) {
                    case SINGLE_SHORCUTS -> sendInputEvent(new KeyInputEvent(key, sequence));
                    case COMMANDS -> sbInput.append((char) key);
                }
            }
        }

        if (inputMode == COMMANDS) {
            printKey(key);
        }

    }

    // es para restaurar terminal cuando el programa termina (por sea caso)
    private void restoreTerminal() {
        try {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "stty sane < /dev/tty"}).waitFor();
        } catch (Exception ignored) {}
    }

    private void loadDefaultKeyInterceptors() {
        addKeyInterceptor(new KeyInputListener(unlockKeyCode) {
            @Override
            public void onInput(KeyInputEvent event) {
                setInputBlocked(!inputBlocked);
            }
        });

        addKeyInterceptor(new KeyInputListener(inputModeConfig.getToggleModeKey()) {
            @Override
            public void onInput(KeyInputEvent event) {
                inputModeConfig.toggleInputMode();
            }
        });

        addKeyInterceptor(new KeyInputListener(SEQ_UP) {
            @Override
            public void onInput(KeyInputEvent event) {
                System.out.print(cartReturn() + consoleHistory.getPrevCommand());
            }
        });

        addKeyInterceptor(new KeyInputListener(SEQ_DOWN) {
            @Override
            public void onInput(KeyInputEvent event) {
                System.out.print(cartReturn() + consoleHistory.getNextCommand());
            }
        });
    }

    public synchronized void setInputBlocked(boolean inputBlocked) {
        this.inputBlocked = inputBlocked;
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

    public void addKeyInterceptor(KeyInputListener keyListener) {
        keyInterceptors.add(keyListener);
    }

    public List<KeyInputListener> getKeyInterceptors(int key) {
        return keyInterceptors.stream()
                .filter(interceptor -> interceptor.isKey(key))
                .toList();
    }

    public void removeKeyInterceptor(KeyInputListener keyListener) {
        keyInterceptors.remove(keyListener);
    }

    public void clearAllKeyInterceptors() {
        keyInterceptors.clear();
        ;
    }

    public String getLine() {
        while (lineRef.get() == null) {

        }

        String auxLine;
        synchronized (lineRef) {
            auxLine = lineRef.get();
            lineRef.set(null);
        }

        return auxLine;
    }

    @SneakyThrows
    public void run() {
        Runtime.getRuntime().exec(new String[]{"sh", "-c", "stty -echo -icanon < /dev/tty"}).waitFor();
        Runtime.getRuntime().addShutdownHook(new Thread(this::restoreTerminal));

        try (var reader = new FileInputStream(FileDescriptor.in)) {
            byte[] buffer = new byte[8];
            byte[] sequence;
            int key;
            List<KeyInputListener> interceptors;
            while (!Thread.currentThread().isInterrupted()) {
                int read = reader.read(buffer);
                if (read <= 0) {
                    continue;
                }

                sequence = Arrays.copyOf(buffer, read);
                if (read > 1) {
                    key = read == 3 ? parseSequence(sequence) : parseExtendedSequence(sequence);
                } else {
                    key = sequence[0];
                }

                if (inputBlocked && key != unlockKeyCode) {
                    continue;
                }

                interceptors = getKeyInterceptors(key);
                if (!interceptors.isEmpty()) {
                    int finalKey = key;
                    byte[] finalSequence = sequence;
                    interceptors.forEach(interceptor -> {
                        interceptor.onInput(new KeyInputEvent(finalKey, finalSequence));
                    });
                } else {
                    handleInput(key, sequence);
                }

            }
        } catch (IOException ignored) {}
    }

}