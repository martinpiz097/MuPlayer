package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.ebot.utils.threads.Interruptor;
import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.config.Resources;
import cl.estencia.labs.muplayer.console.common.constants.KeyCodes;
import cl.estencia.labs.muplayer.console.common.enums.ConsoleOrderCode;
import cl.estencia.labs.muplayer.console.model.ConsoleImage;
import cl.estencia.labs.muplayer.console.unix.NativeConsole;
import cl.estencia.labs.muplayer.console.unix.event.AltKeyCombinationEvent;
import cl.estencia.labs.muplayer.console.unix.event.KeyInputEvent;
import cl.estencia.labs.muplayer.console.unix.listener.AltKeyCombinationListener;
import cl.estencia.labs.muplayer.console.unix.listener.KeyInputListener;
import cl.estencia.labs.muplayer.console.unix.listener.KeyInterceptor;
import cl.estencia.labs.muplayer.console.unix.listener.LineInputListener;
import cl.estencia.labs.muplayer.console.util.SystemCommandExecutor;
import cl.estencia.labs.muplayer.core.cache.CacheVar;
import cl.estencia.labs.muplayer.core.system.SysInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

import static cl.estencia.labs.muplayer.config.Resources.BANNER_PATH;
import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.*;
import static cl.estencia.labs.muplayer.console.common.enums.ConsoleOrderCode.cls;
import static cl.estencia.labs.muplayer.console.common.enums.ConsoleOutputMode.DEFAULT;
import static cl.estencia.labs.muplayer.console.unix.InputMode.SINGLE_SHORCUTS;
import static cl.estencia.labs.muplayer.console.util.ConsolePaintUtil.GradientStyle.LIGHTEN;
import static cl.estencia.labs.muplayer.console.util.ConsolePaintUtil.paintMuPlayerStyle;
import static cl.estencia.labs.muplayer.console.util.ConsolePaintUtil.printConsoleHeader;
import static cl.estencia.labs.muplayer.console.util.PlayerInterpreterUtil.printConsoleInfo;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.*;
import static cl.estencia.labs.muplayer.core.log.ConsolePrinter.printLine;

@Slf4j
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
        nativeConsole = new NativeConsole(EXT_F5, ESC, SINGLE_SHORCUTS);
        interruptor = Interruptor.manual();
    }

    private void loadKeyInterceptors() {
        if (!nativeConsole.getKeyInterceptors().isEmpty()) {
            nativeConsole.clearAllKeyInterceptors();
        }

        nativeConsole.addKeyInterceptors(
                new KeyInterceptor(KeyCodes.EXT_DELETE) {
                    @Override
                    public void intercept(KeyInputEvent event) {
                        sendCommand(cls);
                        printConsoleInfo(player, DEFAULT, false);
                    }
                },
                new KeyInterceptor(CTRL_L) {
                    @Override
                    public void intercept(KeyInputEvent event) {
                        sendCommand(cls);
                        printConsoleInfo(player, DEFAULT, false);
                    }
                },
                new KeyInterceptor(KeyCodes.EXT_PAGE_UP) {
                    @Override
                    public void intercept(KeyInputEvent event) {
                        sendCommand(ConsoleOrderCode.skf.name() + " prev");
                    }
                },
                new KeyInterceptor(KeyCodes.EXT_PAGE_DOWN) {
                    @Override
                    public void intercept(KeyInputEvent event) {
                        sendCommand(ConsoleOrderCode.skf.name() + " next");
                    }
                },
                new KeyInterceptor(SEQ_LEFT) {
                    @Override
                    public void intercept(KeyInputEvent event) {
                        sendCommand(ConsoleOrderCode.p);
                    }
                },
                new KeyInterceptor(SEQ_RIGHT) {
                    @Override
                    public void intercept(KeyInputEvent event) {
                        sendCommand(ConsoleOrderCode.n);
                    }
                },
                new KeyInterceptor(QUESTION) {
                    @Override
                    public void intercept(KeyInputEvent event) {
                        sendCommand(ConsoleOrderCode.h);
                    }
                }
        );

    }

    private void loadKeyListeners() {
        if (!nativeConsole.getKeyInputListeners().isEmpty()) {
            nativeConsole.clearAllKeyInputListeners();
        }

        nativeConsole.addInputListener(new KeyInputListener(MINUS) {
            @Override
            public void onInputEvent(KeyInputEvent event) {
                if (!player.isAlive()) {
                    return;
                }

                sendCommand(ConsoleOrderCode.p);
            }
        });

        nativeConsole.addInputListener(new KeyInputListener(EQUALS) {
            @Override
            public void onInputEvent(KeyInputEvent event) {
                if (!player.isAlive()) {
                    return;
                }

                sendCommand(ConsoleOrderCode.n);
            }
        });

        nativeConsole.addInputListener(new KeyInputListener(SEQ_HOME) {
            @Override
            public void onInputEvent(KeyInputEvent event) {
                if (!player.isAlive()) {
                    return;
                }

                sendCommand(ConsoleOrderCode.sk, "-10");
            }
        });

        nativeConsole.addInputListener(new KeyInputListener(SEQ_END) {
            @Override
            public void onInputEvent(KeyInputEvent event) {
                if (!player.isAlive()) {
                    return;
                }

                sendCommand(ConsoleOrderCode.sk, "10");
            }
        });

        nativeConsole.addInputListener(new KeyInputListener(SEQ_UP) {
            @Override
            public void onInputEvent(KeyInputEvent event) {
                if (!player.isAlive()) {
                    return;
                }

                sendCommand(ConsoleOrderCode.sv, "+5");
            }
        });

        nativeConsole.addInputListener(new KeyInputListener(SEQ_DOWN) {
            @Override
            public void onInputEvent(KeyInputEvent event) {
                if (!player.isAlive()) {
                    return;
                }

                sendCommand(ConsoleOrderCode.sv, "-5");
            }
        });

        nativeConsole.addInputListener(new KeyInputListener(KeyCodes.LINE_FEED) {
            @Override
            public void onInputEvent(KeyInputEvent event) {
                if (player.isAlive()) {
                    return;
                }

                sendCommand(ConsoleOrderCode.st);
            }
        });

        nativeConsole.addInputListener(new KeyInputListener(b, B) {
            @Override
            public void onInputEvent(KeyInputEvent event) {
                printBannerLogo();
                printConsoleInfo(player, DEFAULT, false);
            }
        });

        nativeConsole.addInputListener(new KeyInputListener(L) {
            @Override
            public void onInputEvent(KeyInputEvent event) {
                sendCommand(ConsoleOrderCode.lf);
                printConsoleInfo(player, DEFAULT, false);
            }
        });

        nativeConsole.addInputListener(new KeyInputListener(C) {
            @Override
            public void onInputEvent(KeyInputEvent event) {
                sendCommand(ConsoleOrderCode.lc);
                printConsoleInfo(player, DEFAULT, false);
            }
        });

        nativeConsole.addInputListener(new KeyInputListener(F) {
            @Override
            public void onInputEvent(KeyInputEvent event) {
                sendCommand(ConsoleOrderCode.lf);
                printConsoleInfo(player, DEFAULT, false);
            }
        });

        nativeConsole.addInputListener(new KeyInputListener(e, E, q, Q) {
            @Override
            public void onInputEvent(KeyInputEvent event) {
                if (player.isAlive() && interpreter.isOn()) {
                    sendCommand(ConsoleOrderCode.sh);
                } else {
                    globalCacheManager.clear();
                    System.exit(0);
                }
            }
        });

        nativeConsole.addInputListener(new KeyInputListener(TAB) {
            @SneakyThrows
            @Override
            public void onInputEvent(KeyInputEvent event) {
                if (!player.isAlive()) {
                    return;
                }

                if (player.isPlaying()) {
                    sendCommand(ConsoleOrderCode.ps.name());
                } else if (player.isPaused() || player.isStopped()) {
                    sendCommand(ConsoleOrderCode.r.name());
                }

                sendCommand(cls);
                printConsoleInfo(player, DEFAULT, false);
            }
        });

        nativeConsole.addInputListener(new KeyInputListener(DIGITS) {
            @SneakyThrows
            @Override
            public void onInputEvent(KeyInputEvent event) {
                if (!player.isAlive()) {
                    return;
                }

                int key = event.getKey();
                int number = key == DIGIT_0 ? 10 : KeyCodes.toDigit(key);

                sendCommand(ConsoleOrderCode.pf, String.valueOf(number));
            }
        });

        nativeConsole.addInputListener(new KeyInputListener() {
            @SneakyThrows
            @Override
            public void onInputEvent(KeyInputEvent event) {
                char key = event.getKeyChar();
                if (key == SPACE) {
                    return;
                }

                sendCommand(key);
                printConsoleInfo(player, DEFAULT, false);
            }
        });

    }
    
    private void loadKeyCombinations() {
        nativeConsole.addKeyCombListener(new AltKeyCombinationListener(ALT_c) {
            @Override
            protected void onCombination(AltKeyCombinationEvent event) {
                sendCommand(ConsoleOrderCode.cover);
                printConsoleInfo(player, DEFAULT, false);
            }
        });
    }

    private void loadLineListeners() {
        if (!nativeConsole.getLineInputListeners().isEmpty()) {
            nativeConsole.clearAllLineInputListeners();
        }

        nativeConsole.addInputListener((LineInputListener) event -> {
            if (!event.isEmptyLine()) {
                String line = event.getInput();
                sendCommand(line);
            }

            printConsoleInfo(player, DEFAULT, false);
        });

    }

    private void setupNativeConsole() {
        nativeConsole.getInputConfig().setInputBlocked(false);
        nativeConsole.getInputConfig().setInputMode(SINGLE_SHORCUTS);
        loadKeyInterceptors();
        loadKeyListeners();
        loadKeyCombinations();
        loadLineListeners();

        nativeConsole.start();
    }

    private void printBannerLogo() {
        int terminalWidth = SystemCommandExecutor.getRealTerminalWidth();
        InputStream bannerStream = Resources.getResStream(BANNER_PATH);
        ConsoleImage image = new ConsoleImage(
                bannerStream, 80, 15, terminalWidth);

        String consoleString = image.drawString();
        printLine(consoleString);
    }

    private void printAppVersion() {
        final String appVersion = SysInfo.readAppVersion();
        if (appVersion == null || appVersion.isBlank()) {
            return;
        }

        final String msg = "Version " + appVersion + " started!\n\n";
        printLine(paintMuPlayerStyle(msg, LIGHTEN));
    }

    public void shutdown() {
        interpreter.setOn(false);
    }

    @Override
    public void run() {
        interruptor.setOwner(Thread.currentThread());
        validateRootFolder();
        setupNativeConsole();

        globalCacheManager.saveValue(RUNNER, this);
        globalCacheManager.saveValue(NATIVE_CONSOLE, nativeConsole);

        printBannerLogo();
        printAppVersion();
        interpreter.setOn(true);

        printConsoleHeader(player, DEFAULT);
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
