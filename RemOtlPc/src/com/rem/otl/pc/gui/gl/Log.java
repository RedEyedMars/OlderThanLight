package com.rem.otl.pc.gui.gl;

import com.rem.core.ILog;

public class Log implements ILog{

	private StringBuilder buffer = new StringBuilder();
	public static void log(String string) {
		System.out.println(string);
		
	}

	@Override
	public void debug(String arg0, Object arg1) {
		String bufferedString = buffer.toString();
		
		System.out.println(arg0+"::"+bufferedString+arg1);
		if(!"".equals(bufferedString)){
			buffer = new StringBuilder();
		}
	}

	@Override
	public void bufferDebug(String location, Object toDisplay) {
		buffer.append(""+toDisplay);
	}
	@Override
	public void err(String string) {
		System.err.println(string);
		
	}


}
