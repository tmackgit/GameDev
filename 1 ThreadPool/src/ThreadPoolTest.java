
public class ThreadPoolTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Test the ThredPool task.");
			System.out.println("Usage: java ThreadPoolTest numTasks numTreads");
			System.out.println(" numTasks - integer: number of tasks to run.");
			System.out.println(" numThreads - ineger: number of threads.");
			return;
		}
		
		int numTasks = Integer.parseInt(args[0]);
		int numThreads = Integer.parseInt(args[1]);
		
		ThreadPool threadPool  = new ThreadPool(numThreads);
		for (int i=0; i<numTasks; i++) {
			threadPool.runTask(createTask(i));
		}
		
		threadPool.join();

	}
	
	private static Runnable createTask(final int taskID) {
		return new Runnable() {
			public void run() {
				System.out.println("Task " + taskID + ": start");
				try  {
					Thread.sleep(500);
				} catch (InterruptedException ex) {}
				System.out.println("Task " + taskID + ": end");
			}
		};
	}

}
