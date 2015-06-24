/*
 * MyLogger.java
 * February 27 2013
 */
package hoome.node;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;


/**
 * This class serves as a special format class for logging
 * @author sola_adekunle
 *
 */
public class MyLogger extends Formatter{

	/**
	 * overrides format function for special 
	 */
    @Override
    public String format(LogRecord arg0) {
        return arg0.getMessage();
        
    }

}
