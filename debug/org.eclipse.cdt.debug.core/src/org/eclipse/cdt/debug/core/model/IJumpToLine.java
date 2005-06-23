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

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to resume a debug target at the given line.
 */
public interface IJumpToLine {

	/**
	 * Returns whether this operation is currently available for this file and line number.
	 * 
	 * @return whether this operation is currently available
	 */
	public boolean canJumpToLine( IFile file, int lineNumber );

	/**
	 * Causes this element to resume the execution at the specified line.
	 * 
	 * @exception DebugException
	 *                on failure. Reasons include:
	 */
	public void jumpToLine( IFile file, int lineNumber ) throws DebugException;

	/**
	 * Returns whether this operation is currently available for this file and line number.
	 * 
	 * @return whether this operation is currently available
	 */
	public boolean canJumpToLine( String fileName, int lineNumber );

	/**
	 * Causes this element to resume the execution at the specified line.
	 * 
	 * @exception DebugException on failure. Reasons include:
	 */
	public void jumpToLine( String fileName, int lineNumber ) throws DebugException;
}
