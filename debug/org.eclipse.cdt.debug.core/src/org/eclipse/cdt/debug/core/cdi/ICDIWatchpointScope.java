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
 * is going out of scope.
 * 
 * @since Aug 27, 2002
 */
public interface ICDIWatchpointScope extends ICDISessionObject {
	/**
	 * Returns the out of scope watchpoint.
	 * 
	 * @return the watchpoint
	 */
	ICDIWatchpoint getWatchpoint();
}
