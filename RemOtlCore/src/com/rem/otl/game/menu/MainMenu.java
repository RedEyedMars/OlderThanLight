package com.rem.otl.game.menu;

import java.io.InputStream;
import com.rem.core.gui.graphics.MenuButton;
import com.rem.core.Hub;
import com.rem.core.gui.graphics.GraphicEntity;
import com.rem.core.gui.inputs.ClickEvent;
import com.rem.core.storage.Resource;
import com.rem.core.storage.Storage;
import com.rem.duo.client.Client;
import com.rem.otl.game.Game;
import com.rem.otl.game.hero.Hero;

public class MainMenu extends Menu{

	public MainMenu(){
		super();		
		GraphicEntity button = new MenuButton("Solo"){
			@Override
			public void performOnRelease(ClickEvent e){
				solo();
			}
		};
		button.reposition(0.2f,0.51f);
		addChild(button);

		button = new MenuButton("Duo"){
			@Override
			public void performOnRelease(ClickEvent e){
				duo();
			}
		};
		button.reposition(0.2f,0.35f);
		addChild(button);

		/*
		button = new MenuButton("Editors"){
			@Override
			public void performOnRelease(MotionEvent e){
				editor();
			}
		};
		button.reposition(0.2f,0.19f);
		addChild(button);*/

		final MainMenu self = this;
		button = new IconMenuButton("music_player_icons",4){
			{
				icon.resize(0.08f, 0.08f);
			}
			@Override
			public void performOnRelease(ClickEvent e){
				Hub.gui.setView(new CreditMenu(self));
			}
			@Override
			public void resize(float w, float h){
				super.resize(w, h);
				if(icon!=null){
					left.resize(w*0.5f,h);
					mid.resize(w*0.0f,h);
					right.resize(w*0.5f,h);			
					icon.resize(0.08f,0.08f);
				}
			}
		};
		button.resize(0.09f, 0.08f);
		button.reposition(0.03f,0.77f);
		addChild(button);
	}

	@Override
	public void onAddToDrawable(){
		if(Client.isConnected()){
			Client.endConnectionToTheServer();
		}
		super.onAddToDrawable();
	}

	public void solo(){
		Resource<InputStream> file = GetFileMenu.getFile(this,"maps/races",false);
		if(file!=null){
			Storage.loadMap(file);
			Hub.gui.setView(new Game(Hero.BLACK_BOOL,Hub.getNewRandomSeed(),this));
		}
	}
	public void duo(){
		Hub.gui.setView(new DuoMenu());
	}


	public void editor(){
		Hub.gui.setView(new EditorMenu());
	}

	public void startHighscores(){
	}


}
