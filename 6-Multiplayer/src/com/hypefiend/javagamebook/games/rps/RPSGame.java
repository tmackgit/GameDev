package com.hypefiend.javagamebook.games.rps;

import com.hypefiend.javagamebook.common.*;

/**
 * RPSGame.java
 * 
 * A rock/paper/scissors game between 2 players
 *
 * @author <a href="mailto:bret@hypefiend.com">bret barker</a>
 * @version 1.0
 */
public class RPSGame {
    private static int nextId = 0;
    
    private int gameId;
    
    private Player player1;
    private Player player2;

    private int p1Move;
    private int p2Move;

    private int p1Wins;
    private int p2Wins;
    private int ties;

    public RPSGame(Player p1, Player p2) {
	gameId = getNextId();
	player1 = p1;
	player2 = p2;
	player1.setInGame(true);
	player2.setInGame(true);

	resetGame();
    }

    private synchronized static int getNextId() {
	return nextId++;
    }

    public int getGameId() {
	return gameId;
    }
    public Player getPlayer1() {
	return player1;
    }
    public Player getPlayer2() {
	return player2;
    }

    public Player getOpponent(String pid) {
	return (pid.equals(player1.getPlayerId())) ? player2 : player1;
    }

    public int getP1Wins() {
	return p1Wins;
    }
    public void incP1Wins() {
	p1Wins++;
    }
    public int getP2Wins() {
	return p2Wins;
    }
    public void incP2Wins() {
	p2Wins++;
    }
    public int getTies() {
	return ties;
    }
    public void incTies() {
	ties++;
    }

    public int getP1Move() {
	return p1Move;
    }
    public void setP1Move(int m) {
	p1Move = m;
    }

    public int getP2Move() {
	return p2Move;
    }
    public void setP2Move(int m) {
	p2Move = m;
    }

    public void resetGame() {
	p1Move = -1;
	p2Move = -1;
    }
}
