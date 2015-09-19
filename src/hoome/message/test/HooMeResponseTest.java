/*
 *HooMeResponseTest.java
 *Jan 31, 2013
 */
package hoome.message.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import hoome.message.*;

/**
 * @author Sola Adekunle
 *
 */
@RunWith(Parameterized.class)
public class HooMeResponseTest {

	private String expected;
	long fileId;
	long fileSize;
	String fileName;
	byte[] address;
	int matches;
	int port;
	private HooMEResponse aResponse;
	
	public HooMeResponseTest (String exp, int matches, int port, String address, long fileId, long fileSize, String fileName) throws IOException, BadAttributeValueException {
		this.expected = exp;
		this.matches = matches;
		this.address =  address.getBytes();
		this.fileId = fileId;
		this.fileSize = fileSize;
		this.fileName = fileName;
		
		MessageInput in = new MessageInput(new ByteArrayInputStream(exp.getBytes()));
		this.aResponse = new HooMEResponse(in);
	}
	
	
	@Test
	public void test() {
		assertEquals(this.matches, this.aResponse.getResultList().size());
	}
	
@Parameters	
public static Collection<Object[]> generateData () {
	return Arrays.asList(new Object [][] {
		{"1 24 112 45 7 8 21 45 hello", 1,24,"129 45 7 8",21,45,"hello"},
		{"1 45 111 56 23 118 21 45 power", 1, 45, "239 56 23 118", 21, 45, "power"},
		{"1 24 100 45 7 8 21 45 NoAir", 1, 24, "100 45 7 8", 21, 45, "NoAir"},
		{"1 24 120 45 7 8 21 45 hello", 1, 24, "120 45 7 8", 21, 45, "hello"}
	});
}

}
