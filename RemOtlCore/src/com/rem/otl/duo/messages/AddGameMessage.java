package com.rem.otl.duo.messages;

import com.rem.otl.game.hero.Hero;
import com.rem.duo.MessageHandler;
import com.rem.duo.messages.Message;

/**
 * Adds a Game to the Server. 
 * Then the new list of games will then be sent to all the Players connected to the Server.
 * In this way new games are presented to potential partners to join the game, using {@link com.rem.otl.duo.messages.JoinGameMessage}.
 * @author Geoffrey
 *
 */
public class AddGameMessage extends Message{
	//Serial for message sending.
	private static final long serialVersionUID = 1248603591622901924L;

	//Name the game will be listed under.
	@SuppressWarnings("unused")
	private String gameName;
	//Name of the map the game will start on.
	@SuppressWarnings("unused")
	private String mapName;
	//Boolean to advertise the host's starting colour, black=true, white=false.
	@SuppressWarnings("unused")
	private Boolean colour;
	/**
	 * Makes an AddGameMessage by storing the parameters to be sent to the Server.
	 * @param gameName - String that the Game in the Server will be known as.(not unique)
	 * @param mapName - String of the name of the {@link com.rem.otl.core.game.environment.Map} which the {@link com.rem.otl.core.game.Game} will start in.
	 * @param colour - "black" or "white" whichever the host will start as, basically saying that the opposite colour is available.
	 */
	public AddGameMessage(String gameName, String mapName, String colour){
		this.gameName = gameName;
		this.mapName = mapName;
		this.colour = colour.equals("black")?Hero.BLACK_BOOL:colour.equals("white")?Hero.WHITE_BOOL:null;
	}	

	/**
	 * Empty method because the only receivers of this {@link com.rem.otl.duo.messages.Message} are Servers.
	 */
	@Override
	public void act(MessageHandler handler) {
	}

}
