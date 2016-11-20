package com.rem.otl.game.mode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.rem.core.Action;
import com.rem.core.Hub;
import com.rem.core.IFileManager;
import com.rem.core.gui.graphics.GraphicEntity;
import com.rem.core.gui.graphics.GraphicText;
import com.rem.core.gui.inputs.KeyBoardEvent;
import com.rem.core.gui.inputs.ClickEvent;
import com.rem.core.storage.Resource;
import com.rem.duo.client.Client;
import com.rem.otl.duo.messages.HeroMoveMessage;
import com.rem.otl.game.Game;
import com.rem.otl.game.GameEndState;
import com.rem.otl.game.environment.Map;
import com.rem.otl.game.environment.onstep.OnStepSquare;
import com.rem.otl.game.hero.ConnectedHumanoidHero;
import com.rem.otl.game.hero.Hero;
import com.rem.otl.game.hero.HumanoidHero;
import com.rem.otl.game.menu.IconMenuButton;

public class RaceMode extends GameMouseHandler implements GameMode{

	private static final float uppderViewBorder = 0.6f;
	private static final float lowerViewBorder = 0.4f;
	private static final float standardAcceleration = 0.02f;
	private static final float standingHeight = 0.04f;
	private static final float crouchingHeight = 0.03f;
	private boolean focusedCanJump = false;

	protected Hero wild;
	protected Hero focused;
	private GraphicEntity wildWall;
	protected List<GraphicEntity> auxillaryChildren = new ArrayList<GraphicEntity>();
	private Long ghostNext = 0L;
	private DataInputStream ghostPath = null;
	private DataOutputStream myPath;
	private long bestTime=Long.MAX_VALUE;

	private boolean ending = false;

	private GraphicText showTime;
	private GraphicText showTimeBack;
	private float previousX=0;
	private float previousY=0;
	private Game game;
	private boolean colourToControl;
	private IconMenuButton pauseButton;
	private boolean hasReleased = true;
	protected GameEndState gameEndState;
	public List<GraphicEntity> getAuxillaryChildren(){
		return auxillaryChildren;
	}
	@Override 
	public void setup(Game game, boolean colourToControl, GraphicEntity wildWall, GameEndState gameEndState){
		this.game = game;
		this.gameEndState = gameEndState;
		this.colourToControl = colourToControl;
		focused = (Hero) Hub.map.getEntity(Map.getIdFromHeroBool(colourToControl));
		wild = (Hero) Hub.map.getEntity(Map.getIdFromHeroBool(!colourToControl));

		focused.resize(0.02f, standingHeight);
		wild.resize(0.02f, 0.04f);
		this.wildWall = wildWall;
		wild.reposition(focused.getX(),focused.getY());
		if(!Client.isConnected()){
			setupPathStreams(Hub.map.getName());			
		}
		showTimeBack = new GraphicText("impact","0",1);
		showTimeBack.reposition(0.445f,0.895f);
		showTimeBack.setFontSize(GraphicText.FONT_SIZE_LARGE);
		auxillaryChildren.add(showTimeBack);
		showTime = new GraphicText("impactWhite","0",1);
		showTime.reposition(0.45f,0.9f);
		showTime.setFontSize(GraphicText.FONT_SIZE_LARGE);
		auxillaryChildren.add(showTime);
		
		pauseButton = new IconMenuButton("music_player_icons",2){
			@Override
			public void performOnClick(ClickEvent e){
				onType(new KeyBoardEvent(KeyBoardEvent.KEY_DOWN,' ',KeyBoardEvent.ESCAPE));
			}
			@Override
			public void resize(float x, float y){
				super.resize(x, y);
				if(icon!=null){
					this.icon.resize(0.08f, 0.075f);
				}
			}
		};
		pauseButton.resize(0.09f, 0.08f);
		pauseButton.reposition(0.88f, 0.89f);
		auxillaryChildren.add(pauseButton);
	}

	public void setupPathStreams(String mapName){
		Hub.manager.createDirectory("saves");
		myPath = new DataOutputStream(Hub.manager.createOutputStream("saves"+File.separatorChar+mapName+".temp",IFileManager.RELATIVE).get());
		
		Resource<InputStream> ghost = Hub.manager.createInputStream("saves"+File.separatorChar+mapName+".ghost",IFileManager.RELATIVE);

		ghostPath = new DataInputStream(ghost.get());
		if(ghost.exists()){
			bestTime=Long.MAX_VALUE;
			ghostNext=Long.MAX_VALUE;
			try {
				bestTime = ghostPath.readLong();
				ghostNext = ghostPath.readLong();
				return;
			} catch (IOException e) {
			}
		}
		try {
			ghostPath.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ghostPath = null;
		wild.setVisible(false);
	}
	private void saveGhostPath(String mapName,long now){
		if(now<=bestTime){
			try {
				DataOutputStream writer = new DataOutputStream(
						Hub.manager.createOutputStream("saves"+File.separatorChar+mapName+".ghost",IFileManager.RELATIVE).get());
				DataInputStream reader = new DataInputStream(
						Hub.manager.createInputStream("saves"+File.separatorChar+mapName+".temp",IFileManager.RELATIVE).get());

				writer.writeLong(now);
				try {
					while(true){
						writer.writeLong(reader.readLong());
						writer.writeFloat(reader.readFloat());
						writer.writeFloat(reader.readFloat());
						writer.writeChar(reader.readChar());					
					}
				} catch(EOFException e){					
				}
				writer.writeLong(now);
				writer.writeFloat(focused.getX()-Hub.map.getX());
				writer.writeFloat(focused.getY()-Hub.map.getY());
				writer.writeChar('!');

				reader.close();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void handleGhost(){
		if(ending)return;
		long now = game.getGameTime();
		String minutes = now<60000?" ":now/60000+"m";
		String seconds = ((now/1000)%60<10&&!" ".equals(minutes)?"0":"")+(now/1000)%60+"s ";
		String time = minutes+seconds+now%1000;
		showTimeBack.change(time);
		showTime.change(time);
		if(Client.isConnected()||myPath==null)return;
		if(focused.getX()-Hub.map.getX()!=previousX||focused.getY()-Hub.map.getY()!=previousY){
			previousX = focused.getX()-Hub.map.getX();
			previousY = focused.getY()-Hub.map.getY();

			try {
				myPath.writeLong(now);
				myPath.writeFloat(previousX);
				myPath.writeFloat(previousY);
				myPath.writeChar('\n');
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		while(ghostPath!=null&&now>ghostNext){
			try {
				float x = ghostPath.readFloat();
				float y = ghostPath.readFloat();
				wild.reposition(x+Hub.map.getX(),
						y+Hub.map.getY());
				char endChar = ghostPath.readChar();
				if(endChar=='!'){
					ghostPath.close();
					ghostPath=null;
					break;
				}
				ghostNext = ghostPath.readLong();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	@SuppressWarnings("unchecked")
	private void handleInterceptions(){
		List<OnStepSquare> mapSquares = (List<OnStepSquare>) Hub.map.getEntity(Map.ID_FUNCTIONAL_SQUARES);
		focused.handleWalls(mapSquares);
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
		else if(Hub.map.getY()<0&&focused.getY()<lowerViewBorder){
			heroMoveY = lowerViewBorder;
		}

		Hub.map.reposition(Hub.map.getX()+(heroMoveX-focused.getX()),
				Hub.map.getY()+(heroMoveY-focused.getY()));
		wild.reposition(wild.getX()+(heroMoveX-focused.getX()),
				wild.getY()+(heroMoveY-focused.getY()));
		focused.reposition(heroMoveX,heroMoveY);
		wildWall.reposition(wild.getX()-0.25f,
				wild.getY()-0.25f);
	}
	@Override 
	public void update(double secondsSinceLastFrame){
		handleGhost();
		handleViewMovement();
		handleInterceptions();	
		HeroMoveMessage.update(secondsSinceLastFrame, wild);
		if(focused.foundSouthWall()){
			focusedCanJump=true;
			focused.setJumping(false);
			if(focused.getYAcceleration()<0){
				focused.setYAcceleration(0);
			}
		}
		else if(focused.foundNorthWall()&&focused.getYAcceleration()>0){
			focused.setYAcceleration(0);
			focusedCanJump=false;
		}
		else {
			if(focused.getYAcceleration()>=-0.06){
				focused.setYAcceleration((float) (focused.getYAcceleration()-0.2f*secondsSinceLastFrame));
			}
		}
		if(focused.getY()<-0.05f){
			//System.out.println(focused.isBlack()&&focused.getY()<-0.05f?"black lose":"white lose");
			loseGame(focused.isBlack());
		}
	}
	@Override
	public Hero createConnectedHero(boolean control, Game game, boolean bool) {
		return new ConnectedHumanoidHero(control, game,bool);
	}


	@Override
	public Hero createHero(Game game, boolean bool) {
		return new HumanoidHero(game,bool);
	}
	@Override
	public void loseGame(boolean colour){
		if(colour!=colourToControl||ending){
			return;
		}
		try {
			if(ghostPath!=null){
				ghostPath.close();
				ghostPath = null;
			}
			if(myPath!=null){
				myPath.close();
				myPath=null;
				deleteMyPath(Hub.map.getName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(gameEndState.partnerHasWon()){
			game.transition("Restart", false);
			if(!Client.isConnected()){
				gameEndState.setMyColour(focused.isBlack());
				gameEndState.setTheirColour(!focused.isBlack());
				gameEndState.setStats(focused.isBlack(), false, Long.MAX_VALUE);
				gameEndState.setStats(!focused.isBlack(), false, bestTime);
			}
			ending = true;
		}
		else {
			game.restart();
		}
	}
	@Override
	public void winGame(boolean colour,String nextMap){
		if(colour!=colourToControl||ending){
			return;
		}
		long now = game.getGameTime();
		try {
			if(ghostPath!=null){
				ghostPath.close();
				ghostPath = null;
			}
			if(myPath!=null){
				myPath.close();
				myPath=null;
				saveGhostPath(Hub.map.getName(),now);
				deleteMyPath(Hub.map.getName());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		game.transition(nextMap, true);
		if(!Client.isConnected()){

			gameEndState.setMyColour(focused.isBlack());
			gameEndState.setTheirColour(!focused.isBlack());
			gameEndState.setStats(focused.isBlack(), true, now);
			gameEndState.setStats(!focused.isBlack(), true, bestTime);		
		}
		ending = true;
	}
	private void deleteMyPath(String mapName) {
		
		Hub.manager.deleteFile("saves"+File.separatorChar+mapName+".temp");
	}
	protected void jump(){
		if(focusedCanJump){
			if(focused.isJumping()){
				focused.setYAcceleration(focused.getYAcceleration()+0.06f);
				if(focused.getYAcceleration()>0.06f){
					focused.setYAcceleration(0.06f);					
				}
				focusedCanJump=false;
			}
			else {
				focusedCanJump=false;
				focused.jump(new Action<Hero>(){
					@Override
					public void act(Hero subject) {
						subject.setYAcceleration(0.06f);
						subject.setJumping(true);
						focusedCanJump=true;
					}

				});
			}
		}
	}
	@Override
	public void onType(KeyBoardEvent event) {
		if(event.keyDown()){
			if('a'==event.getChar()){
				focused.setXAcceleration(-standardAcceleration);
			}
			else if('d'==event.getChar()){
				focused.setXAcceleration(standardAcceleration);
			}
			else if('w'==event.getChar()){
				focused.resize(focused.getWidth(), standingHeight);
				jump();
			}
			else if('s'==event.getChar()){
				if(!focused.isJumping()){
					focused.resize(focused.getWidth(), crouchingHeight);
				}
			}
			else if(event.is(KeyBoardEvent.ESCAPE)||event.is(25)||event.is(197)){
				game.pause();
			}
		}
		else if(event.keyUp()){
			if(event.is(32)){
				focused.setXAcceleration(0f);
			}
			else if(event.is(30)){
				focused.setXAcceleration(0f);
			}
			else if(event.is(17)){
				focused.resize(focused.getWidth(), standingHeight);
			}
			else if(event.is(31)){
				focused.resize(focused.getWidth(), standingHeight);
			}
			else if(event.is(KeyBoardEvent.SPACE)){//space
				jump();
			}
			else if(event.is(KeyBoardEvent.ENTER)){//enter
				if(game.getChatBox()!=null){
					focused.setXAcceleration(0f);
					focused.resize(focused.getWidth(), standingHeight);

					game.getChatBox().setVisible(true);
					game.getChatBox().blinkerOn();
					Hub.handler.giveOnType(game.getChatBox().getDefaultKeyBoardListener());
					Hub.handler.giveOnClick(game.getChatBox());
				}
			}
		}
	}

	@Override
	public boolean isCompetetive(){
		return true;
	}

	@Override
	public boolean continuousKeyboard() {
		return false;
	}
	
	@Override
	protected int getDirection(float x, float y) {

		double angle = Math.atan2(y-(focused.getY()+focused.getHeight()/2f),x-(focused.getX()+focused.getWidth()/2f));
		if(angle<=Math.PI/2f&&angle>=-Math.PI/2f){
			return GameMouseHandler.RIGHT;
		}
		else if(angle>=Math.PI/2f||angle<=-Math.PI/2f){
			return GameMouseHandler.LEFT;
		}
		return -1;
	}
	
	@Override
	protected void performOnPress(ClickEvent event){
		if(hasReleased){
			float dx = event.getX()-(focused.getX()+focused.getWidth()/2f);
			float dy = event.getY()-(focused.getY()+focused.getHeight()/2f);
			if(Math.sqrt(dx*dx+dy*dy)>0.1){
				jump();
				hasReleased = false;
			}
		}
	}

	
	@Override
	public void performOnRelease(ClickEvent event){
		hasReleased = true;
	}
	
	@Override
	public boolean onClick(ClickEvent event) {
		if(event.getAction()==ClickEvent.ACTION_DOWN&&
				pauseButton.isWithin(event.getX(), event.getY())){
			pauseButton.performOnClick(event);
			return true;
		}
		else {
			boolean ret = super.onClick(event);
			return ret;
		}
	}
}
