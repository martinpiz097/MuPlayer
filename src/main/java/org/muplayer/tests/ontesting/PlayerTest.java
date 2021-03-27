package org.muplayer.tests.ontesting;

import org.muplayer.main.ConsolePlayer;

import java.io.FileNotFoundException;

public class PlayerTest {
    public static void main(String[] args) throws FileNotFoundException {
        new Thread(new ConsolePlayer("/home/martin/Dropbox/Java/Proyectos/IntelliJ" +
                "/OrangePlayerProject/OrangePlayMusic/muplayer/audio/ogg"))
                .start();
    }
}
