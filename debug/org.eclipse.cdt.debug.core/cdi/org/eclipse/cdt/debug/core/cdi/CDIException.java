/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return super.toString() + '['+ getDetailMessage() + ']';
	}

}
