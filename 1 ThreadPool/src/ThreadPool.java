
import java.util.LinkedList;

public class ThreadPool extends ThreadGroup {
	
	private boolean isAlive;
	private LinkedList taskQueue;
	private int threadID;
	private static int threadPoolID;
	
	public ThreadPool(int numThreads) {
		super("TheadPool-" + (threadPoolID++));
		setDaemon(true);
		isAlive=true;
		taskQueue = new LinkedList();
		for (int i=0; i<numThreads; i++) {
			new PooledThread().start();
		}
	}
	
	/** Requests a new task to run. This method returns immediately, and the taks executes
	 *  on the next available idle thread. 
	 * 
	 * @param task
	 */
	public synchronized void runTask(Runnable task) {
		if (!isAlive) {
			throw new IllegalStateException();
		}
		if (task!=null) {
			taskQueue.add(task);
			notify();
		}
	}
	
	/** 
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	protected synchronized Runnable getTask() throws InterruptedException {
		while  (taskQueue.size() == 0) {
			if (!isAlive) {
				return null;
			}
			wait();
		}
		return (Runnable)taskQueue.removeFirst();
	}
	
	/** closes this ThreadPool and returns immediately. All threads are stopped
	 * and any waiting tasks are not executed. Once a ThreadPool is colsed, no more
	 * tasks can be run. 
	 */
	public synchronized void close() {
		if (isAlive) {
			isAlive = false;
			taskQueue.clear();
			interrupt();
		}
	}
	
	/** Closes this ThreadPool and waits for all running threads to finish. Any waiting tasks are executed */
	public void join() {
		
		// notify all waiting threads that this ThredPool is no longer alive
		synchronized(this) {
			isAlive=false;
			notifyAll();
		}
		
		// wait for all threads to finish
		Thread[] threads = new Thread[activeCount()];
		int count = enumerate(threads);
		for (int i=0; i<count; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException ex) {}
		}
	}
	
	private class PooledThread extends Thread {
		public PooledThread() {
			super(ThreadPool.this, "PooledThread-" + (threadID++));
		}
		
		public void run() {
			while (!isInterrupted()) {
				// get a task to run
				Runnable task = null;
				try {
					task = getTask();
				} catch (InterruptedException ex) {}
				
				//if getTask() returned null or was interrupted,
				// close this thread by returning
				if (task == null) {
					return;
				}
				
				try {
					task.run();
				
				} catch (Throwable t) {
					uncaughtException (this, t);
				}
			}
		}
	}
	
	
}
