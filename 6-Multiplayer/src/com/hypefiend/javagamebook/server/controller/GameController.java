package com.hypefiend.javagamebook.server.controller;

import com.hypefiend.javagamebook.common.*;
import com.hypefiend.javagamebook.server.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * GameController.java
 *
 * Base class for all server-side logic implementations.
 * Extends from Wrap to provide a backing thread pool 
 * and incoming EventQueue
 *
 * @author <a href="mailto:bret@hypefiend.com">bret barker</a>
 * @version 1.0
 */
public abstract class GameController extends Wrap {

    /** reference to the GameServer */
    protected GameServer gameServer;

    /**
     * GameServer will call this init method immediately after construction.
     * It is final so that this initialization does not got overridden by subclasses.
     * Initialization for subclasses is done in the initController() method below.
     */
    public final void init(GameServer s, GameConfig gc) {
	this.gameServer = s;

	// todo: get the preferred number of workers from the GameConfig
	//	int nw = gc.getInt("NUM_WORKERS", 5);

	// init the Wrap first
	initWrap(Globals.DEFAULT_CONTROLLER_WORKERS);
	// now call the subclasses' init
	initController(gc);
    }

    /**
     * utility method for sending events
     */
    protected void sendEvent(GameEvent e, Player p) {
	e.setPlayerId(p.getPlayerId());
	gameServer.writeEvent(e);
    }

    /** 
     * utility method for sending events to multiple players
     */
    protected synchronized void sendBroadcastEvent(GameEvent e, Collection players) {
	Iterator i = players.iterator();
	String[] recipients = new String[players.size()];
	int j=0;
	while(i.hasNext()) {
	    Player p = (Player) i.next();
	    if (!(p.getPlayerId().equals(e.getPlayerId()))) 
		recipients[j++] = p.getPlayerId();
	}
	e.setRecipients(recipients);
	gameServer.writeEvent(e);
    }

    /** 
     * GameController subclasses should implement initController 
     * in order to do any initialization they require.
     */
    protected abstract void initController(GameConfig gc);

    /**
     * subclasses must implement to provide their GameName
     */
    public abstract String getGameName();
    
    /** 
     * factory method for fetching Player objects
     */
    public abstract Player createPlayer();

    /** 
     * factory method for fetching GameEvent objects
     */
    public abstract GameEvent createGameEvent();

}



