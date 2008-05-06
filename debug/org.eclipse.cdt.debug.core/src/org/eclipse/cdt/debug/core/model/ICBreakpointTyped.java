/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Jiju George T- bug 118100
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;

public interface ICBreakpointTyped {
	/**
	 * Breakpoint attribute storing the type this breakpoint
	 * is set in (value <code>"org.eclipse.cdt.debug.core.type"</code>). 
	 * This attribute is a <code>int</code>.
	 * Possible values are 
	 * 	<code>ICBreakpointTyped.REGULAR</code> 
	 *  <code>ICBreakpointTyped.HARDWARE</code>
	 *  <code>ICBreakpointTyped.TEMPORARY</code>
	 * 
	 * @since 5.0
	 */
	public static final String TYPE = "org.eclipse.cdt.debug.core.breakpointType"; //$NON-NLS-1$
	
	
	/**
	 * This type of breakpoint is exposed to the user. How it is installed
	 * (hardware or software) is up to the discretion of the debugger backend. A
	 * common backend approach is to try one way first and then the other if the
	 * first attempt fails.
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
	 * layer. A regular breakpoint will always be a <i>regular</i> breakpoint
	 * regardless of the technique the backend used to install it (software or
	 * hardware).
	 */
	final static public int REGULAR = 0x0;
	
	/**
	 * This type of breakpoint is <b>not</b> exposed to the user and is
	 * typically uninstalled after getting hit. How it is installed (hardware or
	 * software) is up to the discretion of the debugger backend. A common
	 * backend approach is to try one way first and then the other if the first
	 * attempt fails.
	 * 
	 * <p>
	 * Unlike the <i>regular</i> breakpoint type, there are no plans to let the
	 * user coax the backend to install a temporary breakpoint in any particular
	 * way (hardware vs software).
	 */
	final static public int TEMPORARY = 0x1;

	/**
	 * This type of breakpoint is exposed to the user. The user intends for the
	 * breakpoint to be installed as a hardware breakpoint and if it cannot be
	 * installed as such it should not be installed at all.
	 * 
	 * A hardware breakpoint is one that makes use of a breakpoint hardware
	 * facility on the target. For example, the hardware may have a breakpoint
	 * register where an address can be set. The target will halt itself when
	 * the PC matches that register value. Hardware breakpoints have several
	 * advantages. They're non-intrusive to the code and so are simpler for the
	 * debugger to install/uninstall/manage. Also, because they are
	 * non-intrusive, they allow for breakpoints to be set in ROM code. The
	 * disadvantage is that support for them is often limited. Some
	 * architectures only support one or two hardware breakpoints.
	 */
	final static public int HARDWARE = 0x2;

	/**
	 * This type of breakpoint is exposed to the user. The user intends for the
	 * breakpoint to be installed as a software breakpoint and if it cannot be
	 * installed as such it should not be installed at all.
	 * 
	 * A software breakpoint is one that swaps out instructions in the target
	 * code memory with an opcode that causes the target to halt. The only
	 * advantage to software breakpoints is that there is no limit to how many
	 * can be set. The disadvantages are that they are intrusive and thus more
	 * difficult to install/uninstall/manage and, of course, they can't be set
	 * in ROM code.
	 * 
	 */
	final static public int SOFTWARE = 0x4;

	/**
	 * This type of breakpoint is <b>not</b> exposed to the user. The code that
	 * creates such a breakpoint intends for the breakpoint to be installed as a
	 * hardware breakpoint and if it cannot be installed as such it should not
	 * be installed at all.
	 * 
	 */
	final static public int TEMPORARY_HARDWARE = TEMPORARY | HARDWARE;

	/**
	 * This type of breakpoint is <b>not</b> exposed to the user. The code that
	 * creates such a breakpoint intends for the breakpoint to be installed as a
	 * software breakpoint and if it cannot be installed as such it should not
	 * be installed at all.
	 * 
	 */
	final static public int TEMPORARY_SOFTWARE = TEMPORARY | SOFTWARE;

	
	/**
	 * Returns the type of this breakpoint
	 * 
	 * @return type of breakpoint. Defaults to REGULAR if property does not exists in
	 * the underlying marker.
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 * 
	 * @since 5.0
	 */
	public int getType() throws CoreException;
	
	/**
	 * Sets the type of this breakpoint.
	 * 
	 * @param type breakpoint type
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 *  
	 *  @since 5.0
	 */
	public void setType( int type ) throws CoreException;
}
