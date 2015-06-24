/*
 *Consts.java
 *Jan 31, 2013
 */
package mvn;

/**
 * This class stores all literal constants used within the program
 * @author Sola Adekunle
 *
 */
public class Consts {

	public static final int DESTINATION_ADDRESS_SIZE=5;  //max size of destination address

	public static final  int ID_SIZE=15;

	public static final String ILL_ARG ="Invalid argument!";

	public static final int TWO_BITS = 2;

	public static final int FOUR_BITS=4;

	public static final byte SEARCH_PAYLOAD = 0X01;

	public static final byte RESPONSE_PAYLOAD = 0X02;

	public static final String FILE_NAME_REGEX ="[\\w-_.]*";

	public static final long MAX_UNSIGNED_INT = 4294967295l;

	public static long UNSIGNED_LONG_BYTE_MASK = 0xFFl;

	public static long UNSIGNED_LONG_INT_MASK = 0xFFFFFFFFl;

	public static int UNSIGNED_INT_BYTE_MASK = 0xFF;

	public static long UNSIGNED_SHORT_BYTE_MASK = 0xFFFFl;

	public static int UNSIGNED_INT_SHORT_BYTE_MASK = 0xFFFF;

	public static final String EOLN ="\n";

	public static final int MAX_BYTE_SIZE=255;

	public static final int MAX_UNSIGNED_SHORT = 65535; 

	public static final Byte endByte = "\n".getBytes()[0];

	public static final String HANDSHAKE_RESPONSE = "SUP HooME";

	public static final String HANDSHAKE_GREETING = "HELLO HooME/1.0";

	public static final int SEND_QUEUE_CAPACITY =25;

	public static final String DOWNLOADCONFIRMATION ="OK\n\n";

	public static final String PRE_ERROR= "ERROR";

	public static final String NO_FILE ="The file does not exist";

	public static final String DOWNLOAD_OK = "OK";

	public static final String INVALID_GREETING_CODE ="100";

	public static final String PRE_HOOME_ERROR = "T3H";

	public static final String INVALID_HANDSHAKE = "Unrecognized handshake";
	
	public static final int REQUEST_NODE_TYPE = 0;
	
	public static final int REQUEST_MAVEN_TYPE = 1;
	
	public static final int ANSWER_REQUEST_TYPE = 2;
	
	public static final int NODE_ADDITION_TYPE = 3;
	
	public static final int MAVEN_ADDITION_TYPE = 4;
	
	public static final int NODE_DELETION_TYPE = 5;
	
	public static final int MAVEN_DELETION_TYPE = 6;

	public static final int MAX_MAVEN_BUFFER_SIZE = 4096;

}
