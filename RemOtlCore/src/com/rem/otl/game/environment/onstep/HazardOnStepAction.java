package com.rem.otl.game.environment.onstep;

import com.rem.otl.game.environment.Square;
import com.rem.otl.game.hero.Hero;

public class HazardOnStepAction extends OnStepAction<Square> {
	@Override
	public void act(Hero subject) {
		subject.getGame().loseGame(subject.isBlack());
	}
	public boolean isPassible(){
		return false;
	}
	@Override
	public int getIndex() {
		return 2;
	}
	@Override
	public OnStepAction<Square> create() {
		return this;
	}
}
