package mvn;

/*
 * MavenCommon.java
 */


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * This class serves as a base class for all maven nodes.
 * @author Sola Adekunle
 *
 */
public class MavenCommon {
	@Override
	public String toString() {
		return "MavenCommon [sessionId=" + sessionId + ", error=" + error
				+ ", type=" + type + ", addresses=" + addresses + "]";
	}

	public static int version = 4;
	private int sessionId;
	private int error;
	private int type;
	private List<InetSocketAddress> addresses; //represents a list of all addresses and port

	/**
	 * initializes maven common with the parameters passed in
	 */
	public MavenCommon (int type, int error, int sessionId) {
		this.setSessionId(sessionId);
		this.setError(error);
		this.setType(type);
		this.addresses = new ArrayList<InetSocketAddress> ();
	}
	/**
	 * 
	 * @return the list of addresses that the object has
	 */
	public List<InetSocketAddress> getAddresses () {
		return this.addresses;
	}

	public void addAddress (InetSocketAddress anAddr) {
		if(anAddr!=null) {
			this.addresses.add(anAddr);
		} else {
			//TODO determine exception to throw and throw it
		}
	}

	/**
	 * default constructor for maven common
	 */
	public MavenCommon () {
		this.addresses = new ArrayList<InetSocketAddress> ();
	}

	/**
	 * Decodes expected values from input strean
	 * @param in
	 * @throws IOException
	 */
	public void decode (InputStream in) throws IOException {
		DataInputStream theInputStream = new DataInputStream (in);
		int versionAndType = 0;
		int version; 
		int  andMask = 15;      
		versionAndType = theInputStream.readUnsignedByte();
		this.type = versionAndType & andMask;
		version = versionAndType >> Consts.FOUR_BITS;
		if(version==MavenCommon.version) {
			this.error = theInputStream.readUnsignedByte();
			this.sessionId = theInputStream.readUnsignedByte();		
			int count = theInputStream.readUnsignedByte();				
			//if this is not a request read in the addresses 
			if(count > 0) {
				for(int i=0;i< count;i++) {
					byte [] addr = new byte[4];
					theInputStream.read(addr);
					int port = theInputStream.readShort();
					this.addAddress(new InetSocketAddress(InetAddress.getByAddress(addr), port));							
				}
			}
		} else {
			//we have an invalid 
		}
	}
	/**
	 * writes out values to output stream 
	 * @param out the output stream to be written to
	 * @throws IOException if I/O error occurs
	 */
	public void encode (OutputStream out) throws IOException {
		DataOutputStream encoder = new DataOutputStream (out);
		byte versionAndType =  (byte) (this.type & 0xFF);
		byte byteVersion = (byte) (MavenCommon.version  & 0xFF);
		byteVersion = (byte) (byteVersion <<Consts.FOUR_BITS);
		versionAndType |= byteVersion;
		encoder.write(versionAndType);
		encoder.write(error);
		encoder.write(this.sessionId);
		encoder.write(this.addresses.size());
		for(InetSocketAddress addr: this.addresses) {
			encoder.write(addr.getAddress().getAddress());
			encoder.writeShort(addr.getPort());
		}
	}
	/**
	 * gets session id
	 * @return the session id
	 */
	public int getSessionId() {
		return sessionId;
	}

	/**
	 * sets session id
	 * @param sessionId the session id
	 */
	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * gets the value of error
	 * @return error
	 */
	public int getError() {
		return error;
	}

	/**
	 * sets error
	 * @param error the error code
	 */
	public void setError(int error) {
		this.error = error;
	}

	/**
	 * sets addresses
	 * @param addresses addresses
	 */
	public void setAddresses(List<InetSocketAddress> addresses) {
		this.addresses = addresses;
	}

	/**
	 * sets type
	 * @param type the type
	 */
	public void setType (int type) {
		this.type = type;
	}
	
	/**
	 * gets type
	 * @return the type
	 */
	public int getType() {
		return this.type;
	}
}

