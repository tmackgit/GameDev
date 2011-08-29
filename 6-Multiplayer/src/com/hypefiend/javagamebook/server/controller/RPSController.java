package com.hypefiend.javagamebook.server.controller;

import com.hypefiend.javagamebook.games.rps.*;
import com.hypefiend.javagamebook.server.*;
import com.hypefiend.javagamebook.server.controller.*;
import com.hypefiend.javagamebook.common.*;
import java.util.*;
import org.apache.log4j.*;

/**
 * RPSController.java
 * 
 * Server-side game logic for RPS game
 *
 * @author <a href="mailto:bret@hypefiend.com">bret barker</a>
 * @version 1.0
 */
public class RPSController extends GameController {

    /** list of connected players */
    protected Hashtable players;

    /** list of games */
    protected HashMap games;

    /** possible moves */
    String moves[] = {"rock", "paper", "scissors" };
    
    /** 
     * game results
     * 0 = tie, 1 = player1, 2 = player2 
     **/
    int results[][] = { 
	{0,1,2},
	{2,0,1},
	{1,2,0}
    };
    
    /** text strings for results */
    String resultStrs[] = { "Tie!", "You Win", "Opponent Wins"};
    
    /**
     * return the gameName for this controller
     */
    public String getGameName() {
	return "RPS";
    }

    /** 
     * do ChatController specific initialization here 
     */
    public void initController(GameConfig gc) {
	log.info("initController");
	players = new Hashtable();
	//	clients = new HashMap();
	games = new HashMap();
    }

    /**
     * just use the default Player class
     */
    public Player createPlayer() {
	PlayerDefault p = new PlayerDefault();
	p.setSessionId(gameServer.nextSessionId());
	return p;
    }
   	
    /**
     * just use the default GameEvent class
     */
    public GameEvent createGameEvent() {
	return new GameEventDefault();
    }

    /** 
     * process events pulled form the queue
     */
    public void processEvent(GameEvent e) {
	switch (e.getType()) {
	case GameEventDefault.C_LOGIN:
	    login(e);
	    break;
	case GameEventDefault.C_LOGOUT:
	    logout(e);
	    break;
	case GameEventDefault.C_JOIN_GAME:
	    join(e);
	    break;
	case GameEventDefault.C_QUIT_GAME:
	    quit(e);
	    break;
	case GameEventDefault.C_CHAT_MSG:
	    chat(e);
	    break;
	case GameEventDefault.C_MOVE:
	    move(e);
	    break;
	case GameEventDefault.C_GET_PLAYERS:
	    getPlayers(e);
	    break;
	}
    }

    /**
     * handle login events
     */
    protected void login(GameEvent e) {
	String pid = e.getPlayerId();
	
	Player p = gameServer.getPlayerById(pid);
	if (p == null) {
	    log.error("got login event for null player");
	    return;
	}
	
	if (p.loggedIn())
	    log.warn("got login event for already logged in player: " + pid);
	
	p.setLoggedIn(true);
	
	// send ACK to player
	GameEventDefault la = new GameEventDefault(GameEventDefault.S_LOGIN_ACK_OK);
	sendEvent(la, p);

	// tell everyone this player is here
	GameEventDefault sbl = new GameEventDefault(GameEventDefault.SB_LOGIN, p.getPlayerId());
	sendBroadcastEvent(sbl, players.values());

	// add to our list
	players.put(pid, p);

	// send player list
	getPlayers(e);

	log.info("login, player: " + pid + ", players online: " + players.size());
    }

    /**
     * handle logout events
     */
    protected void logout(GameEvent e) {
	String pid = e.getPlayerId();
	Player p = (Player) players.get(pid);
	
	// if in game, kill it first
	if (p.inGame()) {
	    quit(e);
	}

	// remove the player
	players.remove(pid);

	// send them a disconnect
	GameEventDefault dis = new GameEventDefault(GameEventDefault.S_DISCONNECT, "logged out");
	sendEvent(dis, p);

	// tell everyone else
	GameEventDefault sbl = new GameEventDefault(GameEventDefault.SB_LOGOUT, p.getPlayerId());
	sendBroadcastEvent(sbl, players.values());

	log.info("logout, player: " + pid + ", players online: " + players.size());
    }

    /** 
     * initiate a game w/another player 
     * synchronized so we don't have concurrency problems with multiple
     * players starting games with the same opponent
     */
    protected synchronized void join(GameEvent e) {
	String p1_id = e.getPlayerId();
	String p2_id = e.getMessage();

	Player p1 = (Player) players.get(p1_id);

	if (p2_id.equals(p1_id)) {
	    GameEventDefault jf = new GameEventDefault(GameEventDefault.S_JOIN_GAME_ACK_FAIL);
	    jf.setMessage("sorry, can't play against yourself");
	    sendEvent(jf, p1);
	    return;
	}
		
	Player p2 = (Player) players.get(p2_id);

	if ((p1 == null) || (p2==null)) {
	    GameEventDefault jf = new GameEventDefault(GameEventDefault.S_JOIN_GAME_ACK_FAIL);
	    jf.setMessage("unknown player id");
	    sendEvent(jf, p1);
	    return;
	}
	if (p1.inGame()) {
	    GameEventDefault jf = new GameEventDefault(GameEventDefault.S_JOIN_GAME_ACK_FAIL);
	    jf.setMessage("sorry, you are already in a game");
	    sendEvent(jf, p1);
	    return;
	}
	if (p2.inGame()) {
	    GameEventDefault jf = new GameEventDefault(GameEventDefault.S_JOIN_GAME_ACK_FAIL);
	    jf.setMessage("sorry, that player is already in a game");
	    sendEvent(jf, p1);
	    return;
	}

	// create new game
	RPSGame g = new RPSGame(p1, p2);
	games.put("" + g.getGameId(), g);
	p1.setGameId(g.getGameId());
	p2.setGameId(g.getGameId());

	// let them know
	GameEventDefault jok = new GameEventDefault(GameEventDefault.S_JOIN_GAME_ACK_OK);
	jok.setMessage("Game started: " + p1.getPlayerId() + " vs. " + p2.getPlayerId());
	sendEvent(jok, p1);
	
	jok = new GameEventDefault(GameEventDefault.S_JOIN_GAME_ACK_OK);
	jok.setMessage("Game started: " + p1.getPlayerId() + " vs. " + p2.getPlayerId());
	sendEvent(jok, p2);

	log.info("Game started: " + p1.getPlayerId() + " vs. " + p2.getPlayerId());
    }

    /**
     * handle quit events
     */
    protected void quit(GameEvent e) {
	String p1_id = e.getPlayerId();
	Player player = (Player) players.get(p1_id);
	RPSGame g = (RPSGame) games.get("" + player.getGameId());

	if (g == null) {
	    GameEventDefault jf = new GameEventDefault(GameEventDefault.S_JOIN_GAME_ACK_FAIL);
	    jf.setMessage("you are not in a game");
	    sendEvent(jf, player);
	    return;
	}

	Player p1 = g.getPlayer1();
	Player p2 = g.getPlayer2();
	p1.setInGame(false);
	p2.setInGame(false);
	p1.setGameId(g.getGameId());
	p2.setGameId(g.getGameId());
	games.remove("" + g.getGameId());

	// return the ack, and final game stats
	String msg1 = "GameOver, player " + player.getPlayerId() + " has quit.\n";
	String msg2 = "GameOver\n";

	String msgt = "Final tallies\n" + 
	    p1.getPlayerId() + " wins: " + g.getP1Wins() + "\n" +
	    p2.getPlayerId() + " wins: " + g.getP2Wins() + "\n" +
	    "ties: " + g.getTies() + "\n";
	GameEventDefault qe = new GameEventDefault(GameEventDefault.SB_PLAYER_QUIT, msg1 + msgt);
	sendEvent(qe, p1);
	qe = new GameEventDefault(GameEventDefault.SB_PLAYER_QUIT, msg2 + msgt);
	sendEvent(qe, p2);
    }

    /** 
     * handle chat events
     */
    protected void chat(GameEvent e) {
	e.setType(GameEventDefault.SB_CHAT_MSG);
	sendBroadcastEvent(e, players.values());
	log.info("chat, player " + e.getPlayerId() + " says " + e.getMessage());
    }
    
    /**
     * handle move events
     */
    protected void move(GameEvent e) {
	String p1_id = e.getPlayerId();
	Player player = (Player) players.get(p1_id);
	RPSGame g = (RPSGame) games.get("" + player.getGameId());

	if (g==null) {
	    GameEventDefault mf = new GameEventDefault(GameEventDefault.S_MOVE_ACK_FAIL);
	    mf.setMessage("you are not in a game");
	    sendEvent(mf, player);
	    return;
	}
	Player p1 = g.getPlayer1();
	Player p2 = g.getPlayer2();

	// check for a valid move
	String move = e.getMessage();
	if (move.equals("r")) move = "rock";
	if (move.equals("p")) move = "paper";
	if (move.equals("s")) move = "scissors";
	int myMove = -1;
	for (int i=0; i<moves.length;i++) {
	    if (moves[i].equals(move)) 
		myMove = i;
	}

	if (myMove == -1) {
	    GameEventDefault mf = new GameEventDefault(GameEventDefault.S_MOVE_ACK_FAIL);
	    mf.setMessage("invalid move");
	    sendEvent(mf, player);
	    return;
	}

	if (player.equals(p1) && (g.getP1Move() == -1)) {
	    g.setP1Move(myMove);
	    log.debug("setting player 1 move: " + moves[myMove]);
	}
	else if (player.equals(p2) && (g.getP2Move() == -1)) {
	    g.setP2Move(myMove);
	    log.debug("setting player 2 move: " + moves[myMove]);
	}
	else {
	    GameEventDefault mf = new GameEventDefault(GameEventDefault.S_MOVE_ACK_FAIL);
	    mf.setMessage("already submitted move");
	    sendEvent(mf, player);
	    return;
	}

	log.debug(player.getPlayerId() + ", move = " + myMove + " = " + moves[myMove]);

	// send ack
	GameEventDefault e2 = new GameEventDefault(GameEventDefault.S_MOVE_ACK_OK, "move ok");
	sendEvent(e2, player);

	if ((g.getP1Move() != -1) && (g.getP2Move() != -1)) {
	    // round is complete
	    int n = (player.equals(p1)) ? g.getP2Move() : g.getP1Move();

	    int result = results[n][myMove];
	    String resultStr = resultStrs[result];

	    if (result == 0) 
		g.incTies();
	    else if (player.equals(p1) ^ (result == 2))
		g.incP1Wins();
	    else 
		g.incP2Wins();
	    
	    g.resetGame();

	    String msg = "Opponent chooses " + moves[n] + "\n" + resultStr;
	    GameEventDefault e3 = new GameEventDefault(GameEventDefault.S_ROUND_COMPLETE, msg);
	    sendEvent(e3, player);

	    // reverse output for other player
	    result = results[myMove][n];
	    resultStr = resultStrs[result];
	    msg = "Opponent chooses " + moves[myMove] + "\n" + resultStr;
	    e3 = new GameEventDefault(GameEventDefault.S_ROUND_COMPLETE, msg);
	    sendEvent(e3, g.getOpponent(player.getPlayerId()));
	}
    }

    /**
     * handle get_player events
     */
    protected void getPlayers(GameEvent e) {
	String pid = e.getPlayerId();
	Player p = (Player) players.get(pid);

	StringBuffer sb = new StringBuffer();
	sb.append("players online:\n");
	Iterator i = players.values().iterator();
	while(i.hasNext()) {
	    Player p2 = (Player) i.next();
	    sb.append(p2.getPlayerId());
	    sb.append("\n");
	}

	GameEventDefault pl = new GameEventDefault(GameEventDefault.S_GET_PLAYERS, sb.toString());
	sendEvent(pl, p);
    }

}
