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
	 * The number of characters that should be deleted once the GDB traces console
	 * reaches its configurable maximum.
	 */
	private static final int NUMBER_OF_CHARS_TO_DELETE = 100000;

	/**
	 * The minimum number of characters that should be kept when truncating
	 * the console output. 
	 */
	private static final int MIN_NUMBER_OF_CHARS_TO_KEEP = 5000;

	/**
	 * Member to keep track of the preference.
	 * We keep it up-to-date by registering as an IPropertyChangeListener
	 */
	private boolean fTracingEnabled = false;
	
	/**
	 * The maximum number of characters that are allowed per console
	 */
	private int fMaxNumCharacters = 500000;
	
	/**
	 * The number of characters that will be kept in the console once we
	 * go over fMaxNumCharacters and that we must remove some characters
	 */
	private int fMinNumCharacters = fMaxNumCharacters - NUMBER_OF_CHARS_TO_DELETE;
	
	/**
	 * Start the tracing console.  We don't do this in a constructor, because
	 * we need to use <code>this</code>.
	 */
	public void startup() {
		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		
		store.addPropertyChangeListener(this);
		fTracingEnabled = store.getBoolean(IGdbDebugPreferenceConstants.PREF_TRACES_ENABLE);
		int maxChars = store.getInt(IGdbDebugPreferenceConstants.PREF_MAX_GDB_TRACES);
		setWaterMarks(maxChars);
		
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

    @Override
	public void launchesAdded(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			addConsole(launch);
		}
	}

    @Override
	public void launchesChanged(ILaunch[] launches) {
	}

    @Override
	public void launchesRemoved(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			removeConsole(launch);
		}
	}
	
    @Override
	public void launchesTerminated(ILaunch[] launches) {
		for (ILaunch launch : launches) {
			// Since we already had a console, don't get rid of it
			// just yet.  Simply rename it to show it is terminated.
			renameConsole(launch);
		}
	}
	
    @Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IGdbDebugPreferenceConstants.PREF_TRACES_ENABLE)) {
			fTracingEnabled = (Boolean)event.getNewValue();
			toggleTracing(fTracingEnabled);
		} else if (event.getProperty().equals(IGdbDebugPreferenceConstants.PREF_MAX_GDB_TRACES)) {
			int maxChars = (Integer)event.getNewValue();
			updateAllConsoleWaterMarks(maxChars);
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
					console.setWaterMarks(fMinNumCharacters, fMaxNumCharacters);
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
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		if (plugin != null) {
			// I've seen the plugin be null when running headless JUnit tests
			IConsoleManager manager = plugin.getConsoleManager(); 
			IConsole[] consoles = manager.getConsoles();
			for (IConsole console : consoles) {
				if (console instanceof TracingConsole) {
					TracingConsole tracingConsole = (TracingConsole)console;
					if (tracingConsole.getLaunch().equals(launch)) {
						return tracingConsole;
					}
				}
			}
		}
		return null;
	}
	
	/** @since 2.2 */
	protected void setWaterMarks(int maxChars) {
		if (maxChars < (MIN_NUMBER_OF_CHARS_TO_KEEP * 2)) {
			maxChars = MIN_NUMBER_OF_CHARS_TO_KEEP * 2;
		}
		
		fMaxNumCharacters = maxChars;
		// If the max number of chars is anything below the number of chars we are going to delete
		// (plus our minimum buffer), we only keep the minimum.
		// If the max number of chars is bigger than the number of chars we are going to delete (plus
		// the minimum buffer), we truncate a fixed amount chars.
		fMinNumCharacters = maxChars < (NUMBER_OF_CHARS_TO_DELETE + MIN_NUMBER_OF_CHARS_TO_KEEP) 
								? MIN_NUMBER_OF_CHARS_TO_KEEP : maxChars - NUMBER_OF_CHARS_TO_DELETE;
	}

	/** @since 2.2 */
	protected void updateAllConsoleWaterMarks(int maxChars) {
		setWaterMarks(maxChars);
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for (ILaunch launch : launches) {
			updateConsoleWaterMarks(launch);
		}
	}
	
	/** @since 2.2 */
	protected void updateConsoleWaterMarks(ILaunch launch) {
		if (launch instanceof ITracedLaunch) {
			TracingConsole console = getConsole(launch);
			if (console != null) {
				console.setWaterMarks(fMinNumCharacters, fMaxNumCharacters);
			}		
		}
	}
}
