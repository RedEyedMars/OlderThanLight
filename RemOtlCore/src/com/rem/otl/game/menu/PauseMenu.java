package com.rem.otl.game.menu;

import com.rem.core.Hub;
import com.rem.core.gui.graphics.GraphicEntity;
import com.rem.core.gui.inputs.ClickEvent;
import com.rem.core.gui.inputs.KeyBoardEvent;
import com.rem.core.gui.inputs.KeyBoardListener;
import com.rem.otl.game.Game;
import com.rem.core.gui.graphics.MenuButton;

public class PauseMenu extends GraphicEntity implements KeyBoardListener {

	private boolean paused = false;
	public PauseMenu(final Game game) {
		super("blank",Hub.BOT_LAYER);

		MenuButton resumeButton = new MenuButton("Resume"){
			@Override
			public void performOnRelease(ClickEvent e){
				unpause();
			}
		};
		resumeButton.reposition(0.2f,0.51f);
		addChild(resumeButton);

		MenuButton restartButton = new MenuButton("Restart"){
			@Override
			public void performOnRelease(ClickEvent e){
				unpause();
				game.restart();
			}
		};
		restartButton.reposition(0.2f,0.35f);
		addChild(restartButton);

		MenuButton returnToMain = new MenuButton("Return to Main Menu"){
			@Override
			public void performOnRelease(ClickEvent e){
				unpause();
				game.returnToParent();
			}
		};
		returnToMain.reposition(0.2f,0.19f);
		addChild(returnToMain);

	}

	public boolean isPaused() {
		return paused;
	}
	public void pause(){
		this.paused = true;
		this.setVisible(true);
	}

	public void unpause(){
		this.paused = false;
		this.setVisible(false);
		removeInputs();
	}
	public void removeInputs(){
		Hub.handler.removeOnClick(this);
		Hub.handler.removeOnType(this);
	}

	@Override
	public void onType(KeyBoardEvent event) {
		if(event.keyDown()){
			if(event.is(KeyBoardEvent.ESCAPE)||event.is(25)||event.is(197)){
				unpause();
			}
		}
	}

	@Override
	public boolean continuousKeyboard() {
		return false;
	}
}
