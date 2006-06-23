/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
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
 * Provides the ability to set and get the format of a variable.
 */
public interface IFormatSupport {

	/**
	 * Returns whether this variable supports formatting operations.
	 * 
	 * @return whether this variable supports formatting operations
	 */
	boolean supportsFormatting();

	/**
	 * Returns the current format of this variable.
	 * 
	 * @return the current format of this variable
	 */
	CVariableFormat getFormat();

	/**
	 * Sets the current format of this variable to <code>format</code>.
	 * 
	 * @param format the new format type
	 * @throws DebugException if this method fails.
	 */
	void changeFormat( CVariableFormat format ) throws DebugException;
}
