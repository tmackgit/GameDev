import java.io.*;

import com.brackeen.javagamebook.sound.*;

/**
    An example of playing a sound with an echo filter.
    @see EchoFilter
    @see SimpleSoundPlayer
*/
public class EchoFilterTest {

    public static void main(String[] args) {

        // load the sound
        SimpleSoundPlayer sound =
            new SimpleSoundPlayer("../sounds/voice.wav");

        // create the sound stream
        InputStream is =
            new ByteArrayInputStream(sound.getSamples());

        // create an echo with a 11025-sample buffer
        // (1/4 sec for 44100Hz sound) and a 60% decay
        EchoFilter filter = new EchoFilter(11025, .6f);

        // create the filtered sound stream
        is = new FilteredSoundStream(is, filter);

        // play the sound
        sound.play(is);

        // due to bug in Java Sound, explicitly exit the VM.
        System.exit(0);
    }

}
