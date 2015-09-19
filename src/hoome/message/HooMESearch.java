/*
 *HooMESearch.java
 *Jan 31, 2013
 */
package hoome.message;

import mvn.Consts;

/**
 * represents a HooMESearch message
 * @author Sola Adekunle
 *
 */
public class HooMESearch  extends HooMEMessage{

	private String searchString; //the search string

	/**
	 * Constructs HooMESearch Object
	 * @param id messageId
	 * @param ttl messageTTL
	 * @param routingService
	 * @param sourceHooMEAddress
	 * @param destinationHooMEAddress
	 * @throws BadAttributeValueException
	 */

	public HooMESearch(byte[] id, int ttl, RoutingService routingService, byte[] sourceHooMEAddress,
			byte[] destinationHooMEAddress, String searchString) throws BadAttributeValueException {
		super(id, ttl, routingService, sourceHooMEAddress, destinationHooMEAddress);
		this.setSearchString(searchString);
	}


	/**
	 * gets the search string in the HooMeSearch message
	 * @return the search string
	 */

	public String getSearchString() {
		return searchString;
	}

	/**
	 * overrides toString in java.lang.object
	 */

	@Override
	public String toString() {
		return super.toString() + "HooMESearch [searchString=" + searchString + "]";
	}

	/**
	 * sets the value of search string
	 * @param searchString the string being searched for
	 * @throws BadAttributeValueException if bad attribute value
	 */

	public void setSearchString(String searchString) throws BadAttributeValueException {
		if(null==searchString || searchString.contains(Consts.EOLN) || searchString.isEmpty() ||
				searchString.length() >= Consts.MAX_UNSIGNED_SHORT) {
			throw new BadAttributeValueException(Consts.ILL_ARG);
		}
		this.searchString = searchString; 
	}

	/**
	 * gets the message type of the current class
	 * @return themessageType
	 */

	public int getMessageType () {
		return Consts.SEARCH_PAYLOAD;
	}

	/**
	 * constructs HooMESearch from inputstream
	 * @param in input steam
	 * @throws java.io.IOException if i/o error occurs
	 * @throws BadAttributeValueException if bad attribute value
	 */

	public HooMESearch(MessageInput in) throws java.io.IOException, BadAttributeValueException  {
		super(in);
		//read in the payload length
		int payloadLength = HooMEUtilities.getUnsignedInt(in.getScanner().readShort());
		this.setSearchString(( in.readString()));
		if(this.searchString.length()+1 > payloadLength) {
			throw new BadAttributeValueException(Consts.ILL_ARG);
		}
	}	
	/**
	 * overrides encode in HooMEMessage
	 */

	@Override
	public void encode(MessageOutput out) throws java.io.IOException {
		super.encode(out);
		out.getDataOutputStream().writeShort((short)(this.searchString.length()+1));
		out.getDataOutputStream().write(this.searchString.getBytes(HooMEMessage.CHARENCODING));
		out.getDataOutputStream().write(Consts.EOLN.getBytes(HooMEMessage.CHARENCODING));
	}
}
