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
	public MIException(String s) {
		super(s);
	}
}
