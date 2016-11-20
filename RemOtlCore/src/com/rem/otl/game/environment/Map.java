package com.rem.otl.game.environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import com.rem.core.Action;
import com.rem.core.Hub;
import com.rem.core.IFileManager;
import com.rem.core.environment.Environment;
import com.rem.core.storage.Resource;
import com.rem.core.storage.Storage;
import com.rem.core.storage.loader.FloatListLoader;
import com.rem.core.storage.loader.ListLoader;
import com.rem.core.storage.loader.Loader;
import com.rem.core.storage.saver.Saver;
import com.rem.duo.client.Client;
import com.rem.duo.messages.ActionMessage;
import com.rem.otl.duo.messages.LoadMapMessage;
import com.rem.otl.game.environment.onstep.OnStepSquare;
import com.rem.otl.game.environment.onstep.WinStageOnStepAction;
import com.rem.otl.game.environment.update.UpdatableSquare;
import com.rem.otl.game.hero.Hero;
import com.rem.otl.game.mode.GameMode;
import com.rem.otl.game.mode.OverheadMode;
import com.rem.otl.game.mode.PlatformMode;
import com.rem.otl.game.mode.RaceMode;

public class Map extends Environment {

	protected static final float gridSizeX = 100f;
	protected static final float gridSizeY = 100f;
	public static final boolean X_axis = true;
	public static final boolean Y_axis = false;

	public static final int ID_MAP_ID = 0;
	public static final int ID_BLACK_X = 1;
	public static final int ID_BLACK_Y = 2;
	public static final int ID_WHITE_X = 3;
	public static final int ID_WHITE_Y = 4;
	public static final int ID_TEMPLATE_SQUARE_SIZE = 6;
	public static final int ID_SQUARE_SIZE = 8;
	public static final int ID_NEXT_MAP_SIZE = 9;
	public static final int ID_GAME_MODE = 12;
	public static final int ID_SQUARES = 13;
	public static final int ID_FUNCTIONAL_SQUARES = 17;
	public static final int ID_UPDATE_SQUARES = 23;
	public static final int ID_TEMPLATE_SQUARES = 31;
	public static final int ID_NEXT_MAP = 37;
	public static final int ID_BLACK_HERO = 47;
	public static final int ID_WHITE_HERO = 53;
	public static final int ID_BOTH_HEROES = 59;
	public static final int ID_DISPLAY_SQUARE = 61;
	public static final int ID_UNDISPLAY_SQUARE = 67;
	public static final int ID_IS_LIGHT_DEPENDENT = 71;
	public static final int MULT_ID_TEMPLATE_SQUARE = 5;
	public static final int MULT_ID_SQUARE = 7;
	public static final int MULT_ID_FUNCTIONAL_SQUARE = 11;
	public static final int MULT_ID_UPDATE_SQUARE = 41;
	public static final int MULT_ID_NEXT_MAP = 29;
	public static int getIdFromMult(int multiplierId, int index){
		return multiplierId*(index+2);
	}
	public static int getIdFromHeroBool(boolean heroBoolean){
		if(Hero.BLACK_BOOL==heroBoolean){
			return ID_BLACK_HERO;
		}
		else if(Hero.WHITE_BOOL==heroBoolean){
			return ID_WHITE_HERO;
		}
		else return -1;
	}

	private Hero[] heroes = new Hero[]{null,null};
	private List<Square> allSquares = new ArrayList<Square>();
	private List<OnStepSquare> functionalSquares = new ArrayList<OnStepSquare>();
	private List<UpdatableSquare> updateSquares = new ArrayList<UpdatableSquare>();
	private List<Creatable> onCreates = new ArrayList<Creatable>();
	private List<Square> templateSquares = new ArrayList<Square>();
	private List<Square> displaySquares = new ArrayList<Square>();

	private List<String> nextMaps = new ArrayList<String>();

	private java.util.Map<Square,List<OnStepSquare>> adjacentSquares = new HashMap<Square,List<OnStepSquare>>();

	private List<Float> startingPositions = new ArrayList<Float>();

	private int mapId=0;
	private int visibleColour=0;

	private Map() {
		super("blank");
		this.setVisible(false);
	}

	public String getName(){
		return name;
	}
	public String getFileName() {
		return fileName;
	}

	public List<OnStepSquare> getFunctionalSquares() {
		return functionalSquares;
	}

	public List<UpdatableSquare> getUpdateSquares() {
		return updateSquares;
	}

	public void addSquare(Square square){
		if(square==null)return;
		displaySquare(square);
		allSquares.add(square);
		addChild(square);

	}


	public void displaySquare(Square square){
		if(square==null)return;
		if(square.isFunctional()){
			functionalSquares.add((OnStepSquare)square);
		}
		if(square instanceof UpdatableSquare){
			updateSquares.add((UpdatableSquare)square);
			((UpdatableSquare)square).display();
		}
		if(square instanceof Creatable){
			onCreates.add((Creatable) square);
		}
		displaySquares.add(square);
		square.displayFor(visibleColour);
	}
	public void unDisplaySquare(Square square) {
		if(square==null)return;
		if(square.isFunctional()){
			functionalSquares.remove((OnStepSquare)square);
		}
		if(square instanceof UpdatableSquare){
			updateSquares.remove((UpdatableSquare)square);
			((UpdatableSquare)square).undisplay();
		}
		if(square instanceof Creatable){
			onCreates.remove((Creatable) square);
		}
		displaySquares.remove(square);
	}

	public void setVisibleSquares(int colour){
		for(Square square:displaySquares){
			square.displayFor(colour);
		}
		visibleColour = colour;
	}
	public int getVisibleColour() {
		return visibleColour;
	}

	private float xOffset = 0f;
	private float yOffset = 0f;
	private boolean lightDependency = false;
	@Override
	public void reposition(float x, float y){
		xOffset = x-getX();
		yOffset = y-getY();
		super.reposition(x,y);
	}
	@Override
	public float offsetX(int index){
		if(index!=0){
			return getChild(index).getX()+xOffset-getX();
		}
		else {
			return -getX();
		}
	}
	@Override
	public float offsetY(int index){
		if(index!=0){
			return getChild(index).getY()+yOffset-getY();
		}
		else {
			return -getY();
		}
	}

	public boolean isMallible() {
		return true;
	}

	public List<Square> getSquares() {
		return allSquares;
	}

	public void onCreate() {
		for(Creatable square:onCreates){
			square.create();
		}
		for(OnStepSquare square:functionalSquares){
			List<OnStepSquare> list = new ArrayList<OnStepSquare>();
			for(OnStepSquare adj:functionalSquares){
				if(square!=adj){
					if(square.isWithin(adj.getX(), adj.getY())||
							square.isWithin(adj.getX()+adj.getWidth(), adj.getY()+adj.getHeight())||
							square.isWithin(adj.getX()+adj.getWidth(), adj.getY())||
							square.isWithin(adj.getX(), adj.getY()+adj.getHeight())){
						list.add(adj);
					}
					else if(adj.isWithin(square.getX(), square.getY())||
							adj.isWithin(square.getX()+square.getWidth(), square.getY()+square.getHeight())||
							adj.isWithin(square.getX()+square.getWidth(), square.getY())||
							adj.isWithin(square.getX(), square.getY()+square.getHeight())){
						list.add(adj);
					}
				}
			}
			adjacentSquares.put(square,list);
		}
	}
	public List<OnStepSquare> getAdjacentSquares(OnStepSquare q) {
		return adjacentSquares.get(q);
	}

	public List<Square> getTemplateSquares() {
		return templateSquares;
	}

	public void addTemplateSquare(Square square) {
		templateSquares.add(square);
	}
	public Square isWithinWall(Square target, Hero accordingTo) {
		for(int i=functionalSquares.size()-1;i>=0;--i){
			OnStepSquare square = functionalSquares.get(i);
			if(square==target){
				continue;
			}
			if(target.isCompletelyWithin(square)&&square.getOnHitAction(accordingTo).isPassible()){			
				return null;
			}
			else if(square.getOnHitAction(accordingTo).getIndex()==1
					&&(square.isWithin(target))){
				return square;
			}
		}
		return null;
	}

	public static Map createMap(int id){
		if(id==0){
			return createMap(-60);
		}

		Map map = new Map();
		map.setMapId(id);
		return map;

	}

	public String getNextMap(Integer target) {
		if(target>=0&&target<nextMaps.size()){
			return nextMaps.get(target);
		}
		return null;
	}
	public Integer setNextMap(String name){
		if(!nextMaps.contains(name)){
			nextMaps.add(name);
		}
		return nextMaps.indexOf(name);
	}

	public Loader getLoad(){
		return new MapLoader();
	}

	public void setStartPosition(int colour, float x, float y) {
		startingPositions.set(colour==Hero.BLACK_INT?0:colour==Hero.WHITE_INT?2:-1,x);
		startingPositions.set(colour==Hero.BLACK_INT?1:colour==Hero.WHITE_INT?3:-1,y);
	}
	public float getStartingXPosition(int colour) {
		return startingPositions.get(colour==Hero.BLACK_INT?0:colour==Hero.WHITE_INT?2:-1);
	}
	public float getStartingYPosition(int colour) {
		return startingPositions.get(colour==Hero.BLACK_INT?1:colour==Hero.WHITE_INT?3:-1);
	}
	public float getFloatCoordinate(int value,boolean axis){
		if(axis==X_axis){
			return ((float)value)/gridSizeX;
		}
		else if(axis==Y_axis){
			return ((float)value)/gridSizeY;
		}
		else {
			return 0.5f;
		}
	}
	public int getIntCoordinate(float value, boolean axis){
		if(axis==X_axis){
			if(value<=0){
				return (int) (value*gridSizeX-0.5f);
			}
			else {
				return (int) (value*gridSizeX+0.5f);
			}
		}
		else if(axis==Y_axis){
			if(value<=0){
				return (int) (value*gridSizeY-0.5f);
			}
			else {
				return (int) (value*gridSizeY+0.5f);
			}
		}
		else {
			return (int) (gridSizeX/2);
		}
	}

	public int getMapId(){
		return mapId;
	}
	public void setMapId(int id){
		this.mapId = id;
	}


	public boolean isLightDependent() {
		return lightDependency ;
	}
	public void setLightDependency(boolean dependency){
		this.lightDependency = dependency;
	}
	public GameMode getGameMode() {
		switch((mapId+20)/-20){
		case 0:{
			return new OverheadMode();
		}
		case 1:{
			return new PlatformMode();
		}
		case 2:{
			return new RaceMode();
		}
		}
		return new RaceMode();
	}

	private class MapLoader implements Loader {
		@Override
		public void load(final Iterator<Integer> ints,final Iterator<Float> floats,final Iterator<String> strings) {			
			if(Hub.map!=null){
				if(name.equals("Restart")){
					name = Hub.map.getName();
				}
				if("Restart".equals(fileName)){
					fileName = Hub.map.getFileName();
				}
			}
			setMapId(ints.next());
			lightDependency = ints.next()==0;
			while(strings.hasNext()){
				String mapName = strings.next();
				nextMaps.add(mapName);
			}
			new FloatListLoader(startingPositions,4).load(ints, floats, strings);
			new ListLoader<Square>(templateSquares,true){
				@Override
				public Square loadObject(Iterator<Integer> ints, Iterator<Float> floats, Iterator<String> strings) {					
					return Square.create(ints, floats);
				}				
			}.load(ints,floats,strings);
			List<Square> tempSquares = new ArrayList<Square>();
			new ListLoader<Square>(tempSquares,false){
				@Override
				public Square loadObject(Iterator<Integer> ints, Iterator<Float> floats, Iterator<String> strings) {					
					return Square.create(ints, floats);
				}
				protected boolean hasNext(){
					return ints.hasNext();
				}
			}.load(ints,floats,strings);
			for(Square square:tempSquares){
				addSquare(square);
			}
		}

	}

	public static void main(String[] args) throws FileNotFoundException{
		File file = userSave("maps");
		while(file!=null){
			Storage.loadMap(new Resource<InputStream>(Storage.getMapNameFromFileName(file.getPath()),file.getPath(),new FileInputStream(file)));
			Map map = Map.createMap(((Integer)Hub.map.getEntity(ID_MAP_ID)));
			((Map)Hub.map).copyTo(map);
			Storage.saveMap(new Resource<OutputStream>(Storage.getMapNameFromFileName(file.getPath()),file.getPath(),new FileOutputStream(file)), map);
			file = userSave("maps");
		}
	}

	public static File userSave(String sub){
		JFileChooser  fc = new JFileChooser("data"+File.separator+sub);
		int returnVal = fc.showOpenDialog(new JPanel());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return fc.getSelectedFile();
		} else {
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public void copyTo(Map map) {
		for(Square square:allSquares){
			map.addSquare(square);
			if(square instanceof OnStepSquare){
				for(SquareAction action:square.getActions()){
					if(action instanceof WinStageOnStepAction){
						WinStageOnStepAction wsosa = (WinStageOnStepAction)action;
						int index = map.nextMaps.indexOf(wsosa.getTarget());
						if(index==-1){
							map.nextMaps.add(nextMaps.get(wsosa.getTarget()));
							wsosa.setTarget(map.nextMaps.size()-1);
						}
						else {
							wsosa.setTarget(index);
						}
					}
				}
			}
		}
		for(Square square:getTemplateSquares()){
			map.addTemplateSquare(square);
			if(square instanceof OnStepSquare){
				for(SquareAction action:square.getActions()){
					if(action instanceof WinStageOnStepAction){
						WinStageOnStepAction wsosa = (WinStageOnStepAction)action;
						if(!map.nextMaps.contains(nextMaps.get(wsosa.getTarget()))){
							map.nextMaps.add(nextMaps.get(wsosa.getTarget()));
						}
						wsosa.setTarget(map.nextMaps.indexOf(wsosa.getTarget()));
					}
				}
			}
		}
		map.setStartPosition(0, getStartingXPosition(0), getStartingYPosition(0));
		map.setStartPosition(1, getStartingXPosition(1), getStartingYPosition(1));
		map.setLightDependency(this.isLightDependent());
	}

	@Override
	public Loader getLoader() {
		return new MapLoader();
	}

	@Override
	public Saver getSaver() {
		return new Saver(){
			@Override
			public void saveTo(List<Object> toSave) {
				toSave.add(getMapId());
				toSave.add(lightDependency?0:1);
				toSave.add(getStartingXPosition(Hero.BLACK_INT));
				toSave.add(getStartingYPosition(Hero.BLACK_INT));
				toSave.add(getStartingXPosition(Hero.WHITE_INT));
				toSave.add(getStartingYPosition(Hero.WHITE_INT));
				for(String name:nextMaps){
					toSave.add(name);
				}
				toSave.add(getTemplateSquares().size());
				for(Square square:getTemplateSquares()){
					square.saveTo(toSave);
				}
				for(Square square:getSquares()){
					square.saveTo(toSave);
				}
			}			
		};
	}

	@Override
	public void addEntity(int id, Object value) {
		if(id<=ID_WHITE_Y){
			if(id==ID_MAP_ID){
				this.setMapId((Integer)value);
			}
			else if(id==ID_BLACK_X){
				while(this.startingPositions.size()<1){
					this.startingPositions.add(0f);
				}
				this.startingPositions.set(0, (Float)value);				
			}
			else if(id==ID_BLACK_Y){
				while(this.startingPositions.size()<2){
					this.startingPositions.add(0f);
				}
				this.startingPositions.set(1, (Float)value);
			}
			else if(id==ID_WHITE_X){
				while(this.startingPositions.size()<3){
					this.startingPositions.add(0f);
				}
				this.startingPositions.set(2, (Float)value);
			}
			else if(id==ID_WHITE_Y){
				while(this.startingPositions.size()<4){
					this.startingPositions.add(0f);
				}
				this.startingPositions.set(3, (Float)value);
			}
		}
		else if(id==ID_TEMPLATE_SQUARES){
			this.templateSquares.add((Square)value);
		}
		else if(id==ID_FUNCTIONAL_SQUARES){
			this.functionalSquares.add((OnStepSquare)value);
		}
		else if(id==ID_UPDATE_SQUARES){
			this.updateSquares.add((UpdatableSquare)value);
		}
		else if(id==ID_SQUARES){
			addSquare((Square)value);
		}
		else if(id==ID_NEXT_MAP){
			this.nextMaps.add((String)value);
		}
		else if(id==ID_BLACK_HERO){
			this.heroes[Hero.BLACK_INT]=(Hero)value;
		}
		else if(id==ID_WHITE_HERO){
			this.heroes[Hero.WHITE_INT]=(Hero)value;
		}
		else if(id==ID_BOTH_HEROES){
			this.heroes=(Hero[])value;
		}
		else if(id==ID_DISPLAY_SQUARE){
			displaySquare((Square)value);
		}
		else if(id==ID_UNDISPLAY_SQUARE){
			unDisplaySquare((Square)value);
		}
		else if(id%MULT_ID_TEMPLATE_SQUARE==0){
			int index = id/MULT_ID_TEMPLATE_SQUARE-2;
			if(index==-1){
				this.templateSquares.add((Square)value);
			}
			else {
				while(this.templateSquares.size()<=index){
					this.templateSquares.add(null);
				}
				this.templateSquares.set(index, (Square)value);
			}
		}
		else if(id%MULT_ID_SQUARE==0){
			int index = id/MULT_ID_SQUARE-2;
			if(this.allSquares.size()<=index){
				this.addSquare((Square)value);
			}
			else {
				this.allSquares.set(index, (Square)value);
			}
		}
		else if(id%MULT_ID_NEXT_MAP==0){
			int index = id/MULT_ID_NEXT_MAP-2;
			if(index==-1){
				this.nextMaps.add((String)value);
			}
			else {
				while(this.nextMaps.size()<=index){
					this.nextMaps.add(null);
				}
				this.nextMaps.set(index, (String)value);
			}
		}
	}

	@Override
	public Object getEntity(int id) {
		if(id<=ID_WHITE_Y){
			if(id==ID_MAP_ID){
				return getMapId();
			}
			else if(id==ID_BLACK_X){
				return startingPositions.get(0);
			}
			else if(id==ID_BLACK_Y){
				return startingPositions.get(1);
			}
			else if(id==ID_WHITE_X){
				return startingPositions.get(2);
			}
			else if(id==ID_WHITE_Y){
				return startingPositions.get(3);
			}
		}
		else if(id==ID_TEMPLATE_SQUARE_SIZE){
			return this.templateSquares.size();
		}
		else if(id==ID_SQUARE_SIZE){
			return this.allSquares.size();
		}
		else if(id==ID_NEXT_MAP_SIZE){
			return this.nextMaps.size();
		}
		else if(id==ID_GAME_MODE){
			return getGameMode();
		}
		else if(id==ID_BLACK_HERO){
			return heroes[Hero.BLACK_INT];
		}
		else if(id==ID_WHITE_HERO){
			return heroes[Hero.WHITE_INT];
		}
		else if(id==ID_BOTH_HEROES){
			return heroes;
		}
		else if(id==ID_IS_LIGHT_DEPENDENT){
			return this.isLightDependent();
		}
		else if(id==ID_SQUARES){
			return allSquares;
		}
		else if(id==ID_FUNCTIONAL_SQUARES){
			return functionalSquares;
		}
		else if(id==ID_UPDATE_SQUARES){
			return updateSquares;
		}
		else if(id==ID_TEMPLATE_SQUARES){
			return templateSquares;
		}
		else if(id%MULT_ID_TEMPLATE_SQUARE==0){
			int index = id/MULT_ID_TEMPLATE_SQUARE-2;
			return this.templateSquares.get(index);
		}
		else if(id%MULT_ID_FUNCTIONAL_SQUARE==0){
			int index = id/MULT_ID_FUNCTIONAL_SQUARE-2;
			return functionalSquares.get(index);
		}
		else if(id%MULT_ID_UPDATE_SQUARE==0){
			int index = id/MULT_ID_UPDATE_SQUARE-2;
			return updateSquares.get(index);
		}
		else if(id%MULT_ID_SQUARE==0){
			int index = id/MULT_ID_SQUARE-2;
			return allSquares.get(index);
		}		
		else if(id%MULT_ID_NEXT_MAP==0){
			int index = id/MULT_ID_NEXT_MAP-2;
			return nextMaps.get(index);
		}
		return null;
	}
	/**
	 * Tries to restart the current map that is loaded into memory.
	 * @param onReturn - The action to perform after the map has been loaded. 
	 */
	@Override
	public void restart(Action<Object> onReturn) {
		//Basically if the Client is connected, or the the filename is already known then just load that map from the filename.
		if(!Client.isConnected()||Hub.map.getFileName()!=null) {
			//Just load the map from the map's filename.
			Storage.loadMap(Hub.manager.createInputStream(Hub.map.getFileName(), IFileManager.ABSOLUTE));
			//And do whatever it is that happens after the map loading(starting the game usually).
			onReturn.act(null);
		}
		else if(Client.isConnected()&&Hub.map.getFileName()==null){
			//If the client is connected, but the map name is unknown
			if(onReturn!=null){
				//Send a request to the Client that is hosting the Map, to send the map file back.
				Client.pass(new LoadMapMessage(Hub.RESTART_STRING, new ActionMessage(onReturn)));
			}
			else {
				throw new RuntimeException("Hub.restartMap:Tried to Restart the Map with a null onReturn action");
			}
		}
		else {
			throw new RuntimeException("Hub.restartMap:Tried to Restart the Map without hosting the Map.");
		}
	}


}
