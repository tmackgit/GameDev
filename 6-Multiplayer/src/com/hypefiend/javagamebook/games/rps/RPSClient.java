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
 * RPSClient.java
 *
 * @author <a href="mailto:bret@hypefiend.com">bret barker</a>
 * @version 1.0
 */
public class RPSClient extends GameClient {
    /** log4j logger */
    protected static Logger log = Logger.getLogger("RPSClient");

    /** reference to ConsoleEventReader that reads events from user input */
    protected RPSConsoleEventReader consoleReader;

    /**
     * main.
     */
    public static void main(String args[] ) {
	// setup log4j
	BasicConfigurator.configure();

	if (args.length < 2) {
	    System.out.println("usage: java com.hypefiend.javagamebook.games.rps.RPSClient <host> <player_name>\n");
	    System.exit(0);
	}

	// fire up the client
	RPSClient gc = new RPSClient();
	gc.init(args);
	gc.start();
    }

    /** 
     * call GameClient.init() 
     * and start our ConsoleEventReader
     */
    public void init(String args[]) {
	super.init(args);
	consoleReader = new RPSConsoleEventReader(this, inQueue, outQueue);
	consoleReader.start();
    }
	
    /** 
     * shutdown the client
     */
    protected void shutdown() {
	consoleReader.shutdown();
	super.shutdown();
    }

    /** 
     * handle incoming GameEvents from the EventQueue
     */
    protected void processIncomingEvents() {
	GameEvent inEvent;
	while (inQueue.size() > 0) {
	    try {
		inEvent = inQueue.deQueue();

		switch (inEvent.getType()) {
		case GameEventDefault.S_LOGIN_ACK_OK:
		    break;
		case GameEventDefault.SB_LOGIN:
		    stdOut( "login: " + inEvent.getMessage());
		    break;
		case GameEventDefault.SB_LOGOUT:
		    stdOut( "logout: " + inEvent.getMessage());
		    break;
		case GameEventDefault.SB_CHAT_MSG:
		    stdOut( inEvent.getPlayerId() + ": " + inEvent.getMessage());
		    break;
		case GameEventDefault.S_DISCONNECT:
		    stdErr( "disconnected from server: " + inEvent.getMessage());
		    shutdown();
		    break;
		case GameEventDefault.S_JOIN_GAME_ACK_OK:
		    stdOut( inEvent.getMessage());
		    inGame = true;
		    break;
		case GameEventDefault.S_JOIN_GAME_ACK_FAIL:
		    stdOut( inEvent.getMessage());
		    inGame = false;
		    break;
		case GameEventDefault.SB_PLAYER_QUIT:
		    stdOut( inEvent.getMessage());
		    inGame = false;
		    break;		    
		default:
		    stdOut( inEvent.getMessage());
		    break;
		}
	    }
	    catch (InterruptedException ie) {}
	}
    }

    /** 
     * return our GameName
     */
    public String getGameName() {
	return "RPS";
    }

    /** 
     * factory method to create GameEvents
     */
    public GameEvent createGameEvent() {
	return new GameEventDefault();
    }
    
    /** 
     * factory method to create login GameEvents
     */
    public GameEvent createLoginEvent() {
	return new GameEventDefault(GameEventDefault.C_LOGIN);
    }
    
    /** 
     * factory method to create disconnect GameEvents
     */
    public GameEvent createDisconnectEvent(String reason) {
	return new GameEventDefault(GameEventDefault.S_DISCONNECT, reason);
    }
    
}
