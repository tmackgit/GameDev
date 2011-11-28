package com.brackeen.javagamebook.scripting;

/**
    A game task that can be scheduled for one-time execution.
*/
public class GameTask implements Runnable {

    private long remainingTime;
    private Runnable runnable;
    private boolean done;

    /**
        Creates a new GameTask that will execute the specified
        Runnable after a delay.
    */
    public GameTask(long delay, Runnable runnable) {
        this.remainingTime = delay;
        this.runnable = runnable;
    }


    /**
        Cancels this task.
    */
    public void cancel() {
        done = true;
    }


    /**
        Runs this task.
    */
    public void run() {
        if (runnable != null) {
            runnable.run();
        }
    }


    /**
        Checks to see if this GameTask is ready to execute, and if
        so, it is executed. Returns true if the task is done
        (either it executed or previously canceled).
    */
    public boolean check(long elapsedTime) {
        if (!done) {
            remainingTime-=elapsedTime;
            if (remainingTime <= 0) {
                done = true;
                try {
                    run();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        return done;
    }

}