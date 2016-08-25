/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.ui.console.AbstractConsole;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;

/**
 * Used to notify this debugger console of lifecycle methods <code>init()</code>
 * and <code>dispose()</code>.
 */
public class GdbConsoleLifecycleListener implements IConsoleListener {
	
	private final AbstractConsole fConsole;
	
	public GdbConsoleLifecycleListener(AbstractConsole console) {
		fConsole = console;
		CDebugUIPlugin.getDebuggerConsoleManager().addConsoleListener(this);
	}
	
	@Override
	public void consolesAdded(IConsole[] consoles) {
		for (IConsole console : consoles) {
			if (console.equals(fConsole)) {
				fConsole.initialize();
				break;
			}
		}
	}

	@Override
	public void consolesRemoved(IConsole[] consoles) {
		for (IConsole console : consoles) {
			if (console.equals(fConsole)) {
				CDebugUIPlugin.getDebuggerConsoleManager().removeConsoleListener(this);
				fConsole.destroy();
				break;
			}
		}
	}
}
