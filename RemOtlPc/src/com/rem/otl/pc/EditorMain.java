package com.rem.otl.pc;

import com.rem.duo.client.Client;
import com.rem.otl.game.menu.EditorMenu;
import com.rem.core.gui.graphics.GraphicView;
import com.rem.core.Hub;
import com.rem.core.Setupable;

public class EditorMain implements Setupable {
	public static void main(String[] args) {

		Hub.load(new Creator(),new EditorMain(),false,false, true);
	}

	public void setup(){

	}
	public GraphicView getFirstView(){
		return new EditorMenu();
	}

	public void cleanup() {
		Client.endConnectionToTheServer();
	}

}
