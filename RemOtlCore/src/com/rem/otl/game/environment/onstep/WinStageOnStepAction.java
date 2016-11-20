package com.rem.otl.game.environment.onstep;

import java.util.List;

import com.rem.core.Hub;
import com.rem.otl.game.environment.Map;
import com.rem.otl.game.hero.Hero;

public class WinStageOnStepAction extends OnStepAction<Integer>{
	@Override
	public void act(Hero subject) {
		String nextMap = (String) Hub.map.getEntity(Map.getIdFromMult(Map.MULT_ID_NEXT_MAP, target));
		if(nextMap!=null){
			subject.getGame().winGame(subject.isBlack(),nextMap);
		}
		else {
			Hub.log.debug("WinStageOnStepAction.act","Map not found:"+target);
		}
	}
	@Override
	public boolean isPassible(){
		return false;
	}
	@Override
	public int getIndex() {
		return 6;
	}
	public Integer getTarget(){
		return target;
	}
	public int targetType(){
		return 2;
	}
	public void saveTo(List<Object> saveTo){
		saveTo.add(getIndex());
		if(target!=null){
			saveTo.add(target);
		}
		else {
			saveTo.add(-2);
		}
	}
	@Override
	public OnStepAction<Integer> create() {
		return new WinStageOnStepAction();
	}
}
