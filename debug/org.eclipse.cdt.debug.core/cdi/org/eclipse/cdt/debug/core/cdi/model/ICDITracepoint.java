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
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 * Defines a point in the program execution when the specified data to be collected.  
 * 
 * @since May 15, 2003
 */
public interface ICDITracepoint extends ICDIObject {

	/**
	 * Represents an action to be taken when the tracepoint is hit.
	 * 
	 * @since May 15, 2003
	 */
	public interface IAction {
	}

	/**
	 * Returns the location of this tracepoint.
	 * 
	 * @return the location of this tracepoint
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDILocation getLocation() throws CDIException;

	/**
	 * Returns whether this tracepoint is enabled.
	 * 
	 * @return whether this tracepoint is enabled
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	boolean isEnabled() throws CDIException;

	/**
	 * Sets the enabled state of this tracepoint. This has no effect 
	 * if the current enabled state is the same as specified by 
	 * the enabled parameter.
	 * 
	 * @param enabled - whether this tracepoint should be enabled 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void setEnabled( boolean enabled ) throws CDIException;

	/**
	 * Returns the passcount of this tracepoint.
	 * 
	 * @return the passcount of this tracepoint
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	int getPassCount() throws CDIException;
	
	/**
	 * Sets the passcount of this tracepoint.
	 * 
	 * @param the passcount to set
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void setPassCount( int passCount ) throws CDIException;

	/**
	 * Adds the given actions to the action list of thie tracepoint.
	 * 
	 * @param actions to add
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void addActions( ICDITracepoint.IAction[] actions ) throws CDIException;

	/**
	 * Removes the given actions from the action list of thie tracepoint.
	 * 
	 * @param actions to remove
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void removeActions( ICDITracepoint.IAction[] actions ) throws CDIException;

	/**
	 * Clears the action list of thie tracepoint.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void clearActions() throws CDIException;

	/**
	 * Returns the actions assigned to this tracepoint.
	 * 
	 * @return the actions of this tracepoint
	 */
	ICDITracepoint.IAction[] getActions();
}
