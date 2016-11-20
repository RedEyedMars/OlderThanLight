package com.rem.otl.game.environment.onstep;

import com.rem.otl.game.hero.Hero;

public class NullOnStepAction extends OnStepAction<Object>{
	@Override
	public void act(Hero subject) {
	}
	@Override
	public void setTarget(Object target){
	}
	@Override
	public int targetType(){
		return 1;
	}
	@Override
	public int getIndex() {
		return -1;
	}
	@Override
	public NullOnStepAction create() {
		return new NullOnStepAction();
	}
}
