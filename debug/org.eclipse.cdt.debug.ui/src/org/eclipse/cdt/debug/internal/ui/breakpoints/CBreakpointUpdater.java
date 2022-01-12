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
package org.eclipse.cdt.debug.internal.ui.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.ICBreakpointListener;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
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
		if (fInstance == null) {
			fInstance = new CBreakpointUpdater();
		}
		return fInstance;
	}

	@Override
	public boolean installingBreakpoint(IDebugTarget target, IBreakpoint breakpoint) {
		return true;
	}

	@Override
	public void breakpointInstalled(final IDebugTarget target, IBreakpoint breakpoint) {
	}

	@Override
	public void breakpointChanged(IDebugTarget target, final IBreakpoint breakpoint,
			@SuppressWarnings("rawtypes") final Map attributes) {
		asyncExec(() -> {
			try {
				Boolean enabled = (Boolean) attributes.get(IBreakpoint.ENABLED);
				breakpoint.setEnabled((enabled != null) ? enabled.booleanValue() : false);
				Integer ignoreCount = (Integer) attributes.get(ICBreakpoint.IGNORE_COUNT);
				((ICBreakpoint) breakpoint).setIgnoreCount((ignoreCount != null) ? ignoreCount.intValue() : 0);
				String condition = (String) attributes.get(ICBreakpoint.CONDITION);
				((ICBreakpoint) breakpoint).setCondition((condition != null) ? condition : ""); //$NON-NLS-1$
			} catch (CoreException e) {
				CDebugUIPlugin.log(e.getStatus());
			}
		});
	}

	@Override
	public void breakpointsRemoved(IDebugTarget target, final IBreakpoint[] breakpoints) {
		asyncExec(() -> {
			for (int i = 0; i < breakpoints.length; ++i) {
				try {
					if (((ICBreakpoint) breakpoints[i]).decrementInstallCount() == 0)
						DebugPlugin.getDefault().getBreakpointManager().fireBreakpointChanged(breakpoints[i]);
				} catch (CoreException e) {
					// ensureMarker throws this exception
					// if breakpoint has already been deleted
				}
			}
		});
	}

	public void dispose() {
	}

	private void asyncExec(Runnable r) {
		Display display = Display.getDefault();
		if (display != null)
			display.asyncExec(r);
	}
}
