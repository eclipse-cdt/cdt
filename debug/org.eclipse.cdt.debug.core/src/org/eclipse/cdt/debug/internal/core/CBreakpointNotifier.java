/*
 * Created on Apr 14, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.cdt.debug.internal.core;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * @author Mikhail Khodjaiants
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class CBreakpointNotifier implements ICBreakpointListener
{
	private static CBreakpointNotifier fInstance;
	
	public static CBreakpointNotifier getInstance()
	{
		if ( fInstance == null )
		{
			fInstance = new CBreakpointNotifier();
		}
		return fInstance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpointListener#installingBreakpoint(org.eclipse.debug.core.model.IDebugTarget, org.eclipse.debug.core.model.IBreakpoint)
	 */
	public boolean installingBreakpoint( IDebugTarget target, IBreakpoint breakpoint )
	{
		boolean result = true;
		Object[] listeners = CDebugCorePlugin.getDefault().getCBreakpointListeners();
		for ( int i = 0; i < listeners.length; ++i )
		{
			if ( !((ICBreakpointListener)listeners[i]).installingBreakpoint( target, breakpoint ) )
				result = false;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpointListener#breakpointInstalled(org.eclipse.debug.core.model.IDebugTarget, org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointInstalled( IDebugTarget target, IBreakpoint breakpoint )
	{
		Object[] listeners = CDebugCorePlugin.getDefault().getCBreakpointListeners();
		for ( int i = 0; i < listeners.length; ++i )
			((ICBreakpointListener)listeners[i]).breakpointInstalled( target, breakpoint );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IDebugTarget, org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointChanged( IDebugTarget target, IBreakpoint breakpoint, Map attributes )
	{
		Object[] listeners = CDebugCorePlugin.getDefault().getCBreakpointListeners();
		for ( int i = 0; i < listeners.length; ++i )
			((ICBreakpointListener)listeners[i]).breakpointChanged( target, breakpoint, attributes );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IDebugTarget, org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointRemoved( IDebugTarget target, IBreakpoint breakpoint )
	{
		Object[] listeners = CDebugCorePlugin.getDefault().getCBreakpointListeners();
		for ( int i = 0; i < listeners.length; ++i )
			((ICBreakpointListener)listeners[i]).breakpointRemoved( target, breakpoint );
	}
}
