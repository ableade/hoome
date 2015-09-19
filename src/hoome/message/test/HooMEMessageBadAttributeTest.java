/*
 *HooMEMessageBadAttributeTest.java
 *Feb 2, 2013
 */
package hoome.message.test;

import hoome.message.BadAttributeValueException;
import hoome.message.HooMEMessage;
import hoome.message.RoutingService;

import java.util.Arrays;
import java.util.Collection;

import mvn.Consts;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * tests the hoome message class when bad attributes are used
 * @author Sola Adekunle
 *
 */

@RunWith(Parameterized.class)
public class HooMEMessageBadAttributeTest {

	private int ttl;
	private byte [] destination;
	private byte[] source;
	private byte[] id;
	private RoutingService routingService;


	public HooMEMessageBadAttributeTest (byte[] id, int ttl, RoutingService routingService, byte[] 
			sourceHooMEAddress, byte[] destinationHooMEAddress) throws BadAttributeValueException {
		this.destination = destinationHooMEAddress;
		this.source = sourceHooMEAddress;
		this.ttl = ttl;
		this.routingService = routingService;
		this.id = id;
	}

	@Parameters
	public static Collection<Object[]> data() throws BadAttributeValueException {
		Object [][] data = new Object [][] { {RandomByteGenerator.getRandomBytes(Consts.ID_SIZE), 259, RoutingService.getRoutingService(0), 
			RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE)}
		, {RandomByteGenerator.getRandomBytes(Consts.ID_SIZE), 20, null, 
			RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE)}

		};
		return Arrays.asList(data);
	}


	@Test (expected= BadAttributeValueException.class)
	public void test1 () throws BadAttributeValueException {
		HooMEMessage aHooMeMessage = new HooMEMessage (this.id, ttl, routingService, this.source, this.destination);
		aHooMeMessage.setRoutingService(null);
	}
}