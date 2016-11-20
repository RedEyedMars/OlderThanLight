package com.rem.otl.game.environment;

import com.rem.core.Action;

public interface SquareAction <SubjectType,TargetType> extends Action<SubjectType>, Saveable {
	public int targetType();
	public void setTarget(TargetType target);
	public int getIndex();
}
