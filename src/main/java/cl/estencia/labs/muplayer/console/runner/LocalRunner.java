package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.ebot.utils.threads.Interruptor;
import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.config.ResourceFiles;
import cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols;
import cl.estencia.labs.muplayer.console.common.constants.KeyCodes;
import cl.estencia.labs.muplayer.console.model.ConsoleImage;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.console.unix.InputMode;
import cl.estencia.labs.muplayer.console.unix.NativeConsole;
import cl.estencia.labs.muplayer.console.unix.event.KeyInputEvent;
import cl.estencia.labs.muplayer.console.unix.event.LineInputEvent;
import cl.estencia.labs.muplayer.console.unix.listener.KeyInputListener;
import cl.estencia.labs.muplayer.console.unix.listener.KeyInterceptor;
import cl.estencia.labs.muplayer.console.unix.listener.LineInputListener;
import cl.estencia.labs.muplayer.console.util.ConsoleTextPainter;
import cl.estencia.labs.muplayer.console.util.SystemCommandExecutor;
import cl.estencia.labs.muplayer.core.cache.CacheVar;
import cl.estencia.labs.muplayer.core.system.SysInfo;
import org.orangelogger.sys.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.SPACE;
import static cl.estencia.labs.muplayer.console.util.ConsoleTextPainter.GradientStyle.LIGHTEN;
import static cl.estencia.labs.muplayer.console.util.ConsoleTextPainter.paintMuPlayerStyle;
import static cl.estencia.labs.muplayer.console.util.ConsoleUtil.isNumberKey;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.NATIVE_INPUT_READER;

public class LocalRunner extends ConsoleRunner {
    protected final Scanner scanner;
    protected final NativeConsole nativeConsole;
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
        nativeConsole = new NativeConsole();
        interruptor = Interruptor.manual();
    }

    private void processCommand(String cmd) {
        if (!cmd.isEmpty()) {
            ConsoleOutput consoleOutput = execCommand(cmd);
            if (consoleOutput != null && consoleOutput.hasOutput()) {
                System.out.println(consoleOutput.getOutputMsg());
            }
        }
    }

    private void processShorcut(char shorcut) {
        processCommand(String.valueOf(shorcut));
    }

    private void loadKeyInterceptors() {
        if (!nativeConsole.getKeyInterceptors().isEmpty()) {
            nativeConsole.clearAllKeyInterceptors();
        }

        nativeConsole.loadDefaultKeyInterceptors();
        nativeConsole.addKeyInterceptors(
                new KeyInterceptor(KeyCodes.SEQ_RIGHT) {
                    @Override
                    protected void intercept(KeyInputEvent event) {
                        processCommand("k 10");
                    }
                },
                new KeyInterceptor(KeyCodes.SEQ_LEFT) {
                    @Override
                    protected void intercept(KeyInputEvent event) {
                        processCommand("k -10");
                    }
                },
                new KeyInterceptor(KeyCodes.EXT_DELETE) {
                    @Override
                    protected void intercept(KeyInputEvent event) {
                        processCommand("clear");
                    }
                });
    }

    private void loadKeyListeners() {
        if (!nativeConsole.getKeyInputListeners().isEmpty()) {
            nativeConsole.clearAllKeyInputListeners();
        }

        nativeConsole.addInputListeners(new KeyInputListener() {
            @Override
            public void onInput(KeyInputEvent event) {
                int key = event.getKey();
                if (key == KeyCodes.SPACE) {
                    return;
                } else if (isNumberKey(key)) {
                    int number = Integer.parseInt(String.valueOf((char) key));
                    if (number == 0) {
                        number = 10;
                    }

                    processCommand("pf " + number);
                } else if (key == KeyCodes.b || key == KeyCodes.B) {
                    printBannerLogo();
                } else if (key != ConsoleSymbols.LINE_BREAK_CHAR) {
                    processShorcut((char) key);
                } else {
                    System.out.print((char) key);
                }

                printConsoleHeader();
            }
        });

    }

    private void loadLineListeners() {
        if (!nativeConsole.getLineInputListeners().isEmpty()) {
            nativeConsole.clearAllLineInputListeners();
        }

        nativeConsole.addInputListeners(new LineInputListener() {
            @Override
            public void onInput(LineInputEvent event) {
                if (!event.isEmptyLine()) {
                    String line = event.getLine();
                    processCommand(line);
                }

                printConsoleHeader();
            }
        });

    }

    private void setupNativeConsole() {
        nativeConsole.setInputBlocked(false);
        nativeConsole.getInputModeConfig().setInputMode(InputMode.COMMANDS);
        loadKeyInterceptors();
        loadKeyListeners();
        loadLineListeners();

        nativeConsole.start();
    }

    private void printBannerLogo() {
        int terminalWidth = SystemCommandExecutor.getRealTerminalWidth();

        InputStream bannerStream = ResourceFiles.getResStream("/img/banner.png");
        ConsoleImage image = new ConsoleImage(
                bannerStream, 80, 15, terminalWidth);
        String consoleString = image.drawString();
        Logger.getLogger(this, consoleString).rawMessage();
    }

    private void printAppVersion() {
        final String appVersion = SysInfo.readAppVersion();
        if (appVersion == null || appVersion.isBlank()) {
            return;
        }

        final String msg = "Version " + appVersion + " started!\n\n";
        Logger.getLogger(this, paintMuPlayerStyle(msg, LIGHTEN)).rawMessage();
    }

    public void shutdown() {
        interpreter.setOn(false);
    }

    @Override
    public void run() {
        interruptor.setOwner(Thread.currentThread());

        validateRootFolder();
        setupNativeConsole();

        globalCacheManager.saveValue(NATIVE_INPUT_READER, nativeConsole);

        printBannerLogo();
        printAppVersion();
        interpreter.setOn(true);

        printConsoleHeader();
        while (interpreter.isOn()) {
            interruptor.checkSignal();
        }

        final ConsoleRunner runner = globalCacheManager.loadValue(CacheVar.RUNNER);
        if (runner == null || runner instanceof LocalRunner) {
            nativeConsole.shutdown();
            System.exit(0);
        }
    }

}
