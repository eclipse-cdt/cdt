/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi;

/**
 * 
 * Represents an object associated with a debug session.
 * 
 * @since Jul 9, 2002
 */
public interface ICDISessionObject
{
	/**
	 * Returns the debug session this object is associated with.
	 * 
	 * @return the debug session this object is associated with
	 */
	ICDISession getSession();
}
