package hoome.message.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import hoome.message.BadAttributeValueException;
import hoome.message.HooMEMessage;
import hoome.message.HooMESearch;
import hoome.message.MessageInput;
import hoome.message.MessageOutput;
import hoome.message.RoutingService;

import org.junit.Test;

public class HooMeSearchEncodeAndDecode {

	@Test
	public void testEncode() throws BadAttributeValueException, IOException {
		byte[] expected =   new byte[] {
		        1,
		        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
		        3,
		        0,
		        6, 7, 8, 9, 10, 
		        11, 12, 13, 14, 15,
		        0, 6,
		        69, 120, 97, 109, 49, 10
		    };
		HooMESearch aSearch = new HooMESearch(
		        new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
		        3,
		        RoutingService.BREADTHFIRSTBROADCAST,
		        new byte[] {6, 7, 8, 9, 10}, 
		        new byte[] {11, 12, 13, 14, 15},
		        "Exam1");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			aSearch.encode(new MessageOutput(out));
			assertArrayEquals(expected, out.toByteArray());
	}
	
	@Test
	public void testDecode() throws IOException, BadAttributeValueException {
		HooMEMessage search = HooMEMessage.decode(new MessageInput(new ByteArrayInputStream( new byte[] {
		        1,
		        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
		        3,
		        0,
		        6, 7, 8, 9, 10, 
		        11, 12, 13, 14, 15,
		        0, 6,
		        69, 120, 97, 109, 49, 10
		    })));
		
		HooMESearch aSearch = new HooMESearch(
		        new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15},
		        3,
		        RoutingService.BREADTHFIRSTBROADCAST,
		        new byte[] {6, 7, 8, 9, 10}, 
		        new byte[] {11, 12, 13, 14, 15},
		        "Exam1");
		
	}

}
