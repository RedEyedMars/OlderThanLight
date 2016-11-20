package com.rem.otl.game.environment.onstep;

import com.rem.otl.game.environment.Square;
import com.rem.otl.game.hero.Hero;

public class SafeOnStepAction extends OnStepAction<Square>{
	@Override
	public void act(Hero subject) {
	}		
	@Override
	public int getIndex() {
		return 0;
	}
	@Override
	public OnStepAction<Square> create() {
		return OnStepAction.safe;
	}
}
