/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * Represents a signal.
 *
 * @since: Mar 5, 2004
 */
public interface ICSignal extends ICDebugElement {
	
	/**
	 * Returns the name of this signal
	 * 
	 * @return this signal's name
	 * @throws DebugException if this method fails.
	 */
	public String getName() throws DebugException;
	
	/**
	 * Returns the description of this signal.
	 * 
	 * @return this signal's description
	 * @throws DebugException if this method fails.
	 */
	public String getDescription() throws DebugException;
	
	/**
	 * Returns whether "pass" is in effect for this signal.
	 * 
	 * @return whether "pass" is in effect for this signal
	 * @throws DebugException if this method fails.
	 */
	public boolean isPassEnabled() throws DebugException;
	
	/**
	 * Returns whether "stop" is in effect for this signal.
	 * 
	 * @return whether "stop" is in effect for this signal
	 * @throws DebugException if this method fails.
	 */
	public boolean isStopEnabled() throws DebugException;
	
	/**
	 * Enables/disables the "pass" flag of this signal.
	 * 
	 * @param enable the flag value to set
	 * @throws DebugException if this method fails.
	 */
	public void setPassEnabled( boolean enable ) throws DebugException;
	
	/**
	 * Enables/disables the "stop" flag of this signal.
	 * 
	 * @param enable the flag value to set
	 * @throws DebugException if this method fails.
	 */
	public void setStopEnabled( boolean enable ) throws DebugException;

	/**
	 * Resumes execution, but immediately gives the target this signal.
	 * 
	 * @throws DebugException if this method fails.
	 */
	public void signal() throws DebugException;
	
	/**
	 * Returns whether modification is allowed for this signal's parameters.
	 * 
	 * @return whether modification is allowed for this signal's parameters
	 */
	public boolean canModify();
}
