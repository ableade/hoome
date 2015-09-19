/*
 *HooMEResponse.java
 *Jan 31, 2013
 */
package hoome.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import mvn.Consts;

/**
 * represents a HooMEResponse object
 * @author Sola Adekunle
 *
 */
public class HooMEResponse extends HooMEMessage {

	private InetSocketAddress responseHost; //socket address
	private List<Result> resultList; //list of all results


	/**
	 * gets list of results
	 * @return result list
	 */

	public List<Result> getResultList() {
		return resultList;
	}

	/**
	 * gets address of response host
	 * @return response host address and port
	 */

	public InetSocketAddress getResponseHost() {
		return responseHost;
	}

	/**
	 * sets the address of the response host
	 * @param responseHost the response host
	 */

	public void setResponseHost(java.net.InetSocketAddress responseHost)
			throws BadAttributeValueException {
		byte [] addr;
		try {
			addr = responseHost.getAddress().getAddress();
			if(addr==null) {
				throw new NullPointerException();
			}
		}catch (NullPointerException e) {
			throw new BadAttributeValueException(Consts.ILL_ARG);
		}

		this.responseHost = responseHost;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((responseHost == null) ? 0 : responseHost.hashCode());
		result = prime * result
				+ ((resultList == null) ? 0 : resultList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HooMEResponse other = (HooMEResponse) obj;
		if (responseHost == null) {
			if (other.responseHost != null)
				return false;
		} else if (!responseHost.equals(other.responseHost))
			return false;
		if (resultList == null) {
			if (other.resultList != null)
				return false;
		} else if (!resultList.equals(other.resultList))
			return false;
		return true;
	}

	/**
	 * constructs HooMEResponse from values
	 * @param id message id
	 * @param ttl message TTL
	 * @param routingService  message routing service
	 * @param sourceHooMEAddress message source address
	 * @param destinationHooMEAddress message destination address
	 * @throws BadAttributeValueException  if bad attribute
	 */

	public HooMEResponse(byte[] id, int ttl, RoutingService routingService,
			byte[] sourceHooMEAddress, byte[] destinationHooMEAddress,InetSocketAddress responseHost) throws BadAttributeValueException {
		super(id, ttl, routingService, sourceHooMEAddress, 
				destinationHooMEAddress);
		this.setResponseHost(responseHost);
		this.resultList = new ArrayList<Result> ();
	}

	/**
	 * constructs HooMEResponse from input stream
	 * @param in deserialization input source
	 * @throws java.io.IOException if I/O problem
	 * @throws BadAttributeValueException if bad data value
	 */

	public HooMEResponse(MessageInput in)
			throws java.io.IOException,
			BadAttributeValueException {
		super (in);
		this.resultList = new ArrayList<Result> ();
		int payloadLength = HooMEUtilities.getUnsignedInt(in.getScanner().readShort());
		long expecting =0;
		byte rawMatches = in.getScanner().readByte();
		int mul =8; //cobined size in octets of file id and size for every result
		short rawPort = in.getScanner().readShort();
		byte [] addressArray = new byte [Consts.FOUR_BITS];

		for(int i=0;i<Consts.FOUR_BITS;i++) {
			addressArray[i] = in.getScanner().readByte();
		}
		this.setResponseHost(new InetSocketAddress (InetAddress.getByAddress(addressArray), HooMEUtilities.getUnsignedInt(rawPort)));
		long matches = HooMEUtilities.getUnsignedLongInt(rawMatches);
		expecting = (mul* matches);
		for (long i=0;i<matches;i++)  {
			Result aResult = new Result(in);
			//check to see if we have exceeded the specified payload length and throw exception
			if((aResult.getFileName().length() + expecting) > payloadLength || 
					(aResult.getFileName().length() + expecting) > Consts.MAX_UNSIGNED_SHORT ) {
				throw new BadAttributeValueException(Consts.ILL_ARG);
			} else {
				expecting += aResult.getFileName().length();
				this.addResult(aResult);
			}

		}

	}

	@Override
	public String toString() {
		return "HooMEResponse [responseHost=" + responseHost + ", resultList="
				+ resultList + "]";
	}

	/**
	 * gets the message type of the current class
	 * @return themessageType
	 */

	public int getMessageType () {
		return Consts.RESPONSE_PAYLOAD;
	}

	/**
	 * overides encode in HooMEMessage
	 */

	@Override
	public void encode(MessageOutput out)
			throws java.io.IOException {
		super.encode(out);
		int payLoadLength = 0;
		for(Result aResult: this.resultList) {
			payLoadLength += aResult.getFileName().length()+1;
		}
		//add header for result
		payLoadLength += (Consts.FOUR_BITS + Consts.FOUR_BITS) * this.resultList.size();
		//add the header for response
		payLoadLength+= 1 + Consts.TWO_BITS + Consts.FOUR_BITS;

		out.getDataOutputStream().writeShort((short) payLoadLength);
		out.getDataOutputStream().writeByte((byte)this.resultList.size());
		out.getDataOutputStream().writeShort((short)this.responseHost.getPort());
		out.getDataOutputStream().write(this.responseHost.getAddress().getAddress());
		for(Result aResult : this.resultList ) {
			aResult.encode(out);
		}
	}

	/**
	 * adds a result to the result list
	 * @param result new result to add to result list
	 * @throws BadAttributeValueException if bad attribute value
	 */

	public void addResult(Result result)
			throws BadAttributeValueException {
		//check to see if result is null
		if(result==null || this.resultList.size() > Consts.MAX_BYTE_SIZE) {
			throw new BadAttributeValueException (Consts.ILL_ARG);
		}
		if(validateSize(result)) {
			this.resultList.add(result);
		} else {
			throw new BadAttributeValueException(Consts.ILL_ARG);
		}
	}

	/**
	 * checks to see if we have not exceeded the allowable number of bytes in a payload
	 * @param result the result that is being added
	 * @return true if not bad attribute
	 */
	public boolean validateSize(Result result) {
		int existingResultSize = 8; //size of a result object with an empty string
		int count = 28;  //	 combined size of all fields currently in the hoome response class
		count+=existingResultSize+ result.getFileName().length();  //add the length result to be added to the current count
		for(Result aResult: this.resultList) {
			count+= existingResultSize + aResult.getFileName().length();
			if(count>Consts.MAX_UNSIGNED_SHORT) {
				return false;
			}
		}
		return true;
	}
}
