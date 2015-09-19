/*
 * DownloadService.java
 */
package hoome.message;

import hoome.node.HooMENode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import mvn.Consts;

/**
 * threaded connection class for downloads
 * @author adekunle
 *
 */
public class DownloadService implements Runnable  {

	private Socket clntSock;
	InputStream input;
	OutputStream output;
	String fileId;
	String fileName;
	private Logger logger;

	/**
	 * sets up download service
	 * @param aDownload the socket for a download connection
	 * @param fileId the id of the file being dowloaded
	 * @throws IOException  if i/o error occurs
	 */
	public DownloadService (Socket aDownload, Logger aLogger) throws IOException {
		this.clntSock = aDownload;
		this.logger = aLogger;
		this.fileName="";
		this.input = aDownload.getInputStream();
		this.output = aDownload.getOutputStream();
	}

	/**
	 * 
	 * @param aDownload
	 * @param fileId the id of the file that is being downloaded
	 * @param fileName the file that will be downloaded to
	 * @param aLogger for recording activity
	 * @throws IOException 
	 */
	public DownloadService (Socket aDownload, String fileId, String fileName, Logger aLogger) throws IOException {
		this.clntSock = aDownload;
		this.logger = aLogger;
		this.fileId = fileId;
		this.fileName = fileName;
		this.input = aDownload.getInputStream();
		this.output = aDownload.getOutputStream();
	}

	/**
	 * Overrides run in thread class
	 */
	@Override
	public void run() {
		try {
			this.sendAndReceive();
		} catch (BadAttributeValueException e) {
			e.printStackTrace();
		}
	}

	/**
	 * sends and receives download requests
	 * @throws BadAttributeValueException 
	 */
	public void sendAndReceive () throws BadAttributeValueException {
		//if we are responding to a file download request
		if("".equals(this.fileName)) {
			MessageInput anInput = new MessageInput(input);
			try {
				int fileId = Integer.valueOf(anInput.readString());
				if(HooMENode.containsFileId(fileId)) {		
					this.output.write(Consts.DOWNLOAD_OK.getBytes(HooMEMessage.CHARENCODING));
					HooMENode.getFile(fileId, this.output);
					this.logger.log(Level.INFO, "wrote file with id "+ fileId + "to output stream");
					try {
						clntSock.close();
					} catch (IOException e) {
						System.out.println("Cannot close connection");
						this.logger.log(Level.WARNING, "Unable to close connection");
					}
				} else {
					this.output.write((Consts.PRE_ERROR + " " + Consts.NO_FILE).getBytes(HooMEMessage.CHARENCODING));
					try {
						this.logger.log(Level.SEVERE, "***client terminated");
						clntSock.close();
					} catch (IOException e) {
						System.out.println("Cannot close connection");
						this.logger.log(Level.WARNING, "Unable to close connection");
					}
				}				
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				this.logger.log(Level.WARNING, "Unable to read file ID");
			}

			//we are sending a file download request
		} else {
			try {
				this.output.write((this.fileId + Consts.EOLN).getBytes(HooMEMessage.CHARENCODING));
			} catch (IOException e) {
				System.out.println("Unable to send file download request");
				this.logger.log(Level.WARNING, "Unable to send file download request");
			}

			try {
				this.downloadFile();
			} catch (IOException e) {
				this.logger.log(Level.SEVERE, "Unable to download file");
			}

		}
	}

	/**
	 * downloads a file to a local directory
	 * @throws IOException if I/o error occurs
	 */
	public void downloadFile () throws IOException {
		//if there are any bytes to be read from the input stream
		while(this.input.available()==0 && HooMENode.keepRunning) {
			//wait for bytes to become available in the inputstream 
		}

		//there are bytes to read check to see if a file is available
		byte [] response = new byte[Consts.FOUR_BITS];
		this.input.read(response);
		String responseOk = new String (response, HooMEMessage.CHARENCODING);
		if(responseOk.contains("OK")) { 		//we can download content
			File aFile = new File(HooMENode.dir.getCanonicalPath()+File.separator+this.fileName); 
			FileOutputStream fileOutput = new FileOutputStream(aFile);
			byte [] fileBuffer = new byte[4096];
			int len;
			//write the contents to the file specified by the file name
			while((len=this.input.read(fileBuffer))!=-1) {
				fileOutput.write(fileBuffer, 0, len);
			}
			fileOutput.close();
			//ensure that the file we just downloaded gets added to our list
			HooMENode.rePopulate();
		} else if (responseOk.contains("ERRO")) { //an error has occurred print it to the screen
			byte [] errorMessage = new byte [this.input.available()];
			this.input.read(errorMessage);
			String errMsg = new String(errorMessage, HooMEMessage.CHARENCODING);
			System.out.println(responseOk + errMsg);
		}

	}

}
