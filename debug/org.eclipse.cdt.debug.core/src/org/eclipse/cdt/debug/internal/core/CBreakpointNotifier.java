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
package org.eclipse.cdt.debug.internal.core;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;

public class CBreakpointNotifier implements ICBreakpointListener {

	private static CBreakpointNotifier fInstance;

	public static CBreakpointNotifier getInstance() {
		if (fInstance == null) {
			fInstance = new CBreakpointNotifier();
		}
		return fInstance;
	}

	@Override
	public boolean installingBreakpoint(IDebugTarget target, IBreakpoint breakpoint) {
		boolean result = true;
		Object[] listeners = CDebugCorePlugin.getDefault().getCBreakpointListeners();
		for (int i = 0; i < listeners.length; ++i) {
			if (!((ICBreakpointListener) listeners[i]).installingBreakpoint(target, breakpoint))
				result = false;
		}
		return result;
	}

	@Override
	public void breakpointInstalled(IDebugTarget target, IBreakpoint breakpoint) {
		Object[] listeners = CDebugCorePlugin.getDefault().getCBreakpointListeners();
		for (int i = 0; i < listeners.length; ++i)
			((ICBreakpointListener) listeners[i]).breakpointInstalled(target, breakpoint);
	}

	@Override
	public void breakpointChanged(IDebugTarget target, IBreakpoint breakpoint, Map attributes) {
		Object[] listeners = CDebugCorePlugin.getDefault().getCBreakpointListeners();
		for (int i = 0; i < listeners.length; ++i)
			((ICBreakpointListener) listeners[i]).breakpointChanged(target, breakpoint, attributes);
	}

	@Override
	public void breakpointsRemoved(IDebugTarget target, IBreakpoint[] breakpoints) {
		Object[] listeners = CDebugCorePlugin.getDefault().getCBreakpointListeners();
		for (int i = 0; i < listeners.length; ++i)
			((ICBreakpointListener) listeners[i]).breakpointsRemoved(target, breakpoints);
	}
}
