/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICObject;

/**
 * 
 * Represents an information provided by the session when the program 
 * exited.
 * 
 * @since Jul 10, 2002
 */
public interface ICExitInfo extends ICSessionObject
{
	/**
	 * Returns an exit code.
	 * 
	 * @return an exit code
	 */
	int getCode();
}
