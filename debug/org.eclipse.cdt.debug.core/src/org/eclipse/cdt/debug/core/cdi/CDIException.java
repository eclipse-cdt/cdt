/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi;

/**
 * 
 * Represents a failure in the CDI model operations.
 * 
 * @since Jul 9, 2002
 */
public class CDIException extends Exception {

	String details = ""; //$NON-NLS-1$

	public CDIException() {
		super();
	}

	public CDIException(String s) {
		super(s);
	}
	
	public CDIException(String s, String d) {
		super(s);
		details = d;
	}

	/**
	 * Returns a more details message(if any).
	 */
	public String getDetailMessage() {
		return details;
	}

}
