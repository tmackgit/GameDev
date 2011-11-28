package com.brackeen.javagamebook.ai;

import com.brackeen.javagamebook.path.PathFinder;

public class Brain implements Cloneable {

    public PathFinder attackPathFinder;
    public PathFinder dodgePathFinder;
    public PathFinder aimPathFinder;
    public PathFinder idlePathFinder;
    public PathFinder chasePathFinder;
    public PathFinder runAwayPathFinder;

    // probability of each battle state
    // (the sum should be 1)
    public float attackProbability;
    public float dodgeProbability;
    public float runAwayProbability;

    public long decisionTime;
    public long aimTime;
    public float hearDistance;


    public void fixProbabilites() {
        // make the sums of the odds == 1.
        float sum = attackProbability + dodgeProbability +
            runAwayProbability;
        if (sum > 0) {
            attackProbability /= sum;
            dodgeProbability /= sum;
            runAwayProbability /= sum;
        }
    }

    public Brain() {

    }

    /**
        Copy constructor.
    */
    public Brain(Brain brain) {
        attackPathFinder = brain.attackPathFinder;
        dodgePathFinder = brain.dodgePathFinder;
        aimPathFinder = brain.aimPathFinder;
        idlePathFinder = brain.idlePathFinder;
        chasePathFinder = brain.chasePathFinder;
        runAwayPathFinder = brain.runAwayPathFinder;

        attackProbability = brain.attackProbability;
        dodgeProbability = brain.dodgeProbability;
        runAwayProbability = brain.runAwayProbability;

        decisionTime = brain.decisionTime;
        hearDistance = brain.hearDistance;
        aimTime = brain.aimTime;
    }

    public Object clone() {
        return new Brain(this);
    }

    public String toString() {
        return
            "DecisionTime: " + decisionTime + "\n" +
            "AimTime: " + aimTime + "\n" +
            "HearDistance: " + hearDistance + "\n" +

            "AttackProbability: " + attackProbability + "\n" +
            "DodgeProbability: " + dodgeProbability + "\n" +
            "RunAwayProbability: " + runAwayProbability + "\n" +

            "attackPathFinder: " + attackPathFinder +  "\n" +
            "dodgePathFinder: " + dodgePathFinder +  "\n" +
            "aimPathFinder: " + aimPathFinder +  "\n" +
            "idlePathFinder: " + idlePathFinder +  "\n" +
            "runAwayPathFinder: " + runAwayPathFinder +  "\n" +
            "chasePathFinder: " + chasePathFinder;

    }
}
