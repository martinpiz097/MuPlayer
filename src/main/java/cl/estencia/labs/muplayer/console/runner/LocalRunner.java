package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.ebot.utils.threads.Interruptor;
import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.util.MuPlayerUtil;
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
import cl.estencia.labs.muplayer.console.util.SystemCommandExecutor;
import cl.estencia.labs.muplayer.core.bus.message.Messages;
import cl.estencia.labs.muplayer.core.bus.util.MessageBusUtil;
import cl.estencia.labs.muplayer.core.cache.CacheVar;
import cl.estencia.labs.muplayer.core.system.SysInfo;
import lombok.SneakyThrows;
import org.orangelogger.sys.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import static cl.estencia.labs.muplayer.console.util.ConsolePainter.GradientStyle.LIGHTEN;
import static cl.estencia.labs.muplayer.console.util.ConsolePainter.paintMuPlayerStyle;
import static cl.estencia.labs.muplayer.console.util.ConsoleUtil.isNumberKey;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.NATIVE_CONSOLE;

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
        // agregar interceptor para cerrar comprobando si esta activo player
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
                        printConsoleHeader();
                    }
                },
                new KeyInterceptor(KeyCodes.EXT_PAGE_UP) {
                    @Override
                    protected void intercept(KeyInputEvent event) {
                        processCommand("skf prev");
                    }
                },
                new KeyInterceptor(KeyCodes.EXT_PAGE_DOWN) {
                    @Override
                    protected void intercept(KeyInputEvent event) {
                        processCommand("skf next");
                    }
                },
                new KeyInterceptor(KeyCodes.SEQ_HOME) {
                    @Override
                    protected void intercept(KeyInputEvent event) {
                        processCommand("p");
                    }
                },
                new KeyInterceptor(KeyCodes.SEQ_END) {
                    @Override
                    protected void intercept(KeyInputEvent event) {
                        processCommand("n");
                    }
                }
                );

    }

    private void loadKeyListeners() {
        if (!nativeConsole.getKeyInputListeners().isEmpty()) {
            nativeConsole.clearAllKeyInputListeners();
        }

        nativeConsole.addInputListeners(new KeyInputListener() {
            @SneakyThrows
            @Override
            public void onInput(KeyInputEvent event) {
                int key = event.getKey();
                if (key == KeyCodes.SPACE) {
                    return;
                }

                if (key == KeyCodes.LINE_FEED) {
                    if (player.isAlive()) {
                        return;
                    }

                    processCommand("st");
                } else if (isNumberKey(key)) {
                    int number = Integer.parseInt(String.valueOf((char) key));
                    if (number == 0) {
                        number = 10;
                    }

                    processCommand("pf " + number);
                } else if (key == KeyCodes.b || key == KeyCodes.B) {
                    printBannerLogo();
                    printConsoleHeader();
                } else if (key == KeyCodes.L) {
                    processCommand("lf");
                    printConsoleHeader();
                } else if (key == KeyCodes.C) {
                    processCommand("lc");
                    printConsoleHeader();
                } else if (key == KeyCodes.e || key == KeyCodes.E
                        || key == KeyCodes.q || key == KeyCodes.Q) {
                    if (player.isAlive() && interpreter.isOn()) {
                        processCommand("sh");
                    } else {
                        globalCacheManager.clear();
                        System.exit(0);
                    }
                } else if (key == KeyCodes.TAB) {
                    if (player.isPlaying()) {
                        processCommand("ps");
                    } else if (player.isPaused() || player.isStopped()) {
                        processCommand("r");
                    }
                } else {
                    processShorcut((char) key);
                    printConsoleHeader();
                }
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

                // en este modo es probable que el header se vea dos veces cuando se haga next
                // o prev, se veria antes y despues de la info de la cancion
                printConsoleHeader();
            }
        });

    }

    private void setupNativeConsole() {
        nativeConsole.getInputConfig().setInputBlocked(false);
        nativeConsole.getInputConfig().setInputMode(InputMode.SINGLE_SHORCUTS);
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

        globalCacheManager.saveValue(NATIVE_CONSOLE, nativeConsole);

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
