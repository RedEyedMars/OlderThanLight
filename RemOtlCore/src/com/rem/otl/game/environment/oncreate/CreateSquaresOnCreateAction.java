package com.rem.otl.game.environment.oncreate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rem.core.Hub;
import com.rem.otl.game.environment.Map;
import com.rem.otl.game.environment.Square;

public class CreateSquaresOnCreateAction extends OnCreateAction {

	private List<Square> list = new ArrayList<Square>();		
	@Override
	public void loadFrom(Iterator<Integer> ints, Iterator<Float> floats){
		int size = ints.next();
		for(int i=0;i<size;++i){
			int index = ints.next();
			if(index>=0){
				list.add((Square) Hub.map.getEntity(Map.getIdFromMult(Map.MULT_ID_TEMPLATE_SQUARE,index+squareIndexOffset)));				
			}
		}
	}
	@Override
	public void act(OnCreateSquare square) {
		square.add(list);
	}
	@SuppressWarnings("unchecked")
	@Override
	protected void saveArgs(List<Object> saveTo){
		saveTo.add(list.size());
		for(int i=0;i<list.size();++i){
			saveTo.add(((List<Square>)Hub.map.getEntity(Map.ID_TEMPLATE_SQUARES)).indexOf(list.get(i)));
		}
	}
	@Override
	public int getIndex() {
		return 3;
	}
	public OnCreateAction create(){
		return new CreateSquaresOnCreateAction();
	}
}
