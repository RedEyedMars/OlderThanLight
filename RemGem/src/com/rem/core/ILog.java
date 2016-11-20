package com.rem.core;

public interface ILog {
	public void debug(String location, Object toDisplay);

	public void err(String string);

	public void bufferDebug(String location, Object toDisplay);
}
