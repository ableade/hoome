/*
 *HooMEMessageTest.java
 *Feb 1, 2013
 */
package hoome.message.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import hoome.message.*;

import mvn.Consts;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the hoomeMessage class
 * @author Sola Adekunle
 *
 */
@RunWith(Parameterized.class)
public class HooMEMessageTest {
	private HooMEMessage myHooMEMesaage; 
	private int ttl;
	private byte [] destination;
	private byte[] source;
	private byte[] id;
	private RoutingService routingService;

	public HooMEMessageTest (byte[] id, int ttl, RoutingService routingService, byte[] 
			sourceHooMEAddress, byte[] destinationHooMEAddress) throws BadAttributeValueException {
		this.myHooMEMesaage = new HooMEMessage (id, ttl,routingService, sourceHooMEAddress, destinationHooMEAddress);
		this.destination = destinationHooMEAddress;
		this.source = sourceHooMEAddress;
		this.ttl = ttl;
		this.routingService = routingService;
		this.id = id;

	}
	@Parameters
	public static Collection<Object[]> data() throws BadAttributeValueException {
		Object[][] data = new Object[][] { {RandomByteGenerator.getRandomBytes(Consts.ID_SIZE), 20, RoutingService.getRoutingService(0), 
			RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE)},
			{RandomByteGenerator.getRandomBytes(Consts.ID_SIZE), 28, RoutingService.getRoutingService(0), 
				RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE)},
				{RandomByteGenerator.getRandomBytes(Consts.ID_SIZE), 23, RoutingService.getRoutingService(0), 
					RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE)},
					{RandomByteGenerator.getRandomBytes(Consts.ID_SIZE), 10, RoutingService.getRoutingService(0), 
						   RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE)}
		};
		return Arrays.asList(data);
	}

	@Test
	public void testTTL () {
		assertEquals(this.ttl, this.myHooMEMesaage.getTtl());		
	}

	@Test 
	public void testRoutingService () {
		assertEquals(this.routingService, this.myHooMEMesaage.getRoutingService());	
	}
	
	@Test 
	public void testId () {
		assertEquals(this.id, this.myHooMEMesaage.getId());
	}
	
	@Test 
	public void testSource () {
		assertEquals (this.source, this.myHooMEMesaage.getSourceHooMEAddress());
	}
	
	@Test
	
	public void testDestination () {
		assertEquals(this.destination, this.myHooMEMesaage.getDestinationHooMEAddress());
	}

}
