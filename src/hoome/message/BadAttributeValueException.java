/*
 * BadAttributeValueException.java
 * January 31 2013
 */
package hoome.message;

/**
 * Custom exception class for bad attributes
 * @author Sola Adekunle
 *
 */
public class BadAttributeValueException extends Exception {

	//attribute with the bad value
	private String attributeName;

	/**
	 * constructs bad attribute exception
	 * @param message
	 */
	public BadAttributeValueException(String message) {
		super(message);
	}

	/**
	 * Constructs bad attribute exception
	 * @param message the message
	 * @param cause cause of exception
	 */
	public BadAttributeValueException(String message, Throwable cause) {
		super (message,cause);
	}

	/**
	 * Constructs a BadAttributException
	 * @param errMessage error message
	 * @param attributeName  name of attribute
	 */
	public BadAttributeValueException(java.lang.String errMessage,
			java.lang.String attributeName) {
		super(errMessage);
		this.attributeName = attributeName;
	}

	/**
	 * Get name of attribute with bad value
	 * @return attribute name
	 */
	public java.lang.String getAttributeName() {
		return this.attributeName;
	}
	private static final long serialVersionUID = 1L;

}
