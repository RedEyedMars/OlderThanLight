package com.rem.duo.messages;

import java.util.ArrayList;
import java.util.List;

import com.rem.core.Action;
import com.rem.duo.MessageHandler;

/**
 * Specialized {@link com.rem.duo.messages.Message} in that it is not a message for sending, it is a message for receiving.
 * The current use case is the restart function in the {@link com.rem.core.Hub} which, waits for a return message from the partnered {@link com.rem.duo.client.Client}
 * before being able to enact a message. This message allows the {@link com.rem.core.Hub} to queue up actions to enact when received.
 * @author Geoffrey
 *
 */
public class ActionMessage extends Message{
	//For Message sending.
	private static final long serialVersionUID = -6581824971280174492L;
	//Queue of actions to be drawn from.
	private static List<Action<Object>> actions = new ArrayList<Action<Object>>();
	//Index of the action in the queue to be acted upon.
	private int indexOfAction;
	/**
	 * Initializes the {@link com.rem.duo.messages.Message} by adding an action to the queue.
	 * @param action - Action to be acted upon on the receipt of this {@link com.rem.otl.core.duo.message.Message}. Note that only the sending {@link com.rem.duo.client.Client} can also receive and perform the action.
	 */
	public ActionMessage(Action<Object> action){
		//Index the action.
		indexOfAction = actions.size();
		//Add the action to the queue.
		actions.add(action);
	}

	/**
	 * Handles the receipt of this action, that is, when this action is received by the sending {@link com.rem.duo.client.Client} the action is performed.
	 * @param handler - the MessageHandler which accepted this message.
	 */
	@Override
	public void act(MessageHandler handler) {
		//Check to make sure the index is within the bounds.
		if(indexOfAction<actions.size()){
			//If the action is within the bounds, perform it.
			actions.remove(indexOfAction).act(null);		
		}
		else {
			//If somehow the index is no longer in the queue, perform the latest action. This does cause race conditions, but currently this method is not employed enough for this to matter.
			actions.remove(actions.size()-1).act(null);
		}
	}

}
