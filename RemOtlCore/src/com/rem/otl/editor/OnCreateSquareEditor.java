package com.rem.otl.editor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Stack;

import com.rem.core.Hub;
import com.rem.core.IFileManager;
import com.rem.core.gui.graphics.GraphicEntity;
import com.rem.core.gui.graphics.GraphicText;
import com.rem.core.gui.graphics.GraphicView;
import com.rem.core.gui.inputs.KeyBoardListener;
import com.rem.core.gui.inputs.ClickEvent;
import com.rem.core.storage.Resource;
import com.rem.core.storage.Storage;
import com.rem.otl.game.environment.Square;
import com.rem.otl.game.environment.oncreate.OnCreateAction;
import com.rem.otl.game.environment.oncreate.OnCreateSquare;
import com.rem.otl.game.menu.GetFileMenu;

public class OnCreateSquareEditor extends Editor{

	private Resource<InputStream> saveTo = null;
	private float square_x;
	private float square_y;
	private float square_w;
	private float square_h;

	private TextWriter writer;

	public OnCreateSquareEditor(GraphicView parentView,Resource<InputStream> saveTo,float x, float y, float w, float h){
		super(parentView);

		setupButtons();
		Square guide = new Square(15,0,w,h);
		guide.reposition(x,y);
		addChild(guide);
		String text = "";
		this.saveTo = saveTo;
		if(saveTo!=null){
			text = Storage.loadText(saveTo);
			
			if(Hub.map==null){
				Hub.map = com.rem.otl.game.environment.Map.createMap(0);
			}
			squares = ((com.rem.otl.game.environment.Map)Hub.map).getTemplateSquares();	
			for(Square square:squares){
				for(int i=0;i<square.size();++i){
					if(!(square.getChild(i) instanceof Square)){
						square.removeChild(i);
						--i;
					}
					else {
						addIconsToSquare((Square) square.getChild(i));
					}
				}
				addIconsToSquare(square);
				addChild(square);
			}
		}

		this.parentView = parentView;
		this.square_x = x;
		this.square_y = y;
		this.square_w = w;
		this.square_h = h;

		Map<Integer,ButtonAction> ctrlCommands = new HashMap<Integer,ButtonAction>();
		ctrlCommands.put(45, new ButtonAction(){
			@Override
			public void act(ClickEvent event) {
				saveAndReturn();
			}});
		ctrlCommands.put(57, new ButtonAction(){
			@Override
			public void act(ClickEvent event) {
				toggleVisibleSquares();
			}
		});
		ctrlCommands.put(17,  new ButtonAction(){
			@Override
			public void act(ClickEvent event){
				moveView(0,-0.25f);
			}
		});
		ctrlCommands.put(30, new ButtonAction(){
			@Override
			public void act(ClickEvent event){
				moveView(0.25f,0);

			}
		});
		ctrlCommands.put(31, new ButtonAction(){
			@Override
			public void act(ClickEvent event){
				moveView(0,0.25f);			
			}
		});
		ctrlCommands.put(32, new ButtonAction(){
			@Override
			public void act(ClickEvent event){
				moveView(-0.25f,0);
			}
		});
		ctrlCommands.put(33, new ButtonAction(){
			@Override
			public void act(ClickEvent event) {
				granityButton.performOnRelease(null);
			}});
		writer = new TextWriter(text,ctrlCommands);
		addChild(writer);
		mode = Editor.MODE_NEUTRAL;

		saveAndReturnButton.reposition(0.82f, 0.87f);
		saveButton.reposition(0.87f, 0.87f);
		saveAndOpenButton.reposition(0.92f, 0.87f);
	}
	@Override
	public void addIconsToSquare(Square square1){
		super.addIconsToSquare(square1);
		GraphicEntity e = new GraphicText("impact",""+squares.indexOf(square1),0);
		e.reposition(square1.getX(),
				 square1.getY()+square1.getHeight()-0.03f);
		square1.addChild(e);
	}
	@Override
	public KeyBoardListener getDefaultKeyBoardListener(){
		return writer;
	}
	@Override
	public void update(double seconds){
		if(saveTo==null){
			Hub.gui.setView(parentView);
		}
		super.update(seconds);
	}
	@Override
	protected void saveCurrent(){
		Storage.saveText(Hub.manager.createOutputStream(saveTo.getPath(),IFileManager.ABSOLUTE), writer.getText());
	}
	@SuppressWarnings("unchecked")
	protected void saveAndReturn() {
		saveCurrent();
		List<OnCreateAction> actions = new ArrayList<OnCreateAction>();
		List<OnCreateAction> currentSection = actions;
		Stack<List<OnCreateAction>> stack = new Stack<List<OnCreateAction>>();
		int depth = 0;
		for(String line:writer.getLines()){
			if(depth>0){
				int numberOfTabs=0;
				for(;numberOfTabs<line.length()&&line.charAt(numberOfTabs)=='\t';++numberOfTabs){				
				}
				if(numberOfTabs>0){
					line = line.substring(numberOfTabs);
				}
				while(numberOfTabs<depth){
					currentSection = stack.pop();
					--depth;
				}				
			}
			OnCreateAction action = createFromString(line);
			if(action!=null){
				if(action.getIndex()!=8){
					currentSection.add(action);
				}
				if(action.isBlock()){
					stack.push(currentSection);
					currentSection = (List<OnCreateAction>) action;
					++depth;
				}
			}

		}

		final List<Integer> ints = new ArrayList<Integer>();
		final List<Float> floats = new ArrayList<Float>();
		List<Object> probe = new ArrayList<Object>(){
			private static final long serialVersionUID = -599983209956028447L;

			@Override
			public boolean add(Object obj){
				if(obj instanceof Integer){
					return ints.add((Integer) obj);
				}
				else if(obj instanceof Float){
					return floats.add((Float) obj);
				}
				return false;
			}
		};
		ints.add(((com.rem.otl.game.environment.Map)Hub.map).getIntCoordinate(square_x,com.rem.otl.game.environment.Map.X_axis));
		ints.add(((com.rem.otl.game.environment.Map)Hub.map).getIntCoordinate(square_y,com.rem.otl.game.environment.Map.Y_axis));
		ints.add(((com.rem.otl.game.environment.Map)Hub.map).getIntCoordinate(square_w,com.rem.otl.game.environment.Map.X_axis));
		ints.add(((com.rem.otl.game.environment.Map)Hub.map).getIntCoordinate(square_h,com.rem.otl.game.environment.Map.Y_axis));
		ints.add(actions.size());
		for(OnCreateAction action:actions){
			action.saveTo(probe);
		}

		OnCreateAction.squareIndexOffset=0;
		OnCreateSquare square = new OnCreateSquare(0,-1,-1,ints.iterator(),floats.iterator());
		if(parentView instanceof MapEditor){
			while(!square.getChildren().isEmpty()){
				square.removeChild(0);
			}
			((MapEditor)parentView).addSquare(square);
			
		}
		Hub.gui.setView(parentView);
	}

	public OnCreateAction createFromString(String toParse){
		if(toParse.length()==0)return null;
		String[] split = toParse.trim().split(":");
		String name = split[0];
		OnCreateAction action = OnCreateAction.actionMap.get(name).create();
		List<Integer> ints = new ArrayList<Integer>();
		List<Float> floats = new ArrayList<Float>();
		if(action.isBlock()){
			ints.add(0);
		}
		if(split.length>1){
			String[] args = split[1].split(" ");		
			for(String arg:args){
				try {
					ints.add(Integer.parseInt(arg));
				}
				catch(NumberFormatException ie){
					try {
						floats.add(Float.parseFloat(arg));
					}
					catch(NumberFormatException fe){
						if(arg.startsWith("#")){
							if(arg.contains("-")){
								String[] both = arg.substring(1).split("-");
								Integer first = Integer.parseInt(both[0]);
								Integer last = Integer.parseInt(both[1]);
								ints.add(new Integer(last-first+1));
								for(int i=first;i<last+1;++i){
									ints.add(i);
								}
							}
							else {
								ints.add(Integer.parseInt(arg.substring(1)));
							}
						}
					}
				}
			}
		}
		action.loadFrom(ints.iterator(), floats.iterator());
		return action;
	}
	private void moveView(float x, float y){
		for(int i=0;i<squares.size();++i){
			squares.get(i).reposition(squares.get(i).getX()+x,
					              squares.get(i).getY()+y);
		}
	}
	@Override
	protected void openNew() {
		Resource<InputStream> saveTo = GetFileMenu.getFile(parentView,"ocs",true);
		Editor e = new OnCreateSquareEditor(
				parentView,saveTo,
				square_x,square_y,square_w,square_h);
		e.setupModes();
		Hub.gui.setView(e);
	}
	
}
