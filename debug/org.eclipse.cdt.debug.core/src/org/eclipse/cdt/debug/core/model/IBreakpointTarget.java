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

import org.eclipse.cdt.core.IAddress;
import org.eclipse.debug.core.DebugException;

/**
 * Provides access to breakpoint-specific information.
 */
public interface IBreakpointTarget {
	
	/**
	 * Returns whether this target supports the given breakpoint.
	 *   
	 * @return whether this target supports the given breakpoint.
	 */
	boolean isTargetBreakpoint( ICBreakpoint breakpoint );

	/**
	 * Returns the target address of the given breakpoint.
	 * 
	 * @return the target address of the given breakpoint
	 * @throws DebugException if the address is not available
	 */
	IAddress getBreakpointAddress( ICLineBreakpoint breakpoint ) throws DebugException;
}
