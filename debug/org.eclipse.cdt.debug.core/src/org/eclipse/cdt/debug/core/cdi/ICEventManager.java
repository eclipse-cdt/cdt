/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.event.ICEventListener;

/**
 * 
 * Clients interested in the CDI model change notification may
 * register with this object.
 * 
 * @since Jul 10, 2002
 */
public interface ICEventManager extends ICSessionObject
{
	/**
	 * Adds the given listener to the collection of registered 
	 * event listeners. Has no effect if an identical listener is 
	 * already registered. 
	 * 
	 * @param listener - the listener to add
	 */
	void addEventListener( ICEventListener listener );

	/**
	 * Removes the given listener from the collection of registered 
	 * event listeners. Has no effect if an identical listener is not 
	 * already registered. 
	 * 
	 * @param listener - the listener to remove
	 */
	void removeEventListener( ICEventListener listener );
}
