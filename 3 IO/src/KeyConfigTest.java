import java.awt.event.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.Border;


/**
    The KeyConfigTest class extends the MenuTest demo to add
    a dialog to configure the keyboard keys.
*/
public class KeyConfigTest extends MenuTest {

    public static void main(String[] args) {
        new KeyConfigTest().run();
    }

    private static final String INSTRUCTIONS =
        "<html>Click an action's input box to change it's keys." +
        "<br>An action can have at most three keys associated " +
        "with it.<br>Press Backspace to clear an action's keys.";

    private JPanel dialog;
    private JButton okButton;
    private List inputs;

    public void init() {
        super.init();

        inputs = new ArrayList();

        // create the list of GameActions and mapped keys
        JPanel configPanel = new JPanel(new GridLayout(5,2,2,2));
        addActionConfig(configPanel, moveLeft);
        addActionConfig(configPanel, moveRight);
        addActionConfig(configPanel, jump);
        addActionConfig(configPanel, pause);
        addActionConfig(configPanel, exit);

        // create the panel containing the OK button
        JPanel bottomPanel = new JPanel(new FlowLayout());
        okButton = new JButton("OK");
        okButton.setFocusable(false);
        okButton.addActionListener(this);
        bottomPanel.add(okButton);

        // create the panel containing the instructions.
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel(INSTRUCTIONS));

        // create the dialog border
        Border border =
            BorderFactory.createLineBorder(Color.black);

        // create the config dialog.
        dialog = new JPanel(new BorderLayout());
        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(configPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setBorder(border);
        dialog.setVisible(false);
        dialog.setSize(dialog.getPreferredSize());

        // center the dialog
        dialog.setLocation(
            (screen.getWidth() - dialog.getWidth()) / 2,
            (screen.getHeight() - dialog.getHeight()) / 2);

        // add the dialog to the "modal dialog" layer of the
        // screen's layered pane.
        screen.getFullScreenWindow().getLayeredPane().add(dialog,
            JLayeredPane.MODAL_LAYER);
    }


    /**
        Adds a label containing the name of the GameAction and an
        InputComponent used for changing the mapped keys.
    */
    private void addActionConfig(JPanel configPanel,
        GameAction action)
    {
        JLabel label = new JLabel(action.getName(), JLabel.RIGHT);
        InputComponent input = new InputComponent(action);
        configPanel.add(label);
        configPanel.add(input);
        inputs.add(input);
    }


    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (e.getSource() == okButton) {
            // hides the config dialog
            configAction.tap();
        }
    }


    public void checkSystemInput() {
        super.checkSystemInput();
        if (configAction.isPressed()) {
            // hide or show the config dialog
            boolean show = !dialog.isVisible();
            dialog.setVisible(show);
            setPaused(show);
        }
    }


    /**
        Resets the text displayed in each InputComponent, which
        is the names of the mapped keys.
    */
    private void resetInputs() {
        for (int i=0; i<inputs.size(); i++) {
            ((InputComponent)inputs.get(i)).setText();
        }
    }


    /**
        The InputComponent class displays the keys mapped to a
        particular action and allows the user to change the mapped
        keys. The user selects an InputComponent by clicking it,
        then can press any key or mouse button (including the
        mouse wheel) to change the mapped value.
    */
    class InputComponent extends JTextField  {

        private GameAction action;

        /**
            Creates a new InputComponent for the specified
            GameAction.
        */
        public InputComponent(GameAction action) {
            this.action = action;
            setText();
            enableEvents(KeyEvent.KEY_EVENT_MASK |
                MouseEvent.MOUSE_EVENT_MASK |
                MouseEvent.MOUSE_MOTION_EVENT_MASK |
                MouseEvent.MOUSE_WHEEL_EVENT_MASK);
        }


        /**
            Sets the displayed text of this InputComponent to the
            names of the mapped keys.
        */
        private void setText() {
            String text = "";
            List list = inputManager.getMaps(action);
            if (list.size() > 0) {
                for (int i=0; i<list.size(); i++) {
                    text+=(String)list.get(i) + ", ";
                }
                // remove the last comma
                text = text.substring(0, text.length() - 2);
            }

            // make sure we don't get deadlock
            synchronized (getTreeLock()) {
                setText(text);
            }

        }


        /**
            Maps the GameAction for this InputComponent to the
            specified key or mouse action.
        */
        private void mapGameAction(int code, boolean isMouseMap) {
            if (inputManager.getMaps(action).size() >= 3) {
                inputManager.clearMap(action);
            }
            if (isMouseMap) {
                inputManager.mapToMouse(action, code);
            }
            else {
                inputManager.mapToKey(action, code);
            }
            resetInputs();
            screen.getFullScreenWindow().requestFocus();
        }


        // alternative way to intercept key events
        protected void processKeyEvent(KeyEvent e) {
            if (e.getID() == e.KEY_PRESSED) {
                // if backspace is pressed, clear the map
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE &&
                    inputManager.getMaps(action).size() > 0)
                {
                    inputManager.clearMap(action);
                    setText("");
                    screen.getFullScreenWindow().requestFocus();
                }
                else {
                    mapGameAction(e.getKeyCode(), false);
                }
            }
            e.consume();
        }


        // alternative way to intercept mouse events
        protected void processMouseEvent(MouseEvent e) {
            if (e.getID() == e.MOUSE_PRESSED) {
                if (hasFocus()) {
                    int code = InputManager.getMouseButtonCode(e);
                    mapGameAction(code, true);
                }
                else {
                    requestFocus();
                }
            }
            e.consume();
        }


        // alternative way to intercept mouse events
        protected void processMouseMotionEvent(MouseEvent e) {
            e.consume();
        }


        // alternative way to intercept mouse events
        protected void processMouseWheelEvent(MouseWheelEvent e) {
            if (hasFocus()) {
                int code = InputManager.MOUSE_WHEEL_DOWN;
                if (e.getWheelRotation() < 0) {
                    code = InputManager.MOUSE_WHEEL_UP;
                }
                mapGameAction(code, true);
            }
            e.consume();
        }
    }
}
