/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.DebugException;

/**
 * Provides the ability to run a debug target to the given line.
 */
public interface IRunToLine {

	/**
	 * Returns whether this operation is currently available for this file and line number.
	 *
	 * @return whether this operation is currently available
	 */
	public boolean canRunToLine(IFile file, int lineNumber);

	/**
	 * Causes this element to run to specified location.
	 *
	 * @exception DebugException on failure. Reasons include:
	 */
	public void runToLine(IFile file, int lineNumber, boolean skipBreakpoints) throws DebugException;

	/**
	 * Returns whether this operation is currently available for this file and line number.
	 *
	 * @return whether this operation is currently available
	 */
	public boolean canRunToLine(String fileName, int lineNumber);

	/**
	 * Causes this element to run to specified location.
	 *
	 * @exception DebugException on failure. Reasons include:
	 */
	public void runToLine(String fileName, int lineNumber, boolean skipBreakpoints) throws DebugException;
}
