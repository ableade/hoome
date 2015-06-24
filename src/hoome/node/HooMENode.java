/*
 *HooMENode.java
 *Jan 31, 2013
 */

package hoome.node;

import hoome.message.BadAttributeValueException;
import hoome.message.DownloadService;
import hoome.message.HooMEMessage;
import hoome.message.HooMESearch;
import hoome.message.Result;
import hoome.message.RoutingService;
import hoome.message.test.RandomByteGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import mvn.Consts;
import mvn.MVNClient;
import mvn.MavenCommon;

/**
 * This class acts as a HooMENode client
 * @author Sola Adekunle
 *
 */
public class HooMENode {
	public static String address = "wind.ecs.baylor.edu"; //address of the node
	public static byte [] id = RandomByteGenerator.getRandomBytes(Consts.ID_SIZE); //source id of this node
	public static boolean keepRunning = true;  //loop control
	public static File dir; //the current working directory for search downloads and responses
	public static List<Thread> clients = new ArrayList<Thread>(); //all threads currently running
	public static Map <String, Long> files;  //Ids and file names for all files in the cwd
	public static int downloadPort;   //the port for all downloads
	public static Logger aLogger = Logger.getLogger("HoomeLogger"); //logger for logging erros
	public static byte [] sourceAddr = {0,0,0,0,0};   //source address
	public static byte [] destAddr ={0,0,0,0,0};  //destination address
	public static int MAX_MAVEN_NUM=10;
	public static Selector selector;

	public static Set <InetSocketAddress> nodeAddresses = Collections.synchronizedSet(new HashSet<InetSocketAddress>());   //thread safe set for node addresses
	public static Set<InetSocketAddress> mavenAddresses = Collections.synchronizedSet(new HashSet<InetSocketAddress>());    //thread safe set for maven addresses	
	public static Map<byte[], InetAddress> mappingTable = Collections.synchronizedMap(new HashMap<byte[], InetAddress>());
	private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)

	/**
	 * sets up base values for HooMeNode class
	 * @param dirName the name of the directory where search files reside
	 * @throws IOException if I/O error occurs
	 */


	public static void  init(String dirName, int downloadPort, 	List<InetSocketAddress> mavenAddresses) throws IOException { 
		MVNClient.setUp(null);
		HooMENode.files = new ConcurrentHashMap<String, Long>();
		HooMENode.downloadPort = downloadPort;
		dir = new File(dirName);	
		//add all files currently in the working directory to the mapset		
		HooMENode.rePopulate();
		//set up the logger
		FileHandler handler = new FileHandler("hoome.log");
		handler.setFormatter(new MyLogger());
		selector = Selector.open();
		// Add to the desired logger
		HooMENode.aLogger.addHandler(handler);
		HooMENode.aLogger.setUseParentHandlers(false);
		HooMENode.addMavenAndNodes(mavenAddresses); //add all nodes and mavens to the list

	}

	/**
	 * adds files in directory to the HooME node map
	 */
	public static void rePopulate() {
		long count;
		if(HooMENode.files.size()== 0) {
			count =1;
		}
		else {
			count = HooMENode.files.size()+1;
		}
		if(dir.listFiles()!=null) {
			for (File child : dir.listFiles()) {
				if(child.isFile() && !HooMENode.files.containsKey(child.getName())) {
					HooMENode.files.put(child.getName(), count);
					count++;
				}
			}
		}
	}

	/**
	 * writes the contents of a file to an outputstream 
	 * @param fileName the name of the file to be written to the output stream
	 * @param out
	 * @throws IOException 
	 */
	public static void getFile(int fileId , OutputStream out) throws IOException {
		for (String name : HooMENode.files.keySet()) {
			if(HooMENode.files.get(name) == fileId){
				for (File child : dir.listFiles()) {
					if(child.isFile() && child.getName().equals(name)) {
						byte[] buffer = new byte[4096];
						FileInputStream fileInput = new FileInputStream(child);
						int len;
						while((len =fileInput.read(buffer))!=-1) {
							out.write(buffer, 0, len);
						}
						//write new line character to out putstream
						out.write(Consts.EOLN.getBytes(HooMEMessage.CHARENCODING));
						fileInput.close();
						break;
					}
				}
			}
		}
	}

	/**
	 * checks to see if a file with the file Id is present
	 * @param fileId the id of the file being searched for
	 * @return
	 */
	public static boolean containsFileId (int fileId) {
		return (HooMENode.files.containsValue((long)fileId));
	}

	/**
	 * checks to see if a file with the file name is present
	 * @param fileName the name of the file being searched for
	 * @return
	 */
	public static boolean containsFileName (String fileName)  {
		return (HooMENode.files.containsKey(fileName));
	}

	/**
	 * finds all files in the directory that match the given file name
	 * @param fileName the name of the file being searched for
	 * @return files that match the given file name
	 * @throws BadAttributeValueException if problem creating a result
	 */
	public static List<Result> getFileResults (String fileName) throws BadAttributeValueException {
		List<Result> results = new ArrayList<Result>();
		for (String child : HooMENode.files.keySet()) {
			if(child.contains(fileName)) {
				Result aResult = new Result (HooMENode.files.get(child), child.length(), child);
				results.add(aResult);
			}
		}
		return results;    	
	}

	/**
	 * adds node and maven addresses to its local node list
	 * @param mavenAddresses the list of Maven addresses to query for nodes and mavens
	 */
	public static void addMavenAndNodes (List <InetSocketAddress> mavenAddresses) {
		for(InetSocketAddress addr: mavenAddresses) {
			MavenCommon toEncode;
			try{
				//request the list of all node addresses and add them to the local node set
				toEncode  = new MavenCommon (Consts.REQUEST_NODE_TYPE, 0, MVNClient.randomSessionId);
				MavenCommon receivedNodes = MVNClient.sendMessage(toEncode, addr.getAddress(), addr.getPort());
				HooMENode.nodeAddresses.addAll(receivedNodes.getAddresses());

				//now request the list of mavens and add them to the local maven set
				toEncode  = new MavenCommon (Consts.REQUEST_MAVEN_TYPE, 0, MVNClient.randomSessionId);
				MavenCommon receivedMavens = MVNClient.sendMessage(toEncode, addr.getAddress(), addr.getPort());
				for(InetSocketAddress addr1: receivedMavens.getAddresses()) {
					if(HooMENode.mavenAddresses.size()<HooMENode.MAX_MAVEN_NUM) {
						HooMENode.mavenAddresses.add(addr1);
					} else {
						break;
					}
				}

				//now share unknown maven addresses with this maven node for the sake of the Possi
				HooMENode.shareMavensAndNodes(addr, receivedNodes, receivedMavens);
			}

			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * adds mavens and nodes to maven peers
	 * @param mavenAddress the addres of the peer
	 * @param aMavenNodes the nodes to be added
	 * @param aMavenMavens the mavens to be added
	 * @throws IOException if i/o error occurs
	 */

	public static void shareMavensAndNodes (InetSocketAddress mavenAddress, MavenCommon aMavenNodes, MavenCommon aMavenMavens) throws IOException {

		//add all nodes unknown to the maven with maven address
		List<InetSocketAddress> unknownAddresses = new ArrayList <InetSocketAddress> ();
		for(InetSocketAddress addr:HooMENode.nodeAddresses) {
			if(!aMavenNodes.getAddresses().contains(addr)) {
				unknownAddresses.add(addr);
			}
		}		
		MavenCommon toEncode;
		toEncode  = new MavenCommon (Consts.NODE_ADDITION_TYPE, 0, MVNClient.randomSessionId);
		toEncode.getAddresses().addAll(unknownAddresses);
		MVNClient.sendMessage(toEncode, mavenAddress.getAddress(), mavenAddress.getPort());

		// add all mavens unknown to maven with maven address

		unknownAddresses = new ArrayList <InetSocketAddress> ();

		for(InetSocketAddress addr:HooMENode.mavenAddresses) {
			if(!aMavenMavens.getAddresses().contains(addr)) {
				unknownAddresses.add(addr);
			}
		}	
		toEncode  = new MavenCommon (Consts.MAVEN_ADDITION_TYPE, 0, MVNClient.randomSessionId);
		toEncode.getAddresses().addAll(unknownAddresses);
		MVNClient.sendMessage(toEncode, mavenAddress.getAddress(), mavenAddress.getPort());
	}

	/**
	 * listens to and responds to messages
	 * @param protocol the protocol to handle messages
	 * @throws IOException if i/o error occirs
	 */
	public static void Poll (TCPProtocol protocol) throws IOException {
		if (selector.select(TIMEOUT) == 0) { // returns # of ready chans					
			
		}
		Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
		while (keyIter.hasNext()) {
			SelectionKey key = keyIter.next(); // Key is bit mask
			// Server socket channel has pending connection requests?
			if (key.isAcceptable()) {
				protocol.handleAccept(key);
			}
			// Client socket channel has pending data?
			if (key.isReadable()) {
				protocol.handleRead(key);
			}
			// Client socket channel is available for writing and
			// key is valid (i.e., channel not closed)?
			if (key.isValid() && key.isWritable()) {
				protocol.handleWrite(key);
			}
			keyIter.remove(); // remove from set of selected keys
		}
	}
	public static void main (String args []) throws IOException {
		final int NUM_PARAMETERS =5;
		if(args.length != NUM_PARAMETERS) {
			throw new IllegalArgumentException("Parameter(s): <Address> <Port> <Direcotry> <Maven Address> <Maven Port>");
		} else {
			int port = Integer.parseInt(args[2]); //get the port as an integer
			String directory = args[4]; //get the name of the file directory
			int downloadPort = Integer.valueOf(args[3]);
			InetSocketAddress self = new InetSocketAddress (InetAddress.getByName(HooMENode.address),port); 
			HooMENode.nodeAddresses.add(self);
			List<InetSocketAddress> mavenAddresses = new ArrayList<InetSocketAddress>();			
				String address = args[0];
				int mavenport = Integer.valueOf(args[1]);
				InetSocketAddress anAddr = null;				
				anAddr = new InetSocketAddress(InetAddress.getByName(address), mavenport);
				mavenAddresses.add(anAddr);
			try {
				HooMENode.init(directory, downloadPort, mavenAddresses);
				// Create a file handler that write log record to a file called connections
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			ServerSocketChannel listnChannel = ServerSocketChannel.open();
			listnChannel.socket().bind(new InetSocketAddress(port));
			listnChannel.configureBlocking(false); // must be nonblocking to register

			// Register selector with channel. The returned key is ignored
			listnChannel.register(selector, SelectionKey.OP_ACCEPT);

			//start listening for downloadConnections

			new Thread(new DownloadListener(downloadPort)).start();
			TCPProtocol protocol = new HooMENodeProtocol(Consts.MAX_MAVEN_BUFFER_SIZE);
			//initiate the socket connection
			while(HooMENode.keepRunning) {
				HooMENode.Poll(protocol);
				String command;
				Scanner input = new Scanner(System.in);
				System.out.println("Enter commands, or 'exit' to quit: ");
				command =  input.next();   
				if("connect".equals(command)) {
					HooMENode.aLogger.log(Level.INFO, "Client is atempting to establish a connection");
					String connectNode = input.next();
					String connectPort = input.next();
					try {
						SocketChannel clntChan = SocketChannel.open();
						clntChan.configureBlocking(false);
						if (!clntChan.connect(new InetSocketAddress(connectNode, Integer.parseInt(connectPort)))) {
							while (!clntChan.finishConnect()) {
								System.out.print(".");  // Do something else
							}
						}
						clntChan.register(selector, SelectionKey.OP_WRITE, new HooMEAttachment(ByteBuffer.allocate(Consts.MAX_MAVEN_BUFFER_SIZE),
								new LinkedBlockingQueue<HooMEMessage>(Consts.SEND_QUEUE_CAPACITY), true));

						HooMENode.Poll(protocol);
					} catch (NumberFormatException e ) {
						e.printStackTrace();
					} catch (ConnectException e) {
						System.out.println("Unable to connect to remote host");
					} catch (IOException e) {
						e.printStackTrace();
					} 

				}   else if ("download".equals(command)) { //if we are downloading
					String downloadNode = input.next();
					String downloadPort1 = input.next();
					String fileId = input.next();
					String fileName = input.next();

					//check to see if file already exists

					File file = new File(HooMENode.dir.getAbsolutePath()+File.separator+ fileName);
					if(file.exists()) {
						System.out.println("The file you are trying to download already exists in the present directory");
						HooMENode.aLogger.log(Level.INFO, "Client is atempting to redownload existing file");
					} else {
						try {
							Socket aSocket = new Socket(downloadNode, Integer.valueOf(downloadPort1));
							DownloadService aService = new DownloadService(aSocket, fileId, fileName, HooMENode.aLogger);
							DownloadListener.service.execute(aService);
						} catch (NumberFormatException e ) {
							e.printStackTrace();
						}
						catch (IOException e) {
							System.out.println("Unable to connect to remote host");
							HooMENode.aLogger.log(Level.INFO, "Client was unable to connect to remote host");
						}
					}
					HooMENode.Poll(protocol);
				} else if  ("exit".equals(command)) {     //if we wish to exit this program
					keepRunning = false;
					HooMENode.aLogger.log(Level.INFO, "******Client terminated");
				} else {
					//a search string has been entered and will be sent out to all connected neighbours 
					for(SelectionKey aKey : HooMENode.selector.keys()) {
						try {
							HooMEMessage aMessage = new HooMESearch(HooMENode.id, 25, RoutingService.getRoutingService(0), HooMENode.destAddr,
									HooMENode.sourceAddr,command);
							if(aKey.attachment()!=null && aKey.attachment() instanceof HooMEAttachment) {
								((HooMEAttachment)aKey.attachment()).getSendQueue().add(aMessage);
							}
						} catch (BadAttributeValueException e) {
							e.printStackTrace();
						}
					}
					HooMENode.Poll(protocol);
				}

				// Wait for some channel to be ready (or timeout)
				
			}
	
				try {
					listnChannel.close();
					selector.close();
				}  catch (IOException e) {
					HooMENode.aLogger.log(Level.INFO, "Socket already closed"); //report closed socket to the hoome log
				}
			
				
			}

		
	}

}
