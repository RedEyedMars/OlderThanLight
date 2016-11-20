package com.rem.otl.game.environment.oncreate;

import java.util.Iterator;
import java.util.List;

import com.rem.core.Hub;
import com.rem.otl.game.environment.Map;
import com.rem.otl.game.environment.Square;

public class CreateSquareOnCreateAction extends OnCreateAction {

	private Square square;
	@Override
	public void loadFrom(Iterator<Integer> ints, Iterator<Float> floats){
		int index = ints.next();
		if(index<(Integer)Hub.map.getEntity(Map.ID_TEMPLATE_SQUARE_SIZE)){
			square = (Square) Hub.map.getEntity(Map.getIdFromMult(Map.MULT_ID_TEMPLATE_SQUARE,index));
		}
		else {
			square = new Square(2,2,0.05f,0.05f);	
		}
		
	}
	@Override
	public void act(OnCreateSquare square) {
		square.add(this.square);
	}
	@SuppressWarnings("unchecked")
	@Override
	protected void saveArgs(List<Object> saveTo){
		if(!((List<Square>)Hub.map.getEntity(Map.ID_TEMPLATE_SQUARES)).contains(square)){
			Hub.map.addEntity(Map.ID_TEMPLATE_SQUARES,square);
		}
		saveTo.add(((List<Square>)Hub.map.getEntity(Map.ID_TEMPLATE_SQUARES)).indexOf(square));
	}
	@Override
	public int getIndex() {
		return 2;
	}
	public OnCreateAction create(){
		return new CreateSquareOnCreateAction();
	}
}
