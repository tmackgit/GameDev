import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

import com.brackeen.javagamebook.graphics.NullRepaintManager;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.test.GameCore;

/**
    The SoundManagerTest demonstrates the functionality of
    the SoundManager class. It provides the following demos:
    <ul>
    <li>Playing a Midi sequence.
    <li>Toggle a track of a playing Midi sequence.
    <li>Playing a sound.
    <li>Playing a Sound with an Echo filter.
    <li>Looping a sound.
    <li>Playing the maximum number of sounds at once.
    <li>Pausing all sounds.
    </ul>
    <p>This class wasn't listed in the book ;)
    @see SoundManager
    @see Sound
    @see SoundFilter
*/
public class SoundManagerTest extends GameCore
    implements ActionListener
{
    public static void main(String[] args) {
        new SoundManagerTest().run();
    }

    // uncompressed, 44100Hz, 16-bit, mono, signed, little-endian
    private static final AudioFormat PLAYBACK_FORMAT =
        new AudioFormat(44100, 16, 1, true, false);

    private static final int MANY_SOUNDS_COUNT =
        SoundManager.getMaxSimultaneousSounds(PLAYBACK_FORMAT);

    private static final int DRUM_TRACK = 1;

    private static final String EXIT = "Exit";
    private static final String PAUSE = "Pause";
    private static final String PLAY_MUSIC = "Play Music";
    private static final String MUSIC_DRUMS = "Toggle Drums";
    private static final String PLAY_SOUND = "Play Sound";
    private static final String PLAY_ECHO_SOUND = "Play Echoed Sound";
    private static final String PLAY_LOOPING_SOUND =
        "Play Looping Sound";
    private static final String PLAY_MANY_SOUNDS =
        "Play " + MANY_SOUNDS_COUNT + " Sounds";


    private SoundManager soundManager;
    private MidiPlayer midiPlayer;
    private Sequence music;
    private Sound boop;
    private Sound bzz;
    private InputStream lastloopingSound;

    public void init() {
        super.init();
        initSounds();
        initUI();
    }


    /**
        Loads sounds and music.
    */
    public void initSounds() {
        midiPlayer = new MidiPlayer();
        soundManager = new SoundManager(PLAYBACK_FORMAT);
        music = midiPlayer.getSequence("../sounds/music.midi");
        boop = soundManager.getSound("../sounds/boop.wav");
        bzz = soundManager.getSound("../sounds/fly-bzz.wav");
    }


    /**
        Creates the UI, which is a row of buttons.
    */
    public void initUI() {
        // make sure Swing components don't paint themselves
        NullRepaintManager.install();

        JFrame frame = super.screen.getFullScreenWindow();
        Container contentPane = frame.getContentPane();

        contentPane.setLayout(new FlowLayout());
        contentPane.add(createButton(PAUSE, true));
        contentPane.add(createButton(PLAY_MUSIC, true));
        contentPane.add(createButton(MUSIC_DRUMS, false));
        contentPane.add(createButton(PLAY_SOUND, false));
        contentPane.add(createButton(PLAY_ECHO_SOUND, false));
        contentPane.add(createButton(PLAY_LOOPING_SOUND, true));
        contentPane.add(createButton(PLAY_MANY_SOUNDS, false));
        contentPane.add(createButton(EXIT, false));

        // explicitly layout components (needed on some systems)
        frame.validate();
    }


    /**
        Draws all Swing components
    */
    public void draw(Graphics2D g) {
        JFrame frame = super.screen.getFullScreenWindow();
        frame.getLayeredPane().paintComponents(g);
    }


    /**
        Creates a button (either JButton or JToggleButton).
    */
    public AbstractButton createButton(String name,
        boolean canToggle)
    {
        AbstractButton button;

        if (canToggle) {
            button = new JToggleButton(name);
        }
        else {
            button = new JButton(name);
        }
        button.addActionListener(this);
        button.setIgnoreRepaint(true);
        button.setFocusable(false);

        return button;
    }


    /**
        Performs actions when a button is pressed.
    */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        AbstractButton button = (AbstractButton)e.getSource();
        if (command == EXIT) {
            midiPlayer.close();
            soundManager.close();
            stop();
        }
        else if (command == PAUSE) {
            // pause the sound
            soundManager.setPaused(button.isSelected());
            midiPlayer.setPaused(button.isSelected());
        }
        else if (command == PLAY_MUSIC) {
            // toggle music on or off
            if (button.isSelected()) {
                midiPlayer.play(music, true);
            }
            else {
                midiPlayer.stop();
            }
        }
        else if (command == MUSIC_DRUMS) {
            // toggle drums on or off
            Sequencer sequencer = midiPlayer.getSequencer();
            if (sequencer != null) {
                 boolean mute = sequencer.getTrackMute(DRUM_TRACK);
                 sequencer.setTrackMute(DRUM_TRACK, !mute);
            }
        }
        else if (command == PLAY_SOUND) {
            // play a normal sound
            soundManager.play(boop);
        }
        else if (command == PLAY_ECHO_SOUND) {
            // play a sound with an echo
            EchoFilter filter = new EchoFilter(11025, .6f);
            soundManager.play(boop, filter, false);
        }
        else if (command == PLAY_LOOPING_SOUND) {
            // play or stop the looping sound
            if (button.isSelected()) {
                lastloopingSound = soundManager.play(bzz, null, true);
            }
            else if (lastloopingSound != null) {
                try {
                    lastloopingSound.close();
                }
                catch (IOException ex) { }
                lastloopingSound = null;
            }
        }
        else if (command == PLAY_MANY_SOUNDS) {
            // play several sounds at once, to test the system
            for (int i=0; i<MANY_SOUNDS_COUNT; i++) {
                soundManager.play(boop);
            }
        }
    }

}
