/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;

/**
 * 
 * A base interface for all CDI events.
 * 
 * @since Jul 18, 2002
 */
public interface ICDIEvent {
	/**
	 * The CDI object on which the event initially occurred.
	 * 
	 * @return the CDI object on which the event initially occurred
	 */
	ICDIObject getSource();
}
