/**
 * Test class for MavenCommon
 */
package mvn;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
@RunWith(Parameterized.class)

/*
 * Tests the maven common class encode and decode capabilities
 */
public class MavenCommonTest {
	
	private int error;
	private int sessionId;
	ByteArrayInputStream theInputStream;
	MavenCommon aCommon;
	public MavenCommonTest ( int a, int b, int c, byte[] toDecode) throws IOException {
		this.error =b;
		this.sessionId =c;
		this.theInputStream = new ByteArrayInputStream(toDecode);
		aCommon = new MavenCommon();
		aCommon.decode(theInputStream);
	}

	@Test
	public void testDecode() {
		assertEquals(aCommon.getSessionId(), this.sessionId);
	}

	@Test
	public void testDecode2 () {
		assertEquals(aCommon.getError(), this.error);
	}
	
	
	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { {64, 1, 50, (new byte [] {64,1,50,0}) },
				 {64, 2, 70, (new byte [] {64,2,70,0}) },
		 {64, 3, 50, (new byte [] {67,3,50,0}) },
		 
		 {67, 1, 50, (new byte [] {67,1,50,0}) }
		
		};
		return Arrays.asList(data);
	}
}
