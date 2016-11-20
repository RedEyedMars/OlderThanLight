package com.rem.otl.game.mode;

import java.util.List;

import com.rem.core.gui.graphics.GraphicEntity;
import com.rem.core.gui.inputs.KeyBoardListener;
import com.rem.core.gui.inputs.MouseListener;
import com.rem.otl.game.Game;
import com.rem.otl.game.GameEndState;
import com.rem.otl.game.hero.Hero;

public interface GameMode extends KeyBoardListener, MouseListener{
	public void setup(Game game, boolean colourToControl, GraphicEntity wildWall, GameEndState gameEndState);
	public void update(double seconds);
	public List<GraphicEntity> getAuxillaryChildren();
	public void loseGame(boolean isBlack);
	public void winGame(boolean isBlack,String nextMap);
	public boolean isCompetetive();
	public Hero createConnectedHero(boolean control, Game game, boolean whiteBool);
	public Hero createHero(Game game, boolean whiteBool);
}
