/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * A breakpoint that suspends the execution when a function is entered.
 */
public interface ICFunctionBreakpoint extends ICLineBreakpoint {

	/**
	 * Breakpoint attribute storing the function this breakpoint suspends
	 * execution at (value <code>"org.eclipse.cdt.debug.core.function"</code>).
	 * This attribute is a <code>String</code>.
	 */
	public static final String FUNCTION = "org.eclipse.cdt.debug.core.function"; //$NON-NLS-1$	

	/**
	 * Returns the function this breakpoint suspends execution in.
	 * 
	 * @return the function this breakpoint suspends execution in
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public String getFunction() throws CoreException;

	/**
	 * Sets the function this breakpoint suspends execution in.
	 * 
	 * @param function the function this breakpoint suspends execution in
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public void setFunction( String function ) throws CoreException;

	/**
	 * Returns the source file of the function.
	 *  
	 * @return the source file of the function
	 * @throws CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 */
	public String getFileName() throws CoreException;
}