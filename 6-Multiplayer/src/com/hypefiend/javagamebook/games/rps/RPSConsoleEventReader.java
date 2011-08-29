package com.hypefiend.javagamebook.games.rps;

import com.hypefiend.javagamebook.client.*;
import com.hypefiend.javagamebook.common.*;

import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.*;
import java.io.*;

import org.apache.log4j.*;

/**
 * RPSConsoleEventReader.java
 *
 * @author <a href="mailto:bret@hypefiend.com">bret barker</a>
 * @version 1.0
 */
public class RPSConsoleEventReader extends Thread {
    /** log4j logger */
    private Logger log = Logger.getLogger("RPSConsoleEventReader");

    /** reference to the gameClient */
    private GameClient gameClient;

    /** incoming event queue */
    private EventQueue inQueue;
    
    /** outgoing event queue */
    private EventQueue outQueue;

    /** still running? */
    private  boolean running;
    
    /** text displayed to newly connected client */
    private static final String WELCOME_TEXT = "\n\nWelcome to the RPS (Rock, Paper, Scissors) Multi-player Game\n\n";

    /** help text */
    private static final String HELP_TEXT = 
	"commands:\n" + 
	"'/quit'\t\t\t\t\t quit the application \n" + 
	"'/help'\t\t\t\t\t show this help \n" + 
	"'/players'\t\t\t\t list players online\n" + 
	"'/newgame <opponent name>'\t\t start a new game against opponent\n" +
	"'/move <(r)ock|(p)aper|(s)cissors>'\t enter your move \n" + 
	"'/endgame' \t\t\t\t end the game\n" + 
	"all other input is treated as a chat message\n";

    /** 
     * constructor.
     */
    public RPSConsoleEventReader(GameClient gc, EventQueue inQueue, EventQueue outQueue) {
	super("ConsoleEventReader");
	this.gameClient = gc;
	this.inQueue = inQueue;
	this.outQueue = outQueue;
    }

    public void run() {
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	System.out.println(WELCOME_TEXT + HELP_TEXT);
			
	running = true;
	while (running) {
	    try {
		String s = br.readLine();
		String tokens[] = tokenizeCmdString(s);
		if (tokens.length > 0) {
		    GameEvent event = parseInput(tokens, s);
		    if (event != null) {
			outQueue.enQueue(event);
		    }
		}
		else {
		    gameClient.stdOut(null);
		}
	    }
	    catch (Exception ioe) {
		log.error("exception while reading GameEvent", ioe);
	    }
	}
    }  

    public void shutdown() {
	running = false;
	interrupt();
    }

    private GameEvent parseInput(String tok[], String line) {
	tok[0] = tok[0].toLowerCase();

	GameEvent e = null;
	if (tok[0].equals("/quit")) {
	    e = new GameEventDefault(GameEventDefault.C_LOGOUT, "Bye Bye!");
	}
	else if (tok[0].equals("/help") || tok[0].equals("?")) {
	    gameClient.stdOut(HELP_TEXT);
	}
	else if (tok[0].equals("/players")) {
	    e = new GameEventDefault(GameEventDefault.C_GET_PLAYERS);
	}
	else if (tok[0].equals("/newgame")) {
	    if (tok.length < 2)
		badCmd(tok, "no opponent specified");
	    else {
		e = new GameEventDefault(GameEventDefault.C_JOIN_GAME, tok[1]);
		gameClient.setOpponent(tok[1]);
	    }
	}
	else if (tok[0].equals("/move")) {
	    if (tok.length > 2)
		badCmd(tok, "illegal move: too many parameters");
	    else if ( validMove(tok[1])) 
		e = new GameEventDefault(GameEventDefault.C_MOVE, tok[1]);
	}
	else if (tok[0].equals("/endgame")) {
	    e = new GameEventDefault(GameEventDefault.C_QUIT_GAME);
	}
	else {
	    e = new GameEventDefault(GameEventDefault.C_CHAT_MSG, line.trim());
	    gameClient.stdOut(null);
	}
	return e;
    }
    
    private boolean validMove(String m) {
	return (m.equals("rock") || m.equals("paper") || m.equals("scissors") ||
		m.equals("r") || m.equals("p") || m.equals("s"));
    }

    private void badCmd(String tok[], String mesg) {
	inQueue.enQueue(new GameEventDefault(GameEventDefault.C_CMD_ACK, mesg));
    }

    public static String[] tokenizeCmdString(String s) {
	StringTokenizer toke = new StringTokenizer(s, " ");
	int numTokes = toke.countTokens();
	
	String[] tokens = new String[numTokes];
	
	for (int i=0; toke.hasMoreTokens(); i++) {
	    tokens[i] = toke.nextToken();
	}
	return tokens;
    }
    
}


