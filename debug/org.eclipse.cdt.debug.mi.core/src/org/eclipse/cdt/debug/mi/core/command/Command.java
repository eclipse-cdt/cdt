/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 * A base class for all mi requests.
 * 
 * @author Mikhail Khodjaiants
 * @since Jul 11, 2002
 */
public abstract class Command
{
	/**
	 * Returns the identifier of this request.
	 * 
	 * @return the identifier of this request
	 */
	public abstract String getToken();

	public abstract String toString();
}
