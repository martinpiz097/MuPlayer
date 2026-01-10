package cl.estencia.labs.muplayer.console.unix;

import cl.estencia.labs.muplayer.console.unix.event.KeyInputEvent;
import cl.estencia.labs.muplayer.console.unix.event.LineInputEvent;
import cl.estencia.labs.muplayer.console.unix.listener.KeyInputListener;
import cl.estencia.labs.muplayer.console.unix.listener.KeyInterceptor;
import cl.estencia.labs.muplayer.console.unix.listener.LineInputListener;
import cl.estencia.labs.muplayer.console.unix.listener.NativeInputListener;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.LINE_BREAK_CHAR;
import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.*;
import static cl.estencia.labs.muplayer.console.unix.InputMode.COMMANDS;
import static cl.estencia.labs.muplayer.console.util.SystemCommandExecutor.getTerminalWidth;

@Getter
public class NativeConsole extends Thread {
    private volatile boolean inputBlocked;
    private final int unlockKeyCode;
    private final StringBuilder sbInput;

    private final List<KeyInterceptor> keyInterceptors;
    private final List<KeyInputListener> keyInputListeners;
    private final List<LineInputListener> lineInputListeners;

    private final InputModeConfig inputModeConfig;
    private final ConsoleHistory consoleHistory;

    public NativeConsole() {
        this(ESC);
    }

    public NativeConsole(int unlockKeyCode) {
        this(unlockKeyCode, COMMANDS);
    }

    public NativeConsole(int unlockKeyCode, InputMode inputMode) {
        this.inputBlocked = true;
        this.unlockKeyCode = unlockKeyCode;
        this.inputModeConfig = new InputModeConfig(inputMode, EXT_F5);
        this.sbInput = new StringBuilder();
        this.keyInterceptors = CollectionUtil.newFastArrayList();
        this.keyInputListeners = CollectionUtil.newFastArrayList();
        this.lineInputListeners = CollectionUtil.newFastArrayList();
        this.consoleHistory = new ConsoleHistory();
        setName("native-input-reader");
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

    private void sendKeyToInterceptors(int key, byte[] sequence,
                                       List<KeyInterceptor> interceptors) {
        interceptors.forEach(interceptor ->
                interceptor.onInput(new KeyInputEvent(key, sequence)));
    }

    private void handleShorcut(int key, byte[] sequence) {
        sendInputEvent(new KeyInputEvent(key, sequence));
    }

    private void handleCommand(int key, byte[] sequence) {
        switch (key) {
            case LINE_BREAK_CHAR -> {
                String line = sbInput.toString();
                consoleHistory.addCommand(line);
                sbInput.delete(0, sbInput.length());

                Thread.ofVirtual().start(() -> sendInputEvent(
                        new LineInputEvent(line)));

                printKey(key);
            }
            case DELETE -> {
                if (!sbInput.isEmpty()) {
                    sbInput.deleteCharAt(sbInput.length() - 1);
                    consoleHistory.updateLastCommand(sbInput.toString());
                    printKey(key);
                }
            }
            default -> {
                sbInput.append((char) key);
                consoleHistory.updateLastCommand(sbInput.toString());
                printKey(key);
            }
        }

    }

    private void handleInput(int key, byte[] sequence) {
        InputMode inputMode = inputModeConfig.getInputMode();

        switch (inputMode) {
            case SINGLE_SHORCUTS -> handleShorcut(key, sequence);
            case COMMANDS -> handleCommand(key, sequence);
        }

    }

    // es para restaurar terminal cuando el programa termina (por sea caso)
    private void restoreTerminal() {
        try {
            Runtime.getRuntime().exec(new String[]{"sh", "-c", "stty sane < /dev/tty"}).waitFor();
        } catch (Exception ignored) {}
    }

    public void loadDefaultKeyInterceptors() {
        addKeyInterceptor(new KeyInterceptor(unlockKeyCode) {
            @Override
            public void intercept(KeyInputEvent event) {
                setInputBlocked(!inputBlocked);
            }
        });

        addKeyInterceptor(new KeyInterceptor(inputModeConfig.getToggleModeKey()) {
            @Override
            public void intercept(KeyInputEvent event) {
                inputModeConfig.toggleInputMode();
            }
        });

        addKeyInterceptor(new KeyInterceptor(SEQ_UP) {
            @Override
            public void intercept(KeyInputEvent event) {
                System.out.print(cartReturn() + consoleHistory.getPrevCommand());
            }
        });

        addKeyInterceptor(new KeyInterceptor(SEQ_DOWN) {
            @Override
            public void intercept(KeyInputEvent event) {
                System.out.print(cartReturn() + consoleHistory.getNextCommand());
            }
        });
    }

    public synchronized void setInputBlocked(boolean inputBlocked) {
        this.inputBlocked = inputBlocked;
    }

    public <L extends NativeInputListener> void addInputListeners(L... inputListeners) {
        if (inputListeners == null || inputListeners.length == 0) {
            return;
        }

        int listenersCount = inputListeners.length;
        for (int i = 0; i < listenersCount; i++) {
            addInputListener(inputListeners[i]);
        }
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

    public void clearAllKeyInputListeners() {
        synchronized (keyInputListeners) {
            keyInputListeners.clear();
        }
    }

    public void clearAllLineInputListeners() {
        synchronized (lineInputListeners) {
            lineInputListeners.clear();
        }
    }

    public void addKeyInterceptor(KeyInterceptor keyListener) {
        keyInterceptors.add(keyListener);
    }

    public void addKeyInterceptors(KeyInterceptor... keyInterceptors) {
        if (keyInterceptors == null || keyInterceptors.length == 0) {
            return;
        }

        int interceptorsCount = keyInterceptors.length;
        for (int i = 0; i < interceptorsCount; i++) {
            addKeyInterceptor(keyInterceptors[i]);
        }
    }

    public List<KeyInterceptor> getKeyInterceptors(int key) {
        return keyInterceptors.stream()
                .filter(interceptor -> interceptor.isKey(key))
                .toList();
    }

    public void removeKeyInterceptor(KeyInputListener keyListener) {
        keyInterceptors.remove(keyListener);
    }

    public void clearAllKeyInterceptors() {
        synchronized (keyInterceptors) {
            keyInterceptors.clear();
        }
    }

    public void shutdown() {
        interrupt();
        restoreTerminal();
        clearAllKeyInterceptors();
        clearAllKeyInputListeners();
        clearAllLineInputListeners();
    }

    @SneakyThrows
    public void run() {
        Runtime.getRuntime().exec(new String[]{"sh", "-c", "stty -echo -icanon < /dev/tty"}).waitFor();
        Runtime.getRuntime().addShutdownHook(new Thread(this::restoreTerminal));

        try (var reader = new FileInputStream(FileDescriptor.in)) {
            byte[] buffer = new byte[8];
            byte[] sequence;
            int key;
            List<KeyInterceptor> interceptors;
            while (!Thread.currentThread().isInterrupted()) {
                int read = reader.read(buffer);
                if (read <= 0) {
                    continue;
                }

                sequence = Arrays.copyOf(buffer, read);
                key = read > 1
                        ? (read == 3 ? parseSequence(sequence) : parseExtendedSequence(sequence))
                        : sequence[0];

                if (inputBlocked && key != unlockKeyCode) {
                    continue;
                }

                interceptors = getKeyInterceptors(key);
                if (!interceptors.isEmpty()) {
                    sendKeyToInterceptors(key, sequence, interceptors);
                } else {
                    handleInput(key, sequence);
                }

            }
        } catch (IOException ignored) {}
    }

}