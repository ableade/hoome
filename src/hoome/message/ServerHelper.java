/*
 *ServerHelper.java
 *Jan 31, 2013
 */
package hoome.message;

import hoome.node.HooMENode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import mvn.Consts;

/**
 * A threaded class that acts as a client for sending and receiving search messages
 * @author sola adekunle
 *
 */
public class ServerHelper implements Runnable {

	private Socket clntSock;
	private Queue<HooMEMessage> sendQueue;     //contains HooMEmessages to send mostly search messages
	private MessageOutput anOutput; //the socket output stream
	private MessageInput anInput;  //the socket input stream
	private Logger serverLogger;   //the logger for the HoomeNode server
	private boolean sendHandshake;  //for knowing if we are sending or receiving handshakes
	private boolean connectionOk = true;

	public ServerHelper(Socket aClient, Logger aLogger, boolean sendHandshake ) throws IOException {
		if(aClient!= null && aLogger!=null) {
			this.clntSock = aClient;
			this.serverLogger = aLogger;
			InputStream in = aClient.getInputStream();
			OutputStream out = aClient.getOutputStream();
			anOutput = new MessageOutput (out);		
			anInput = new MessageInput(in);
			this.sendQueue = new LinkedBlockingQueue<HooMEMessage>(Consts.SEND_QUEUE_CAPACITY);
			this.sendHandshake = sendHandshake;
			
			SocketAddress clientAddress = clntSock.getRemoteSocketAddress();
			System.out.println("Handling client at " + clientAddress);
			this.serverLogger.log(Level.INFO, "Handling client at " + clientAddress);
		}
	}

	/**
	 * 
	 * @throws IOException
	 * @throws BadAttributeValueException if error reading values
	 */
	private void getHandshake () throws IOException, BadAttributeValueException {
		while(anInput.getInput().available() ==0 ) {
			//do nothing
		}
		
		String greeting = anInput.readString();
		anInput.readString(); //consume the second new line character
		
		if(greeting.equals(Consts.HANDSHAKE_GREETING)) {
			anOutput.getOutput().write((Consts.HANDSHAKE_RESPONSE+Consts.EOLN+Consts.EOLN).getBytes(HooMEMessage.CHARENCODING));
			
		} else {
			//connection handshake is invalid send an error code
				anOutput.getOutput().write((Consts.PRE_HOOME_ERROR +" " + Consts.INVALID_GREETING_CODE + Consts.INVALID_HANDSHAKE + 
						Consts.EOLN + Consts.EOLN).getBytes(HooMEMessage.CHARENCODING));
				this.connectionOk = false;
		}
	}

	/**
	 * sends a HooME handshake to a HooMeNode when it is connected
	 * @throws IOException if I/O error occurs
	 * @throws BadAttributeValueException 
	 */
	
	private void sendHandshake() throws IOException, BadAttributeValueException {
		anOutput.getOutput().write((Consts.HANDSHAKE_GREETING+Consts.EOLN+Consts.EOLN).getBytes(HooMEMessage.CHARENCODING));
		while(anInput.getInput().available() ==0) {
			//do nothing
		}
		String response =anInput.readString();
		if(!response.equals(Consts.HANDSHAKE_RESPONSE)) {
			System.out.println("Connection not ok");
			this.connectionOk = false;
		}
		anInput.readString(); //consume the second newline character
	}

	/**
	 * sends and receives search messages to connected clients
	 * @param aClient a socket connection to a client
	 */
	
	public void handleClient (Socket aClient) {
		HooMEMessage aMessage= null;
		try {
			if(sendHandshake) {
				this.sendHandshake();
			} else {
				this.getHandshake();
			}
		} catch (IOException e) {
			System.out.println("Unable to connect to remote host");
			this.serverLogger.log(Level.INFO, "cannot establish handshake");
		} catch (BadAttributeValueException e) {
			this.serverLogger.log(Level.INFO, "Bad attribute found");

		}
		try {
			//establish connection and get greeting, then respond			
			while(!aClient.isClosed() && this.connectionOk && HooMENode.keepRunning) {
				if(anInput.getInput().available() >0 ) {
					aMessage = HooMEMessage.decode(anInput);
					
					//if we are getting a response back
					if(aMessage instanceof HooMEResponse) {
						System.out.println(aMessage);							
						//if we are getting a search back
					} else if(aMessage instanceof HooMESearch) { 
						List<Result> searchResults = HooMENode.getFileResults(((HooMESearch)aMessage).getSearchString());
						HooMEMessage response = new HooMEResponse(aMessage.getId(), 1, RoutingService.getRoutingService(0), aMessage.getDestinationHooMEAddress(), aMessage.getSourceHooMEAddress(), 
								new InetSocketAddress(HooMENode.address,HooMENode.downloadPort));

						((HooMEResponse)response).getResultList().addAll(searchResults);
						response.encode(anOutput);
					}
				}
				//check for search messages that have been added to the queue and send them out
				HooMEMessage aMessage1 = this.sendQueue.poll();
				if(aMessage1 != null) {
					aMessage1.encode(anOutput);
				}
			}

		} catch (IOException e) {
				e.printStackTrace();
		} catch (BadAttributeValueException e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * adds a search message to the send qeueue of a connected client
	 * @param searchMessage the search string to be sent out
	 * @throws BadAttributeValueException 
	 */
	
	public void addSearch (String searchMessage) throws BadAttributeValueException {
		HooMEMessage aMessage = new HooMESearch(HooMENode.id, 1, RoutingService.getRoutingService(0), HooMENode.destAddr,
				HooMENode.sourceAddr,searchMessage);
		this.sendQueue.add(aMessage);
	}
	/**
	 * overides run function in thread class to handle client connections
	 */
	
	@Override 
	public void run() {
		this.handleClient(this.clntSock);
	}

}
