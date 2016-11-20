package com.rem.otl.game;

import com.rem.core.Hub;
import com.rem.duo.Acceptor;
import com.rem.duo.client.Client;
import com.rem.otl.duo.messages.HeroEndGameMessage;
import com.rem.otl.game.hero.Hero;
import com.rem.otl.game.menu.TransitionMenu;

public class GameEndState implements Acceptor{

	private static GameEndState currentState;
	private TransitionMenu menu=null;
	private String previousMapName=null;
	private String nextMapFileName=null;
	private String nextMapName=null;
	private Boolean myColour=null;
	private Boolean theirColour=null;
	private Boolean blackWinner=null;
	private Long blackTime=null;
	private Boolean whiteWinner=null;
	private Long whiteTime=null;


	public GameEndState() {
		
	}


	public void setMyColour(boolean colourToControl) {
		myColour = colourToControl;
	}	

	public void setTheirColour(boolean colourToControl) {
		this.theirColour = colourToControl;
	}
	
	public void setStats(boolean colourToControl, boolean successful, long time) {
		if(colourToControl==Hero.BLACK_BOOL){
			blackWinner = successful;
			blackTime = time;
		}
		else if(colourToControl==Hero.WHITE_BOOL){
			whiteWinner = successful;
			whiteTime = time;
		}
	}

	public void setOpenColour(boolean colour) {
		if(myColour==null){
			myColour = colour;
		}
		else if(theirColour==null){
			theirColour = colour;
		}
	}
	
	public void send(boolean colourToControl, boolean isWinner, long time){
		if(Client.isConnected()){
			Client.pass(new HeroEndGameMessage(colourToControl,isWinner,time));
		}
	}



	public boolean isFinished(){
		if(myColour!=null&&theirColour!=null){
			return true;
		}
		else {
			return false;
		}
	}


	public String getNextMap() {
		return nextMapFileName;
	}


	public void setPreviousMapName(String mapName) {
		this.previousMapName = mapName;
	}


	public void setNextMapName(String mapName) {
		this.nextMapName = mapName;
	}


	public void setNextMapFileName(String nextMapFileName) {
		this.nextMapFileName = nextMapFileName;
	}


	public void finish() {
		Hub.loadMapFromFileName(nextMapFileName);
		if(theirColour!=null){
			if(theirColour){
				menu.verifyWhoWon(blackWinner,blackTime);
			}
			else {
				menu.verifyWhoWon(whiteWinner,whiteTime);				
			}			
		}
		if(nextMapName!=null){			
			menu.canProceed(previousMapName, nextMapName, myColour);
		}
		currentState = null;
	}


	public boolean partnerHasWon() {
		if(theirColour==null)return false;
		if(theirColour==Hero.BLACK_BOOL){
			return blackWinner!=null&&blackWinner==true;
		}
		else if(theirColour==Hero.WHITE_BOOL){
			return whiteWinner!=null&&whiteWinner==true;
		}
		else return false;
	}

	public TransitionMenu getMenu() {
		return menu;
	}


	public void setMenu(TransitionMenu menu) {
		this.menu = menu;
	}


	public static GameEndState create() {
		if(currentState==null){
			currentState = new GameEndState();
		}	
		Client.addAcceptor("endState",currentState);
		return currentState;
	}


	@Override
	public void accept(String command, Object object) {
		if("theirColour".equals(command)){
			setTheirColour((Boolean)object);
		}
		else if("stats".equals(command)){
			Object[] args = (Object[]) object;
			setStats((Boolean)args[0],(Boolean)args[1],(Long)args[2]);
			
		}
		else if("saveTime".equals(command)){
			if(menu!=null){
				menu.saveTime((String)object);
			}
		}
		else if("finishIfFinished".equals(command)){
			if(isFinished()){
				//Finish the game.
				finish();
			}
		}
	}


}
