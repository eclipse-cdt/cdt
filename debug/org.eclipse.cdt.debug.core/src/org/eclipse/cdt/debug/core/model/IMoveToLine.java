/*******************************************************************************
 * Copyright (c) 2008, 2009 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Freescale Semiconductor - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to move the instruction pointer of a debug target to the given line.
 * @since 6.0
 */
public interface IMoveToLine {

	/**
	 * Returns whether this operation is currently available for this file and line number.
	 * 
	 * @return whether this operation is currently available
	 */
	public boolean canMoveToLine( String fileName, int lineNumber );

	/**
	 * Causes this element to move the instruction pointer to the specified line.
	 * 
	 * @exception DebugException on failure. Reasons include:
	 */
	public void moveToLine( String fileName, int lineNumber ) throws DebugException;
}
