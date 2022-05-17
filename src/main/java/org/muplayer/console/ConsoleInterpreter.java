package org.muplayer.console;

import org.muplayer.audio.MusicPlayer;
import org.muplayer.audio.Track;
import org.muplayer.interfaces.Player;
import org.muplayer.model.Album;
import org.muplayer.model.Artist;
import org.muplayer.model.SeekOption;
import org.muplayer.properties.HelpInfo;
import org.muplayer.system.*;
import org.muplayer.util.TrackUtil;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.WRITE;

public class ConsoleInterpreter implements CommandInterpreter {
    private Player musicPlayer;
    private final File playerFolder;
    private boolean on;

    private final HelpInfo helpInfo;

    private static final String CMD_DIVISOR = " && ";

    public ConsoleInterpreter(Player musicPlayer) {
        this.musicPlayer = musicPlayer;
        this.playerFolder = musicPlayer.getRootFolder();
        on = false;
        helpInfo = HelpInfo.getInstance();
    }

    private boolean isPlayerOn() {
        return musicPlayer != null &&
                musicPlayer.isAlive();
    }

    private void execSysCommand(String cmd) {
        Process process;
        try {
            process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            if (process.exitValue() == 0)
                printStreamOut(process.getInputStream());
            else
                printStreamOut(process.getErrorStream());
        } catch (IOException | InterruptedException e) {
            Logger.getLogger(this, e.getMessage()).error();
        }
    }

    private void printTracks() {
        final File rootFolder = musicPlayer.getRootFolder();
        final List<Track> listTracks = musicPlayer.getTracks();
        final Track current = musicPlayer.getCurrent();

        Logger.getLogger(this, "------------------------------").rawInfo();
        if (rootFolder == null)
            Logger.getLogger(this, "Music in folder").rawInfo();
        else
            Logger.getLogger(this, "Music in folder "+rootFolder.getName()).rawInfo();
        Logger.getLogger(this, "------------------------------").rawInfo();

        if (rootFolder != null) {
            Track track;
            File fileTrack;
            for (int i = 0; i < musicPlayer.getSongsCount(); i++) {
                track = listTracks.get(i);
                fileTrack = track.getDataSourceAsFile();
                if (current != null && fileTrack.getPath().equals(((File)current.getDataSourceAsFile()).getPath()))
                    Logger.getLogger(this, "Track "+(i+1)+": "
                            +fileTrack.getName()).rawWarning();
                else
                    Logger.getLogger(this, "Track "+(i+1)+": "
                            +fileTrack.getName()).rawInfo();
            }
            Logger.getLogger(this, "------------------------------").rawInfo();
        }
    }

    private void printDetailedTracks() {
        final File rootFolder = musicPlayer.getRootFolder();
        final List<Track> listTracks = musicPlayer.getTracks();
        final List<String> listFolderPaths = musicPlayer.getListFolders().stream()
                .map(File::getPath).collect(Collectors.toList());
        final Track current = musicPlayer.getCurrent();

        Logger.getLogger(this, "------------------------------").rawInfo();
        if (rootFolder == null)
            Logger.getLogger(this, "Music in folder").rawInfo();
        else
            Logger.getLogger(this, "Music in folder "+rootFolder.getName()).rawInfo();
        Logger.getLogger(this, "------------------------------").rawInfo();

        if (rootFolder != null) {
            File trackFile;
            File trackFolder, prevTrackFolder = null;
            for (int i = 0; i < musicPlayer.getSongsCount(); i++) {
                trackFile = listTracks.get(i).getDataSourceAsFile();
                trackFolder = trackFile.getParentFile();
                if (prevTrackFolder == null || !trackFolder.getPath().equals(prevTrackFolder.getPath()))
                    Logger.getLogger(this,
                            (prevTrackFolder != null
                                    ? "----------------------------------------------------------------------" +
                                    "\n\n----------------------------------------------------------------------" +
                                    "\nFolder: "
                                    : "----------------------------------------------------------------------\n"
                                    + "Folder: ")+trackFolder.getName()).rawInfo();
                if (current != null && trackFile.getPath().equals(current.getDataSourceAsFile().getPath()))
                    Logger.getLogger(this, "\tTrack "+(i+1)+": "
                            +trackFile.getName()).rawWarning();
                else
                    Logger.getLogger(this, "\tTrack "+(i+1)+": "
                            +trackFile.getName()).rawInfo();

                prevTrackFolder = trackFile.getParentFile();
            }
            Logger.getLogger(this, "------------------------------").rawInfo();
        }
    }

    private synchronized void printFolderTracks() {
        final List<Track> listTracks = musicPlayer.getTracks();
        final Track current = musicPlayer.getCurrent();
        final int songsCount = musicPlayer.getSongsCount();

        File parentFolder = current == null ? null : current.getDataSourceAsFile().getParentFile();
        Logger.getLogger(this, "------------------------------").rawInfo();

        if (parentFolder == null)
            Logger.getLogger(this, "Music in current folder").rawInfo();
        else
            Logger.getLogger(this, "Music in folder "+parentFolder.getName()).rawInfo();
        Logger.getLogger(this, "------------------------------").rawInfo();

        if (parentFolder != null) {
            File fileTrack;
            File currentFile = current.getDataSourceAsFile();

            for (int i = 0; i < songsCount; i++) {
                fileTrack = listTracks.get(i).getDataSourceAsFile();
                if (fileTrack.getParentFile().equals(parentFolder)) {
                    if (fileTrack.getPath().equals(currentFile.getPath()))
                        Logger.getLogger(this, "Track "+(i+1)+": "
                                +fileTrack.getName()).rawWarning();
                    else
                        Logger.getLogger(this, "Track "+(i+1)+": "
                                +fileTrack.getName()).rawInfo();
                }
            }
            Logger.getLogger(this, "------------------------------").rawInfo();
        }
    }

    private synchronized void printFolders() {
        final File rootFolder = musicPlayer.getRootFolder();
        final List<String> listFolderPaths = musicPlayer.getListFolders().stream().map(
                File::getPath).collect(Collectors.toList());
        final Track current = musicPlayer.getCurrent();

        Logger.getLogger(this, "------------------------------").rawInfo();
        if (rootFolder == null)
            Logger.getLogger(this, "Folders").rawInfo();
        else
            Logger.getLogger(this, "Folders in "+rootFolder.getName()).rawInfo();
        Logger.getLogger(this, "------------------------------").rawInfo();

        if (current == null)
            return;

        File currentTrackFile = current.getDataSourceAsFile();
        File folder;

        for (int i = 0; i < musicPlayer.getFoldersCount(); i++) {
            folder = new File(listFolderPaths.get(i));
            if (folder.getPath().equals(currentTrackFile.getParentFile().getPath())) {
                Logger.getLogger(this, "Folder "+(i+1)+": "
                        +folder.getName()).rawWarning();
            }
            else {
                Logger.getLogger(this, "Folder "+(i+1)+": "
                        +folder.getName()).rawInfo();
            }

        }
        Logger.getLogger(this, "------------------------------").rawInfo();
    }

    protected void printStreamOut(InputStream cmdStream) throws IOException {
        int read;
        FileOutputStream stdout = SystemUtil.getStdout();
        while ((read = cmdStream.read()) != -1)
            stdout.write(read);
    }

    protected void clearConsole() throws IOException {
        Process process;
        if (SysInfo.IS_UNIX)
            process = Runtime.getRuntime().exec("clear");
        else
            process = Runtime.getRuntime().exec("cls");
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

    protected void printHelp() {
        final Set<String> propertyNames = helpInfo.getPropertyNames();
        final Iterator<String> it = propertyNames.iterator();
        final StringBuilder sbHelp = new StringBuilder();

        String key;
        int count = 1;
        while (it.hasNext()) {
            key = it.next();
            sbHelp.append(count++).append(") ")
                    .append(key)
                    .append(": ")
                    .append(helpInfo.getProperty(key))
                    .append('\n');
        }
        Logger.getLogger(this, "---------").rawWarning();
        Logger.getLogger(this, "Help Info").rawWarning();
        Logger.getLogger(this, "---------").rawWarning();
        Logger.getLogger(this, sbHelp.toString()).rawWarning();
    }

    public void showSongInfo(Track track) {
        if (track == null)
            Logger.getLogger(this, "Current track unavailable").rawError();
        else
            Logger.getLogger(this, TrackUtil.getSongInfo(track)).rawWarning();
    }

    public ConsoleExecution executeCommand(String cmd) throws Exception {
        if (cmd.contains(CMD_DIVISOR)) {
            final String[] cmdSplit = cmd.split(CMD_DIVISOR);
            ConsoleExecution consoleExecution = new ConsoleExecution();

            final List<Object> listExec = new ArrayList<>();
            ConsoleExecution exec;

            for (int i = 0; i < cmdSplit.length; i++) {
                exec = executeCommand(cmdSplit[i].trim());
                if (exec != null)
                    listExec.add(exec.getOutput());
            }
            consoleExecution.setCmd(cmd);
            consoleExecution.setOutput(listExec);
            return consoleExecution;
        }
        else
            return executeCommand(new Command(cmd));
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    @Override
    public ConsoleExecution executeCommand(Command cmd) throws Exception {
        final String cmdOrder = cmd.getOrder();
        Track current;

        final ConsoleExecution consoleExecution = new ConsoleExecution();
        consoleExecution.setCmd(cmd.toString());
        switch (cmdOrder) {
            case ConsoleOrder.START:
                if (musicPlayer == null)
                    musicPlayer = new MusicPlayer(playerFolder);
                if (musicPlayer.isAlive() && cmd.hasOptions()) {
                    File musicFolder = new File(cmd.getOptionAt(0));
                    if (musicFolder.exists()) {
                        MusicPlayer newMusicPlayer = new MusicPlayer(musicFolder);
                        musicPlayer.shutdown();
                        newMusicPlayer.start();
                        musicPlayer = newMusicPlayer;
                    } else
                        Logger.getLogger(this, "Folder not exists").rawError();
                } else if (!musicPlayer.isAlive())
                    musicPlayer.start();
                if (musicPlayer.getCurrent() != null) {
                    showSongInfo(musicPlayer.getCurrent());
                }
                break;
            case ConsoleOrder.ISSTARTED:
                Logger.getLogger(this, isPlayerOn() ? "Is playing" : "Is not playing").rawWarning();
                break;

            case ConsoleOrder.PLAY:
                if (isPlayerOn())
                    if (cmd.hasOptions()) {
                        Number playIndex = cmd.getOptionAsNumber(0);
                        if (playIndex != null &&
                                playIndex.intValue() > 0 && playIndex.intValue() <= musicPlayer.getSongsCount())
                            musicPlayer.play(playIndex.intValue() - 1);
                        showSongInfo(musicPlayer.getCurrent());
                    } else
                        musicPlayer.play();
                break;

            case ConsoleOrder.PAUSE:
                if (isPlayerOn())
                    musicPlayer.pause();
                break;

            case ConsoleOrder.STOP:
                if (isPlayerOn()) {
                    try {
                        musicPlayer.stopTrack();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case ConsoleOrder.RESUME:
                if (isPlayerOn())
                    musicPlayer.resumeTrack();
                break;

            case ConsoleOrder.NEXT:
                if (isPlayerOn())
                    if (cmd.hasOptions()) {
                        Number jumps = cmd.getOptionAsNumber(0);
                        if (jumps == null)
                            Logger.getLogger(this, "Jump value incorrect").rawError();
                        else
                            musicPlayer.jumpTrack(jumps.intValue(), SeekOption.NEXT);
                    } else
                        musicPlayer.playNext();
                showSongInfo(musicPlayer.getCurrent());
                break;

            case ConsoleOrder.PREV:
                if (isPlayerOn()) {
                    if (cmd.hasOptions()) {
                        Number jumps = cmd.getOptionAsNumber(0);
                        if (jumps == null)
                            Logger.getLogger(this, "Jump value incorrect").rawError();
                        else
                            musicPlayer.jumpTrack(jumps.intValue(), SeekOption.PREV);
                    } else
                        musicPlayer.playPrevious();
                    showSongInfo(musicPlayer.getCurrent());
                }
                break;

            case ConsoleOrder.MUTE:
                if (isPlayerOn())
                    musicPlayer.mute();
                break;

            case ConsoleOrder.UNMUTE:
                if (isPlayerOn())
                    musicPlayer.unMute();
                break;

            case ConsoleOrder.LIST1:
            case ConsoleOrder.LIST2:
                if (musicPlayer != null)
                    printTracks();
                break;

            case ConsoleOrder.LISTCURRENTFOLDER:
                if (isPlayerOn())
                    printFolderTracks();
                break;

            case ConsoleOrder.LISTFOLDERS:
                if (isPlayerOn())
                    printFolders();
                break;

            case ConsoleOrder.LISTDETAILED:
                if (isPlayerOn())
                    printDetailedTracks();
                break;

            case ConsoleOrder.GETGAIN:
                if (musicPlayer != null)
                    Logger.getLogger(this, "Player Volume(0-100): " + musicPlayer.getGain()).rawWarning();
                break;

            case ConsoleOrder.SETGAIN:
                if (musicPlayer != null)
                    if (cmd.hasOptions()) {
                        Number volume = cmd.getOptionAsNumber(0);
                        if (volume == null)
                            Logger.getLogger(this, "Volume value incorrect").rawError();
                        else {
                            musicPlayer.setGain(volume.floatValue());
                            Logger.getLogger(this, "Volume value changed").rawWarning();
                        }
                    }
                break;
            case ConsoleOrder.GETSYSVOL:
                if (musicPlayer != null)
                    Logger.getLogger(this, "Player Volume(0-100): " + musicPlayer.getSystemVolume()).rawWarning();
                break;

            case ConsoleOrder.SETSYSVOL:
                if (musicPlayer != null)
                    if (cmd.hasOptions()) {
                        Number volume = cmd.getOptionAsNumber(0);
                        if (volume == null)
                            Logger.getLogger(this, "Volume value incorrect").rawError();
                        else {
                            musicPlayer.setSystemVolume(volume.floatValue());
                            Logger.getLogger(this, "Volume value changed").rawWarning();
                        }
                    }
                break;

            case ConsoleOrder.SHUTDOWN:
                if (isPlayerOn()) {
                    musicPlayer.shutdown();
                    musicPlayer = null;
                }
                break;

            case ConsoleOrder.EXIT:
            case ConsoleOrder.QUIT:
                if (isPlayerOn()) {
                    musicPlayer.shutdown();
                    musicPlayer = null;
                }
                on = false;
                break;

            case ConsoleOrder.SEEK:
                if (isPlayerOn())
                    if (cmd.hasOptions()) {
                        Number seekSec = cmd.getOptionAsNumber(0);
                        if (seekSec == null)
                            Logger.getLogger(this, "Seek value incorrect").rawError();
                        else {
                            musicPlayer.seek(seekSec.doubleValue());
                            //Logger.getLogger(this, "Seeked").rawWarning();
                        }
                    }
                break;
            case ConsoleOrder.SEEKFLD:
                if (isPlayerOn()) {
                    if (cmd.hasOptions()) {
                        String optionParam = cmd.getOptionAt(0);
                        SeekOption option = optionParam.equals("next") ? SeekOption.NEXT
                                : (optionParam.equals("prev") ? SeekOption.PREV : null);
                        Number jumps = cmd.getOptionAsNumber(1);

                        if (cmd.getOptionsCount() > 1 && jumps == null)
                            Logger.getLogger(this, "Seek value incorrect").rawError();
                        else {
                            if (jumps == null && option == null)
                                Logger.getLogger(this, "Option value incorrect").rawError();
                            else if (jumps == null) {
                                musicPlayer.seekFolder(option);
                            }
                            else if (jumps.intValue() < 0)
                                Logger.getLogger(this, "Jumps value incorrect").rawError();
                            else {
                                musicPlayer.seekFolder(option, jumps.intValue());
                                //Logger.getLogger(this, "Seeked").rawWarning();
                            }
                        }

                    } else
                        musicPlayer.seekFolder(SeekOption.NEXT);
                    showSongInfo(musicPlayer.getCurrent());
                }
                break;

            case ConsoleOrder.RELOAD:
                if (musicPlayer != null)
                    musicPlayer.reloadTracks();
                break;
            case ConsoleOrder.GOTOSEC:
                if (musicPlayer != null)
                    if (cmd.hasOptions()) {
                        Number gotoSec = cmd.getOptionAsNumber(0);
                        if (gotoSec == null)
                            Logger.getLogger(this, "Go to value incorrect").rawError();
                        else
                            musicPlayer.gotoSecond(gotoSec.doubleValue());
                    }
                break;
            case ConsoleOrder.SOUNDCOUNT:
                System.out.println("in soundcount option");
                int count = musicPlayer.getSongsCount();
                System.out.println("SoundCount: "+count);
                if (musicPlayer != null)
                    Logger.getLogger(this, count).info();
                break;
            case ConsoleOrder.DURATION:
                if (musicPlayer != null) {
                    Logger.getLogger(this, musicPlayer.getCurrent().getFormattedDuration()).rawInfo();
                }
                break;
            case ConsoleOrder.GETCOVER:
                current = musicPlayer.getCurrent();
                if (current == null)
                    Logger.getLogger(this, "Current track unavailable").rawError();
                else if (!current.hasCover())
                    Logger.getLogger(this, "Current song don't have cover").rawError();
                else if (cmd.hasOptions()) {
                    File folderPath = new File(cmd.getOptionAt(0));
                    if (!folderPath.exists())
                        folderPath = musicPlayer.getRootFolder();
                    File fileCover = new File(folderPath, "cover-" + current.getTitle() + ".png");
                    fileCover.createNewFile();
                    Files.write(fileCover.toPath(), current.getCoverData(), WRITE);
                    Logger.getLogger(this, "Created cover with name " + fileCover.getName()).rawWarning();
                } else {
                    Logger.getLogger(this, "Cover path not defined").rawError();
                }
                break;

            case ConsoleOrder.GETINFO:
                if (musicPlayer.hasSounds() && musicPlayer.getCurrent() != null) {
                    showSongInfo(musicPlayer.getCurrent());
                }
                else {
                    Logger.getLogger(this, "No song available").rawWarning();
                }
                break;

            case ConsoleOrder.GETPROGRESS:
                current = musicPlayer.getCurrent();
                if (current == null)
                    Logger.getLogger(this, "Current track unavailable").rawError();
                else {
                    final String formattedProgress = current.getFormattedProgress();;
                    final String formattedDuration = current.getFormattedDuration();
                    Logger.getLogger(this, formattedProgress+"/"+formattedDuration).rawWarning();
                }
                break;

            case ConsoleOrder.CLEAR1:
            case ConsoleOrder.CLEAR2:
                clearConsole();
                break;

            case ConsoleOrder.FORMAT:
                current = musicPlayer.getCurrent();
                if (current == null)
                    Logger.getLogger(this, "Current track unavailable").rawError();
                else {
                    String className = current.getClass().getSimpleName();
                    Logger.getLogger(
                            this,
                            className.substring(0, className.length() - 5).toLowerCase()).rawWarning();

                }
                break;

            case ConsoleOrder.TITLE:
                current = musicPlayer.getCurrent();
                if (current == null)
                    Logger.getLogger(this, "Current track unavailable").rawError();
                else {
                    Logger.getLogger(this, current.getTitle()).rawWarning();

                }
                break;

            case ConsoleOrder.NAME:
                current = musicPlayer.getCurrent();
                if (current == null)
                    Logger.getLogger(this, "Current track unavailable").rawError();
                else {
                    Logger.getLogger(this, current.getDataSourceAsFile().getName()).rawWarning();

                }
                break;

            case ConsoleOrder.HELP1:
            case ConsoleOrder.HELP2:
                printHelp();
                break;

            case ConsoleOrder.SYSTEM1:
            case ConsoleOrder.SYSTEM2:
                if (cmd.hasOptions())
                    execSysCommand(cmd.getOptionsAsString());
                break;

            case ConsoleOrder.SHOW_NEXT:
                if (isPlayerOn()) {
                    Track next = (Track) musicPlayer.getNext();
                    System.out.println(TrackUtil.getSongInfo(next));
                }
                break;

            case ConsoleOrder.SHOW_PREV:
                if (isPlayerOn()) {
                    Track prev = (Track) musicPlayer.getPrevious();
                    System.out.println(TrackUtil.getSongInfo(prev));
                }
                break;

            case ConsoleOrder.PLAY_FOLDER:
                if (isPlayerOn()) {
                    if (cmd.hasOptions()) {
                        final Number fldIndex = cmd.getOptionAsNumber(0);
                        if (fldIndex != null && fldIndex.intValue() > 0) {
                            musicPlayer.playFolder(fldIndex.intValue()-1);
                            showSongInfo(musicPlayer.getCurrent());
                        }
                    }
                }
                break;

            case ConsoleOrder.LOAD:
                if (cmd.hasOptions()) {
                    File folder = new File(cmd.getOptionAt(0));
                    if (folder.exists()) {
                        if (folder.isDirectory()) {
                            if (folder.list() == null) {
                                Logger.getLogger(this, "Folder is empty").rawError();
                            }
                            else {
                                MusicPlayer newMusicPlayer = new MusicPlayer(folder);
                                newMusicPlayer.start();
                                if (isPlayerOn()) {
                                    musicPlayer.shutdown();
                                }
                                musicPlayer = newMusicPlayer;
                            }
                        }
                        else {
                            Logger.getLogger(this, "Path not be a directory").rawError();
                        }
                    }
                    else {
                        Logger.getLogger(this, "Folder not exists").rawError();
                    }
                }
                break;

            case ConsoleOrder.LIST_ARTISTS:
                if (musicPlayer.isAlive() && musicPlayer.hasSounds()) {
                    List<Artist> listArtists = musicPlayer.getArtists();
                    for(Artist artist : listArtists) {
                        Logger.getLogger(this, artist.getName()).rawInfo();
                    }
                    Logger.getLogger(this, "------------------------------").rawInfo();
                    Logger.getLogger(this, "Total: "+listArtists.size()).rawInfo();
                }
                else {
                    Logger.getLogger(this, "The player is not active").rawWarning();
                }
                break;

            case ConsoleOrder.LIST_ALBUMS:
                if (musicPlayer.isAlive() && musicPlayer.hasSounds()) {
                    List<Album> listAlbums = musicPlayer.getAlbums();
                    for(Album album : listAlbums) {
                        Logger.getLogger(this, album.getName()).rawInfo();
                    }
                    Logger.getLogger(this, "------------------------------").rawInfo();
                    Logger.getLogger(this, "Total: "+listAlbums.size()).rawInfo();
                }
                else {
                    Logger.getLogger(this, "The player is not active").rawWarning();
                }
                break;

            default:
                Logger.getLogger(this, "Comando desconocido, inserte el comando \"h\" o \"help\"\n" +
                        "para desplegar el menú de ayuda.").rawWarning();
                break;
        }

        return consoleExecution;
    }
}
