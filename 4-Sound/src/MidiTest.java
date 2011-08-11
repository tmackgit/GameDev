import javax.sound.midi.*;

import com.brackeen.javagamebook.sound.MidiPlayer;

/**
    An example that plays a Midi sequence. First, the sequence
    is played once with track 1 turned off. Then the sequence is
    played once with track 1 turned on. Track 1 is the drum track
    in the example midi file.
*/
public class MidiTest implements MetaEventListener {

    // The drum track in the example Midi file
    private static final int DRUM_TRACK = 1;

    public static void main(String[] args) {
        new MidiTest().run();
    }

    private MidiPlayer player;

    public void run() {

        player = new MidiPlayer();

        // load a sequence
        Sequence sequence =
            player.getSequence("../sounds/music.midi");

        // play the sequence
        player.play(sequence, true);

        // turn off the drums
        System.out.println("Playing (without drums)...");
        Sequencer sequencer = player.getSequencer();
        sequencer.setTrackMute(DRUM_TRACK, true);
        sequencer.addMetaEventListener(this);
    }


    /**
        This method is called by the sound system when a meta
        event occurs. In this case, when the end-of-track meta
        event is received, the drum track is turned on.
    */
    public void meta(MetaMessage event) {
        if (event.getType() == MidiPlayer.END_OF_TRACK_MESSAGE) {
            Sequencer sequencer = player.getSequencer();
            if (sequencer.getTrackMute(DRUM_TRACK)) {
                // turn on the drum track
                System.out.println("Turning on drums...");
                sequencer.setTrackMute(DRUM_TRACK, false);
            }
            else {
                // close the sequencer and exit
                System.out.println("Exiting...");
                player.close();
                System.exit(0);
            }
        }
    }

}
