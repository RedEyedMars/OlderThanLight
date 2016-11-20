package com.rem.otl.game.environment.update;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rem.core.Hub;
import com.rem.core.storage.Storage;
import com.rem.otl.game.environment.Map;
import com.rem.otl.game.environment.Square;
import com.rem.otl.game.environment.SquareAction;
import com.rem.otl.game.environment.onstep.OnStepSquare;

public class UpdatableSquare extends OnStepSquare {
	protected UpdateAction updateAction;
	private boolean activated = false;
	private List<Square> dependants = new ArrayList<Square>();


	@SuppressWarnings("unused")
	private float originalX = 0f;
	@SuppressWarnings("unused")
	private float originalY = 0f;
	public UpdatableSquare(int actionType,int shapeType, int blackColour, int whiteColour, Iterator<Integer> ints, Iterator<Float> floats) {
		super(actionType-3,shapeType,blackColour, whiteColour,ints, floats);
		this.actionType=actionType;

		int depends = ints.next();
		for(int i=0;i<depends;++i){
			addDependant(Square.create(ints, floats));
		}
		this.originalX = getX();
		this.originalY = getY();
	}
	public UpdatableSquare(int shapeType, int blackColour, int whiteColour,float width, float height,int action, float x, float y,int blackAction,int whiteAction) {
		super(shapeType,blackColour, whiteColour,width,height,blackAction,whiteAction);
		this.actionType=3;
		this.updateAction = UpdateAction.getAction(action).create();		
		this.updateAction.setValue(UpdateAction.X,x);
		this.updateAction.setValue(UpdateAction.Y,y);
		this.updateAction.setTarget(this);
	}

	@Override
	protected void loadActions(Iterator<Integer> ints, Iterator<Float> floats){
		super.loadActions(ints,floats);
		int actionIndex = ints.next();
		this.updateAction = UpdateAction.getAction(actionIndex).create();
		this.updateAction.loadFrom(ints,floats);
		this.updateAction.setTarget(this);
	}
	public void recycle() {
		updateAction.undo();
	}
	public void move(float x, float y){
		super.move(x, y);
		for(Square square:dependants){
			square.reposition(square.getX()-x,
					      square.getY()-y);
		}
	}
	public void activate(){
		if(!this.activated){
			this.updateAction.onActivate();
		}
		this.activated = true;
	}
	public void deactivate(){
		this.activated = false;
		this.updateAction.onDeactivate();
	}
	public UpdateAction getAction(){
		return updateAction;
	}

	public void update(double secondsSinceLastFrame){
		if(activated){
			this.updateAction.act(secondsSinceLastFrame);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addDependant(Square square){
		this.dependants.add(square);
		for(SquareAction action:square.getActions()){
			if(action==null)continue;
			if(action.targetType()==1){
				action.setTarget(this);
			}
		}
		addChild(square);
	}
	public List<Square> getDependants() {
		return dependants;
	}
	
	public void display(){
		for(Square square:dependants){
			Hub.map.addEntity(Map.ID_DISPLAY_SQUARE,square);
		}
	}
	public void undisplay(){
		for(Square square:dependants){
			Hub.map.addEntity(Map.ID_UNDISPLAY_SQUARE,square);
		}
	}

	public void run(){
		if(this.updateAction.getInt(UpdateAction.DEFAULT_STATE)==UpdateAction.DEFAULT_STATE_ACTIVATE){
			activate();
		}
		else {
			deactivate();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<SquareAction> getActions() {
		List<SquareAction> list = super.getActions();
		list.add(updateAction);
		return list;
	}
	@Override
	public void saveTo(List<Object> toSave) {
		super.saveTo(toSave);
		toSave.add(dependants.size());
		for(Square square:dependants){
			if(Storage.debug_save)System.out.print('\t');
			square.saveTo(toSave);
		}
		if(Storage.debug_save)System.out.print('\n');
	}
	
	public boolean isActive(){
		return activated;
	}


}
