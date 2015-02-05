/***************************************************************************************************
 * Copyright (c) 2008 Mirko Raner.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - initial implementation for Eclipse Bug 196337
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local.process;

import java.util.IdentityHashMap;
import java.util.Map;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.tm.internal.terminal.local.LocalTerminalUtilities;

/**
 * The {@link LocalTerminalProcessRegistry} keeps a map between {@link ILaunch} objects and the
 * {@link LocalTerminalProcess} objects that were associated with them. To prevent standard consoles
 * from being opened, a {@link LocalTerminalProcess} is immediately removed from its {@link ILaunch}
 * when the process is created (see {@link LocalTerminalProcess} for details).
 * {@link LocalTerminalProcessRegistry} is a singleton (without lazy initialization).
 *
 * @author mirko
 * @version $Revision: 1.1 $
 */
public class LocalTerminalProcessRegistry implements ILaunchesListener2 {

	private final static LocalTerminalProcessRegistry INSTANCE = new LocalTerminalProcessRegistry();

	private Map processes;

	private LocalTerminalProcessRegistry() {

		// The ILaunch interface does not make any statements about the suitability of implementing
		// objects as hash keys. There might be ILaunch implementations that return a different
		// hash code value if the object changes internally. To be safe in those cases, an
		// IdentityHashMap is used:
		//
		processes = new IdentityHashMap();
	}

	/**
	 * Gets the {@link LocalTerminalProcess} that was originally associated with a given
	 * {@link ILaunch} object.
	 *
	 * @param launch the {@link ILaunch} that was used for creating the process
	 * @return the corresponding {@link LocalTerminalProcess}, or <code>null</code> if no process
	 * could be found
	 */
	public static LocalTerminalProcess getFromLaunch(ILaunch launch) {

		return (LocalTerminalProcess)INSTANCE.processes.get(launch);
	}

	/**
	 * Adds a {@link LocalTerminalProcess} object back to its original {@link ILaunch}. This method
	 * will also perform a {@link LocalTerminalProcess#resetStreamsProxy()} on the process.
	 * The {@link #addProcessBackToFinishedLaunch(ILaunch)} method is necessary for properly
	 * terminating the launch of a terminal application (see {@link LocalTerminalProcess} for
	 * details).
	 *
	 * @param launch the {@link ILaunch} whose original process is to be re-attached
	 */
	public static void addProcessBackToFinishedLaunch(ILaunch launch) {

		LocalTerminalProcess process = getFromLaunch(launch);
		if (process == null) {

			// Maybe the process wasn't actually started in a terminal (can happen when a Terminal
			// is launched from the External Tools menu):
			//
			return;
		}
		process.resetStreamsProxy();
		if (launch.getProcesses().length == 0) {

			launch.addProcess(process);
		}
	}

	/**
	 * Registers a {@link LocalTerminalProcess} with a given {@link ILaunch} so that the process can
	 * be safely removed from the launch.
	 *
	 * @param launch the {@link ILaunch}
	 * @param process the {@link LocalTerminalProcess} originally associated with that launch
	 */
	public static void registerWithLaunch(ILaunch launch, LocalTerminalProcess process) {

		synchronized (INSTANCE) {

			if (INSTANCE.processes.isEmpty()) {

				// Start listening to terminated launches as soon as the first launch/process pair
				// is registered:
				//
				LocalTerminalUtilities.LAUNCH_MANAGER.addLaunchListener(INSTANCE);
			}
			INSTANCE.processes.put(launch, process);
		}
	}

	/**
	 * Handles the termination of launches. The {@link LocalTerminalProcessRegistry} acts as a
	 * {@link ILaunchesListener2} if there are monitored launches outstanding. It will automatically
	 * de-register itself (as a listener) when the last monitored launch was terminated.
	 *
	 * @param terminated the launches that were terminated
	 */
	public void launchesTerminated(ILaunch[] terminated) {

		synchronized (INSTANCE) {

			int numberOfTerminatedLaunches = terminated.length;
			for (int launch = 0; launch < numberOfTerminatedLaunches; launch++) {

				INSTANCE.processes.remove(terminated[launch]);
			}
			if (INSTANCE.processes.isEmpty()) {

				// If there are no more outstanding launches the listener can be removed again:
				//
				LocalTerminalUtilities.LAUNCH_MANAGER.removeLaunchListener(INSTANCE);
			}
		}
	}

	/**
	 * <i>Not implemented</i>.
	 * @see ILaunchesListener2#launchesAdded(ILaunch[])
	 */
	public void launchesAdded(ILaunch[] launches) {

		// Not implemented.
	}

	/**
	 * <i>Not implemented</i>.
	 * @see ILaunchesListener2#launchesRemoved(ILaunch[])
	 */
	public void launchesRemoved(ILaunch[] launches) {

		// Not implemented.
	}

	/**
	 * <i>Not implemented</i>.
	 * @see ILaunchesListener2#launchesChanged(ILaunch[])
	 */
	public void launchesChanged(ILaunch[] launches) {

		// Not implemented.
	}
}
