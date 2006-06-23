/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

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
