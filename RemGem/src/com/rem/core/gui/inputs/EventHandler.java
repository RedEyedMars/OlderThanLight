package com.rem.core.gui.inputs;

import java.util.LinkedList;
import java.util.Stack;

import com.rem.core.Hub;
import com.rem.core.gui.inputs.ClickEvent;
import com.rem.core.gui.inputs.InputEvent;
import com.rem.core.gui.inputs.KeyBoardEvent;

public class EventHandler extends Thread {
	public static boolean debug = false;
	public static boolean continuousKeyboard = false;
	private Boolean running = true;
	private LinkedList<InputEvent> queue = new LinkedList<InputEvent>();
	private Stack<KeyBoardListener> keyboardListener = new Stack<KeyBoardListener>();
	private Stack<MouseListener> mouseListener = new Stack<MouseListener>();
	private boolean hasEventToProcess = false;



	public EventHandler(){
		super();
		start();
	}

	@Override
	public void run(){
		try{
			while(running){
				synchronized(this){
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(EventHandler.debug)Hub.log.debug("EventHandler", ">>"+queue.size());
				while(running&&!queue.isEmpty()){
					InputEvent event = queue.removeFirst();
					if(event.getType()==InputEvent.CLICK){
						mouseListener.peek().onClick((ClickEvent) event);
					}
					else if(event.getType()==InputEvent.KEYBOARD){
						keyboardListener.peek().onType((KeyBoardEvent) event);
					}
					else if(event.getType()==InputEvent.WHEEL){
						mouseListener.peek().onMouseScroll(((WheelEvent) event).getAmount());
					}
					else if(event.getType()==InputEvent.HOVER){
						mouseListener.peek().onHover((HoverEvent) event);
					}
					if(EventHandler.debug)Hub.log.debug("EventHandler", queue.size());

				}
				if(EventHandler.debug)Hub.log.debug("EventHandler", "release");
				hasEventToProcess =false;
				synchronized(this){
					this.notifyAll();
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			synchronized(this){
				this.notifyAll();					
			}
			Hub.gui.setFinished(true);
		}
	}

	public void handleEvent(InputEvent event){
		queue.add(event);
		hasEventToProcess=true;
	}
	public void end() {
		clear();
		running = false;
		synchronized(this){
			this.notifyAll();
		}
	}

	public void processEvents() {
		if(EventHandler.debug)Hub.log.debug("EventHandler", "processEvents");
		if(hasEventToProcess&&running){
			synchronized(this){
				this.notifyAll();
			}
			synchronized(this){
				try {
					if(hasEventToProcess){
						this.wait();
						if(EventHandler.debug)Hub.log.debug("EventHandler", "RELEASED");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}


	public void giveOnClick(MouseListener listener) {
		mouseListener.push(listener);
		//System.out.println("push"+mouseListener.size()+listener.getClass());
	}

	public void removeOnClick(MouseListener listener) {
		while(!mouseListener.isEmpty()&&mouseListener.pop()!=listener){		
		}
		//System.out.println("pop"+mouseListener.size()+mouseListener.peek().getClass());
	}


	public void giveOnType(KeyBoardListener listener) {
		if(!Hub.gui.isKeyBoardShowing()||keyboardListener.isEmpty()||keyboardListener.peek()==Hub.genericKeyBoardListener){
			if(listener!=Hub.genericKeyBoardListener){
				Hub.gui.showKeyBoardDisplay(listener);
			}
		}
		keyboardListener.push(listener);

		continuousKeyboard = listener.continuousKeyboard();

	}
	public void removeOnType(KeyBoardListener listener) {
		if(Hub.gui.isKeyBoardShowing()||!keyboardListener.empty()){			
			if(keyboardListener.peek()==listener){
				keyboardListener.pop();
			}
			if(keyboardListener.isEmpty()||keyboardListener.peek()==Hub.genericKeyBoardListener){
				Hub.gui.hideKeyBoardDisplay(listener);
			}
		}

		if(!keyboardListener.empty()){
			continuousKeyboard = keyboardListener.peek().continuousKeyboard();
		}
		//System.out.println("pop"+mouseListener.size()+mouseListener.peek().getClass());
	}
	public void clear(){
		this.keyboardListener.clear();
		this.keyboardListener.push(Hub.genericKeyBoardListener);
		this.mouseListener.clear();
		this.mouseListener.push(Hub.genericMouseListener);
	}
}
