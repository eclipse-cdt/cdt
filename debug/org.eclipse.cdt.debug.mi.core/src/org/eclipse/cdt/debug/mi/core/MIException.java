/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core;

/**
 * 
 * A checked exception representing a failure.
 *
 */
public class MIException extends Exception {
	String log = ""; //$NON-NLS-1$

	public MIException(String s) {
		super(s);
	}

	public MIException(String s, String l) {
		super(s);
		log = l;
	}

	public String getLogMessage() {
		return log;
	}
}
