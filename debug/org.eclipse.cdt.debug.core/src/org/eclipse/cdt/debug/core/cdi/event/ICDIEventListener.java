/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.event;

/**
 * 
 * An event listener registers with the event manager to receive event 
 * notification from the CDI model objects.
 * 
 * @since Jul 10, 2002
 */
public interface ICDIEventListener
{
	/**
	 * Notifies this listener of the given event.
	 * 
	 * @param event - the event
	 */
	void handleDebugEvent( ICDIEvent event );
}
