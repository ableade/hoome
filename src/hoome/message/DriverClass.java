/*
 *DriverClass.java
 *Feb 3, 2013
 */
package hoome.message;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * @author Sola Adekunle
 *
 */
public class DriverClass {

	public static void main (String args[]) throws IOException {
		String myTest ="ERROR";
		ByteArrayInputStream b = new ByteArrayInputStream(myTest.getBytes());
		MessageInput anInput = new MessageInput(b);
		System.out.println(anInput.getInput().available());
		File file = new File("C:\\Users\\sola_adekunle\\Documents\\pee.txt");
		Long path;
		path= file.length();
		System.out.println(path);
		System.out.println(file.getAbsolutePath());
	}


}
