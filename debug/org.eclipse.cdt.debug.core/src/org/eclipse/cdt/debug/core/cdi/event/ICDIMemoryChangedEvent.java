/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi.event;

/**
 * 
 * Notifies that the originator has changed.
 *
 */
public interface ICDIMemoryChangedEvent extends ICDIChangedEvent
{
	/**
	 * @return the modified addresses.
	 */
	Long[] getAddresses();
}
