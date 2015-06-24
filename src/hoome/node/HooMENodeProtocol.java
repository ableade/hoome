/*
 *HooMENodeProtocol.java
 *April 25, 2013
 */

package hoome.node;

import hoome.message.BadAttributeValueException;
import hoome.message.HooMEMessage;
import hoome.message.HooMEResponse;
import hoome.message.HooMESearch;
import hoome.message.HooMEUtilities;
import hoome.message.MessageInput;
import hoome.message.MessageOutput;
import hoome.message.Result;
import hoome.message.RoutingService;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mvn.Consts;

/**
 * This class implements TCP protocol for handling read and write operations on selectors
 * @author adekunle
 *
 */
public class HooMENodeProtocol implements TCPProtocol {

	private int bufSize; // Size of I/O buffer

	/**
	 * initializes buffer size
	 * @param bufSize the size of the byte buffer
	 */
	public HooMENodeProtocol(int bufSize) {
		this.bufSize = bufSize;
	}

	/**
	 * accepts incoming network connections
	 */
	public void handleAccept(SelectionKey key) throws IOException {
		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
		clntChan.configureBlocking(false); // Must be nonblocking to register
		// Register the selector with new channel for read and attach byte buffer
		clntChan.register(key.selector(), SelectionKey.OP_READ, new HooMEAttachment(ByteBuffer.allocate(bufSize),
				new LinkedBlockingQueue<HooMEMessage>(Consts.SEND_QUEUE_CAPACITY), false));
	}

	/**
	 * handles read connections for channels
	 */
	public void handleRead(SelectionKey key) throws IOException {
		// Client socket channel has pending data
		SocketChannel clntChan = (SocketChannel) key.channel();
		ByteBuffer buf = ((HooMEAttachment) key.attachment()).getBuffer();
		buf = (ByteBuffer.allocate(Consts.MAX_MAVEN_BUFFER_SIZE));
		long bytesRead = clntChan.read(buf);
		if (bytesRead == -1) { // Did the other end close?
			clntChan.close();
		} else if (bytesRead > 0 && !HooMEUtilities.isEmpty(buf.array())) {
			InputStream anInputStream = new ByteArrayInputStream (buf.array());
			MessageInput anInput = new MessageInput (anInputStream);
			//if we are supposed to receive a handshake 
			if(!((HooMEAttachment)key.attachment()).getSendHandshake()&&!((HooMEAttachment)key.attachment()).connectionOk()) {				
				String greeting = null;	
				try {
					System.out.println("received handshake");
					greeting = anInput.readString();
					anInput.readString();
				} //consume the second new line character

				catch (BadAttributeValueException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(greeting.equals(Consts.HANDSHAKE_GREETING)) {
					key.interestOps(SelectionKey.OP_WRITE);
				} else {
					//handshake greeting was wrong we will terminate this connection
					key.channel().close();
					key.cancel();
				}

				// if we are supposed to be reading an acknowledgment
			} else if( ((HooMEAttachment)key.attachment()).getSendHandshake() && !((HooMEAttachment)key.attachment()).connectionOk()) {
				String response = null;
				try {
					response = anInput.readString();
					if(response.equals(Consts.HANDSHAKE_RESPONSE)) {
						((HooMEAttachment)key.attachment()).setConnectionOk(true);
						key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);	
						anInput.readString(); //consume the second newline character
					} else {
						key.channel().close();
						key.cancel();
					}
				} catch (BadAttributeValueException e) {
					e.printStackTrace();
				}
			} else {
				HooMEMessage aMessage= null;
				try {
					aMessage = HooMEMessage.decode(anInput);  
					//if we have a response sent back
					if(aMessage instanceof HooMEResponse && aMessage.getTtl()>0) { //treat all responses as comcast
						System.out.println(aMessage);
						//look up id of message in the forwarding table
						for (Entry<byte[], InetAddress> entry : HooMENode.mappingTable.entrySet()) {
							if(Arrays.equals(aMessage.getId(), entry.getKey())) {  //if we have a mapping for this id in our routing table
								//loop through our neighbours and find the one we want
								for(SelectionKey aKey:key.selector().keys()) {
									if(aKey.channel() instanceof SocketChannel && 
											((SocketChannel)aKey.channel()).socket().getInetAddress()==entry.getValue()) {
										aMessage.setTtl(aMessage.getTtl()-1);
										((HooMEAttachment)key.attachment()).getSendQueue().add(aMessage);
										break;
									}
								}
							}
						}
					} else if (aMessage instanceof HooMESearch) {   //if a search message has been sent . 
						//broadcast service requested broadcast the search to all and sundry
						if(aMessage.getRoutingService().getServiceCode()==0 &&aMessage.getTtl()>0) {
							aMessage.setTtl(aMessage.getTtl()-1);
							for(SelectionKey aKey:key.selector().keys()) {
								if(aKey.attachment()!=null && aKey.attachment() instanceof HooMEAttachment) {
									((HooMEAttachment)aKey.attachment()).getSendQueue().add(aMessage);
								}
							}

							//place mapping for the broadcast message in the mapping table
							HooMENode.mappingTable.put(aMessage.getId(),  ((SocketChannel)key.channel()).socket().getInetAddress());
						} 

						//respond to the search
						List<Result> searchResults = HooMENode.getFileResults(((HooMESearch)aMessage).getSearchString());
						HooMEMessage response = new HooMEResponse(aMessage.getId(), 25, RoutingService.getRoutingService(1), aMessage.getDestinationHooMEAddress(), aMessage.getSourceHooMEAddress(), 
								new InetSocketAddress(HooMENode.address,HooMENode.downloadPort));
						((HooMEResponse)response).getResultList().addAll(searchResults);
						((HooMEAttachment)key.attachment()).getSendQueue().add(response);
					}
					// Indicate via key that reading/writing are both of interest now.
					key.interestOps(SelectionKey.OP_WRITE);

				}catch (IOException e) {
					e.printStackTrace();
				} catch (BadAttributeValueException e) {
					System.out.println(e.getMessage());
				}

			}
		}
	}

	/**
	 * handles write connections for channels
	 */
	public void handleWrite(SelectionKey key) throws IOException {
		OutputStream anOutput = new ByteArrayOutputStream();
		ByteBuffer buf = ((HooMEAttachment) key.attachment()).getBuffer();
		buf = (ByteBuffer.allocate(Consts.MAX_MAVEN_BUFFER_SIZE));
		buf.clear();
		/*
		 * Channel is available for writing, and key is valid (i.e., client channel
		 * not closed).
		 */

		//we are supposed to send an acknowledgement of a handshake
		if(!((HooMEAttachment)key.attachment()).getSendHandshake()&&!((HooMEAttachment)key.attachment()).connectionOk()) { 
			anOutput.write((Consts.HANDSHAKE_RESPONSE+Consts.EOLN+Consts.EOLN).getBytes(HooMEMessage.CHARENCODING));
			//confirm that the connection is ok
			((HooMEAttachment)key.attachment()).setConnectionOk(true);
			buf.put(((ByteArrayOutputStream)anOutput).toByteArray());
			SocketChannel clntChan = (SocketChannel) key.channel();
			buf.flip();
			clntChan.write(buf);
			if(!buf.hasRemaining()) {
				key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);	
			}

			//if we are supposed to be sending an handshake 
		} else if( ((HooMEAttachment)key.attachment()).getSendHandshake() && !((HooMEAttachment)key.attachment()).connectionOk()) {
			anOutput.write((Consts.HANDSHAKE_GREETING+Consts.EOLN+Consts.EOLN).getBytes(HooMEMessage.CHARENCODING));
			buf.put(((ByteArrayOutputStream)anOutput).toByteArray());
			SocketChannel clntChan = (SocketChannel) key.channel();
			buf.flip();
			clntChan.write(buf);
			
			if(!buf.hasRemaining()) {
				key.interestOps(SelectionKey.OP_READ);	
			}
		} else {
			//check the send queue for messages that can be sent if there are none then do nothing
			HooMEMessage aMessage = ((HooMEAttachment) key.attachment()).getSendQueue().poll();
			if(aMessage != null) {
				MessageOutput aMessageOutput = new MessageOutput(anOutput);
				aMessage.encode(aMessageOutput);	
				buf.put(((ByteArrayOutputStream)anOutput).toByteArray());
				SocketChannel clntChan = (SocketChannel) key.channel();
				buf.flip();
				clntChan.write(buf);
			}		
			
			if(!buf.hasRemaining()) {
				key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			}
		}


		// Make room for more data to be read in
	}
}
