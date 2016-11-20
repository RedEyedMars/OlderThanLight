package com.rem.otl.game.menu;

import com.rem.core.Hub;
import com.rem.core.gui.graphics.GraphicEntity;
import com.rem.core.gui.inputs.ClickEvent;
import com.rem.core.gui.graphics.MenuButton;

public class DuoMenu extends Menu {
	public DuoMenu() {
		super();
		GraphicEntity button = new MenuButton("Host"){
			@Override
			public void performOnRelease(ClickEvent e){
					host();
			}
		};
		button.reposition(0.2f,0.51f);
		addChild(button);
		
		button = new MenuButton("Join"){
			@Override
			public void performOnRelease(ClickEvent e){
				join();
			}
		};
		button.reposition(0.2f,0.35f);
		addChild(button);
		
		button = new MenuButton("Return"){
			@Override
			public void performOnRelease(ClickEvent e){
					returnToMain();
			}
		};
		button.reposition(0.2f,0.19f);
		addChild(button);
	}
	
	public void host(){
		Hub.gui.setView(new HostMenu());
	}
	
	public void join(){
		Hub.gui.setView(new JoinMenu());
	}

	public void returnToMain(){
		Hub.gui.setView(new MainMenu());
	}
}
