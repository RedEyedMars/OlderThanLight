package com.rem.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import com.rem.core.environment.Environment;
import com.rem.core.gui.IGui;
import com.rem.core.gui.graphics.GraphicRenderer;
import com.rem.core.gui.graphics.GraphicView;
import com.rem.core.gui.inputs.ClickEvent;
import com.rem.core.gui.inputs.EventHandler;
import com.rem.core.gui.inputs.HoverEvent;
import com.rem.core.gui.inputs.KeyBoardEvent;
import com.rem.core.gui.inputs.KeyBoardListener;
import com.rem.core.gui.inputs.MouseListener;
import com.rem.core.gui.music.MusicPlayer;
import com.rem.core.storage.Resource;
import com.rem.core.storage.Storage;
import com.rem.duo.client.Client;
import com.rem.duo.messages.BlankMessage;

/**
 * Central hub, holds the major controllers:
 *   creator: makes the other controllers.
 *   handler: controls the {@link com.rem.core.gui.inputs.InputEvent}s.
 *   manager: controls the file manipulation, mostly the opening and closing of input/output streams.
 *   view: The view is what is displayed on the screen/the update node to the inner {@link com.rem.core.gui.graphics.GraphicEntity}.
 *   gui: setups up the view, the renderer, and handles the interface between the {@link com.rem.core.gui.inputs.InputEvent} and the handler.
 *   log: handles debug messages.
 *   renderer: handles the rendering of the view.
 *   music: controls the music being played (can be null if the gui/creator does not have a music player setup).
 *   map: controls the map which the game is being played on.
 *   
 * @author Geoffrey
 *
 */
public class Hub {
	/**
	 * The string sent to the map holder to tell them to send the map file back. Basically a request to restart the map to the other client.
	 */
	public static final String RESTART_STRING = "\n";
	
	/**
	 * The id of the top draw layer, no other layer will cover this layer of {@link com.rem.core.gui.graphics.GraphicElement}'s.
	 */
	public static final int TOP_LAYER = 2;
	/**
	 * The id of the middle draw layer, any {@link com.rem.core.gui.graphics.GraphicElement} in the top layer will cover elements in this layer, additionally any elements in this layer will cover any elements in the bototm layer.
	 */
	public static final int MID_LAYER = 1;
	/**
	 * The id of the bottom draw layer, any {@link com.rem.core.gui.graphics.GraphicElement} in the above layers will cover the elements in this layer.
	 */
	public static final int BOT_LAYER = 0;


	/**
	 * The creator of the other controllers, basically the launch point for the application.
	 */
	public static ICreator creator;
	/**
	 * The {@link com.rem.core.gui.inputs.EventHandler} is a thread that handles incoming {@link com.rem.core.gui.inputs.InputEvent}'s by calling the appropriate onClick/onHover/onType methods.
	 */
	public static EventHandler handler;
	

	public static Delayer delayer = null;
	
	/**
	 * The file manager controls the opening of input/output streams as well as creating and deleting files.
	 */
	public static IFileManager manager;
	/**
	 * {@link com.rem.core.gui.graphics.GraphicView} is the parent of all {@link com.rem.core.gui.graphics.GraphicEntity}'s, meaning this is the launch point for all the {@link com.rem.core.gui.graphics.GraphicElement}'s as well as the update method, which controls the frame to frame of the game flow.
	 */
	public static GraphicView view;
	/**
	 * While the {@link com.rem.otl.core.gui.graphics.inputs.EventHandler} handles {@link com.rem.core.gui.inputs.InputEvent} after they are created, the gui is the creator of these input events. It also controls the game flow by calling the update method of the view.
	 */
	public static IGui gui;
	/**
	 * The log controls debug messages from the core to the implementation.
	 */
	public static ILog log;
	/**
	 * This variable controls the interface between the {@link com.rem.core.gui.graphics.GraphicElement} and the display medium.
	 */
	public static GraphicRenderer renderer;
	/**
	 * The music player is a {@link com.rem.core.gui.graphics.GraphicEntity} that can control the music, this can be null if no music is available for the current display medium(i.e. the editor).
	 */
	public static MusicPlayer music;
	/**
	 * Map object that controls the environmental elements on the stage, i.e. the squares.
	 */
	public static Environment map;
	
	/**
	 * The first mapfile that is defaultly chosen when trying to select a stage in host mode.
	 */
	public static String defaultMapFile = "";
	/**
	 * The default name used in the join menu to identify the joining party.
	 */
	public static String defaultPlayerName = "";
	
	/**
	 * Width of the screen.
	 */
	public static float width;
	/**
	 * Height of the screen.
	 */
	public static float height;
	
	/**
	 * The seed for the randomizer, this seed can be transfered to other randomizers so that identical maps are produced.
	 */
	public static long seed = new Random().nextLong();
	/**
	 * The actual randomizer element that uses the Hub.seed to produce random elements for the maps to be more random with.
	 */
	public static Random randomizer = new Random(seed);

	
	//Just for the initialization, there is a generic mouseListener so that the eventHandler is not calling a null element.
	public static MouseListener genericMouseListener = new MouseListener(){
		@Override
		public boolean onClick(ClickEvent event) {	return false; }
		@Override
		public boolean onHover(HoverEvent event) { return false; }
		@Override
		public void onMouseScroll(int distance) {}
	};
	//During initialization if there is an input event without a keyboard listener to listen to it an error is thrown, this class ensures there is a listener to listen.
	public static KeyBoardListener genericKeyBoardListener = new KeyBoardListener(){
		@Override
		public boolean continuousKeyboard() {
			return false;
		}
		@Override
		public void onType(KeyBoardEvent event) {
		}
	};
	/**
	 * Tries to load the mapFileName into memory. If there is a connected {@link com.rem.duo.client.Client} then it also tries to send that map to the accompanying Client.
	 * @param mapFilename - the filename of the map which is trying to be loaded.
	 */
	public static void loadMapFromFileName(String mapFilename) {
		//Check to make sure the map name is not null.
		if(mapFilename!=null){
			//If there is a current Client that is connected:
			if(Client.isConnected()){
				try {
					//Then open the map by also sending it to the accompanying Client.
					Client.sendMapMessage(mapFilename, new BlankMessage());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				//Otherwise just open the map for this client.
				Storage.loadMap(Hub.manager.createInputStream(mapFilename,IFileManager.RELATIVE));
			}
		}
	}
	/**
	 * This method basically does all the initialization for the app, from setting up the music, the renderer, the gui, the event handler, etc. all the things that need a boost start here.
	 * 
	 * @param creator - ICreator object that will create the necessary objects like the log, the gui and so on.
	 * @param main - This object is the caller of the load. So, in android applications it will be the MainActivity, in pc app's it'll be the Main or EditorMain objects. 
	 * @param loadMusic - If there is no music player to load, then set this to false, otherwise set to true.
	 * @param loadDefaults - If the loading of the defaults is not needed/wanted, set to false, otherwise load the defaults.
	 * @param run - The gui has a run method, if this method needs to be called, set to true, otherwise false. In the android gui, what happens is that the Android client is the run method is called from within the setContext process, so the run doesn't need to be called in this method.
	 */
	public static void load(
			ICreator creator,
			Setupable main,
			boolean loadMusic, boolean loadDefaults, boolean run){
		//Set the creator.
		Hub.creator = creator;
		//Create the log element, and set it.
		log = creator.createLog();
		//Make a preliminary GameEndState object, in case the client is going to play solo.
		//GameEndState.create();
		//Create and set the gui with the main object as an argument.
		gui = creator.createGui(main);
		//Now that we have a gui, we can get the display width.
		Hub.width = gui.getDisplayWidth();
		//and height.
		Hub.height = gui.getDisplayHeight();
		try {
			//Next we try to setup the gui, the creation of the gui only makes a skeleton, the setup initializes key variables and sets the perspective.
			gui.setup();
			//Now that we have a gui, time to create and set the file manager.
			manager = creator.createFileManager(main);
			//And create and setup the event handler.
			handler = new EventHandler();
			//This mouseListener is soon cleared by the gui.setView method, however there used to be a bug where not having a listener would cause an error.
			handler.giveOnClick(Hub.genericMouseListener);
			//A generic keyBoardListener is added to the handler in case any stray events are tried to process before a view is set.
			handler.giveOnType(Hub.genericKeyBoardListener);
			//Creates and sets the renderer with the main object as argument.
			renderer = creator.createGraphicRenderer(main);
			//If the music player should be created, this local variable should be true.
			if(loadMusic){
				//Create and set the music player.
				music = creator.createMusic();
			}
			//If the defaults can be loaded by the client.
			if(loadDefaults){
				//Load the defaults.
				loadDefaults();
			}
			delayer = new Delayer();
			//If this client needs the setView and run method called.
			if(run){
				//Call the setView method with the main.getFirstView().
				//One thing to note is that this view cannot be created before the renderer and gui are created because the fonts must be loaded for most views to operate.
				gui.setView(main.getFirstView());
				//Call the run method.
				gui.run();
			}

		}
		catch (Exception e) {
			log.err("GLApp.run(): " + e);
			e.printStackTrace(System.out);
		}
		//The run method, if run, will block the program and not let the this part be run until the run method releases it.
		if(run){
			cleanup();
			System.exit(0);
		}
	}
	
	public static void cleanup(){
		gui.hideKeyBoardDisplay(null);
		delayer.end();
		handler.end();
		saveDefaults();
		view.end();
		gui.end();
		Client.endConnectionToTheServer();
	}
	/**
	 * Loads the default parameters, the music volume, the mute-ed-ness of the music player, the default player name, and the default map file.
	 */
	public static void loadDefaults() {
		//Opens the input stream containing the default values.
		Resource<InputStream> resource  = Hub.manager.createInputStream("data"+File.separator+"meta.data",IFileManager.RELATIVE);
		//Loads the text within the input stream.
		String file = Storage.loadText(resource);
		//Set the variable id, either: mute, volume, defaultPlayerName, defaultMapFile.
		int variable = -1;
		
		for(String line:file.split("\n")){
			if(line.startsWith("\t")){
				String var = line.substring(1);
				if(variable==0){
					if(Boolean.parseBoolean(var)){
						Hub.music.pause();
					}
				}
				else if(variable==1){
					Hub.music.adjustVolume(Float.parseFloat(var));
				}
				else if(variable==2){
					Hub.defaultPlayerName = var;
				}
				else if(variable==3){
					Hub.defaultMapFile = var;
				}
			}
			else {
				if("muted".equals(line)){
					variable = 0;
				}
				else if("volume".equals(line)){
					variable = 1;
				}
				else if("name".equals(line)){
					variable = 2;
				}
				else if("map".equals(line)){
					variable = 3;
				}
			}
		}

	}
	public static void saveDefaults() {
		Resource<OutputStream> resource  = Hub.manager.createOutputStream("data"+File.separator+"meta.data",IFileManager.RELATIVE);

		StringBuilder writer = new StringBuilder();
		writer.append("muted\n\t");
		if(music==null){
			writer.append("false\n");
		}
		else {
			writer.append(!Hub.music.isPlaying()+"\n");
		}
		writer.append("volume\n\t");
		if(music==null){
			writer.append("0.8\n");
		}
		else {
			writer.append(Hub.music.getVolume()+"\n");
		}
		writer.append("name\n\t");
		writer.append(defaultPlayerName);
		writer.append("\nmap\n\t");
		writer.append(defaultMapFile);
		Storage.saveText(resource, writer.toString());


	}

	public static long getNewRandomSeed() {
		Hub.seed = new Random().nextLong();
		return Hub.seed;
	}
}
