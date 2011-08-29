package com.hypefiend.javagamebook.server;

import com.hypefiend.javagamebook.common.*;
import com.hypefiend.javagamebook.server.controller.*;

import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.net.*;
import java.io.*;
import org.apache.log4j.*;

/**
 * GameServer.java
 *
 * The heart of the framework, GameServer accepts
 * incoming client connections and hands them off to 
 * the SelectAndRead class.
 * GameServer also keeps track of the connected players
 * and the GameControllers.
 *
 * @author <a href="mailto:bret@hypefiend.com">bret barker</a>
 * @version 1.0
 */
public class GameServer extends Thread {
    /** log4j Logger */
    private Logger log = Logger.getLogger("GameServer");

    /** ServerSocketChannel for accepting client connections */
    private ServerSocketChannel sSockChan;

    /** selector for multiplexing ServerSocketChannels */
    private Selector selector;

    /** GameControllers keyed by GameName */
    private Hashtable gameControllers;

    /** classname prefix used for dynamically loading GameControllers */
    private static final String CONTROLLER_CLASS_PREFIX = 
	"com.hypefiend.javagamebook.server.controller.";

    /** players keyed by playerId */
    private static Hashtable playersByPlayerId;
    
    /** players keyed by sessionId */
    private static Hashtable playersBySessionId;

    private boolean running;
    private SelectAndRead selectAndRead;
    private EventWriter eventWriter;

    private static long nextSessionId = 0;

    /**
     * main. 
     * setup log4j and fireup the GameServer
     */
    public static void main(String args[]) {
	BasicConfigurator.configure();
	GameServer gs = new GameServer();
	gs.start();
    }

    /**
     * constructor, just initialize our hashtables
     */
    public GameServer() {
	gameControllers = new Hashtable();
	playersByPlayerId = new Hashtable();
  	playersBySessionId = new Hashtable();
    }

    /**
     * init the GameServer, startup our workers, etc.
     */ 
    public void init() {
	log.info("GameServer initializing");

	loadGameControllers();
	initServerSocket();

	selectAndRead = new SelectAndRead(this);
	selectAndRead.start();

	eventWriter = new EventWriter(this, Globals.EVENT_WRITER_WORKERS); 
    }

    /**
     * GameServer specific initialization, bind to the server port,
     * setup the Selector, etc.
     */
    private void initServerSocket() {
	try {
	    // open a non-blocking server socket channel
	    sSockChan = ServerSocketChannel.open();
	    sSockChan.configureBlocking(false);

	    // bind to localhost on designated port
	    InetAddress addr = InetAddress.getLocalHost();
	    log.info("binding to address: " + addr.getHostAddress());
	    sSockChan.socket().bind(new InetSocketAddress(addr, Globals.PORT));
	    
	    // get a selector
	    selector = Selector.open();

	    // register the channel with the selector to handle accepts
	    SelectionKey acceptKey = sSockChan.register(selector, SelectionKey.OP_ACCEPT);
	}
	catch (Exception e) {
	    log.error("error initializing ServerSocket", e);
	    System.exit(1);
	}
    }

    /**
     * Here's the meat, loop over the select() call to 
     * accept socket connections and hand them off to SelectAndRead
     */
    public void run() {
	init();
	log.info("******** GameServer running ********");
	running = true;
	int numReady = 0;

	while (running) {
	    // note, since we only have one ServerSocket to listen to,
	    // we don't need a Selector here, but we set it up for 
	    // later additions such as listening on another port 
	    // for administrative uses.
	    try {
		// blocking select, will return when we get a new connection
		selector.select();
		
		// fetch the keys
		Set readyKeys = selector.selectedKeys();
		
		// run through the keys and process
		Iterator i = readyKeys.iterator();
		while (i.hasNext()) {
		    SelectionKey key = (SelectionKey) i.next();
		    i.remove();
		    
		    ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
		    SocketChannel clientChannel = ssChannel.accept();
		    
		    // add to the list in SelectAndRead for processing
		    selectAndRead.addNewClient(clientChannel);
		    log.info("got connection from: " + clientChannel.socket().getInetAddress());
		}		
	    }
	    catch (IOException ioe) {
		log.warn("error during serverSocket select(): " + ioe.getMessage());
	    }
	    catch (Exception e) {
		log.error("exception in run()", e);
	    }
	}
    }

    /** 
     * shutdown the GameServer
     */
    public void shutdown() {
	selector.wakeup();
    }

    /**
     * Return the next available sessionId
     */
    public synchronized String nextSessionId() {
	return "" + nextSessionId++;
    }

    /**
     * finds the GameController for a given GameName
     */
    public GameController getGameController(String gameName) {
	return getGameControllerByHash(gameName.hashCode());
    }

    /**
     * finds the GameController for a given GameName hash code
     */
    public GameController getGameControllerByHash(int gameNameHash) {
	GameController gc = (GameController) gameControllers.get("" + gameNameHash);
	if (gc == null) 
	    log.error("no gamecontroller for gameNameHash: " + gameNameHash);
	return gc;
    }

    /**
     *  Dynamically loads GameControllers
     */
    private void loadGameControllers() {
	log.info("loading GameControllers");

	// grab all class files in the same directory as GameController
	String baseClass = "com/hypefiend/javagamebook/server/controller/GameController.class";
	File f = new File( this.getClass( ).getClassLoader().getResource(baseClass).getPath());
	File[] files = f.getParentFile().listFiles( );

	if (files == null) {
	    log.error("error getting GameController directory");
	    return;
	}
     
	for( int i = 0; ( i < files.length); i++) {
	    String file = files[i].getName( );
	    if (file.indexOf( ".class") == -1)
		continue;
	    if (file.equals("GameController.class"))
		continue;

	    try {
		// grab the class
		String controllerClassName = CONTROLLER_CLASS_PREFIX + file.substring(0, file.indexOf(".class"));
		log.info("loading class: " + controllerClassName);

		Class cl = Class.forName(controllerClassName);
		
		// make sure it extends GameController
		if (!GameController.class.isAssignableFrom(cl)) {
		    log.warn("class file does not extend GameController: " + file);
		    continue;
		}
		
		// get an instance and initialize
		GameController gc = (GameController) cl.newInstance();
		String gameName = gc.getGameName();
		gc.init(this, getGameConfig(gameName));
		
		// add to our controllers hash
		gameControllers.put("" + gameName.hashCode(), gc);
		
		log.info("loaded controller for gameName: " + gameName + ", hash: " + gameName.hashCode());
	    } 
	    catch (Exception e) {
		log.error("Error instantiating GameController from file: " + file, e);
	    }
	}
    }


    /**
     * pass the event on to the EventWriter
     */
    public void writeEvent(GameEvent e) {
	eventWriter.handleEvent(e);
    }

    /**
     * returns the GameConfig object for the given gameName
     */
    public GameConfig getGameConfig(String gameName) {
	// todo: implement getGameConfig()
	return null;
    }
   
    /**
     * fetches the Player for a given playerId
     */
    public static Player getPlayerById( String id) {
	return (Player) playersByPlayerId.get(id);
    }

    /**
     * fetches the Player for a given sessionId
     */
    public static Player getPlayerBySessionId(String id) {
	return (Player) playersBySessionId.get(id);
    }

    /** 
     * add a player to our lists
     */
    public static void addPlayer(Player p) {
	playersByPlayerId.put(p.getPlayerId(), p);
	playersBySessionId.put(p.getSessionId(), p);
    }
    
    /**
     * remove a player from our lists
     */
    public static void removePlayer(Player p) {
	playersByPlayerId.remove(p.getPlayerId());
	playersBySessionId.remove(p.getPlayerId());
    }

}// GameServer
