package com.rem.duo.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.rem.core.Hub;
import com.rem.core.IFileManager;
import com.rem.core.storage.Resource;
import com.rem.core.storage.Storage;
import com.rem.duo.Acceptor;
import com.rem.duo.MessageHandler;
import com.rem.duo.messages.AddPlayerMessage;
import com.rem.duo.messages.EndConnectionMessage;
import com.rem.duo.messages.Message;
import com.rem.duo.messages.SendMapMessage;

/**
 * Client is the general, less functional class which handles communication between the two Players in the game, and the Server
 * The Client wraps a {@link com.rem.duo.MessageHandler}, which controls the actual streams of input/output.
 * Basically Client is the buffer for commands to handler.
 * It also handles the setting up of the {@link com.rem.duo.MessageHandler} by the initial communication with the Server.
 * @see com.rem.duo.MessageHandler Halder
 * @author Geoffrey
 */
public class Client {
	public static final String defaultServerIP = "52.35.55.220";
	//For the static Client method to interact with the inner client method.
	//Also, if this client is null, the client is said to be disconnected/closed/off.
	private static Client client;
	//Slave Handler object that will do the interacting with the streams after the initial initialization.
	protected MessageHandler handler;
	//The server address this client will try to initially connect to.
	private String serverAddress;
	//The Server needs a player name for this client. It does not need to be unique, just something to display
	//in the game list/start game.
	private String playerName;
	/**
	 * Basic constructor, initializes a serverAddress and a playerName
	 * @param severAddress - the Server IP address that this Client is suppose to connect to.
	 * @param playerName - the player name that this Client will identify as. Can be any string.
	 */
	public Client(String severAddress, String playerName){
		this.serverAddress = severAddress;				
		this.playerName = playerName;
		this.handler = new MessageHandler(this);
	}

	/**
	 * Attempts to establish a {@link com.rem.duo.MessageHandler} to server.Handler link between the Server handler and this Client's handler.
	 * Will block until that link is established. If a hang happens it is because the Server's handler is failing to respond.
	 * (Note: this cannot be the cause of not being able to connect to the server, if that happens an exception is thrown and no block happens)
	 * @throws IOException - If there is an error communicating with the server, will throw that error.
	 */
	public void establishConnectionWithTheServer() throws IOException{
		//If there is a client already connected, display an error, but continue anyway. 
		if(client!=null){
			System.err.println("multiple clients per app have been created!");
		}

		//Attempt to establish a socket with the main server.
		Socket socket = new Socket(serverAddress,8000);
		//If the socket is established without an IOException being thrown, make this client the client which is "connected.
		client = this;
		//Grab the output stream.
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		//Send an "AddPlayerMessage" which will add this client as a Player to the Server 
		oos.writeObject(new AddPlayerMessage(getPlayerName()));
		//Grab the input stream.
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		try {
			//Should receive a "MeetMeAtPortMessage", but if another message comes, act on that also.
			((Message)ois.readObject()).act(handler);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		//Now that we know the port that the server wants to communicate on, close this socket.
		oos.close();
		ois.close();
		socket.close();

		synchronized(this){
			try {
				//Wait until the handler is connected before releasing the block.
				while(!handler.isConnected()){
					wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Allows access to the enclosed {@link com.rem.duo.MessageHandler}
	 * @return {@link com.rem.duo.MessageHandler} which this client is using to control the input/output streams.
	 */
	public MessageHandler getHandler() {
		return this.handler;
	}
	/**
	 * If a client is connected, sends {@link com.rem.duo.messages.Message} to the handler to send.
	 * @param message {@link com.rem.duo.messages.Message} to send to the server's Handler
	 */
	public static void send(Message message) {
		if(client!=null){
			client.handler.send(message);
		}
	}
	/**
	 * If a client is connected, sends {@link com.rem.duo.messages.Message} to the partnered Client.
	 * If no partner is known by the Server, this Message is consumed without effect.
	 * @param message {@link com.rem.duo.messages.Message} to send to the partnered Client's {@link com.rem.duo.MessageHandler}.
	 */
	public static void pass(Message message) {
		if(client!=null){
			client.handler.pass(message);
		}
	}

	/**
	 * Askes if there is a Client that is currently connected.
	 * @return true - if there is a Client that is connected to the Server. false - if there is no client that can currently send {@link com.rem.duo.messages.Message}'s.
	 */
	public static boolean isConnected(){
		return client!=null;
	}

	/**
	 * If there is a client currently connected, closes that connection to the server.
	 * This method should be called if the application running the client is the one closing the connection.
	 */
	public static void endConnectionToTheServer() {
		if(isConnected()){
			client.handler.sendAllMessagesNow();
			client.closing();
		}
	}

	/**
	 * Wraps the Client.close method with a {@link com.rem.duo.messages.EndConnectionMessage}.
	 * 
	 */
	protected void closing(){
		if(isConnected()){
			client.handler.sendNow(new EndConnectionMessage());
			client.close();
			client = null;
		}
	}

	/**
	 * If there is a client currently connected, closes that connection to the server.
	 * This method should be called if the Server is the one closing the connection.
	 */
	public static void serverEndThisConnection() {
		if(isConnected()){
			client.close();
			client = null;
		}
	}
	/**
	 * Prevents any further {@link com.rem.duo.messages.Message}'s being sent by the handler.
	 * The socket/outputStream/inputStream are all subsequently closed.
	 */
	public void close(){
		Hub.delayer.updateState();
		handler.disconnect();
	}
	/**
	 * Access to the name that represents this Client.
	 * @return string representing this Client.
	 */
	public String getPlayerName() {
		return playerName;//+" ("+serverAddress+")";
	}
	/**
	 * Access to the IP address to which this Client will attempt to connect.
	 * @return string of the IP address.
	 */
	public String getServerAddress() {
		return serverAddress;
	}

	/**
	 * Opens the map file, then sends it as bytes to the partnered {@link com.rem.duo.client.Client}.
	 * The map file is then used to load a map for this {@link com.rem.duo.client.Client} as well.
	 * @param client - {@link com.rem.duo.client.Client} that is used to send the map bytes.
	 * @param filename - File name of where the map resides.
	 * @param onEnd - {@link com.rem.duo.messages.Message} that is acted upon after the SendMapMessage has been received.
	 * @throws IOException - if the file cannot be read from, or cannot be closed properly, this method throws the exception.
	 */
	public static void sendMapMessage(String filename, Message onEnd) throws IOException{
		boolean loadForSelf = true;
		if(Hub.RESTART_STRING.equals(filename)&&Hub.map!=null&&Storage.getFileName(Hub.map)!=null){
			filename = Storage.getFileName(Hub.map);
			loadForSelf = false;
		}
		//Gets the map name from the file name, basically removing the extension ".map" and the folder names.
		String mapName = Storage.getMapNameFromFileName(filename);
		//Retrieves the input stream for the map file.
		Resource<InputStream> resource = Hub.manager.createInputStream(filename,IFileManager.ABSOLUTE);
		//Unwraps the input stream.
		InputStream input = resource.get();
		//Initializes the byte array;
		byte[] file = null;
		//Check to see if the map file exists.
		if(resource.exists()){
			//If it does exist, read the bytes.
			file = Storage.readVerbatum(input);
		}
		else {
			//If the file doesn't exist, close the stream.
			input.close();			
		}
		
		//Sends the herald message in the SendMapMessage to prime the Server and consequently the partnered Client. 
		client.getHandler().sendNow(new SendMapMessage(mapName,onEnd,file));
		
		if(loadForSelf){
			//Load the map from the bytes.
			Storage.loadMap(mapName,filename,file);
		}
	}
	
	public static void addAcceptor(String name, Acceptor acceptor){
		if(Client.isConnected()){
			client.handler.addAcceptor(name, acceptor);
		}
	}
}
