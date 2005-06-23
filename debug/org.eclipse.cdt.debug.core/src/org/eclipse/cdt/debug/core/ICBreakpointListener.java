/*******************************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

/*
 * Created on Apr 14, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.cdt.debug.core;

import java.util.Map;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Provides event and error notification for C/C++ breakpoints.
 * Listeners register with the <code>CDebugModel</code>.
 */
public interface ICBreakpointListener
{
	/**
	 * Notification that the given breakpoint is about to be installed in
	 * the specified target. Returns whether the installation should proceed. 
	 * If any registered listener returns <code>false</code> the breakpoint is 
	 * not installed in the given target.
	 * 
	 * @param target debug target
	 * @param breakpoint breakpoint being installed
	 * @return whether the the breakpoint should be installed in the given target
	 */
	public boolean installingBreakpoint( IDebugTarget target, IBreakpoint breakpoint );
		
	/**
	 * Notification that the given breakpoint has been installed in the specified target.
	 * 
	 * @param target debug target
	 * @param breakpoint breakpoint being installed
	 */
	public void breakpointInstalled( IDebugTarget target, IBreakpoint breakpoint );

	/**
	 * Notification that the attributes of the given breakpoint have been changed 
	 * from the specified target.
	 * 
	 * @param target debug target
	 * @param breakpoint breakpoint
	 * @param attributes a map of changed attributes
	 */
	public void breakpointChanged( IDebugTarget target, IBreakpoint breakpoint, Map attributes );	

	/**
	 * Notification that the given breakpoints have been removed from the specified target.
	 * 
	 * @param target debug target
	 * @param breakpoints the breakpoints being removed
	 */
	public void breakpointsRemoved( IDebugTarget target, IBreakpoint[] breakpoints );	
}
