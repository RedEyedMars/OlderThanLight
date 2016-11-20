package com.rem.otl.duo.messages;

import com.rem.duo.MessageHandler;
import com.rem.duo.messages.Message;

/**
 * When a hero is trying to end the game (via a {@link com.rem.otl.core.game.environment.onstep.WinStageOnStepAction}), a {@link com.rem.otl.duo.messages.Message} must be sent to the partnered {@link com.rem.otl.duo.client.Client}.
 * This {@link com.rem.otl.duo.messages.Message} achieves the goal of sending the relevant information to the partnered {@link com.rem.otl.duo.client.Client}.
 * @author Geoffrey
 *
 */
public class HeroEndGameMessage extends Message{
	//For Message sending.
	private static final long serialVersionUID = -6677799385359052291L;

	//Colour of the hero which is ending the game. (Hero.BLACK_BOOL, Hero.WHITE_BOOL)
	private boolean incomingColour;
	//The status of the ender, either a winner or a loser.
	private boolean isWinner;
	//The amount of time it took in game.
	private long gameTime;

	/**
	 * Initializes this {@link com.rem.otl.duo.messages.Message} with the ending Hero's colour, their win/loss status, and the time it took to complete to that status.
	 * @param incomingColour - Boolean representation of the Hero's colour, Hero.BLACK_BOOL or Hero.WHITE_BOOL
	 * @param isWinner - True if the sending hero believes it won the stage, false otherwise.
	 * @param gameTime - The total game of this {@link com.rem.otl.core.game.hero.Hero} took to send this message/end the game.
	 * @see com.rem.otl.core.game.hero.Hero
	 */
	public HeroEndGameMessage(boolean incomingColour, boolean isWinner, long gameTime) {
		//Initializes the incoming hero's colour.
		this.incomingColour = incomingColour;
		//Initializes this message with the winning status of the Hero.
		this.isWinner=isWinner;
		//Initializes this message with the time it took to end the game.
		this.gameTime = gameTime;
	}

	/**
	 * Handles this {@link com.rem.otl.duo.messages.Message} by changing the current Game
	 */
	@Override
	public void act(MessageHandler handler) {
		//Sets this Client's partner to the incoming colour.
		handler.accept("endState", "theirColour",incomingColour);
		//Sets the data that was sent, specifically the winner and game time.
		handler.accept("endState", "stats",new Object[]{incomingColour,isWinner,gameTime});
		//If both parties have ended the game:
		handler.accept("endState", "finishIfFinished", null);
	}


}
