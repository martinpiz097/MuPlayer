package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.ebot.utils.threads.Interruptor;
import cl.estencia.labs.ebot.utils.threads.InterruptorType;
import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols;
import cl.estencia.labs.muplayer.console.common.constants.KeyCodes;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.console.unix.InputMode;
import cl.estencia.labs.muplayer.console.unix.NativeInputReader;
import cl.estencia.labs.muplayer.console.unix.event.KeyInputEvent;
import cl.estencia.labs.muplayer.console.unix.event.LineInputEvent;
import cl.estencia.labs.muplayer.console.unix.listener.KeyInputListener;
import cl.estencia.labs.muplayer.console.unix.listener.LineInputListener;
import cl.estencia.labs.muplayer.core.cache.CacheManager;
import cl.estencia.labs.muplayer.core.cache.CacheVar;
import cl.estencia.labs.muplayer.core.system.SysInfo;
import org.orangelogger.sys.Logger;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static cl.estencia.labs.muplayer.core.cache.CacheVar.NATIVE_INPUT_READER;

public class LocalRunner extends ConsoleRunner {
    protected final Scanner scanner;
    protected final NativeInputReader nativeInputReader;
    protected final Interruptor interruptor;

    public LocalRunner() throws FileNotFoundException {
        this(new MuPlayer());
    }

    public LocalRunner(String folder) throws FileNotFoundException {
        this(new File(folder));
    }

    public LocalRunner(File rootFolder) throws FileNotFoundException {
        this(new MuPlayer(rootFolder));
    }

    public LocalRunner(Player player) {
        super(player);
        scanner = new Scanner(System.in);
        nativeInputReader = new NativeInputReader();
        interruptor = Interruptor.auto();
    }

    private void processCmd(String cmd) {
        if (!cmd.isEmpty()) {
            ConsoleOutput consoleOutput = execCommand(cmd);
            if (consoleOutput != null && consoleOutput.hasOutput()) {
                System.out.println(consoleOutput.getOutputMsg());
            }
        }
    }

    private void processCmd(char shorcut) {
        processCmd(String.valueOf(shorcut));
    }

    private void loadKeyInterceptors() {
        nativeInputReader.addKeyInterceptors(
                new KeyInputListener(KeyCodes.SEQ_RIGHT) {
                    @Override
                    public void onInput(KeyInputEvent event) {
                        processCmd("k 10");
                    }
                },
                new KeyInputListener(KeyCodes.SEQ_LEFT) {
                    @Override
                    public void onInput(KeyInputEvent event) {
                        processCmd("k -10");
                    }
                });
    }

    private void loadInputListeners() {
        nativeInputReader.addInputListeners(new KeyInputListener((Character) null) {
            @Override
            public void onInput(KeyInputEvent event) {
                if (event.getKey() == KeyCodes.SPACE) {
                    return;
                }
                if (event.getKey() != ConsoleSymbols.LINE_BREAK_CHAR) {
                    processCmd((char) event.getKey());
                } else {
                    System.out.print((char) event.getKey());
                }

                printConsoleHeader();
            }
        });

        nativeInputReader.addInputListeners(new LineInputListener() {
            @Override
            public void onInput(LineInputEvent event) {
                if (!event.isEmptyLine()) {
                    String line = event.getLine();
                    processCmd(line);
                }

                printConsoleHeader();
            }
        });

    }

    private void setupNativeReader() {
        nativeInputReader.setInputBlocked(false);
        nativeInputReader.getInputModeConfig().setInputMode(InputMode.COMMANDS);
        loadKeyInterceptors();
        loadInputListeners();
    }

    public void shutdown() {
        interpreter.setOn(false);
    }

    @Override
    public void run() {
        interruptor.setOwner(Thread.currentThread());

        validateRootFolder();
        setupNativeReader();

        nativeInputReader.start();
        globalCacheManager.saveValue(NATIVE_INPUT_READER, nativeInputReader);

        final String appVersion = SysInfo.readAppVersion();
        final String msg = appVersion != null
                ? "MuPlayer v"+appVersion+" started..."
                : "MuPlayer started...";
        Logger.getLogger(this, msg).rawInfo();
        interpreter.setOn(true);

        printConsoleHeader();
        while (interpreter.isOn()) {
            interruptor.checkSignal();
        }

        final ConsoleRunner runner = globalCacheManager.loadValue(CacheVar.RUNNER);
        if (runner == null || runner instanceof LocalRunner) {
            nativeInputReader.shutdown();
            System.exit(0);
        }
    }

}