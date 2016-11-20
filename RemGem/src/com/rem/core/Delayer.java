package com.rem.core;

import com.rem.core.storage.Storage;
import com.rem.duo.client.Client;

public class Delayer {
	private static final int NONE = -1;
	private static final int MASTER = 0;
	private static final int SLAVE = 1;
	private static final int FINISHED = 2;
	public static boolean debug = false;
	private int state;
	private long travelTimeCorrection = 0;
	private long gameTime = 0;
	private long travelDelay = 0;
	private long correction = 0;
	private long lastFrame = 0;
	
	public Delayer(){
		state = NONE;
		travelTimeCorrection = 0;
		travelDelay = 0;
		gameTime = 0;
		correction = 0;
		lastFrame = 0;
	}

	public void end(){
		state = FINISHED;
		synchronized(this){
			this.notifyAll();
		}

	}
	public void updateState(){
		if(state==NONE){
			lastFrame = System.currentTimeMillis();
		}
		if(Client.isConnected()&&Hub.map!=null){
			if(Storage.getFileName(Hub.map)==null){
				state = SLAVE;
			}
			else {
				state = MASTER;
			}
		}
		else {
			state = NONE;
			gameTime = 0;
			travelTimeCorrection = 0;
			lastFrame = System.currentTimeMillis();
			correction = 0;
			travelDelay = 0;
			travelTimeCorrection = 0;
		}
	}
	public void delay(long travelDelay) {
		if(state==MASTER){
			travelTimeCorrection =-(travelDelay-this.travelDelay);
			
		}
		else if(state==SLAVE){
			travelTimeCorrection = (travelDelay-this.travelDelay);
		}
		this.correction += travelTimeCorrection;
		this.travelDelay = travelDelay;

		if(Delayer.debug&&state==SLAVE){
			Hub.log.debug("Delayer.delay", "travelTimeDelay:"+travelDelay);
			Hub.log.debug("Delayer.delay", "travelTimeCorrection:"+travelTimeCorrection);
		}
	}
	public void updateMap(){
		double secondsSinceLastFrame = (System.currentTimeMillis()-lastFrame)/1000.0;
		lastFrame = System.currentTimeMillis();
		Wrapper<Double> seconds = new Wrapper<Double>(secondsSinceLastFrame);
		if(correction!=0){
			correction = calculateCorrection(seconds,correction/1000.0);			
		}
		gameTime+=seconds.get()*1000;
		Hub.map.update(seconds.get());
	}
	private long calculateCorrection(Wrapper<Double> seconds, double secondsCorrection){
		long correctionCorrection = (long) (secondsCorrection*1000L);
		double secondsSinceLastFrame = seconds.get();
		if(secondsCorrection>0){
			if(secondsCorrection>secondsSinceLastFrame){
				correctionCorrection-=secondsSinceLastFrame*1000.0/2;
				secondsSinceLastFrame = secondsSinceLastFrame/2;
			}
			else {
				secondsSinceLastFrame-=secondsCorrection;
				correctionCorrection = 0;
			}
		}
		else if(secondsCorrection<0){
			if(-secondsCorrection>secondsSinceLastFrame){
				correctionCorrection+=secondsSinceLastFrame*1000.0/2;
				secondsSinceLastFrame = secondsSinceLastFrame*3/2;
			}
			else {
				secondsSinceLastFrame-=secondsCorrection;
				correctionCorrection = 0;
			}
		}
		seconds.set(secondsSinceLastFrame);
		return correctionCorrection;
	}

	public long getGameTime() {
		return this.gameTime ;
	}

	private class Wrapper <Type extends Object>{
		private Type value;
		public Wrapper(Type value){
			this.value  = value;
		}
		public void set(Type value){
			this.value = value;
		}
		public Type get(){
			return this.value;
		}
	}
}
