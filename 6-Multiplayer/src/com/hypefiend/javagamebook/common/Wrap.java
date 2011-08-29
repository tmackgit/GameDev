package com.hypefiend.javagamebook.common;

import org.apache.log4j.Logger;

/**
 * Wrap.java
 * 
 * Wrap is a thread pool with an incoming BlockingQueue
 * of GameEvents
 *
 * @author <a href="mailto:bret@hypefiend.com">bret barker</a>
 * @version 1.0
 */
public abstract class Wrap implements Runnable, EventHandler {
    /** log4j logger */
    protected Logger log;

    /** milliseconds to sleep between processing runs */
    protected static final long WORKER_SLEEP_MILLIS = 10;

    /** incoming event queue */
    protected EventQueue eventQueue;

    /** are we running? **/
    protected boolean running = false;

    /** our pool of worker threads */
    private Thread workers[];

    /** number of idle workers */
    private int spareCount;
    private Object countLock = new Object();

    /** short Class name of the implementing class */
    private String shortname;

    /**
     * @param numWorkers number of worker threads to spawn
     */
    public final void initWrap(int numWorkers) {
	// setup the log4j Logger
	shortname = this.getClass().getName().substring(this.getClass().getName().lastIndexOf(".") + 1);
	log = Logger.getLogger(shortname);
	log.info("initWrap - " + shortname);

	eventQueue = new EventQueue(shortname + "-in");

	// spawn worker threads
	workers = new Thread[numWorkers];
	for (int i=0; i<numWorkers; i++) {
	    workers[i] = new Thread(this, shortname + "-" + (i+1));
	    workers[i].setDaemon(true);
	    workers[i].start();
	}
    }

    /**
     * shutdown the worker threads
     */
    public void shutdown () {
	running = false;
	if (workers != null) {
	    for (int i=0;i<workers.length;i++) {
		workers[i].interrupt();
	    }
	}
    }
    
    /**
     * queue the event for later processing by worker threads
     */
    public void handleEvent(GameEvent event) {
	eventQueue.enQueue(event);
    }

    /** 
     * retrieve events from the queue and process.
     */
    public void run() {
	GameEvent event;
	running = true;
	while (running) {
	    try {
		if ((event = eventQueue.deQueue()) != null) {
		    processEvent(event);
		}
	    }
	    catch (InterruptedException e) {
	    }
	}
    }

    /**
     * subclasses must implement to do their processing
     */
    protected abstract void processEvent(GameEvent event);

}
