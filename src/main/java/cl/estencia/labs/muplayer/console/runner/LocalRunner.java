package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.console.unix.InputMode;
import cl.estencia.labs.muplayer.console.unix.NativeInputReader;
import cl.estencia.labs.muplayer.console.unix.event.KeyInputEvent;
import cl.estencia.labs.muplayer.console.unix.listener.KeyInputListener;
import cl.estencia.labs.muplayer.core.cache.CacheManager;
import cl.estencia.labs.muplayer.core.cache.CacheVar;
import cl.estencia.labs.muplayer.core.system.SysInfo;
import org.orangelogger.sys.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LocalRunner extends ConsoleRunner {
    protected final Scanner scanner;
    protected final NativeInputReader nativeInputReader;

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
    }

    private void processCmd(String cmd) {
        if (!cmd.isEmpty()) {
            ConsoleOutput consoleOutput = execCommand(cmd);
            if (consoleOutput != null && consoleOutput.hasOutput()) {
                System.out.println(consoleOutput.getOutputMsg());
            }
        }
    }

    private void setupNativeReader() {
        nativeInputReader.setInputBlocked(false);
        nativeInputReader.getInputModeConfig().setInputMode(InputMode.COMMANDS);
        nativeInputReader.addInputListener(new KeyInputListener((Character) null) {
            @Override
            public void onInput(KeyInputEvent event) {
                processCmd(event.getLine());
            }
        });
    }

    public void shutdown() {
        interpreter.setOn(false);
    }

    @Override
    public void run() {
        validateRootFolder();
        setupNativeReader();
        nativeInputReader.start();
        CacheManager.getGlobalCache().saveValue(CacheVar.NATIVE_INPUT_READER, nativeInputReader);


        final String appVersion = SysInfo.readAppVersion();
        final String msg = appVersion != null
                ? "MuPlayer v"+appVersion+" started..."
                : "MuPlayer started...";
        Logger.getLogger(this, msg).rawInfo();
        interpreter.setOn(true);

        String cmd;
        while (interpreter.isOn()) {
            printConsoleHeader();
            cmd = nativeInputReader.getLine();
            processCmd(cmd);
        }

        final ConsoleRunner runner = globalCacheManager.loadValue(CacheVar.RUNNER);
        if (runner == null || runner instanceof LocalRunner) {
            System.exit(0);
        }
    }

}