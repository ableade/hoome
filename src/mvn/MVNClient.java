package mvn;

/*
 * MVNClient.java
 * March 24 2013
 */


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;



/**
 * This class is the client for the Maven protocol
 * @author Sola Adekunle
 *
 */
public class MVNClient {

	private static final int TIMEOUT =3000; //maximum number of seconds to hold connection
	private static final int MAX_ATTEMPTS =3;
	private static DatagramSocket connSock;
	private static boolean receivedResponse = false;
	private static InetAddress address;
	private static int port;
	public static int randomSessionId;
	private static boolean keepRunning = true;
	private static DatagramPacket receivePacket; // a datagram packet that will be received
	private static DatagramPacket sendPacket; // a datagram packet that will be sent
	private static void handleUserInput() {
		boolean exit = false;
		while(!exit) {
			String command;
			Scanner input = new Scanner (System.in);
			System.out.println("Enter commands or exit to terminate");

			command = input.next();
			if("RN".equals(command) || "RM".equals(command)) {
				MavenCommon toEncode;
				if("RN".equals(command)) {
					toEncode  = new MavenCommon (Consts.REQUEST_NODE_TYPE, 0, randomSessionId);  //if its a maven addition
				} else {
					toEncode  = new MavenCommon (Consts.REQUEST_MAVEN_TYPE, 0, randomSessionId);
				}
				try {
					System.out.println(MVNClient.sendMessage(toEncode, MVNClient.address, MVNClient.port));
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Communication Problem");
				}

			} else if ("MA".equals(command) || "NA".equals(command) || "ND".equals(command) ||
					"MD".equals(command)) {	
				MavenCommon toEncode;
				String addresses = input.nextLine();
				Scanner addressParser = new Scanner(addresses);
				if("MA".equals(command)) {
					toEncode  = new MavenCommon (Consts.MAVEN_ADDITION_TYPE, 0, randomSessionId);  //if its a maven addition
				} else if("NA".equals(command)) {
					toEncode  = new MavenCommon (Consts.NODE_ADDITION_TYPE, 0, randomSessionId);  //if its a node addition
				} else if ("ND".equals(command)) {
					toEncode  = new MavenCommon (Consts.NODE_DELETION_TYPE, 0, randomSessionId);  // if its a node deletion
				} else {
					//the command specified was MD 
					toEncode  = new MavenCommon (Consts.MAVEN_DELETION_TYPE, 0, randomSessionId);  //if its a maven deletion
				}
				while(addressParser.hasNext()) {
					String addressAndPort = addressParser.next();
					String address = addressAndPort.substring(0, addressAndPort.indexOf(":"));
					int port = Integer.valueOf (addressAndPort.substring((addressAndPort.indexOf(":")+1)));
					try {
						InetSocketAddress anAddr = new InetSocketAddress(InetAddress.getByName(address), port);
						toEncode.addAddress(anAddr);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					MVNClient.sendMessage(toEncode, MVNClient.address, MVNClient.port);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Communication Problem");
				}



			} else if ("exit".equals(command)) {
				exit = true;
				MVNClient.keepRunning = false;
			}

		}
	}

	/**
	 * This function sends packets to the server and listens for user input
	 * @throws IOException if there is an I/O problem
	 * @throws SocketException if there was a problem connecting to the Maven
	 */
	public static MavenCommon sendMessage(MavenCommon toEncode, InetAddress address, int port) throws IOException, SocketException {
		
		ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
		toEncode.encode(outputstream);
		//the datagram packet that will be sent
		MVNClient.sendPacket = new DatagramPacket(outputstream.toByteArray(), outputstream.toByteArray().length,address, port);

		//the datagram packet that will be received
		MVNClient.receivePacket =  new DatagramPacket(new byte[Consts.MAX_MAVEN_BUFFER_SIZE], Consts.MAX_MAVEN_BUFFER_SIZE);
		connSock.send(sendPacket); // Send the echo string
		if(toEncode.getType()==Consts.REQUEST_NODE_TYPE || toEncode.getType()==Consts.REQUEST_MAVEN_TYPE) {
			return MVNClient.receiveMessage(address);
		}
		return null;
	}

	/**
	 * receives and prints notify messages to console output
	 * @throws IOException if trhere is an I/O problem
	 */
	public static MavenCommon receiveMessage(InetAddress sourceAddress) throws IOException {
		MavenCommon received = new MavenCommon();
		int tries =0;
		do {
			try {
				MVNClient.connSock.receive(receivePacket); // Attempt echo reply reception

				if (!receivePacket.getAddress().equals(sourceAddress)) {// Check source
					throw new IOException("Received packet from an unknown source");
				}
				MVNClient.receivedResponse = true;
			} catch (InterruptedIOException e) { // We did not get anything
				tries += 1;
				System.out.println("Timed out, " + (MVNClient.MAX_ATTEMPTS- tries) + " more tries...");
			} 
		} while ((!receivedResponse) && (tries < MVNClient.MAX_ATTEMPTS) && MVNClient.keepRunning );
		//if a message was received back from the server
		if(receivedResponse) {
			int receivedLength= receivePacket.getLength();		
			//create a new byte array with the actual length of bytes received
			byte[] actualBytesReceived = new byte[receivedLength] ;	

			//copy the received data packet into the new byte array
			System.arraycopy(receivePacket.getData(), 0, actualBytesReceived, 0, receivedLength);

			ByteArrayInputStream inputstream = new ByteArrayInputStream (actualBytesReceived);		

			received.decode(inputstream);			
			if(received.getSessionId()!= MVNClient.randomSessionId) {
				System.out.println("Session ID does not match");
			}

			if(received.getType() != Consts.ANSWER_REQUEST_TYPE) {
				System.out.println("Unexpected message type");
			}
		}
		return received;
	}

	/**
	 * sets up the mvn client to receive and send messages
	 * @param args arguments passed into main
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public static void setUp (String []args) throws SocketException, UnknownHostException { 
		MVNClient.connSock = new DatagramSocket();
		MVNClient.connSock .setSoTimeout(TIMEOUT);
		Random randomGenerator = new Random();
		MVNClient.randomSessionId = randomGenerator.nextInt(Consts.MAX_BYTE_SIZE);
		if(args!=null) {
			MVNClient.address = InetAddress.getByName(args[0]);
			MVNClient.port = Integer.valueOf(args[1]);
		}
	}
	public static void main(String[] args) throws IOException {
		if ((args.length < 2) || (args.length > 3)) { // check that we have the correct number of arguments
			throw new IllegalArgumentException("Parameter(s): <Server> <Word> [<Port>]");
		}
		MVNClient.setUp(args);
		MVNClient.handleUserInput();
	}
}
