/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.CoreException;

/**
 * 
 * A watchpoint specific to the C/C++ debug model.
 * 
 * @since Sep 4, 2002
 */
public interface ICWatchpoint extends ICBreakpoint
{
	/**
	 * Watchpoint attribute storing the expression associated with this 
	 * watchpoint (value <code>"org.eclipse.cdt.debug.core.expression"</code>).
	 * This attribute is a <code>String</code>.
	 */
	public static final String EXPRESSION = "org.eclipse.cdt.debug.core.expression"; //$NON-NLS-1$	

	/**
	 * Write access watchpoint attribute (value <code>"org.eclipse.cdt.debug.core.write"</code>).
	 * This attribute is a <code>boolean</code>.
	 */
	public static final String WRITE = "org.eclipse.cdt.debug.core.write"; //$NON-NLS-1$	

	/**
	 * Read access watchpoint attribute (value <code>"org.eclipse.cdt.debug.core.read"</code>).
	 * This attribute is a <code>boolean</code>.
	 */
	public static final String READ = "org.eclipse.cdt.debug.core.read"; //$NON-NLS-1$
	
	/**
	 * Returns whether this watchppoint is a write watchpoint.
	 * 
	 * @return whether this watchppoint is a write watchpoint
	 */
	boolean isWriteType() throws CoreException;
	
	/**
	 * Returns whether this watchppoint is a read watchpoint.
	 * 
	 * @return whether this watchppoint is a read watchpoint
	 */
	boolean isReadType() throws CoreException;
	
	/**
	 * Returns the watchpoint's expression.
	 * 
	 * @return the expression of this watchpoint
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getExpression() throws CoreException;
}
