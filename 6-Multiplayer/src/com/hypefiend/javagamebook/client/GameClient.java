package com.hypefiend.javagamebook.client;

import com.hypefiend.javagamebook.common.*;

import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.*;
import java.io.*;

import org.apache.log4j.*;

/**
 * GameClient.java
 *
 *
 * @author <a href="mailto:bret@hypefiend.com">bret barker</a>
 * @version 1.0
 */
public abstract class GameClient extends Thread{
    /** log4j logger */
    protected static Logger log = Logger.getLogger("GameClient");

    /** address of server */
    protected InetAddress serverAddress;
    /** connection to server */
    protected SocketChannel channel;

    /** queue for incoming events */
    protected EventQueue inQueue;
    /** queue for outoging events */
    protected EventQueue outQueue;
    
    /** reference to NIOEventReader that reads events from the server */
    protected NIOEventReader netReader;

    /** buffer for outgoing events */
    protected ByteBuffer writeBuffer;

    /** id of our player */
    protected String playerId;
    /** id of our current opponent */
    protected String opponentId;

    /** are we playing right now? */
    protected boolean inGame = false;

    /** still running? */
    protected boolean running = true;

    /** 
     * do some initialization
     */
    public void init(String args[]) {	
	inQueue = new EventQueue("GameClient-in");
	outQueue = new EventQueue("GameClient-out");
	writeBuffer = ByteBuffer.allocate(Globals.MAX_EVENT_SIZE );

	try {
	    serverAddress = InetAddress.getByName(args[0]);
	}
	catch (UnknownHostException uhe) {
	    log.error("unknown host: " + args[0]);
	    System.exit(1);
	}
	this.playerId = args[1];

	// connect to the server
	if (!connect()) 
	    System.exit(1);
	
	// start our net reader
	netReader = new NIOEventReader(this, channel, inQueue);
	netReader.start();
	
    }

    public void run() {
	// now login
	login();

	// wait for LOGIN_ACK (hack)
	threadSleep(200L);

	// main loop
	while(running) {
	    processIncomingEvents();
	    writeOutgoingEvents();

	    // take a quick nap so we don't spin too hard 
	    //when nothing is happening
	    threadSleep(50);
	}
    }


    /** 
     * subclasses must implement to provide their GameName 
     */
    public abstract String getGameName();

    /**
     * subclasses must implement this factory method 
     */
    public abstract GameEvent createGameEvent();

    /**
     * and this one to create a game specific login event
     */
    public abstract GameEvent createLoginEvent();
    
    /**
     * and this one to create a game specific disconnect event
     */
    public abstract GameEvent createDisconnectEvent(String reason);
    
    /**
     * handle incoming GameEvents from the EventQueue
     */
    protected abstract void processIncomingEvents();

    /** 
     * write all events waiting in the outQueue
     */
    private void writeOutgoingEvents() {
	GameEvent outEvent;
	while (outQueue.size() > 0) {
	    try {
		outEvent = outQueue.deQueue();
		writeEvent(outEvent);
	    }
	    catch (InterruptedException ie) {}
	}	
    }
    
    /**
     * connect to the server
     */
    protected boolean connect() {
	log.info("connect()");
	try {
	    // open the socket channel
	    channel = SocketChannel.open(new InetSocketAddress(serverAddress, Globals.PORT));
	    channel.configureBlocking(false);

 	    // we don't like Nagle's algorithm
	    channel.socket().setTcpNoDelay(true);
	    return true;
	}
	catch (ConnectException ce) {
	    log.error("Connect Exception: " + ce.getMessage());
	    return false;
	}
	catch (Exception e) {
	    log.error("Exception while connecting", e);
	    return false;
	}
    }

    /**
     * send the login event
     */
    protected void login() {
	GameEvent e = createLoginEvent();
	e.setGameName(getGameName());
	e.setPlayerId(playerId);
	writeEvent(e);
    }

    /**
     * shutdown the client
     * stop our readers and close the channel
     */
    protected void shutdown() {
	running = false;
	netReader.shutdown();
	//	consoleReader.shutdown();
	try {
	    channel.close();
	}
	catch (IOException ioe) {
	    log.error("exception while closing channel", ioe);
	}
    }    

    /** 
     * send an event to the server 
     */
    protected void writeEvent(GameEvent ge) {
	// set the gamename and player id
	ge.setGameName(getGameName());
	ge.setPlayerId(playerId);

	NIOUtils.prepBuffer(ge, writeBuffer);
	NIOUtils.channelWrite(channel, writeBuffer);
    }

    /** 
     * utility method to call Thread.sleep()
     */
    private void threadSleep(long time) {
	try { 
	    Thread.sleep(time); 
	} 
	catch(InterruptedException e) {}
    }

    public void stdOut(String str) {
	if ((str != null) && !str.equals(""))
	    System.out.println("\n" + str);
	if (inGame)
	    System.out.print( playerId + " vs. " + opponentId + " > " );
	else
	    System.out.print( playerId + " > " );

    }   
    
    public void stdErr(String str) {
	System.err.println("\n" + str);
    }
    
    public void setOpponent(String opp) {
	opponentId = opp;
    }
}    

