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
public interface ICDIObject {
	/**
	 * Returns the target this object is contained in.
	 * 
	 * @return the target this object is contained in
	 */
	ICDITarget getTarget();
}
