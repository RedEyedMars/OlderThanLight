package com.rem.otl.duo.messages;

import com.rem.duo.MessageHandler;
import com.rem.duo.messages.Message;

/**
 * Sent after a {@link com.rem.otl.core.game.Game} has ended, this {@link com.rem.otl.duo.messages.Message} asks the {@link com.rem.otl.core.game.menu.TransitionMenu} the game was completed in the "best" time. If so, save the game times (date and time it took to complete/fail).
 * The "Best" time means, if the game was failed and the player has no history of winning that map, the Best time is the longest the play has lasted on the map. (i.e. bad times are low time).
 * If the player has won the map before, the best time is the lowest time.
 * @author Geoffrey
 *
 */
public class SaveGameMessage extends Message {
	//For Message sending.
	private static final long serialVersionUID = 440733243665455430L;
	
	//The map name to be saved.
	private String previousMapName;
	/**
	 * Initializes this {@link com.rem.otl.duo.messages.Message} with the necessary variables.
	 * @param previousMapName - The name of the map which has just been played, and will reflect the name of the map to be saved.
	 */
	public SaveGameMessage(String previousMapName, int minutes, int seconds) {
		//Initializes the map name.
		this.previousMapName = previousMapName;
	}

	/**
	 * Cause the {@link com.rem.otl.core.game.menu.TransitionMenu} to save the variables if this is indeed the best time.
	 */
	@Override
	public void act(MessageHandler handler) {
		handler.accept("endState","saveTime",previousMapName);
	}

}
