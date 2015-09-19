/*
 *HooMESearchTest.java
 *Jan 31, 2013
 */
package hoome.message.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import hoome.message.BadAttributeValueException;
import hoome.message.HooMESearch;
import hoome.message.RoutingService;

import mvn.Consts;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Sola Adekunle
 *
 */
@RunWith(Parameterized.class)
public class HooMESearchTest {
	
	private HooMESearch myHooMESearch; 
	private int ttl;
	private byte [] destination;
	private byte[] source;
	private byte[] id;
	private RoutingService routingService;
	private String search;


	public HooMESearchTest (byte[] id, int ttl, RoutingService routingService, byte[] 
			sourceHooMEAddress, byte[] destinationHooMEAddress, String search) throws BadAttributeValueException {
		this.destination = destinationHooMEAddress;
		this.source = sourceHooMEAddress;
		this.ttl = ttl;
		this.routingService = routingService;
		this.id = id;
		this.search = search;
		this.myHooMESearch = new HooMESearch(this.id, ttl, routingService, this.source, this.destination, this.search);
	}
	

	@Test
	public void testSearchString() {
		assertEquals(this.search, this.myHooMESearch.getSearchString());
	}
	
	
	@Test(expected= BadAttributeValueException.class) 
	public void testSetSearchStringException () throws BadAttributeValueException{
		this.myHooMESearch.setSearchString("man\n");
	}
	@Parameters
	public static Collection<Object[]> data() throws BadAttributeValueException {
		Object[][] data = new Object[][] { {RandomByteGenerator.getRandomBytes(Consts.ID_SIZE), 20, RoutingService.getRoutingService(0), 
			RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), "hello"},
			{RandomByteGenerator.getRandomBytes(Consts.ID_SIZE), 28, RoutingService.getRoutingService(0), 
				RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), "hi"},
				{RandomByteGenerator.getRandomBytes(Consts.ID_SIZE), 23, RoutingService.getRoutingService(0), 
					RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), "love"},
					{RandomByteGenerator.getRandomBytes(Consts.ID_SIZE), 10, RoutingService.getRoutingService(0), 
						   RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), RandomByteGenerator.getRandomBytes(Consts.DESTINATION_ADDRESS_SIZE), "me"}
		};
		return Arrays.asList(data);
	}

}
