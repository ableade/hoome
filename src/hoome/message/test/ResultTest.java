/*
 *ResultTest.java
 *Feb 3, 2013
 */
package hoome.message.test;

import static org.junit.Assert.*;

import hoome.message.BadAttributeValueException;
import hoome.message.Result;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Junit Test class for result
 * @author Sola Adekunle
 *
 */
@RunWith(Parameterized.class)
public class ResultTest {
	private long fileId;
	private long fileSize;
	private String fileName;
	private Result aResult;
	
	public ResultTest(long fileId, long fileSize, String fileName) throws BadAttributeValueException {
		this.fileId = fileId;	
		this.fileSize= fileSize;
		this.fileName = fileName;
		aResult = new Result (this.fileId, this.fileSize, this.fileName);
	}
	@Test
	public void test1(){
		assertEquals(this.fileId, aResult.getFileId());
	}
	
	@Test
	public void test2() {
		assertEquals(this.fileName, aResult.getFileName());
	}
	
	@Test
	public void test3() {
		assertEquals(this.fileSize, aResult.getFileSize());
	}
	
	@Parameters
	public static Collection<Object[]> data() {
		
		Object [][] data = new Object [][] {
				{125, 240, "Hellogorgeous" },
				{24, 456, "Hellodolly"},
				{225, 140, "HelloMonday" },
				{1025, 69, "GreatExpectations" },
				{215, 440, "HelloWorld" },
		};
		
		return Arrays.asList(data);
		
	}

}
