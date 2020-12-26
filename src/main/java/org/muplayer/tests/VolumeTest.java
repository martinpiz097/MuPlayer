package org.muplayer.tests;

import org.aucom.sound.AudioQuality;
import org.muplayer.audio.Player;
import org.muplayer.system.AudioUtil;
import org.muplayer.system.LineUtil;

import javax.sound.sampled.*;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Scanner;

public class VolumeTest {
    static final Mixer.Info[] mixersInfo = AudioSystem.getMixerInfo();
    static Scanner scan = new Scanner(System.in);
    static final AudioFormat format = AudioQuality.HIGH;

    public static void main(String[] args) throws Exception {

        System.out.println(new Player("/home/martin/Escritorio/").getInfo().toString());

        System.out.println(LineUtil.getMasterOutputVolume());
        Line masterLine = LineUtil.getMasterOutputLine();
        masterLine.open();
        FloatControl volume = LineUtil.getVolumeControl(masterLine);

        float vol;

        while (true) {
            vol = scan.nextFloat();
            if (vol > 100)
                vol = 100f;
            else if (vol < 0)
                vol = 0f;
            volume.setValue(AudioUtil.convertVolRangeToLineRange(vol, volume));

        }


        //getPortMixer();
        //adjustRecordingVolume(new Port.Info(Port.class, "Capture", true));
        //Mixer portMixer = getPortMixer();
        //System.out.println(portMixer.toString());
        /*Mixer mixer = chooseMixer();
        SourceDataLine speaker =
                (SourceDataLine) chooseLine(mixer, SourceDataLine.class);
        speaker.open();

        FloatControl control = (FloatControl) speaker.getControl(FloatControl.Type.MASTER_GAIN);

        control.setValue(AudioUtil.convertVolRangeToLineRange(50));*/

    }

    private static Port getPortLine(Mixer portMixer) {
        Port port = null;
        Line[] lines = portMixer.getSourceLines();

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].getClass().equals(Port.class))
                port = (Port) lines[i];
        }

        if (port == null)
            lines = portMixer.getTargetLines();

            for (int i = 0; i < lines.length; i++) {
                if (lines[i].getClass().equals(Port.class))
                    port = (Port) lines[i];
        }

        return port;
    }

    private static void setCtrl(
            Control ctl) {
        if(ctl.getType().toString(
        ).equals("Select")) {
            ((BooleanControl)
                    ctl).setValue(true);
        }
        if(ctl.getType().toString(
        ).equals("Volume")) {
            FloatControl vol =
                    (FloatControl) ctl;
            /*float setVal = vol.getMinimum()
                    + (vol.getMaximum()
                    - vol.getMinimum())
                    * 50;*/
            vol.setValue(0);
        }
    }

    private static void setRecControlValue(Port inPort)
            throws Exception {
        inPort.open();
        Control [] controls =
                inPort.getControls();
        for(int i=0; i<controls.length; i++) {
            if(controls[i] instanceof
                    CompoundControl) {
                Control[] members =
                        ((CompoundControl)
                                controls[i]).
                                getMemberControls();
                for(int j=0; j<members.length; j++) {
                    setCtrl(members[j]);
                } // for int j
            } // if
            else
                setCtrl(controls[i]);
        } // for i
        inPort.close();
    }

    public static void adjustRecordingVolume(Port.Info portInfo)
            throws Exception {
        Port recPort = (Port)
                AudioSystem.getLine(
                        portInfo);
        setRecControlValue(recPort);
    }

    private static void showControls(
            Line inLine) throws Exception {
        // must open the line to get
        // at controls
        inLine.open();
        System.out.println("\t\tAvailable controls:");
        LinkedList<Control> ctrls =
                new LinkedList<>(
                        Arrays.asList(
                                inLine.getControls()));
        for (Control ctrl: ctrls) {
            System.out.println( "\t\t\t" +
                    ctrl.toString());
            if (ctrl instanceof
                    CompoundControl) {
                CompoundControl cc =
                        ((CompoundControl) ctrl);
                LinkedList<Control> ictrls =
                        new LinkedList<Control>(
                                Arrays.asList(
                                        cc.getMemberControls()));
                for(Control ictrl : ictrls)
                    System.out.println("\t\t\t\t" +
                            ictrl.toString());
            } // of if (ctrl instanceof)
        } // of for(Control ctrl)
        inLine.close();
    }

    public static void scanPortMixer(Mixer mixer) throws Exception {
        // found a Port Mixer
        Mixer.Info mixerInfo = mixer.getMixerInfo();

        System.out.println("Found mixer: " +
                mixerInfo.getName());
        System.out.println("\t" +
                mixerInfo.getDescription());
        System.out.println("Source Line Supported:");
        LinkedList<Line.Info> srcInfos =
                new LinkedList<>(
                        Arrays.asList(
                                mixer.getSourceLineInfo()));
        for (Line.Info srcInfo:
                srcInfos) {
            Port.Info pi =
                    (Port.Info) srcInfo;
            System.out.println("\t" + pi.getName() +
    			", " + (pi.isSource()?
    			"source" : "target"));
            showControls(mixer.getLine(
                    srcInfo));
        } // of for Line.Info
        System.out.println("Target Line Supported:");
        LinkedList<Line.Info>
                targetInfos =
                new LinkedList<>(
                        Arrays.asList(
                                mixer.getTargetLineInfo()));
        for (Line.Info targetInfo:
                targetInfos) {
            Port.Info pi =
                    (Port.Info) targetInfo;
            System.out.println("\t" + pi.getName()
                    + ", " +
                    (pi.isSource()?
    			"source" : "target"));
            showControls(mixer.getLine(
                    targetInfo));
        }
        System.out.println("---------------------------");
    } // of if
    // (mixer.isLineSupported)

    public static Mixer getPortMixer() throws Exception {
        Mixer portMixer = null;
        for (int i = 0; i < mixersInfo.length; i++) {
            portMixer = AudioSystem.getMixer(mixersInfo[i]);
            if (portMixer.isLineSupported(new Line.Info(Port.class))) {
                scanPortMixer(portMixer);
                break;
            }
        }
        return portMixer;
    }

    public static void showArrayContent(Object[] array) {
        for (int i = 0; i < array.length; i++)
            System.out.println(i+": "+array[i]);
        System.out.println("--------------");
        System.out.print("ID: ");
    }

    public static Line chooseLine(Mixer mixer, Class<? extends DataLine> lineClazz) throws LineUnavailableException {
        Line line = null;
        Line.Info[] linesInfo = lineClazz.equals(SourceDataLine.class) ?
                mixer.getSourceLineInfo() : mixer.getTargetLineInfo();

        int id;

        while (line == null) {
            showArrayContent(linesInfo);
            id = scan.nextInt();
            line = mixer.getLine(linesInfo[id]);
            /*line = lineClazz.equals(SourceDataLine.class) ?
                    DigitalAudioSystem.getSourceDataLine(format, mixer.getMixerInfo()) :
                    DigitalAudioSystem.getTargetDataLine(format, mixer.getMixerInfo())*/
        }
        return line;
    }

    /*public static void testCamera() throws IOException {
        Webcam webcam = Webcam.getDefault();
        Webcam.getWebcams().forEach(System.out::println);
        webcam.setViewSize(new Dimension(640, 480));
        webcam.open(true);
        ImageIO.write(webcam.getImage(), "PNG", new File("/home/martin/hello-world.png"));
        System.exit(0);
    }*/

    public static void showMixersInfo() {
        Arrays.stream(mixersInfo)
                .forEach(info -> showMixerInfo(AudioSystem.getMixer(info)));
    }

    public static void showMixerInfo(Mixer mixer) {
        boolean speakerSupport = mixer.isLineSupported(
                new Line.Info(SourceDataLine.class));
        boolean microSupport = mixer.isLineSupported(
                new Line.Info(TargetDataLine.class));

        boolean portSupport = mixer.isLineSupported(
                new Line.Info(Port.class));

        boolean clipSupport = mixer.isLineSupported(
                new Line.Info(Clip.class));

        StringBuilder sbMixer = new StringBuilder();

        sbMixer.append(mixer.getMixerInfo().getName())
                .append("\n\t; ").append(mixer.getMixerInfo().getDescription())
                .append("\n\t\t")
                ;

        if (speakerSupport)
            sbMixer.append("; ").append("speaker supported");

        if (microSupport)
            sbMixer.append("; ").append("micro supported");

        if (portSupport)
            sbMixer.append("; ").append("port supported");

        if (clipSupport)
            sbMixer.append("; ").append("clip supported");
        System.out.println(sbMixer.toString());
    }

    public static Mixer chooseMixer() {
        Mixer mixer = null;
        int mixerId;

        while (mixer == null) {
            showArrayContent(mixersInfo);
            mixerId = scan.nextInt();
            mixer = AudioSystem.getMixer(mixersInfo[mixerId]);
        }
        return mixer;
    }

    public static class SoundInfo {
        public SoundInfo() {}
        public static void main(String[]
                                        args)throws Exception {
            showMixers();
        }
        public static void showMixers() {
            LinkedList<Mixer.Info>
                    mixInfos =
                    new LinkedList<Mixer.Info>(
                            Arrays.asList(
                                    AudioSystem.getMixerInfo(
                                    )));
            Line.Info sourceDLInfo =
                    new Line.Info(
                            SourceDataLine.class);
            Line.Info targetDLInfo =
                    new Line.Info(
                            TargetDataLine.class);
            Line.Info clipInfo =
                    new Line.Info(Clip.class);
            Line.Info portInfo =
                    new Line.Info(Port.class);
            String support;
            for (Mixer.Info mixInfo:
                    mixInfos) {
                Mixer mixer =
                        AudioSystem.getMixer(
                                mixInfo);
                support = ", supports ";
                if (mixer.isLineSupported(
                        sourceDLInfo))
                    support +=
    				"SourceDataLine ";
                if (mixer.isLineSupported(
                        clipInfo))
                    support += "Clip ";
                if (mixer.isLineSupported(
                        targetDLInfo))
                    support +=
    				"TargetDataLine ";
                if (mixer.isLineSupported(
                        portInfo))
                    support += "Port ";
                System.out.println("Mixer: "
                + mixInfo.getName() +
                        support + ", " +
                        mixInfo.getDescription(
                        ));
            } } }
}

