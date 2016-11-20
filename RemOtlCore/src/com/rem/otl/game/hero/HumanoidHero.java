package com.rem.otl.game.hero;

import com.rem.core.Action;
import com.rem.otl.game.Game;

public class HumanoidHero extends Hero {

	public HumanoidHero(Game game, boolean colour) {
		super("human_heroes",game, colour);
		setAnimation(new HumanoidHeroAnimation());
	}
	
	@Override
	public void jump(Action<Hero> action){
		((HumanoidHeroAnimation)animation).jump(this,action);
	}

}
