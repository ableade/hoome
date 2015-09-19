/*
 *HooMEMessage.java
 *Jan 31, 2013
 */
package hoome.message;

import java.io.IOException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

import mvn.Consts;

/**
 * Base class for serialization and deserialization functionality
 * @author Sola Adekunle
 *
 */
public class HooMEMessage{

	//class members
	public static java.lang.String  CHARENCODING ="ASCII";  //Default character encoding
	private RoutingService theRoutingService;
	private byte[] destinationAddress; //destination byte address
	private byte[] id; //the message id
	private byte[] sourceAddress;  //the source addresss
	private int timeToLive;   //the time to live

	/**
	 * Constructs base message with given values
	 * @param id   message id
	 * @param ttl message TTL
	 * @param routingService message routing service
	 * @param sourceHooMEAddress message source address
	 * @param destinationHooMEAddress message destination address
	 * @throws BadAttributeValueException if bad attribute value
	 */

	public HooMEMessage(byte[] id, int ttl, RoutingService routingService, byte[] sourceHooMEAddress, byte[] destinationHooMEAddress) throws BadAttributeValueException {
		this.setId(id);
		this.setTtl(ttl);
		this.setRoutingService (routingService);
		this.setSourceHooMEAddress(sourceHooMEAddress);
		this.setDestinationHooMEAddress(destinationHooMEAddress);
	}


	/**
	 * constructs base message from input stream
	 * @param in inputstream
	 * @throws BadAttributeValueException if bad attribute value
	 */

	public HooMEMessage(MessageInput in) throws BadAttributeValueException {
		byte[] id = new byte[Consts.ID_SIZE];
		byte rawTTL;
		byte rawRoutingService;
		byte [] sourceAddress= new byte[Consts.DESTINATION_ADDRESS_SIZE];
		byte [] destinationAddress = new byte [Consts.DESTINATION_ADDRESS_SIZE];
		try {
			//read in the unique identifier for this message
			for(int i=0;i<Consts.ID_SIZE;i++) {
				id[i] = in.getScanner().readByte();
			}
			//read in the time to live
			rawTTL = in.getScanner().readByte();
			//read in the routing service
			rawRoutingService = in.getScanner().readByte();
			//read in source and destination address
			for (int i=0;i< Consts.DESTINATION_ADDRESS_SIZE;i++) {
				sourceAddress[i] = in.getScanner().readByte();
			}
			for (int i=0;i< Consts.DESTINATION_ADDRESS_SIZE;i++) {
				destinationAddress[i] = in.getScanner().readByte();
			}

			//set the values that have been read in
			this.setTtl(HooMEUtilities.getUnsignedInt(rawTTL));
			this.setId(id);
			this.setRoutingService(RoutingService.getRoutingService(HooMEUtilities.getUnsignedInt(rawRoutingService)));
			this.setDestinationHooMEAddress(destinationAddress);
			this.setSourceHooMEAddress(sourceAddress);
		} catch (InputMismatchException  e) {		
			throw new BadAttributeValueException (Consts.ILL_ARG);
		} catch (NoSuchElementException  e) {
			throw new BadAttributeValueException (Consts.ILL_ARG);
		} catch (IllegalStateException  e) {
			throw new BadAttributeValueException (Consts.ILL_ARG);
		} catch (IOException e) {
			throw new BadAttributeValueException (Consts.ILL_ARG);
		}
	}


	/**
	 * Deserializes message from input source
	 * @param in deserialization input source
	 * @return a specific HooME message resulting from deserialization
	 * throws java.io.IOException if deserialization fails
	 * throws BadAttributeValueException if bad attribute value
	 */

	public static HooMEMessage decode(MessageInput in)  throws java.io.IOException,
	BadAttributeValueException {
		HooMEMessage decodedMessage = null;
		byte messageType;
		try {
			messageType = in.getScanner().readByte();
			switch (messageType) {
			case Consts.RESPONSE_PAYLOAD:
				return (decodedMessage = new HooMEResponse(in));
			case Consts.SEARCH_PAYLOAD:
				return (decodedMessage = new HooMESearch(in));
			default:
				break;

			}
		} catch (InputMismatchException  e) {
			throw new BadAttributeValueException (Consts.ILL_ARG);
		} catch (NoSuchElementException  e) {
			throw new BadAttributeValueException (Consts.ILL_ARG);
		} catch (IllegalStateException  e) {
			throw new BadAttributeValueException (Consts.ILL_ARG);
		}
		return decodedMessage;
	}


	/**
	 * serialize output
	 * @param out serialization output destination
	 * @throws java.io.IOException if serialization fails
	 */

	public void encode(MessageOutput out)
			throws java.io.IOException {
		if (this instanceof HooMESearch) {
			out.getDataOutputStream().write(Consts.SEARCH_PAYLOAD);
		}  else {
			out.getDataOutputStream().write(Consts.RESPONSE_PAYLOAD);
		}
		out.getDataOutputStream().write(this.id);
		out.getDataOutputStream().writeByte((byte) this.timeToLive);
		out.getDataOutputStream().write((byte) this.theRoutingService.getServiceCode());
		out.getDataOutputStream().write(this.sourceAddress);
		out.getDataOutputStream().write(this.destinationAddress);
	}

	/**
	 * Get destination address
	 * @return destination address
	 */

	public byte[] getDestinationHooMEAddress() {
		return this.destinationAddress;
	}

	/**
	 * Get message id
	 * @return message id
	 */

	public byte[] getId() {
		return this.id;
	}

	/**
	 * 	Get message routing service
	 * @return routing service
	 */

	public RoutingService getRoutingService() {
		return this.theRoutingService;
	}

	/**
	 * Get source address
	 * @return source address
	 */

	public byte[] getSourceHooMEAddress() {
		return this.sourceAddress;
	}

	/**
	 * Get message TTL
	 * @return message TTL
	 */

	public int getTtl() {
		return this.timeToLive;
	}

	/**
	 * Set destination address
	 * @param destinationHooMEAddress destination address
	 * @throws BadAttributeValueException if bad address value
	 */

	public void setDestinationHooMEAddress(byte[] destinationHooMEAddress)
			throws BadAttributeValueException {
		if(destinationHooMEAddress == null || destinationHooMEAddress.length >Consts.DESTINATION_ADDRESS_SIZE
				|| destinationHooMEAddress.length <Consts.DESTINATION_ADDRESS_SIZE) {
			throw new BadAttributeValueException(Consts.ILL_ARG, "destinationAddress");
		}
		this.destinationAddress = destinationHooMEAddress;
	}

	/**
	 * Set message id
	 * @param id new ID
	 * @throws BadAttributeValueException if bad ID value
	 */

	public void setId(byte[] id)
			throws BadAttributeValueException {
		if( id==null || id.length>Consts.ID_SIZE || id.length< Consts.ID_SIZE ) {
			throw new BadAttributeValueException (Consts.ILL_ARG, "iD");
		}
		this.id = id;
	}

	/**
	 * Set message routing service
	 * @param routingService new routing service
	 * @throws BadAttributeValueException 
	 */

	public void setRoutingService(RoutingService routingService) throws BadAttributeValueException {
		if(routingService== null) {
			throw new BadAttributeValueException (Consts.ILL_ARG, "routing service");
		}
		this.theRoutingService = routingService;
	}

	/**
	 * Set source address
	 * @param sourceHooMEAddress source address
	 * @throws BadAttributeValueException if bad address value
	 */

	public void setSourceHooMEAddress(byte[] sourceHooMEAddress)
			throws BadAttributeValueException {
		if(sourceHooMEAddress== null|| sourceHooMEAddress.length >Consts.DESTINATION_ADDRESS_SIZE || 
				sourceHooMEAddress.length<Consts.DESTINATION_ADDRESS_SIZE) {
			throw new BadAttributeValueException (Consts.ILL_ARG, "source address");
		}
		this.sourceAddress = sourceHooMEAddress;
	}

	/**
	 * Set message TTL
	 * @param ttl new TTL
	 * @throws BadAttributeValueException  if bad TTL value
	 */

	public void setTtl(int ttl)
			throws BadAttributeValueException {
		if(ttl < 0 || ttl> Consts.MAX_BYTE_SIZE) {
			throw new BadAttributeValueException(Consts.ILL_ARG, "ttl");
		}
		this.timeToLive =ttl;
	}

	/**
	 * Overides toString in Java.Lang.Object
	 */

	@Override
	public String toString() {
		return "HooMEMessage [theRoutingService=" + theRoutingService
				+ ", destinationAddress=" + Arrays.toString(destinationAddress)
				+ ", id=" + Arrays.toString(id) + ", sourceAddress="
				+ Arrays.toString(sourceAddress) + ", timeToLive=" + timeToLive
				+ "]";
	}




}
