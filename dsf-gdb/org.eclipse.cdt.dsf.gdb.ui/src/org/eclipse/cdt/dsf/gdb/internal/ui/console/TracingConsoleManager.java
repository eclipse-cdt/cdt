/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console;

import java.util.HashMap;

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

/**
 * A tracing console manager which adds and removes tracing consoles
 * based on launch events and preference events.
 * TracingConsoles are always running but are only shown in the console
 * view if enabled by the user preference.
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
	 * A map of all TracingConsoles for their corresponding launch.
	 * We keep this list because TracingConsoles may not be registered
	 * with the ConsoleManager, so we need another way to find them.
	 */
	private HashMap<ITracedLaunch, TracingConsole> fTracingConsoles = new HashMap<>();

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

		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
	}

	public void shutdown() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		GdbUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		removeAllConsoles();
	}

	protected void toggleTracingVisibility(boolean visible) {
		if (visible) {
			ConsolePlugin.getDefault().getConsoleManager()
					.addConsoles(fTracingConsoles.values().toArray(new IConsole[fTracingConsoles.size()]));
		} else {
			ConsolePlugin.getDefault().getConsoleManager()
					.removeConsoles(fTracingConsoles.values().toArray(new IConsole[fTracingConsoles.size()]));
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
			fTracingEnabled = (Boolean) event.getNewValue();
			toggleTracingVisibility(fTracingEnabled);
		} else if (event.getProperty().equals(IGdbDebugPreferenceConstants.PREF_MAX_GDB_TRACES)) {
			int maxChars = (Integer) event.getNewValue();
			updateAllConsoleWaterMarks(maxChars);
		}
	}

	protected void addConsole(ILaunch launch) {
		// Tracing consoles are only added to ITracingLaunches
		if (launch instanceof ITracedLaunch) {
			// Make sure we didn't already add this console
			if (getConsole(launch) == null) {
				if (!launch.isTerminated()) {
					// Create a new tracing console.
					TracingConsole console = new TracingConsole(launch,
							ConsoleMessages.ConsoleMessages_trace_console_name);
					console.initialize();
					console.setWaterMarks(fMinNumCharacters, fMaxNumCharacters);

					fTracingConsoles.put((ITracedLaunch) launch, console);
					if (fTracingEnabled) {
						ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
					}
				} // else we don't display a new console for a terminated launch
			}
		}
	}

	protected void removeConsole(ILaunch launch) {
		TracingConsole console = fTracingConsoles.remove(launch);
		if (console != null) {
			console.destroy();
			if (fTracingEnabled) {
				ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] { console });
			}
		}
	}

	protected void renameConsole(ILaunch launch) {
		TracingConsole console = getConsole(launch);
		if (console != null) {
			console.resetName();
		}
	}

	private TracingConsole getConsole(ILaunch launch) {
		return fTracingConsoles.get(launch);
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
				? MIN_NUMBER_OF_CHARS_TO_KEEP
				: maxChars - NUMBER_OF_CHARS_TO_DELETE;
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
		TracingConsole console = getConsole(launch);
		if (console != null) {
			console.setWaterMarks(fMinNumCharacters, fMaxNumCharacters);
		}
	}
}
