package com.rem.otl.game.mode;

import java.util.ArrayList;
import java.util.List;

import com.rem.core.Hub;
import com.rem.core.gui.graphics.GraphicEntity;
import com.rem.core.gui.inputs.KeyBoardEvent;
import com.rem.duo.client.Client;
import com.rem.otl.duo.messages.HeroMoveMessage;
import com.rem.otl.game.Game;
import com.rem.otl.game.GameEndState;
import com.rem.otl.game.environment.Map;
import com.rem.otl.game.environment.onstep.OnStepSquare;
import com.rem.otl.game.hero.ConnectedHero;
import com.rem.otl.game.hero.Hero;
import com.rem.otl.game.hero.VisionBubble;

public class OverheadMode extends GameMouseHandler implements GameMode{

	private static final float uppderViewBorder = 0.6f;
	private static final float lowerViewBorder = 0.4f;
	private static final float standardAcceleration = 0.03f;

	private Hero controlled;
	private Hero wild;
	private Hero focused;
	private VisionBubble visionBubble;
	private GraphicEntity wildWall;
	private List<GraphicEntity> auxillaryChildren = new ArrayList<GraphicEntity>();
	protected boolean colourToControl;
	protected Game game;
	protected GameEndState gameEndState;
	@Override 
	public void setup(Game game, boolean colourToControl, GraphicEntity wildWall, GameEndState gameEndState){
		this.game = game;
		this.gameEndState = gameEndState;
		this.colourToControl = colourToControl;
		controlled = (Hero) Hub.map.getEntity(Map.getIdFromHeroBool(colourToControl));
		wild = (Hero) Hub.map.getEntity(Map.getIdFromHeroBool(!colourToControl));
		focused = controlled;

		this.wildWall = wildWall;
		visionBubble = new VisionBubble(focused,wild);
		auxillaryChildren.add(visionBubble);
	}


	public List<GraphicEntity> getAuxillaryChildren(){
		return auxillaryChildren;
	}
	@SuppressWarnings("unchecked")
	private void handleInterceptions(){
		List<OnStepSquare> mapSquares = (List<OnStepSquare>) Hub.map.getEntity(Map.ID_FUNCTIONAL_SQUARES);
		for(Hero hero:new Hero[]{focused,wild}){
			hero.handleWalls(mapSquares);
		}
	}

	private void handleViewMovement(){		
		float heroMoveX = focused.getX();
		float heroMoveY = focused.getY();
		if(focused.getX()>uppderViewBorder){
			heroMoveX = uppderViewBorder;
		}
		else if(focused.getX()<lowerViewBorder){
			heroMoveX = lowerViewBorder;
		}
		if(focused.getY()>uppderViewBorder){
			heroMoveY = uppderViewBorder;
		}
		else if(focused.getY()<lowerViewBorder){
			heroMoveY = lowerViewBorder;
		}

		Hub.map.reposition(Hub.map.getX()+(heroMoveX-focused.getX()),
				       Hub.map.getY()+(heroMoveY-focused.getY()));
		wild.reposition(wild.getX()+(heroMoveX-focused.getX()),
				    wild.getY()+(heroMoveY-focused.getY()));
		focused.reposition(heroMoveX,heroMoveY);
		wildWall.reposition(wild.getX()-0.1f,
				        wild.getY()-0.1f);
	}
	@Override
	public void loseGame(boolean colour){
		if(Client.isConnected()){
			if(colourToControl==colour){
				game.transition("Restart", false);
			}
		}
		else {
			gameEndState.setMyColour(colourToControl);
			gameEndState.setTheirColour(!colourToControl);
			gameEndState.setStats(this.colourToControl, false, game.getGameTime());
			gameEndState.setStats(!this.colourToControl, false, game.getGameTime());			
			game.transition("Restart", false);
		}
	}
	@Override
	public void winGame(boolean colour,String nextMap){
		if(Client.isConnected()){
			if(colourToControl==colour){
				game.transition(nextMap, true);
			}
		}
		else {
			gameEndState.setOpenColour(colour);
			gameEndState.setStats(colour, true, game.getGameTime());
			if(gameEndState.isFinished()){
				game.transition(nextMap, true);
			}
		}
	}
	@Override
	public void update(double seconds) {
		handleViewMovement();
		handleInterceptions();	
		HeroMoveMessage.update(seconds, wild);
		visionBubble.update(seconds);
	}

	@Override
	public Hero createConnectedHero(boolean control, Game game, boolean bool) {
		return new ConnectedHero(control, game,bool);
	}


	@Override
	public Hero createHero(Game game, boolean bool) {
		return new Hero(game,bool);
	}
	@Override
	public void onType(KeyBoardEvent event) {
		if(event.keyDown()){
			if('a'==event.getChar()){
				controlled.setXAcceleration(-standardAcceleration);
			}
			else if('d'==event.getChar()){
				controlled.setXAcceleration(standardAcceleration);
			}
			else if('w'==event.getChar()){
				controlled.setYAcceleration(standardAcceleration);
			}
			else if('s'==event.getChar()){
				controlled.setYAcceleration(-standardAcceleration);
			}
			else if(event.is(KeyBoardEvent.ESCAPE)||event.is(25)||event.is(197)){
				game.pause();
			}
			else if(!Client.isConnected()){
				if(event.is(KeyBoardEvent.UP)){//up
					controlled.getPartner().setYAcceleration(standardAcceleration);
				}
				else if(event.is(KeyBoardEvent.LEFT)){//left
					controlled.getPartner().setXAcceleration(-standardAcceleration);
				}
				else if(event.is(KeyBoardEvent.DOWN)){//down
					controlled.getPartner().setYAcceleration(-standardAcceleration);
				}
				else if(event.is(KeyBoardEvent.RIGHT)){//right
					controlled.getPartner().setXAcceleration(standardAcceleration);
				}
			}			
		}
		else if(event.keyUp()){
			if(event.is(32)){
				controlled.setXAcceleration(0f);
			}
			else if(event.is(30)){
				controlled.setXAcceleration(0f);
			}
			else if(event.is(17)){
				controlled.setYAcceleration(0f);
			}
			else if(event.is(31)){
				controlled.setYAcceleration(0f);
			}
			else if(event.is(KeyBoardEvent.SPACE)){//space
				Hero temp = focused;
				focused = wild;
				wild = temp;
				((Map)Hub.map).setVisibleSquares(focused.isBlack()?Hero.BLACK_INT:focused.isWhite()?Hero.WHITE_INT:Hero.BOTH_INT);
				if(!Client.isConnected()){
					visionBubble.setHeroes(focused,wild);
				}
			}
			else if(!Client.isConnected()){
				if(event.is(KeyBoardEvent.UP)){//up
					controlled.getPartner().setYAcceleration(0f);
				}
				else if(event.is(KeyBoardEvent.LEFT)){//left
					controlled.getPartner().setXAcceleration(0f);
				}
				else if(event.is(KeyBoardEvent.DOWN)){//down
					controlled.getPartner().setYAcceleration(0f);
				}
				else if(event.is(KeyBoardEvent.RIGHT)){//right
					controlled.getPartner().setXAcceleration(0f);
				}
			}
		}
	}
	@Override
	public boolean isCompetetive(){
		return false;
	}
	@Override
	public boolean continuousKeyboard() {
		return false;
	}

}
