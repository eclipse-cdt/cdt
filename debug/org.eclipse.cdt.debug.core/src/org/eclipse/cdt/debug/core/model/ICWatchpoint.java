/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.ILineBreakpoint;

/**
 * A watchpoint specific to the C/C++ debug model.
 */
public interface ICWatchpoint extends ICBreakpoint, ILineBreakpoint {

	/**
	 * Breakpoint marker type for this breakpoint type.
	 * @since 7.2
	 */
	public static final String C_WATCHPOINT_MARKER = "org.eclipse.cdt.debug.core.cWatchpointMarker"; //$NON-NLS-1$

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
