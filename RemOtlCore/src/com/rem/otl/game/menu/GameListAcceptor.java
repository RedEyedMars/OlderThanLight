package com.rem.otl.game.menu;

import java.util.ArrayList;

import com.rem.core.gui.graphics.MenuButton;
import com.rem.duo.Acceptor;

public class GameListAcceptor extends ArrayList<String[]> implements Acceptor {

	private static final long serialVersionUID = 6849911565108959389L;
	private MenuButton gameList;
	public GameListAcceptor(MenuButton gameList){
		this.gameList = gameList;
	}
	@Override
	public void accept(String command, Object object) {
		if(gameList==null)return;
		if("addGame".equals(command)){
			Object[] args = (Object[])object;
			String[] game = new String[3];
			game[0] = (String) args[0];
			game[1] = (String) args[1];
			game[2] = (String) args[2];
			if(add(game)){
				gameList.changeText("");
			}
		}
		else if("clearGames".equals(command)){
			clear();
			gameList.changeText("");
		}
	}
}
