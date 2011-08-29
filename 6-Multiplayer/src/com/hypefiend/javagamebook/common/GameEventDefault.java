package com.hypefiend.javagamebook.common;

import java.nio.ByteBuffer;

/**
 * GameEventDefault.java
 *
 * A basic GameEvent class, this can be extended for other Games
 * or a completely different class may be used as required by a specific game.
 * 
 * @author <a href="mailto:bret@hypefiend.com">bret barker</a>
 * @version 1.0
 */
public class GameEventDefault implements GameEvent {
    //--------------------------------------------------
    // eventType constants
    // C_* is for Client initiated events
    // S_* is for Server initiated events
    // SB_* is for Server broadcast events

    /** request to login */
    public static final int C_LOGIN = 1001; 
    /** login ok */
    public static final int S_LOGIN_ACK_OK = 1002; 
    /** login failed */
    public static final int S_LOGIN_ACK_FAIL = 1003; 
    /** broadcast notice of a player login */
    public static final int SB_LOGIN = 1004; 
    /** logout request */
    public static final int C_LOGOUT = 1005;
    /** broadcast notice of a player logout */
    public static final int SB_LOGOUT = 1006;
    /** notice of disconnect */
    public static final int S_DISCONNECT = 1007;
    
    /** request to join a game */
    public static final int C_JOIN_GAME = 1101; 
    /** join success */
    public static final int S_JOIN_GAME_ACK_OK = 1102;
    /** join failure */
    public static final int S_JOIN_GAME_ACK_FAIL = 1103;
    /** broadcast notice that player has joined */
    public static final int SB_PLAYER_JOINED = 1104;
    /** request to quit game */
    public static final int C_QUIT_GAME = 1105;
    /** broadcast notice that player has quit */
    public static final int SB_PLAYER_QUIT = 1106;     
    /** request to get player list */
    public static final int C_GET_PLAYERS = 1107;
    /** notice of player list */
    public static final int S_GET_PLAYERS = 1108;
	    
    /** client chat mesg */
    public static final int C_CHAT_MSG = 1201; 
    /** server broadcast chat mesg */
    public static final int SB_CHAT_MSG = 1202;
 
    /** client move */
    public static final int C_MOVE = 1301;
    /** move ok */
    public static final int S_MOVE_ACK_OK = 1302;
    /** move failed */
    public static final int S_MOVE_ACK_FAIL = 1303;
    /** round is complete */
    public static final int S_ROUND_COMPLETE = 1304;
    /** game over notice */
    public static final int S_GAME_OVER = 1305;

    /** used internally in client */
    public static final int C_CMD_ACK = 1401;
    /** generic ok response */
    public static final int S_ACK_OK = 1402;
    /** generic fail response */
    public static final int S_ACK_FAIL = 1403;

    /** first id that a subclass should use for events */    
    public static final int SUBCLASS_FIRST_CMD_ID = 2000;

    //-----------------------------------------------------

    /** event type */
    protected int eventType;

    /** playerID that sent the message (for client mesgs) */
    protected String playerId;

    /** player's session id */
    protected String sessionId;

    /** gameID that the event belongs to, if any */
    protected int gameId = -1;

    /** gameName that the event belongs to */
    protected String  gameName;

    /** # of recipients */
    protected int numRecipients;

    /** array of event recipient playerIDs */
    protected String[] recipients;

    /** chat message or other command specific string */
    protected String message;

    /** 
     * default contructor
     */
    public GameEventDefault(){
    }

    /** 
     * constructor that takes eventType
     */
    public GameEventDefault(int type) {
	this.eventType = type;
    }

    /**
     * constructor that takes eventType and message
     */
    public GameEventDefault(int type, String message){
	this.eventType = type;
	this.message = message;
    }

    public void setType(int type) {
	eventType = type;
    }
    public int getType() {
	return eventType;
    }
    
    public void setGameName(String gameName) {
	this.gameName = gameName;
    }
    public String getGameName() {
	return gameName;
    }
    
    public String getMessage() {
	return message;
    }
    public void setMessage(String message) {
	this.message = message;
    }

    public String getPlayerId() {
	return playerId;
    }
    public void setPlayerId(String id) {
	playerId = id;
    }
    
    public String getSessionId() {
	return sessionId;
    }
    public void setSessionId(String id) {
	sessionId = id;
    }

    public String[] getRecipients() {
	return recipients;
    }
    public void setRecipients(String[] recipients) {
	this.recipients = recipients;
	numRecipients = recipients.length;
    }

    /** 
     * write the event to the given ByteBuffer
     * 
     * note we are using 1.4 ByteBuffers for both client and server
     * depending on the deployment you may need to support older java
     * versions on the client and use old-style socket input/output streams
     */
    public int write(ByteBuffer buff) {
	int pos = buff.position();

	buff.putInt(eventType);
	NIOUtils.putStr(buff, playerId);
	NIOUtils.putStr(buff, sessionId);
	buff.putInt(gameId);
	NIOUtils.putStr(buff, gameName);
	buff.putInt(numRecipients);
	for (int i=0;i<numRecipients;i++) 
	    NIOUtils.putStr(buff, recipients[i]);
	NIOUtils.putStr(buff, message);

	// return the length of the event, this will get inserted at the beginning of the buffer
	// in the EventWriter so the Reader knows how many bytes to read for the payload
	return buff.position() - pos;
    }

    /**
     * read the event from the given ByteBuffer
     */
    public void read(ByteBuffer buff) {
	eventType = buff.getInt();
	playerId = NIOUtils.getStr(buff);
	sessionId = NIOUtils.getStr(buff);
	gameId = buff.getInt();
	gameName = NIOUtils.getStr(buff);
	numRecipients = buff.getInt();
	recipients = new String[numRecipients];
	for (int i=0;i<numRecipients;i++) 
	    recipients[i] = NIOUtils.getStr(buff);
	message = NIOUtils.getStr(buff);
    }

}// GameEvent
