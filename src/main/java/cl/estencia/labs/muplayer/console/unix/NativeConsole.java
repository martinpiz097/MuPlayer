package cl.estencia.labs.muplayer.console.unix;

import cl.estencia.labs.muplayer.core.aucom.util.ProcessManager;
import cl.estencia.labs.muplayer.console.common.enums.InterceptorMode;
import cl.estencia.labs.muplayer.console.unix.event.AltKeyCombinationEvent;
import cl.estencia.labs.muplayer.console.unix.event.KeyInputEvent;
import cl.estencia.labs.muplayer.console.unix.event.LineInputEvent;
import cl.estencia.labs.muplayer.console.unix.listener.*;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.*;
import static cl.estencia.labs.muplayer.console.unix.InputMode.COMMANDS;
import static cl.estencia.labs.muplayer.console.util.ConsoleUtil.cartReturn;
import static cl.estencia.labs.muplayer.core.log.ConsolePrinter.info;

@Getter
public class NativeConsole extends Console {
    private final StringBuilder sbInput;

    private final List<KeyInterceptor> defaultKeyInterceptors;
    private final List<KeyInterceptor> keyInterceptors;
    private final List<AltKeyCombinationListener> altKeyCombinationListeners;
    private final List<KeyInputListener> keyInputListeners;
    private final List<LineInputListener> lineInputListeners;

    private final InputConfig inputConfig;
    private final ConsoleHistory consoleHistory;

    public NativeConsole() {
        this(ESC);
    }

    public NativeConsole(int toggleModeKey) {
        this(toggleModeKey, ESC, COMMANDS);
    }

    public NativeConsole(int toggleModeKey, int unlockKeyCode) {
        this(toggleModeKey, unlockKeyCode, COMMANDS);
    }

    public NativeConsole(int toggleModeKey, int unlockKeyCode, InputMode inputMode) {
        this.inputConfig = new InputConfig(inputMode, false, toggleModeKey, unlockKeyCode);
        this.sbInput = new StringBuilder();
        this.defaultKeyInterceptors = loadDefaultKeyInterceptors();
        this.keyInterceptors = CollectionUtil.newFastArrayList();
        this.altKeyCombinationListeners = CollectionUtil.newFastArrayList();
        this.keyInputListeners = CollectionUtil.newFastArrayList();
        this.lineInputListeners = CollectionUtil.newFastArrayList();
        this.consoleHistory = new ConsoleHistory();
        setName("native-console");
    }

    // es para restaurar terminal cuando el programa termina (por sea caso)
    private void restoreTerminal() {
        ProcessManager.executeLegacy("sh", "-c", "stty sane < /dev/tty");
    }

    private void printHistoryCommand(String command) {
        if (command == null) {
            return;
        }

        int cartReturnColumns = sbInput.length();
        sbInput.delete(0, sbInput.length());
        sbInput.append(command);

//        info((cartReturn(cartReturnColumns) + command);
    }

    private void printKey(int key) {
        info((char) key);
    }

    private void sendInputEvent(KeyInputEvent event) {
        if (keyInputListeners.isEmpty()) {
            return;
        }

        List<KeyInputListener> filteredKeyListeners = keyInputListeners.parallelStream()
                .filter(keyInputListener ->
                        keyInputListener.hasKey(event.getKeyChar()))
                .toList();

        if (filteredKeyListeners.isEmpty()) {
            filteredKeyListeners = keyInputListeners.parallelStream()
                    .filter(KeyInputListener::isGeneric)
                    .toList();
        }

        filteredKeyListeners
                .forEach(inputListener -> inputListener.onInputEvent(event));
    }

    private void sendInputEvent(AltKeyCombinationEvent event) {
        if (altKeyCombinationListeners.isEmpty()) {
            return;
        }

        altKeyCombinationListeners.parallelStream()
                .filter(keyCombListener ->
                        keyCombListener.isKeysCombination(event.getKeys()))
                .forEach(inputListener -> inputListener.onInputEvent(event));
    }

    private void sendInputEvent(LineInputEvent event) {
        if (lineInputListeners.isEmpty()) {
            return;
        }

        lineInputListeners.parallelStream()
                .forEach(inputListener -> inputListener.onInputEvent(event));
    }

    private void sendKeyToInterceptors(int key, byte[] sequence,
                                       List<KeyInterceptor> interceptors) {
        interceptors.forEach(interceptor ->
                interceptor.onInputEvent(new KeyInputEvent(key, sequence)));
    }

    // los combination listener solo se usaran con los ALT (de momento)
    private void handleShorcut(int key, byte[] sequence) {
        final boolean isAltCombination = sequence.length == 2;
        if (isAltCombination) {
            sendInputEvent(new AltKeyCombinationEvent(sequence));
        } else {
            sendInputEvent(new KeyInputEvent(key, sequence));
        }
    }

    private void handleCommand(int key, byte[] sequence) {
        if (key == LINE_FEED) {
            String line = sbInput.toString();
            consoleHistory.addCommand(line);
            sbInput.delete(0, sbInput.length());

            Thread.ofVirtual().start(() -> sendInputEvent(
                    new LineInputEvent(line)));
        } else {
            sbInput.append((char) key);
            consoleHistory.updateCurrentCommand(sbInput.toString());
        }

        printKey(key);
    }

    private void handleInput(int key, byte[] sequence) {
        InputMode inputMode = inputConfig.getInputMode();

        switch (inputMode) {
            case SINGLE_SHORCUTS -> handleShorcut(key, sequence);
            case COMMANDS -> handleCommand(key, sequence);
        }

    }

    public List<KeyInterceptor> loadDefaultKeyInterceptors() {
        List<KeyInterceptor> defaultInterceptors = CollectionUtil.newFastArrayList();

        defaultInterceptors.add(new KeyInterceptor(inputConfig.getUnlockKey()) {
            @Override
            public void intercept(KeyInputEvent event) {
                inputConfig.toggleInputBlocked();
            }
        });

        defaultInterceptors.add(new KeyInterceptor(inputConfig.getToggleModeKey()) {
            @Override
            public void intercept(KeyInputEvent event) {
                inputConfig.toggleInputMode();
            }
        });

        defaultInterceptors.add(new KeyInterceptor(SEQ_UP, InterceptorMode.SIMPLE) {
            @Override
            public void intercept(KeyInputEvent event) {
                String prevCommand = consoleHistory.getPrevCommand();
                printHistoryCommand(prevCommand);
            }
        });

        defaultInterceptors.add(new KeyInterceptor(SEQ_DOWN, InterceptorMode.SIMPLE) {
            @Override
            public void intercept(KeyInputEvent event) {
                String nextCommand = consoleHistory.getNextCommand();
                printHistoryCommand(nextCommand);
            }
        });

        defaultInterceptors.add(new KeyInterceptor(DELETE) {
            @Override
            public void intercept(KeyInputEvent event) {
                if (hasLine()) {
                    return;
                }

                int cartReturnCount = sbInput.length();
                sbInput.deleteCharAt(sbInput.length() - 1);
                consoleHistory.updateCurrentCommand(sbInput.toString());

                info(cartReturn(cartReturnCount) + sbInput);
            }
        });

        return defaultInterceptors;
    }

    public boolean hasLine() {
        return !sbInput.isEmpty();
    }

    public String getLine() {
        return sbInput.toString();
    }

    public void addDefaultKeyInterceptors() {
        keyInterceptors.addAll(defaultKeyInterceptors);
    }

    public void removeAllDefaultKeyInterceptors() {
        keyInterceptors.removeAll(defaultKeyInterceptors);
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
        if (inputListener instanceof KeyInputListener keyInputListener) {
            keyInputListeners.add(keyInputListener);
        } else if (inputListener instanceof LineInputListener lineInputListener) {
            lineInputListeners.add(lineInputListener);
        }
    }

    public <L extends NativeInputListener> void removeInputListener(L inputListener) {
        if (inputListener instanceof KeyInputListener keyInputListener) {
            keyInputListeners.remove(keyInputListener);
        } else if (inputListener instanceof LineInputListener lineInputListener) {
            lineInputListeners.remove(lineInputListener);
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

    public void addKeyInterceptor(KeyInterceptor keyInterceptor) {
        keyInterceptors.add(keyInterceptor);
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

    public List<KeyInterceptor> getInterceptorsForKey(int key) {
        final boolean isCommandMode = getInputMode() == COMMANDS;
        return keyInterceptors.stream()
                .filter(interceptor -> interceptor.isKey(key)
                    && (isCommandMode || interceptor.isGlobal()))
                .toList();
    }

    public void removeKeyInterceptor(KeyInterceptor keyInterceptor) {
        keyInterceptors.remove(keyInterceptor);
    }

    public void clearAllKeyInterceptors() {
        synchronized (keyInterceptors) {
            keyInterceptors.clear();
        }
    }

    public void addKeyCombListener(AltKeyCombinationListener altKeyCombinationListener) {
        altKeyCombinationListeners.add(altKeyCombinationListener);
    }

    public void addKeyCombListeners(AltKeyCombinationListener... altKeyCombinationListeners) {
        if (altKeyCombinationListeners == null || altKeyCombinationListeners.length == 0) {
            return;
        }

        int interceptorsCount = altKeyCombinationListeners.length;
        for (int i = 0; i < interceptorsCount; i++) {
            addKeyCombListener(altKeyCombinationListeners[i]);
        }
    }

    public List<AltKeyCombinationListener> getCombListenerForKeys(int[] keys) {
        return altKeyCombinationListeners.stream()
                .filter(combListener -> combListener.isKeysCombination(keys))
                .toList();
    }

    public void removeKeyInterceptor(AltKeyCombinationListener combListener) {
        altKeyCombinationListeners.remove(combListener);
    }

    public void clearAllCombListeners() {
        synchronized (altKeyCombinationListeners) {
            altKeyCombinationListeners.clear();
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
        addDefaultKeyInterceptors();

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
                key = parseSequence(sequence);
                if (inputConfig.isInputBlocked() && key != inputConfig.getUnlockKey()) {
                    continue;
                }

                interceptors = getInterceptorsForKey(key);
                if (!interceptors.isEmpty()) {
                    sendKeyToInterceptors(key, sequence, interceptors);
                } else {
                    handleInput(key, sequence);
                }

            }
        } catch (IOException ignored) {}
    }

}