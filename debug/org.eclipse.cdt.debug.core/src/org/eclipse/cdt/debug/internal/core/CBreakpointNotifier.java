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
package org.eclipse.cdt.debug.internal.core;

import java.util.Map;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;

public class CBreakpointNotifier implements ICBreakpointListener {

	private static CBreakpointNotifier fInstance;

	public static CBreakpointNotifier getInstance() {
		if ( fInstance == null ) {
			fInstance = new CBreakpointNotifier();
		}
		return fInstance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpointListener#installingBreakpoint(org.eclipse.debug.core.model.IDebugTarget,
	 *      org.eclipse.debug.core.model.IBreakpoint)
	 */
	public boolean installingBreakpoint( IDebugTarget target, IBreakpoint breakpoint ) {
		boolean result = true;
		Object[] listeners = CDebugCorePlugin.getDefault().getCBreakpointListeners();
		for( int i = 0; i < listeners.length; ++i ) {
			if ( !((ICBreakpointListener)listeners[i]).installingBreakpoint( target, breakpoint ) )
				result = false;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpointListener#breakpointInstalled(org.eclipse.debug.core.model.IDebugTarget,
	 *      org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointInstalled( IDebugTarget target, IBreakpoint breakpoint ) {
		Object[] listeners = CDebugCorePlugin.getDefault().getCBreakpointListeners();
		for( int i = 0; i < listeners.length; ++i )
			((ICBreakpointListener)listeners[i]).breakpointInstalled( target, breakpoint );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IDebugTarget,
	 *      org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointChanged( IDebugTarget target, IBreakpoint breakpoint, Map attributes ) {
		Object[] listeners = CDebugCorePlugin.getDefault().getCBreakpointListeners();
		for( int i = 0; i < listeners.length; ++i )
			((ICBreakpointListener)listeners[i]).breakpointChanged( target, breakpoint, attributes );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpointListener#breakpointsRemoved(org.eclipse.debug.core.model.IDebugTarget,
	 *      org.eclipse.debug.core.model.IBreakpoint[])
	 */
	public void breakpointsRemoved( IDebugTarget target, IBreakpoint[] breakpoints ) {
		Object[] listeners = CDebugCorePlugin.getDefault().getCBreakpointListeners();
		for( int i = 0; i < listeners.length; ++i )
			((ICBreakpointListener)listeners[i]).breakpointsRemoved( target, breakpoints );
	}
}