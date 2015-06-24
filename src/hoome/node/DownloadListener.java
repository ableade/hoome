/*
 * DownloadListener.java
 */
package hoome.node;

import hoome.message.DownloadService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import mvn.Consts;

/**
 * listens for incoming download connections
 * @author adekunle
 *
 */
public class DownloadListener implements Runnable {

	private int port; //the port number that the download service will listen on
	public static Executor service = Executors.newFixedThreadPool(Consts.FOUR_BITS); //fixed thread pool
	
	/**
	 * Overides run method in superclass threa
	 */
	@Override
	public void run() {
		try {
			DownloadListener.listen(this.port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * listens for incoming download connections on a specified port
	 * @param port the port number to listen to
	 * @throws IOException
	 */
	public static void listen (int port) throws IOException {
		ServerSocket socket = new ServerSocket(port); //start listening in on specified port
		socket.setReuseAddress(true);
		while (HooMENode.keepRunning) {
			Socket clntSock = socket.accept();
			DownloadService aService = new DownloadService(clntSock, HooMENode.aLogger);
			DownloadListener.service.execute(aService);
		}
	}
	
	/**
	 * sets up download listener
	 * @param port port number for the server socket
	 */
	public DownloadListener(int port) {
		this.port= port;
	}
	
}
