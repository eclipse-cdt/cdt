/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;

/**
 * 
 * Represents a signal.
 * 
 * @since Jul 10, 2002
 */
public interface ICDISignalReceived extends ICDISessionObject {

	/**
	 * Method getSignal.
	 * @return ICDISignal
	 */
	ICDISignal getSignal();
}
