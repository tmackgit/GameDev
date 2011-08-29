package com.hypefiend.javagamebook.common;

import java.nio.channels.SocketChannel;

/**
 * Player.java
 *
 * Interface for Players, all player classes must implement this interface
 * 
 * @author <a href="mailto:bret@hypefiend.com">bret barker</a>
 * @version 1.0
 */
public interface Player {
    public String getPlayerId();
    public void setPlayerId(String id);

    public String getSessionId();
    public void setSessionId(String id);

    public SocketChannel getChannel();
    public void setChannel(SocketChannel channel);

    public boolean loggedIn();
    public void setLoggedIn(boolean in);

    public boolean inGame();
    public void setInGame(boolean in);

    public int getGameId();
    public void setGameId(int gid);
}
