package com.rem.duo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import com.rem.duo.client.Client;
import com.rem.duo.messages.Message;
import com.rem.duo.messages.PassMessage;
import com.rem.duo.messages.PingMessage;

/**
 * See {@link com.rem.duo.client.Client} for more details on the client/handler relationship
 * The Handler is the more functional class, receiving commands from its host client.
 * The Handler handles input and output streams.
 * Receives {@link com.rem.duo.messages.Message}'s and runs them.
 * Sends messages which have been added to a queue.
 * Holds some general objects that are needed by the message act methods. Such as a Player for the {@link com.rem.otl.core.game.Game} related actions, and a IDuo{@link com.rem.otl.core.game.menu.IDuoMenu Menu} for the pre-game menu actions. 
 * 
 * @see com.rem.duo.client.Client Client
 * @author Geoffrey
 *
 */
public class MessageHandler {
	//boolean which gates the input/output streams, when !connected the streams will close asap.
	private boolean connected = false;
	//The stream the sends Message's bound for the Server's handler, or for the partner client's handler.
	private ObjectOutputStream output;
	//The stream that receives Message's from the Server's handler and from the partner client.
	private ObjectInputStream input;
	//General socket object, used to close the connection after the conneciton is ended.
	private Socket socket;
	//The queue for outgoing Message's.
	private LinkedList<Message> outgoingMail = new LinkedList<Message>();
	//Host client, the client which control's this handler.
	private Client client;
	
	private Map<String,Acceptor> acceptors = new HashMap<String,Acceptor>();

	/**
	 * Basic Constructor.
	 * @param client - host {@link com.rem.duo.client.Client}, the {@link com.rem.duo.client.Client} which control's this handler.
	 */
	public MessageHandler(Client client) {
		//Set the host Client.
		this.client = client;		
	}
	/**
	 * Called by the {@link com.rem.duo.messages.MeetMeAtPortMessage}, which proves the Server port which this handler will do future communications through.
	 * Initializes the Input and Output streams/threads.
	 * @param port - Server port which this handler will connect for future communication.
	 */
	public void setup(int port){
		try {
			//Start the socket with the Server which will produce the communication streams.
			socket = new Socket(client.getServerAddress(),port);
			//Retrieve the output stream and wrap it in an ObjectOutputStream, allowing Message sending.
			output = new ObjectOutputStream(socket.getOutputStream());
			//Send a ping message, this is purely cosmetic, but allows someone watching the stream to see that a connection was made.
			send(new PingMessage());
			//Retrieve the input stream and wrap it in an ObjectInputStream, allowing Message receiving.
			input = new ObjectInputStream(socket.getInputStream());
			//Now that we have both our streams intact, the handler can be said to be "connected"
			this.connected = true;
			//Starts sending outgoing Messages which have been added to the outgoingMail.
			new HandlerOutputThread().start();
			//Starts receiving Message's sent to this handler.
			new HandlerInputThread(this).start();
			synchronized(client){
				//The client is waiting in Client.run for the handler to be setup, this releases that waiting loop.
				client.notifyAll();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * No future messages will be received or sent.
	 */
	public void disconnect(){
		connected = false;
	}

	/**
	 * This handler is open for sending or receiving messages.
	 * @return boolean, true if this handler can accept/receive {@link com.rem.duo.messages.Message}'s, false otherwise.
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Used by the {@link com.rem.duo.messages.EndConnectionMessage} to shut down this client/handler.
	 */
	public void close(){
		//If this handler has already been disconnected, we should not try to close the client again.
		if(connected){
			//Closes the client, which will in turn disconnect the handler.
			client.close();
		}
	}

	/**
	 * Used when a message needs to be sent before any further messages are sent, less safe. 
	 * For example if the client was ending it's connection this method should be used to get off any last messages before that close.
	 * @param message - {@link com.rem.duo.messages.Message} to send before anything else.
	 */
	public void sendNow(Message message) {
		try {
			//Send the message immediately.
			output.writeObject(message);
		} catch(NullPointerException n){

		} catch(SocketException s){

		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	public void sendAllMessagesNow() {
		try {
			while(!outgoingMail.isEmpty()){
				//Send the message immediately.
				output.writeObject(outgoingMail.removeFirst());

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch(NoSuchElementException e){

		}
	}

	/**
	 * Thread which collects incoming messages to this handler and calls the {@link com.rem.duo.messages.Message}.act method.
	 * Exits when Handler is no longer connected.
	 * If there is an error in receiving it is assumed to be the cause of the connection being interrupted and the client is shut down.
	 * @author Geoffrey
	 *
	 */
	private class HandlerInputThread extends Thread{
		//handler to use in message receiving.
		private MessageHandler handler;
		/**
		 * Basic Constructor
		 * @param handler - takes the parent handler to use as the Message.act()'s subject.
		 */
		public HandlerInputThread(MessageHandler handler){
			super();
			//stores the handler.
			this.handler = handler;
		}
		/**
		 * While the handler is connected reads incoming messages from the Server/partner {@link com.rem.duo.client.Client}.
		 */
		@Override
		public void run(){
			//Lasts while the handler is connected.
			try {
				while(connected){
					//Reads a message.
					Message message = ((Message)input.readObject());
					//Calls that act method of the Message, completing the message's purpose.
					message.act(handler);
				}
			} catch(SocketException s){
				//If the socket is closed, the connection should be terminated.
				Client.endConnectionToTheServer();
			}
			catch (ClassNotFoundException e){
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			};

		}
	}
	/**
	 * Standard method to send {@link com.rem.duo.messages.Message}'s to the Server, queues up the message to be sent after previous messages have been sent.
	 * @param message - {@link com.rem.duo.messages.Message} to be sent to the Server.
	 */
	public void send(Message message){
		//Add the message to the queue.
		outgoingMail.add(message);
	}
	/**
	 * Sends a special "{@link com.rem.duo.messages.PassMessage}" which causes the Server to relay the {@link com.rem.duo.messages.Message} to the partnered {@link com.rem.duo.client.Client} if there is such a client.
	 * If there is no partner, the {@link com.rem.duo.messages.Message} is ignored.
	 * @param message - {@link com.rem.duo.messages.Message} to be sent to the partner client.
	 */
	public void pass(Message message){
		send(new PassMessage(message));
	}
	/**
	 * Sends the outgoing mail that the handler has accumulated through the Handler.send method.
	 * Stops sending when the handler is disconnected.
	 * @author Geoffrey
	 *
	 */
	private class HandlerOutputThread extends Thread{
		/**
		 * Continually sends outgoing {@link com.rem.duo.messages.Message}s while the Handler is connected
		 */
		@Override
		public void run(){
			try {
				//If the handler becomes disconnected, stop sending messages
				while(connected){

					try {
						Message message;
						//while there are messages to send, send them
						while(!outgoingMail.isEmpty()&&connected){
							//System.out.println("client send:"+outgoingMail.get(0));
							message = outgoingMail.removeFirst();
							//Send outgoing mail
							message.presend();
							output.writeObject(message);
						}
						//Wait a bit.							
						Thread.sleep(1);
					} catch(NoSuchElementException e){
						outgoingMail = new LinkedList<Message>();
					}  catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (SocketException s){
				s.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addAcceptor(String name, Acceptor acceptor){
		this.acceptors.put(name, acceptor);
	}
	
	public void accept(String name, String command, Object object){
		this.acceptors.get(name).accept(command,object);
	}
}
