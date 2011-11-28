package com.brackeen.javagamebook.ai;

import java.util.*;
import com.brackeen.javagamebook.ai.pattern.*;
import com.brackeen.javagamebook.path.*;
import com.brackeen.javagamebook.util.MoreMath;
import com.brackeen.javagamebook.bsp2D.BSPTree;

/**
    The EvolutionGenePool class keeps track of a collection of
    Brains. When an EvolutionBot is created, it requests a
    Brain from the gene pool. The Brain is either one of the
    brains in the collection, or a mutation of one of the brains.
    Only the top brains are collected, ranked by the amount of
    damage a brain's bot caused. There for, only the best brains
    reproduce.
*/

public class EvolutionGenePool {

    private static final int NUM_TOP_BRAINS = 5;
    private static final int NUM_TOTAL_BRAINS = 10;

    private class BrainStat extends Brain implements Comparable {

        long totalDamageCaused;
        int numBots;
        int generation;

        public BrainStat() {

        }

        public BrainStat(BrainStat brain) {
            super(brain);
            this.generation = brain.generation;
        }


        /**
            Gets the average damage this brain causes.
        */
        public float getAverageDamageCaused() {
            if (numBots == 0) {
                return 0;
            }
            else {
                return (float)totalDamageCaused / numBots;
            }
        }


        /**
            Reports damaged caused by a bot with this brain
            after the bot was destroyed.
        */
        public void report(long damageCaused) {
            totalDamageCaused+=damageCaused;
            numBots++;
        }


        /**
            Returns a smaller number if this brain caused more
            damage that the specified object, which should
            also be a brain.
        */
        public int compareTo(Object obj) {
            BrainStat other = (BrainStat)obj;
            float thisScore = this.getAverageDamageCaused();
            float otherScore = other.getAverageDamageCaused();
            if (thisScore == 0 && otherScore == 0) {
                // less number of bots is better
                return (this.numBots - other.numBots);
            }
            else {
                // more damage is better
                return (int)MoreMath.sign(otherScore - thisScore);
            }

        }


        /**
            Mutates this brain. The specified mutationProbability
            is the probability that each brain attribute
            becomes a different value, or "mutates".
        */
        public void mutate(float mutationProbability) {
            if (MoreMath.chance(mutationProbability)) {
                attackProbability = (float)Math.random();
            }
            if (MoreMath.chance(mutationProbability)) {
                dodgeProbability = (float)Math.random();
            }
            if (MoreMath.chance(mutationProbability)) {
                runAwayProbability = (float)Math.random();
            }
            if (MoreMath.chance(mutationProbability)) {
                decisionTime = MoreMath.random(3000, 6000);
            }
            if (MoreMath.chance(mutationProbability)) {
                aimTime = MoreMath.random(300, 2000);
            }
            if (MoreMath.chance(mutationProbability)) {
                hearDistance = MoreMath.random(50, 2000);
            }
            if (MoreMath.chance(mutationProbability)) {
                attackPathFinder = (PathFinder)
                    MoreMath.random(attackPathFinders);
            }
            if (MoreMath.chance(mutationProbability)) {
                dodgePathFinder = (PathFinder)
                    MoreMath.random(dodgePathFinders);
            }
            if (MoreMath.chance(mutationProbability)) {
                aimPathFinder = (PathFinder)
                    MoreMath.random(aimPathFinders);
            }
            if (MoreMath.chance(mutationProbability)) {
                idlePathFinder = (PathFinder)
                    MoreMath.random(idlePathFinders);
            }
            if (MoreMath.chance(mutationProbability)) {
                chasePathFinder = (PathFinder)
                    MoreMath.random(chasePathFinders);
            }
            if (MoreMath.chance(mutationProbability)) {
                runAwayPathFinder = (PathFinder)
                    MoreMath.random(runAwayPathFinders);
            }

            fixProbabilites();
        }


        public Object clone() {
            BrainStat brain = new BrainStat(this);
            brain.generation++;
            return brain;
        }

        public String toString() {
            if (numBots == 0) {
                return "(Not Used)\n" + super.toString();
            }
            else {
                return "Average damage per bot: " +
                    getAverageDamageCaused() + " " +
                    "(" + numBots + " bots)\n" +
                    "Generation: " + generation + "\n" +
                    super.toString();
            }
        }
    }

    private List brains;

    private List attackPathFinders;
    private List aimPathFinders;
    private List dodgePathFinders;
    private List idlePathFinders;
    private List chasePathFinders;
    private List runAwayPathFinders;

    public EvolutionGenePool(BSPTree bspTree) {

        // create path finders
        attackPathFinders = Arrays.asList(new Object[] {
            new AttackPatternRush(bspTree),
            new AttackPatternStrafe(bspTree)
        });
        aimPathFinders = Arrays.asList(new Object[] {
            new AimPattern(bspTree)
        });
        dodgePathFinders = Arrays.asList(new Object[] {
            new DodgePatternZigZag(bspTree),
            new DodgePatternRandom(bspTree)
        });
        idlePathFinders = Arrays.asList(new Object[] {
            null
        });
        chasePathFinders = Arrays.asList(new Object[] {
            new AStarSearchWithBSP(bspTree)
        });
        runAwayPathFinders = Arrays.asList(new Object[] {
            new RunAwayPattern(bspTree)
        });

        // make a few random brains to start
        brains = new ArrayList();
        for (int i=0; i<NUM_TOTAL_BRAINS; i++) {
            BrainStat brain = new BrainStat();
            // randomize (mutate) all brain properties
            brain.mutate(1);
            brains.add(brain);
        }
    }

    /**
        The BSP tree used for certain patterns (like the
        shortest path alogirthm used for the chase pattern)
    */
    public void setBSPTree(BSPTree bspTree) {
        ((AStarSearchWithBSP)chasePathFinders.get(0)).
            setBSPTree(bspTree);
    }


    public void resetEvolution() {
        brains.clear();
    }


    /**
        Gets a new brain from the gene pool. The brain will either
        be a "top" brain or a new, mutated "top" brain.
    */
    public Brain getNewBrain() {

        // 50% chance of creating a new, mutated a brain
        if (MoreMath.chance(.5f)) {
            BrainStat brain =
                (BrainStat)getRandomTopBrain().clone();

            // 10% to 25% chance of changing each attribute
            float p = MoreMath.random(0.10f, 0.25f);
            brain.mutate(p);
            return brain;
        }
        else {
            return getRandomTopBrain();
        }
    }


    /**
        Gets a random top-performing brain.
    */
    public Brain getRandomTopBrain() {
        int index = MoreMath.random(NUM_TOP_BRAINS-1);
        return (Brain)brains.get(index);
    }


    /**
        Notify that a creature with the specified brain has
        been destroyed. The brain's stats aer recorded. If the
        brain's stats are within the top
    */
    public void notifyDead(Brain brain, long damageCaused) {
        // update statistics for this brain
        if (brain instanceof BrainStat) {
            BrainStat stat = (BrainStat)brain;

            // report the damage
            stat.report(damageCaused);

            // sort and trim the list
            if (!brains.contains(stat)) {
                brains.add(stat);
            }
            Collections.sort(brains);
            while (brains.size() > NUM_TOTAL_BRAINS) {
                brains.remove(NUM_TOTAL_BRAINS);
            }
        }
    }



    public String toString() {

        // display best brains
        String retVal = "Top " + NUM_TOP_BRAINS + " Brains:\n";
        for (int i=0; i<NUM_TOP_BRAINS; i++) {
            retVal+= (i+1) + ".\n";
            retVal+=brains.get(i) + "\n";
        }

        return retVal;
    }
}
