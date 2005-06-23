/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
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
 * Represents a signal.
 * 
 * @since Jul 10, 2002
 */
public interface ICDISignal extends ICDIObject {

	/**
	 * Returns the name of this signal.
	 * 
	 * @return the name of this signal
	 */
	String getName();
	
	/**
	 * Returns the meaning of this signal.
	 * 
	 * @return the meaning of this signal
	 */
	String getDescription();

	/**
	 * if false means program will see the signal.
	 * Otherwise program does not know.
	 * 
	 * @return boolean
	 */
	boolean isIgnore();

	/**
	 * Means reenter debugger if this signal happens
	 * 
	 * Method  isStopSet.
	 * @return boolean
	 */
	boolean isStopSet();

	/**
	 * Continue program giving it this signal.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void signal() throws CDIException ;

	/**
	 * Change the way debugger handles this signal.
	 * 
	 * @param ignore - if true the debugger should not allow your program to see this signal
	 * @param stop - if true the debugger should stop your program when this signal happens
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void handle(boolean ignore, boolean stop) throws CDIException;
}
