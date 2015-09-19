/*
 *HooMEUtilities.java
 *Jan 31, 2013
 */

package hoome.message;

import mvn.Consts;

/**
 * stores functions for serialization and deserialization
 * @author Sola Adekunle
 *
 */
public class HooMEUtilities {
	
	/**
	 * convert a byte to unsigned int
	 * @param rawData byte to be converted
	 * @return unsigned int
	 */
	
	public static int getUnsignedInt  ( byte rawData) {
		int val =0;
		val = Consts.UNSIGNED_INT_BYTE_MASK & rawData;
		return val;
	}
	
	/**
	 * convert a short value to an unsigned int
	 * @param rawData signed value to be converted
	 * @return
	 */
	
	public static int getUnsignedInt (short rawData) {
		int val =0;
		val = Consts.UNSIGNED_INT_SHORT_BYTE_MASK & rawData;
		return val;
		
	}
	
	/**
	 * converts a short to an unsigned int
	 * @param rawData signed value to be converted
	 * @return
	 */
	
	public static long getUnsignedint (short rawData) {
		long val =0;
		
		return val;
	}
	
	/**
	 * converts an integer value to an unsigned int
	 * @param rawData signed value to be converted
	 * @return
	 */
	
	public static long getUnsignedint (int rawData) {
		long val =0;
		val = (rawData & Consts.UNSIGNED_LONG_INT_MASK)  ;
		return val;
	}
	
	/**
	 * converts a byte to an unsigned int
	 * @param rawData byte to be converted
	 * @return unsignedByte
	 */
	
	public static long getUnsignedLongInt (byte rawData) {
		long val =0;
		val = Consts.UNSIGNED_LONG_BYTE_MASK & rawData;
		return val;
		
	}
	/**
	 * checks to see if a byte array is empty
	 * @param arr the array to be checked
	 * @return true or false
	 */
	
	public static boolean isEmpty(byte[] arr) {
		
		for (int i=0;i<arr.length;i++) {
			if (arr[i]!=0) {
				return false;
			}
		}
		System.out.println(" empty");
		return true;
	}
}
