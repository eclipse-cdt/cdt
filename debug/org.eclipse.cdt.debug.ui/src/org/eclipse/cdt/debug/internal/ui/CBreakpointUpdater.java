/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import java.util.Map;
import org.eclipse.cdt.debug.core.ICBreakpointListener;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.swt.widgets.Display;

/**
 * Provides UI-related handles for the breakpoint events.
 */
public class CBreakpointUpdater implements ICBreakpointListener {

	private static CBreakpointUpdater fInstance;

	public static CBreakpointUpdater getInstance() {
		if ( fInstance == null ) {
			fInstance = new CBreakpointUpdater();
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
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpointListener#breakpointInstalled(org.eclipse.debug.core.model.IDebugTarget,
	 *      org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointInstalled( final IDebugTarget target, IBreakpoint breakpoint ) {
		if ( breakpoint instanceof ICBreakpoint && target instanceof ICDebugTarget ) {
			final ICBreakpoint b = (ICBreakpoint)breakpoint;
			asyncExec( new Runnable() {
	
				public void run() {
						try {
							if ( b.incrementInstallCount() == 1 )
								DebugPlugin.getDefault().getBreakpointManager().fireBreakpointChanged( b );
						}
						catch( CoreException e ) {
							CDebugUIPlugin.log( e.getStatus() );
						}
					}
			} );
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IDebugTarget,
	 *      org.eclipse.debug.core.model.IBreakpoint, java.util.Map)
	 */
	public void breakpointChanged( IDebugTarget target, final IBreakpoint breakpoint, final Map attributes ) {
		asyncExec( new Runnable() {

			public void run() {
				try {
					Boolean enabled = (Boolean)attributes.get( IBreakpoint.ENABLED );
					breakpoint.setEnabled( (enabled != null) ? enabled.booleanValue() : false );
					Integer ignoreCount = (Integer)attributes.get( ICBreakpoint.IGNORE_COUNT );
					((ICBreakpoint)breakpoint).setIgnoreCount( (ignoreCount != null) ? ignoreCount.intValue() : 0 );
					String condition = (String)attributes.get( ICBreakpoint.CONDITION );
					((ICBreakpoint)breakpoint).setCondition( (condition != null) ? condition : "" ); //$NON-NLS-1$
				}
				catch( CoreException e ) {
					CDebugUIPlugin.log( e.getStatus() );
				}
			}
		} );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IDebugTarget,
	 *      org.eclipse.debug.core.model.IBreakpoint[])
	 */
	public void breakpointsRemoved( IDebugTarget target, final IBreakpoint[] breakpoints ) {
		asyncExec( new Runnable() {

			public void run() {
				for ( int i = 0; i < breakpoints.length; ++i ) {
					try {
						((ICBreakpoint)breakpoints[i]).decrementInstallCount();
					}
					catch( CoreException e ) {
						// ensureMarker throws this exception 
						// if breakpoint has already been deleted 
					}
				}
			}
		} );
	}

	public void dispose() {
	}

	private void asyncExec( Runnable r ) {
		Display display = Display.getDefault();
		if ( display != null )
			display.asyncExec( r );
	}
}
