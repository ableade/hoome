/*
 *RoutingService.java
 *Jan 31, 2013
 */
package hoome.message;

import mvn.Consts;

/**
 * This class serves as an enumrated type for routing service
 * @author Sola Adekunle
 *
 */

public enum RoutingService{

	BREADTHFIRSTBROADCAST,CONCAST;

	/**
	 * 
	 * @param code service code
	 * @return routing service associated with code
	 * @throws BadAttributeValueException if bad attribute value
	 */
	public static RoutingService getRoutingService(int code)
			throws BadAttributeValueException  {
		if(code!=0&&code!=1) {
			throw new BadAttributeValueException(Consts.ILL_ARG);
		} 
		if(code==0) {
			return BREADTHFIRSTBROADCAST;
		}
		return CONCAST;

	}

	/**
	 * gets the service code for an enumerated constant
	 * @return service code
	 */
	public int getServiceCode() {
		switch (this) {
		case BREADTHFIRSTBROADCAST:
			return 0;
			
		case CONCAST:
			return 1;
		default:
			return -1;
		}
	}



}
