import java.io.File;
import javax.sound.sampled.*;

/**
    An example of loading and playing a sound using a Clip.
    This complete class isn't in the book ;)
*/
public class ClipTest {

    public static void main(String[] args) throws Exception {

        // specify the sound to play
        // (assuming the sound can be played by the audio system)
        File soundFile = new File("../sounds/voice.wav");
        AudioInputStream sound =
            AudioSystem.getAudioInputStream(soundFile);

        // load the sound into memory (a Clip)
        DataLine.Info info = new DataLine.Info(Clip.class,
            sound.getFormat());
        Clip clip = (Clip)AudioSystem.getLine(info);
        clip.open(sound);

        // due to bug in Java Sound, explicitly exit the VM when
        // the sound has stopped.
        clip.addLineListener(new LineListener() {
            public void update(LineEvent event) {
                if (event.getType() == LineEvent.Type.STOP) {
                    event.getLine().close();
                    System.exit(0);
                }
            }
        });

        // play the sound clip
        clip.start();
    }
}
