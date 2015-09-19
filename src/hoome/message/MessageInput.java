/*
 *MessageInput.java
 *Jan 31, 2013
 */
package hoome.message;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;



/**
 * stores input stream for deserialization
 * @author Sola Adekunle
 *
 */
public class MessageInput {

	private InputStream input;
	private DataInputStream scanner;
	/**
	 * Constructs a new input source from an InputStream
	 * @param in byte input source
	 */
	public MessageInput(java.io.InputStream in) {
		this.setInput( in);
		this.setScanner(this.input);
	}

	/**
	 * sets the scanner for the input stream
	 * @param input2 inputstream
	 */
	private void setScanner(InputStream input2) {
		this.scanner = new DataInputStream(input2);

	}

	/**
	 * gets the input stream
	 * @return input stream
	 */
	public InputStream getInput() {
		return input;
	}
	/**
	 * sets the input stream
	 * @param input input stream
	 */
	public void setInput(InputStream input) {
		this.input = input;
	}

	/**
	 * gets the scanner for the input stream
	 * @return scanner
	 */
	public DataInputStream getScanner () {
		return this.scanner;
	}

	/**
	 * reads a string from an input stream using data input stream
	 * @return the string that has been read in
	 * @throws UnsupportedEncodingException 
	 * @throws EOFException 
	 * @throws IOException 
	 */
	public String readString () throws BadAttributeValueException, UnsupportedEncodingException, EOFException {
		Byte endByte = "\n".getBytes(HooMEMessage.CHARENCODING)[0];
		try {
			List<Byte> myBytes = new ArrayList <Byte> ();
			byte aByte;
			aByte = this.scanner.readByte();
			while(aByte!= endByte) {
				myBytes.add(aByte);
				aByte = this.scanner.readByte();	
			}
			byte[] myNewBytes = new  byte[myBytes.size()];

			for (int i = 0; i < myNewBytes.length; i++) {
				myNewBytes[i] = (byte) myBytes.get(i);
			}
			return new String (myNewBytes, HooMEMessage.CHARENCODING);
		}  catch (EOFException e) {	
			throw e;			
		} catch (UnsupportedEncodingException e) {
			System.out.println("2");
			e.printStackTrace();
		} catch (IOException e) {		
			System.out.println("3");
			throw new BadAttributeValueException (e.getMessage());
		}
		return null;

	}
}
