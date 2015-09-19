/*
 *MessageOutput.java
 *Jan 31, 2013
 */
package hoome.message;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * stores output stream for serialization
 * @author Sola Adekunle
 *
 */
public class MessageOutput {
	
	private OutputStream output;
	private DataOutputStream dataOutput;
	
	
	/**
	 * Constructs a new output source from an OutputStream
	 * @param out byte output sink
	 */
	public MessageOutput(java.io.OutputStream out) {
		this.output = out;
		this.dataOutput = new DataOutputStream (out);
	}

	/**
	 * gets the output stream
	 * @return output stream
	 */
	public OutputStream getOutput() {
		return output;
	}

	/**
	 * sets the output stream
	 * @param output output stream
	 */
	public void setOutput(OutputStream output) {
		this.output = output;
	}
	/**
	 * gets the data output stream for the output stream
	 * @return data output stream
	 */
	public DataOutputStream getDataOutputStream () {
		return this.dataOutput;
	}

}
