package com.rem.otl.game.environment.program;

public interface Statable{
	public ProgramState getState();
	public void setState(ProgramState state);
}
