package com.rem.otl.pc;

import com.rem.otl.game.Game;
import com.rem.otl.game.hero.Hero;
import com.rem.otl.game.menu.MainMenu;
import com.rem.core.gui.graphics.GraphicView;
import com.rem.core.Hub;
import com.rem.core.IFileManager;
import com.rem.core.Setupable;
import com.rem.core.storage.Storage;

public class Main implements Setupable {

	private static final int LOAD_TO_MAIN = 0;
	private static final int LOAD_TO_SOLO = 1;
	@SuppressWarnings("unused")
	private static final int LOAD_TO_HOST = 2;
	@SuppressWarnings("unused")
	private static final int LOAD_TO_JOIN = 3;
	private static int state = LOAD_TO_MAIN;
	private static String filePath = "";
	private static boolean heroColour = Hero.BLACK_BOOL;
	
	public static void main(String[] args) {
		Storage.debug_load = false;
		handleArgs(args);
		Hub.load(new Creator(),new Main(),true,true, true);
	}

	private static boolean handleArgs(String[] args) {
		for(int i=0;i<args.length;++i){
			if("solo".equals(args[i])&&i<args.length-1){
				state = LOAD_TO_SOLO;
				filePath = args[i+1];
				if(i<args.length-2){
					if("black".equals(args[i+2])){
						heroColour = Hero.BLACK_BOOL;
					}
					else if("white".equals(args[i+2])){
						heroColour = Hero.WHITE_BOOL;
					}
				}				
			}
		}
		return false;
	}

	public void setup(){
	}
	public GraphicView getFirstView(){
		if(state==LOAD_TO_MAIN){

			return new MainMenu();
		}
		else if(state==LOAD_TO_SOLO){
			Storage.loadMap(Hub.manager.createInputStream(filePath,IFileManager.RELATIVE));
			return new Game(heroColour,Hub.getNewRandomSeed(),new MainMenu());
		}
		else return null;
	}
}
