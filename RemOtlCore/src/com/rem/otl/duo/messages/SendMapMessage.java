package com.rem.otl.duo.messages;

import java.util.ArrayList;
import java.util.List;

import com.rem.core.storage.Storage;
import com.rem.duo.MessageHandler;
import com.rem.duo.messages.Message;

/**
 * SendMapMessages are sent to prime the Server and the partnered {@link com.rem.otl.duo.client.Client} for a byte data transfer.
 * {@link com.rem.otl.core.game.environment.Map}'s are contained within map files, completely as byte data.
 * To send this data to the partnered {@link com.rem.otl.duo.client.Client} a SendMapMessage is sent first to change the mode of the handler from accepting {@link com.rem.otl.duo.messages.Message}'s to accepting bytes.
 * The bytes follow. 
 * @author Geoffrey
 */
public class SendMapMessage extends Message{
	//For Message sending.
	private static final long serialVersionUID = -7927768988704831847L;

	//The name of the map which will be sent, this name is used mostly for saving complete times.
	private String mapName;
	//The Message that is called after the map has been received.
	private Message onEnd;
	//The number of bytes in the map data.
	private List<Byte> bytes;

	/**
	 * Called by the Client.sendMapMessage method, this constructor initializes the {@link com.rem.otl.duo.messages.Message} with the necessary variables.
	 * @param mapName - The token name of the map, this name is not protected, and is used primarily for saving completed times.
	 * @param onEnd - {@link com.rem.otl.duo.messages.Message} that is acted upon after the map is received. Typically this is {@link com.rem.otl.duo.messages.StartGameMessage}, but is left for general use.
	 * @param numberOfBytes - The number of bytes that the map data has, i.e. the number of bytes to be sent.
	 */
	public SendMapMessage(String mapName, Message onEnd, byte[] file){
		this.mapName = mapName;
		this.onEnd = onEnd;	
		
		//Initializes the byte list.
		bytes = new ArrayList<Byte>(file.length);
		for(int i=0;i<file.length;++i){
			//For each byte in the file, add it to the list of bytes.
			bytes.add(file[i]);
		}
	}

	/**
	 * When received, this {@link com.rem.otl.duo.messages.Message} calls the {@link com.rem.otl.duo.MessageHandler}.acceptByte(int) method, which takes in incoming bytes.
	 * Those bytes are then converted into a {@link com.rem.otl.core.game.environment.Map} object using the {@link com.rem.otl.core.storage.Storage}.loadMap method.
	 * Then, the onEnd message is acted upon.
	 */
	@Override
	public void act(MessageHandler handler) {
		byte[] bs = new byte[bytes.size()];
		for(int i=0;i<bytes.size();++i){
			bs[i]=bytes.get(i);
		}
		//Those bytes are combined into a map object.
		Storage.loadMap(mapName,null,bs);
		//onEnd Message is acted upon.
		onEnd.act(handler);
	}

}
