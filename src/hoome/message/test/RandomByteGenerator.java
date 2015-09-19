/*
 *RandomByteGenerator.java
 *Feb 1, 2013
 */
package hoome.message.test;

import java.util.Random;

/**
 * For testing purposes this will generate arrays of random bytes;
 * @author Sola Adekunle
 *
 */
public class RandomByteGenerator {
	public static byte [] getRandomBytes (int size ) {
		byte [] randomBytes = new byte [size];
		new Random().nextBytes(randomBytes);
		return randomBytes;
	}

}
