package com.brackeen.javagamebook.scripting;

import java.util.*;

/**
    Manages a queue of GameTask objects.
*/
public class GameTaskManager {

    private List tasks;


    /**
        Creates a new GameTaskManager with a empty task queue.
    */
    public GameTaskManager() {
        tasks = new ArrayList();
    }


    /**
        Adds a task to the queue that exetued the specified
        runnable after a delay.
    */
    public void addTask(long delay, Runnable runnable) {
        addTask(new GameTask(delay, runnable));
    }


    /**
        Adds a task to the queue.
    */
    public void addTask(GameTask task) {
        tasks.add(task);
    }


    /**
        Clears the task queue.
    */
    public void clear() {
        tasks.clear();
    }


    /**
        Updates this manager, executing any ready tasks.
    */
    public void update(long elapsedTime) {

        List removeList = null;
        int size = tasks.size();

        // note that executing a task can potentially add more
        // tasks onto the queue.
        for (int i=0; i<size; i++) {
            GameTask task = (GameTask)tasks.get(i);
            if (task.check(elapsedTime)) {
                // add object to list of objects to remove later
                if (removeList == null) {
                    removeList = new ArrayList();
                }
                removeList.add(task);
            }
        }

        // clear tasks that executed
        if (removeList != null) {
            tasks.removeAll(removeList);
        }
    }
}
