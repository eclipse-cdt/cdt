/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.core.cdi.model;

/**
 * 
 * Represents an object in the CDI model.
 * 
 * @since Jul 8, 2002
 */
public interface ICDIObject
{
	/**
	 * Returns the identifier of this object.
	 * 
	 * @return the identifier of this object
	 */
	String getId();
	
	/**
	 * Returns the target this object is contained in.
	 * 
	 * @return the target this object is contained in
	 */
	ICDITarget getCDITarget();
	
	/**
	 * Returns the parent of this object or <code>null</code> if this 
	 * object is a top level object.
	 * 
	 * @return the parent of this object
	 */
	ICDIObject getParent();
}
