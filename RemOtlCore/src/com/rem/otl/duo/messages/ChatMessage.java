package com.rem.otl.duo.messages;

import com.rem.otl.game.chat.ChatLogEntry;
import com.rem.duo.MessageHandler;
import com.rem.duo.messages.Message;


/**
 * {@link com.rem.otl.duo.messages.Message} to send String texts from this {@link com.rem.otl.duo.client.Client} to the partnered one.
 * @author Geoffrey
 *
 */
public class ChatMessage extends Message{
	//For Message sending.
	private static final long serialVersionUID = 1116023491496884530L;

	//The chat text to be sent/received.
	private String text;
	/**
	 * Initializes this ChatMessage with the text to be sent to the partnered {@link com.rem.otl.duo.client.Client}.
	 * @param text - The String to be sent to the partnered {@link com.rem.otl.duo.client.Client}.
	 */
	public ChatMessage(String text){
		super();
		//Initializes the text object this Message holds.
		this.text = text;
	}
		
	
	/**
	 * Method which will be called when this {@link com.rem.otl.duo.messages.Message} is received by this {@link com.rem.otl.duo.client.Client}.
	 */
	@Override
	public void act(MessageHandler handler) {
		//Creates a new chat log entry with the enclosed text and sends it.
		handler.accept("chat","addEntry",new ChatLogEntry(ChatLogEntry.YOURS, text));
	}
	
	
}
