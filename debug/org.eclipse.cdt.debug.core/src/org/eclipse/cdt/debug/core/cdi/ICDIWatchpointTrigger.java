/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;

/**
 * 
 * Represents an information provided by the session when a watchpoint 
 * is triggered.
 * 
 * @since Aug 27, 2002
 */
public interface ICDIWatchpointTrigger extends ICDISessionObject
{
	/**
	 * Returns the triggered watchpoint.
	 * 
	 * @return the triggered watchpoint
	 */
	ICDIWatchpoint getWatchpoint();
	
	/**
	 * Returns the old value of the watching expression.
	 * 
	 * @return the old value of the watching expression
	 */
	String getOldValue();
	
	/**
	 * Returns the new value of the watching expression.
	 * 
	 * @return the new value of the watching expression
	 */
	String getNewValue();
}
