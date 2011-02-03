/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.launching.ITracedLaunch;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;

/**
 * A tracing console manager which adds and removes tracing consoles
 * based on launch events and preference events.
 * 
 * @since 2.1
 * This class was moved from package org.eclipse.cdt.dsf.gdb.internal.ui.tracing
 */
public class TracingConsoleManager implements ILaunchesListener2, IPropertyChangeListener {

	/**
	 * Member to keep track of the preference.
	 * We keep it up-to-date by registering as an IPropertyChangeListener
	 */
	private boolean fTracingEnabled = false;
	
	/**
	 * Start the tracing console.  We don't do this in a constructor, because
	 * we need to use <code>this</code>.
	 */
	public void startup() {
		GdbUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		
		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		fTracingEnabled = store.getBoolean(IGdbDebugPreferenceConstants.PREF_TRACES_ENABLE);
		
		if (fTracingEnabled) {
			toggleTracing(true);
		}
	}

	public void shutdown() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		GdbUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		removeAllConsoles();
	}

	protected void toggleTracing(boolean enabled) {
		if (enabled) {
			DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
			addAllConsoles();
		} else {
			DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
			removeAllConsoles();
		}
	}
	
	protected void addAllConsoles() {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for (ILaunch launch : launches) {
			addConsole(launch);
		}
	}

	protected void removeAllConsoles() {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for (ILaunch launch : launches) {
			removeConsole(launch);
		}
	}

	public void launchesAdded(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			addConsole(launch);
		}
	}

	public void launchesChanged(ILaunch[] launches) {
	}

	public void launchesRemoved(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			removeConsole(launch);
		}
	}
	
	public void launchesTerminated(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			// Since we already had a console, don't get rid of it
			// just yet.  Simply rename it to show it is terminated.
			renameConsole(launch);
		}
	}
	
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IGdbDebugPreferenceConstants.PREF_TRACES_ENABLE)) {
			fTracingEnabled = (Boolean)event.getNewValue();
			toggleTracing(fTracingEnabled);
		}
	}

	protected void addConsole(ILaunch launch) {
		// Tracing consoles are only added to ITracingLaunches
		if (launch instanceof ITracedLaunch) {
			// Make sure we didn't already add this console
			if (getConsole(launch) == null) {
				if (launch.isTerminated() == false) {
					// Create and  new tracing console.
					TracingConsole console = new TracingConsole(launch, ConsoleMessages.ConsoleMessages_trace_console_name);
					console.setWaterMarks(400000, 500000);
					ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{console});
				} // else we don't display a new console for a terminated launch
			}
		}
	}

	protected void removeConsole(ILaunch launch) {
		if (launch instanceof ITracedLaunch) {
			TracingConsole console = getConsole(launch);
			if (console != null) {
				ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{console});
			}
		}
	}

	protected void renameConsole(ILaunch launch) {
		if (launch instanceof ITracedLaunch) {
			TracingConsole console = getConsole(launch);
			if (console != null) {
				console.resetName();
			}		
		}
	}
	
	private TracingConsole getConsole(ILaunch launch) {
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager(); 
		IConsole[] consoles = manager.getConsoles();
		for (IConsole console : consoles) {
			if (console instanceof TracingConsole) {
				TracingConsole tracingConsole = (TracingConsole)console;
				if (tracingConsole.getLaunch().equals(launch)) {
					return tracingConsole;
				}
			}
		}
		return null;
	}
}
