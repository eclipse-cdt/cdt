/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
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
 * Jiju George T- bug 118100
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;

public interface ICBreakpointType {
	/**
	 * Breakpoint attribute storing the type of the breakpoint
	 * This attribute is a <code>int</code>. Strictly speaking,
	 * types are even values, but the least-significant bit
	 * is used to qualify it as temporary. See {@link #TEMPORARY}
	 *
	 * @since 5.0
	 */
	public static final String TYPE = "org.eclipse.cdt.debug.core.breakpointType"; //$NON-NLS-1$

	/**
	 * A bit-wise qualifier (the only one); not a type in and of itself. All
	 * types are even values (including zero). The least significant bit can be
	 * turned on to further qualify the breakpoint as one that the user
	 * doesn't see. It is typically installed programatically by the debugger,
	 * hit once and then uninstalled.
	 */
	final static public int TEMPORARY = 0x1;

	/**
	 * A REGULAR breakpoint is a line, function or address breakpoint whose
	 * installation mechanism (hardware vs software) is up to the discretion of
	 * the debugger backend A common backend approach is to try one way first
	 * and then the other if the first attempt fails.
	 *
	 * <p>
	 * In the future, we will expose a preference that lets the user dictate his
	 * preference to the backend. That is, the user will be able to say "I want
	 * the backend to install regular breakpoints using hardware breakpoint
	 * capabilities on the target."
	 *
	 * <p>
	 * It's important to realize that how a backend ends up installing the
	 * breakpoint does not alter the type of the breakpoint in the Eclipse/CDT
	 * layer. A REGULAR breakpoint will always be a <i>REGULAR</i> breakpoint
	 * regardless of the technique the backend used to install it (software or
	 * hardware).
	 */
	final static public int REGULAR = 0x0 << 1;

	/**
	 * A HARDWARE breakpoint is a line, function or address breakpoint that the
	 * user wants installed using a hardware breakpoint facility on the target.
	 * If it cannot be installed that way, it should not be installed at all.
	 *
	 * For example, the hardware may have a breakpoint register where an address
	 * can be set. The target will halt itself when the PC matches that register
	 * value. Hardware breakpoints have several advantages. They're
	 * non-intrusive to the code and so are simpler for the debugger to
	 * install/uninstall/manage. Also, because they are non-intrusive, they
	 * allow for breakpoints to be set in ROM code. The disadvantage is that
	 * support for them is often limited. Some architectures only support one or
	 * two hardware breakpoints.
	 */
	final static public int HARDWARE = 0x1 << 1;

	/**
	 * A SOFTWARE breakpoint is a line, function or address breakpoint that the
	 * user wants installed by swapping out instructions in the target code
	 * memory with an opcode that causes the target to halt. If it cannot be
	 * installed that way, it should not be installed at all.
	 *
	 * The only advantage to software breakpoints is that there is no limit to
	 * how many can be set. The disadvantages are that they are intrusive and
	 * thus more difficult to install/uninstall/manage and, of course, they
	 * can't be set in ROM code.
	 *
	 */
	final static public int SOFTWARE = 0x2 << 1;

	// ------------------------------------------------------------------------
	// ALL FUTURE ADDITIONS MUST HAVE EVEN VALUES. The lease-significant
	// bit is reserved.
	// ------------------------------------------------------------------------

	/**
	 * Returns the type of this breakpoint
	 *
	 * @return type of breakpoint. Defaults to REGULAR if property does not
	 *         exists in the underlying marker.
	 * @exception CoreException
	 *                if unable to access the property on this breakpoint's
	 *                underlying marker
	 *
	 * @since 5.0
	 */
	public int getType() throws CoreException;

	/**
	 * Sets the type of this breakpoint.
	 *
	 * @param type
	 *            breakpoint type
	 * @exception CoreException
	 *                if unable to access the property on this breakpoint's
	 *                underlying marker
	 *
	 * @since 5.0
	 */
	public void setType(int type) throws CoreException;
}
