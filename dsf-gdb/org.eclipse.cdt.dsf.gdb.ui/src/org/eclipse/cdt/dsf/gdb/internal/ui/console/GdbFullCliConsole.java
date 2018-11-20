/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.debug.ui.debuggerconsole.IDebuggerConsoleView;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.AbstractConsole;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A GDB CLI console.
 * This console actually runs a GDB process in CLI mode to achieve a
 * full-featured CLI interface.  This is only supported with GDB >= 7.12
 * and if IGDBBackend.isFullGdbConsoleSupported() returns true.
 */
public class GdbFullCliConsole extends AbstractConsole implements IGDBDebuggerConsole {
	private final ILaunch fLaunch;
	private final String fLabel;
	private final PTY fGdbPty;

	private GdbFullCliConsolePage fConsolePage;
	private final GdbTerminalConnector fTerminalConnector;

	public GdbFullCliConsole(ILaunch launch, String label, Process process, PTY pty) {
		super(label, null, false);
		fLaunch = launch;
		fLabel = label;
		fGdbPty = pty;

		// Create a lifecycle listener to call init() and dispose()
		new GdbConsoleLifecycleListener(this);
		fTerminalConnector = new GdbTerminalConnector(process);

		resetName();
	}

	@Override
	protected void dispose() {
		fTerminalConnector.dispose();
		super.dispose();
	}

	@Override
	public ILaunch getLaunch() {
		return fLaunch;
	}

	@Override
	public void resetName() {
		String newName = computeName();
		String name = getName();
		if (!name.equals(newName)) {
			try {
				PlatformUI.getWorkbench().getDisplay().asyncExec(() -> setName(newName));
			} catch (SWTException e) {
				// display may be disposed, so ignore the exception
				if (e.code != SWT.ERROR_WIDGET_DISPOSED && e.code != SWT.ERROR_DEVICE_DISPOSED) {
					throw e;
				}
			}
		}
	}

	protected String computeName() {
		if (fLaunch == null) {
			return ""; //$NON-NLS-1$
		}

		String label = fLabel;

		ILaunchConfiguration config = fLaunch.getLaunchConfiguration();
		if (config != null && !DebugUITools.isPrivate(config)) {
			String type = null;
			try {
				type = config.getType().getName();
			} catch (CoreException e) {
			}
			StringBuffer buffer = new StringBuffer();
			buffer.append(config.getName());
			if (type != null) {
				buffer.append(" ["); //$NON-NLS-1$
				buffer.append(type);
				buffer.append("] "); //$NON-NLS-1$
			}
			buffer.append(label);
			label = buffer.toString();
		}

		if (fLaunch.isTerminated()) {
			return ConsoleMessages.ConsoleMessages_console_terminated + label;
		}

		return label;
	}

	@Override
	public IPageBookViewPage createPage(IConsoleView view) {
		// This console is not meant for the standard console view
		return null;
	}

	@Override
	public IPageBookViewPage createDebuggerPage(IDebuggerConsoleView view) {
		view.setFocus();
		fConsolePage = new GdbFullCliConsolePage(this, view, fGdbPty);
		return fConsolePage;
	}

	public IGdbTerminalControlConnector getTerminalControlConnector() {
		return fTerminalConnector;
	}

}
