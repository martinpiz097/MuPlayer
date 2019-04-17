package org.muplayer.system;

import org.orangelogger.sys.SystemUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SystemConsole {

    private static void printStreamOut(InputStream cmdStream) throws IOException {
        int read;
        FileOutputStream stdout = SystemUtil.getStdout();
        while ((read = cmdStream.read()) != -1)
            stdout.write(read);
    }

    public static void exec(String cmd) throws IOException {
        Process process;
        if (cmd.equals("clear"))
            if (SysInfo.ISUNIX)
                process = Runtime.getRuntime().exec("clear");
            else
                process = Runtime.getRuntime().exec("cls");
        else
            process = Runtime.getRuntime().exec(cmd);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (process.exitValue() == 0)
            printStreamOut(process.getInputStream());
        else
            printStreamOut(process.getErrorStream());
    }
}
