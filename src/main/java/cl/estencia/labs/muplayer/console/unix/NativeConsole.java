package cl.estencia.labs.muplayer.console.unix;

import cl.estencia.labs.aucom.core.util.ProcessManager;
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

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleEscapeSequences.padding;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleEscapeSequences.partialCartReturn;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.LINE_BREAK_CHAR;
import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.*;
import static cl.estencia.labs.muplayer.console.unix.InputMode.COMMANDS;

@Getter
public class NativeConsole extends Console {
    private final StringBuilder sbInput;

    private final List<KeyInterceptor> keyInterceptors;
    private final List<KeyInputListener> keyInputListeners;
    private final List<LineInputListener> lineInputListeners;

    private final InputConfig inputConfig;
    private final ConsoleHistory consoleHistory;

    public NativeConsole() {
        this(ESC);
    }

    public NativeConsole(int unlockKeyCode) {
        this(unlockKeyCode, COMMANDS);
    }

    public NativeConsole(int unlockKeyCode, InputMode inputMode) {
        this.inputConfig = new InputConfig(inputMode, false, EXT_F5, ESC);
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

    private String cartReturn(int columns) {
        String partialCartReturn = partialCartReturn(columns);
        String padding = padding(columns);

        return partialCartReturn + padding + partialCartReturn;
    }

    private void printKey(int key) {
        IO.print((char) key);
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
            default -> {
                sbInput.append((char) key);
                consoleHistory.updateCurrentCommand(sbInput.toString());
                printKey(key);
            }
        }

    }

    private void handleInput(int key, byte[] sequence) {
        InputMode inputMode = inputConfig.getInputMode();

        switch (inputMode) {
            case SINGLE_SHORCUTS -> handleShorcut(key, sequence);
            case COMMANDS -> handleCommand(key, sequence);
        }

    }

    // es para restaurar terminal cuando el programa termina (por sea caso)
    private void restoreTerminal() {
        ProcessManager.executeLegacy("sh", "-c", "stty sane < /dev/tty");
    }

    public void loadDefaultKeyInterceptors() {
        addKeyInterceptor(new KeyInterceptor(inputConfig.getUnlockKey()) {
            @Override
            protected void intercept(KeyInputEvent event) {
                inputConfig.toggleInputBlocked();
            }
        });

        addKeyInterceptor(new KeyInterceptor(inputConfig.getToggleModeKey()) {
            @Override
            protected void intercept(KeyInputEvent event) {
                inputConfig.toggleInputMode();
            }
        });

        addKeyInterceptor(new KeyInterceptor(SEQ_UP) {
            @Override
            protected void intercept(KeyInputEvent event) {
                String prevCommand = consoleHistory.getPrevCommand();
                if (prevCommand == null) {
                    return;
                }

                int cartReturnColumns = Math.max(prevCommand.length(), sbInput.length());
                System.out.print(cartReturn(cartReturnColumns) + prevCommand);

                sbInput.delete(0, sbInput.length());
                sbInput.append(prevCommand);
            }
        });

        addKeyInterceptor(new KeyInterceptor(SEQ_DOWN) {
            @Override
            protected void intercept(KeyInputEvent event) {
                String nextCommand = consoleHistory.getNextCommand();
                if (nextCommand == null) {
                    return;
                }

                int cartReturnColumns = Math.max(nextCommand.length(), sbInput.length());
                System.out.print(cartReturn(cartReturnColumns) + nextCommand);

                sbInput.delete(0, sbInput.length());
                sbInput.append(nextCommand);
            }
        });

        addKeyInterceptor(new KeyInterceptor(DELETE) {
            @Override
            protected void intercept(KeyInputEvent event) {
                if (sbInput.isEmpty()) {
                    return;
                }

                int cartReturnCount = sbInput.length();
                sbInput.deleteCharAt(sbInput.length() - 1);
                consoleHistory.updateCurrentCommand(sbInput.toString());

                IO.print(cartReturn(cartReturnCount) + sbInput);
            }
        });
    }

    public InputMode getInputMode() {
        return inputConfig.getInputMode();
    }

    public void setInputMode(InputMode inputMode) {
        inputConfig.setInputMode(inputMode);
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

    public void clearAllInterceptorsAndListeners() {
        clearAllKeyInterceptors();
        clearAllKeyInputListeners();
        clearAllLineInputListeners();
    }

    public void shutdown() {
        interrupt();
        restoreTerminal();
        clearAllInterceptorsAndListeners();
    }

    @SneakyThrows
    public void run() {
        ProcessManager.executeLegacy("sh", "-c", "stty -echo -icanon < /dev/tty");
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

                if (inputConfig.isInputBlocked() && key != inputConfig.getUnlockKey()) {
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