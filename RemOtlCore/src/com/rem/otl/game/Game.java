package com.rem.otl.game;

import java.util.List;
import java.util.Random;

import com.rem.core.Action;
import com.rem.core.Hub;
import com.rem.core.gui.graphics.GraphicEntity;
import com.rem.core.gui.graphics.GraphicView;
import com.rem.core.gui.inputs.HoverEvent;
import com.rem.core.gui.inputs.KeyBoardListener;
import com.rem.core.gui.inputs.ClickEvent;
import com.rem.core.storage.Storage;
import com.rem.duo.client.Client;
import com.rem.otl.duo.messages.HeroMoveMessage;
import com.rem.otl.game.chat.Chat;
import com.rem.otl.game.environment.Map;
import com.rem.otl.game.environment.Square;
import com.rem.otl.game.environment.onstep.OnStepSquare;
import com.rem.otl.game.environment.update.UpdatableSquare;
import com.rem.otl.game.hero.Hero;
import com.rem.otl.game.menu.PauseMenu;
import com.rem.otl.game.menu.TransitionMenu;
import com.rem.otl.game.mode.GameMode;

public class Game extends GraphicView{

	//private float pointerX = 0.05f;
	//private float pointerY = 0.05f;

	private GameMode gameMode;
	private Chat chat;
	private boolean colourToControl;
	private long seed;
	private boolean transition = false;
	private boolean successful = false;
	private String nextMap = "";
	private PauseMenu pauseMenu;
	private boolean waiting=false;
	private GraphicView parentView;

	private GameEndState gameEndState;
	public Game(boolean colourToControl, long seed, GraphicView parentView){
		this(colourToControl,seed,parentView,null);
	}
	@SuppressWarnings("unchecked")
	public Game(boolean colourToControl, long seed, GraphicView parentView, Chat oldChat){
		gameEndState = GameEndState.create();	
		this.parentView = parentView;
		if(Hub.music!=null){
			addChild(Hub.music);
		}
		if(Hub.map.getFileName()!=null){
			Hub.defaultMapFile = Hub.map.getFileName();
		}
		HeroMoveMessage.reset();
		Hub.delayer.updateState();
		Hub.randomizer = new Random(seed);
		this.seed = seed;
		this.colourToControl = colourToControl;
		Hero black = null;
		Hero white = null;
		gameMode = ((GameMode)Hub.map.getEntity(Map.ID_GAME_MODE));
		if(gameMode==null) return;		

		if(Client.isConnected()){
			if(colourToControl){
				black = gameMode.createConnectedHero(true,this,Hero.BLACK_BOOL);
				white = gameMode.createConnectedHero(false,this,Hero.WHITE_BOOL);
			}
			else {
				white = gameMode.createConnectedHero(true,this,Hero.WHITE_BOOL);
				black = gameMode.createConnectedHero(false,this,Hero.BLACK_BOOL);				
			}
		}
		else {
			black = gameMode.createHero(this,Hero.BLACK_BOOL);
			white = gameMode.createHero(this,Hero.WHITE_BOOL);
		}
		black.setPartner(white);
		white.setPartner(black);
		Hub.map.addEntity(Map.ID_BLACK_HERO,black);
		Hub.map.addEntity(Map.ID_WHITE_HERO,white);
		addChild(Hub.map);

		if(((Integer)Hub.map.getEntity(Map.ID_SQUARE_SIZE))>0){
			OnStepSquare wildWall = new OnStepSquare(-1,0.5f,
					((OnStepSquare)Hub.map.getEntity(Map.getIdFromMult(Map.MULT_ID_FUNCTIONAL_SQUARE, 0))).getBlackAction());
			((List<OnStepSquare>)Hub.map.getEntity(Map.ID_FUNCTIONAL_SQUARES)).add(0,wildWall);
			if(Client.isConnected()){
				if(oldChat==null){
					chat = new Chat(Hub.TOP_LAYER,colourToControl);
					chat.reposition(0.03f, 0.03f);
					Client.addAcceptor("chat", chat);
				}
				else {
					chat = oldChat;
				}

				addChild(chat);
				chat.setVisible(false);
			}
			gameMode.setup(this,colourToControl, wildWall, gameEndState);
			for(GraphicEntity e:gameMode.getAuxillaryChildren()){
				addChild(e);
			}
			Hub.map.onCreate();
			for(UpdatableSquare square:
				((List<UpdatableSquare>)Hub.map.getEntity(Map.ID_UPDATE_SQUARES))){
				square.run();
			}
			((Square)Hub.map.getEntity(Map.getIdFromMult(Map.MULT_ID_SQUARE, 0))).reposition(0f,0f);
			
			black.reposition(((Float)Hub.map.getEntity(Map.ID_BLACK_X)),
							 ((Float)Hub.map.getEntity(Map.ID_BLACK_Y)));
			white.reposition(((Float)Hub.map.getEntity(Map.ID_WHITE_X)),
					 		((Float)Hub.map.getEntity(Map.ID_WHITE_Y)));
			if(colourToControl==Hero.BLACK_BOOL){
				addChild(white);
				addChild(black);
			}
			else if(colourToControl==Hero.WHITE_BOOL){
				addChild(black);
				addChild(white);
			}

			((Map)Hub.map).setVisibleSquares(colourToControl==Hero.BLACK_BOOL?Hero.BLACK_INT:
				colourToControl==Hero.WHITE_BOOL?Hero.WHITE_INT:Hero.BOTH_INT);			
		}
	}
	public KeyBoardListener getDefaultKeyBoardListener(){
		return gameMode;
	}
	private double delay = 0.0;
	private boolean hasDelay = false;
	@Override
	public void update(double secondsSinceLastFrame){
		if(hasDelay){
			delay+=secondsSinceLastFrame;
			if(delay<0.1)return;
			delay-=0.1;
		}
		if(transition){
			enactTransition();
		}
		if(gameMode==null){
			Hub.handler.removeOnType(gameMode);
			Hub.renderer.clearAdditions();
			Hub.gui.setView(parentView);
		}
		if((pauseMenu!=null&&pauseMenu.isPaused()) || waiting /*||secondsSinceLastFrame>0.1f*/)return;

		for(Hero hero:(Hero[])Hub.map.getEntity(Map.ID_BOTH_HEROES)){
			if(hero!=null){
				hero.update(secondsSinceLastFrame);
			}
		}
		Hub.delayer.updateMap();
		gameMode.update(secondsSinceLastFrame);
		if(chat!=null){
			chat.update(secondsSinceLastFrame);
		}
	}

	@Override
	public boolean onHover(HoverEvent event){
		//pointerX = event.getX();
		//pointerY = event.getY();
		return super.onHover(event);
	}
	@Override
	public boolean onClick(ClickEvent e){
		if(chat!=null&&
				chat.getOpenChatButton().isWithin(e.getX(),e.getY())){
			chat.getOpenChatButton().performOnRelease(e);
			e.setAction(ClickEvent.ACTION_UP);
		}
		if(gameMode!=null){
			return gameMode.onClick(e);
		}
		else return super.onClick(e);
	}

	public void transition(String nextMap, boolean success) {		
		transition=true;
		this.nextMap = nextMap;
		this.successful = success;
	}

	private void enactTransition(){
		String previousMapName = Hub.map.getName();
		Hub.handler.removeOnType(gameMode);
		Hub.renderer.clearAdditions();
		String nextMapName = Storage.getMapNameFromFileName(nextMap);
		//Hub.log.debug("Game.enactTransition", nextMapName);
		TransitionMenu menu = new TransitionMenu(
				this,gameMode.isCompetetive(),successful,
				Hub.delayer.getGameTime(),previousMapName,nextMapName,colourToControl,Hub.map.getFileName()!=null);

		gameEndState.setMenu(menu);

		gameEndState.setPreviousMapName(previousMapName);
		gameEndState.setNextMapName(nextMapName);
		if(Hub.map.getFileName()!=null){
			gameEndState.setNextMapFileName(nextMap);
		}
		if(Client.isConnected()){		
			gameEndState.setMyColour(colourToControl);
			gameEndState.setStats(colourToControl, successful, Hub.delayer.getGameTime());
			gameEndState.send(colourToControl,successful,Hub.delayer.getGameTime());			
		}
		if(gameEndState.isFinished()){
			gameEndState.finish();
		}
		Hub.gui.setView(menu);		
	}
	public void loseGame(boolean heroColour) {
		gameMode.loseGame(heroColour);
	}
	public void winGame(boolean heroColour,String nextMap) {
		gameMode.winGame(heroColour,nextMap);
	}
	public long getGameTime() {
		return Hub.delayer.getGameTime();
	}
	public void pause() {
		if(pauseMenu==null){
			pauseMenu = new PauseMenu(this);
			addChild(pauseMenu);
		}
		pauseMenu.pause();
		Hub.handler.giveOnClick(pauseMenu);
		Hub.handler.giveOnType(pauseMenu);
	}
	public void restart() {

		final float theirX = ((Hero)Hub.map.getEntity(Map.getIdFromHeroBool(!colourToControl))).getX()-Hub.map.getX();
		final float theirY = ((Hero)Hub.map.getEntity(Map.getIdFromHeroBool(!colourToControl))).getY()-Hub.map.getY();
		Hero hero = ((Hero)Hub.map.getEntity(Map.ID_BLACK_HERO));
		if(colourToControl==Hero.BLACK_BOOL){
			hero.move(
					(Float)Hub.map.getEntity(Map.ID_BLACK_X)-(hero.getX()-Hub.map.getX()), 
					(Float)Hub.map.getEntity(Map.ID_BLACK_Y)-(hero.getY()-Hub.map.getY()));
		}
		else if(colourToControl==Hero.WHITE_BOOL){
			hero.move(
					(Float)Hub.map.getEntity(Map.ID_WHITE_X)-(hero.getX()-Hub.map.getX()), 
					(Float)Hub.map.getEntity(Map.ID_WHITE_Y)-(hero.getY()-Hub.map.getY()));
		}

		waiting=true;
		//final long timeAtRestart = System.currentTimeMillis();
		//Hub.delayer.reset();
		Hub.map.restart(new Action<Object>(){
			@Override
			public void act(Object subject) {
				double previousGameTime = Hub.delayer.getGameTime()/1000.0;
				Game game = new Game(colourToControl,seed,parentView,getChatBox());
				((Hero)Hub.map.getEntity(Map.getIdFromHeroBool(!colourToControl))).reposition(theirX+Hub.map.getX(),theirY+Hub.map.getY());
				Hub.gui.setView(game);
				if(Client.isConnected()){
					Hub.map.update(previousGameTime/*+(System.currentTimeMillis()-timeAtRestart)/1000.0*/);
				}
				waiting=false;
			}
		});
	}

	public Chat getChatBox(){
		return chat;
	}
	public GraphicView getParentView() {
		return this.parentView;
	}
	public void returnToParent() {
		Client.endConnectionToTheServer();
		Hub.gui.setView(getParentView());

	}
}
