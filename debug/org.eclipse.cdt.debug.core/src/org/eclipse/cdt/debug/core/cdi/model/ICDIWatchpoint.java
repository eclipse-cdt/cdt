/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * 
 * Represents a watchpoint.
 * 
 * @since Jul 9, 2002
 */
public interface ICDIWatchpoint extends ICDIBreakpoint {
	final static public int WRITE = 0x1;
	final static public int READ = 0x2;

	/**
	 * Returns whether this watchppoint is a write watchpoint.
	 * 
	 * @return whether this watchppoint is a write watchpoint
	 */
	boolean isWriteType();
	
	/**
	 * Returns whether this watchppoint is a read watchpoint.
	 * 
	 * @return whether this watchppoint is a read watchpoint
	 */
	boolean isReadType();
	
	/**
	 * Returns the watchpoint's expression.
	 * 
	 * @return the expression of this watchpoint
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getWatchExpression() throws CDIException;
}
