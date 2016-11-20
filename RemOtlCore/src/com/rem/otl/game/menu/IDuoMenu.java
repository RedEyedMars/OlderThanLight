package com.rem.otl.game.menu;

import com.rem.duo.Acceptor;

public interface IDuoMenu extends Acceptor{

	public void playerJoins(String playerName);
	public void kick();
	public void startGame(boolean colour, long seed, long startTime);

}
