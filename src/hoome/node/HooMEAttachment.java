package hoome.node;

import hoome.message.HooMEMessage;

import java.nio.ByteBuffer;
import java.util.Queue;

public class HooMEAttachment {

	private ByteBuffer buffer;
	private Queue<HooMEMessage> sendQueue;     //contains HooMEmessages to send mostly search messages
	private boolean sendHandshake;  //for knowing if we are sending or receiving handshakes
	private boolean connectionOk = false;
	
	
	public HooMEAttachment (ByteBuffer aBuffer, Queue<HooMEMessage> sendQueue, boolean sendHandshake) {
		this.buffer = aBuffer;
		this.sendQueue = sendQueue;
		this.sendHandshake = sendHandshake;
	}
	
	public ByteBuffer getBuffer () {
		return this.buffer;
	}
	
	public Queue<HooMEMessage> getSendQueue () {
		return this.sendQueue;
	}
	
	public boolean connectionOk() {
		return connectionOk;
		
	}
	
	public boolean getSendHandshake() {
		return this.sendHandshake;
	}
	
	public void setConnectionOk(boolean connection) {
		this.connectionOk = connection;
	}
}
