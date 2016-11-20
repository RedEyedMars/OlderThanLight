package com.rem.otl.game.environment.oncreate;

import java.util.Iterator;
import java.util.List;

import com.rem.core.Hub;
import com.rem.otl.game.environment.Map;
import com.rem.otl.game.environment.Square;

public class DisplayListOnCreateAction extends OnCreateAction {

	private int indexOfList;
	@Override
	public void loadFrom(Iterator<Integer> ints, Iterator<Float> floats){
		indexOfList=ints.next();
	}
	@SuppressWarnings("unchecked")
	@Override
	public void act(OnCreateSquare square) {
		List<Object> list = (List<Object>) square.getData().get(indexOfList);
		float dx = Float.MAX_VALUE;
		float dy = Float.MAX_VALUE;
		for(Object sqr:list){
			if(((Square)sqr).getX()<dx){
				dx=((Square)sqr).getX();
			}
			if(((Square)sqr).getY()<dy){
				dy=((Square)sqr).getY();
			}
		}
		dx=square.getX()-dx;
		dy=square.getY()-dy;
		for(Object sqr:list){
			Square s = Square.copy((Square)sqr);
			s.reposition(s.getX()+dx,
					s.getY()+dy);
			square.addChild(s);
			if(Hub.map!=null){
				Hub.map.addEntity(Map.ID_DISPLAY_SQUARE,s);
			}
		}
	}
	@Override
	protected void saveArgs(List<Object> saveTo){
		saveTo.add(indexOfList);
	}
	@Override
	public int getIndex() {
		return 7;
	}
	public OnCreateAction create(){
		return new DisplayListOnCreateAction();
	}

}
