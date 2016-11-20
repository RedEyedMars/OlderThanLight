package com.rem.otl.game.hero;

import com.rem.otl.duo.messages.HeroMoveMessage;
import com.rem.otl.game.Game;

public class ConnectedHero extends Hero{

	private boolean control;
	public ConnectedHero(boolean control, Game game, boolean colour) {
		super(game, colour);
		this.control = control;
	}
	public ConnectedHero(boolean control, String texture, Game game, boolean colour) {
		super(texture, game, colour);
		this.control = control;
	}
	@Override
	public void move(float x, float y){
		if(control){
			super.move(x,y);
			HeroMoveMessage.send(x,y);
		}
	}
}
