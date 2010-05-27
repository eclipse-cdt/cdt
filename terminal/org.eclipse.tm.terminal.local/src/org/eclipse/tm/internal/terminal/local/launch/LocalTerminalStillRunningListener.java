/***************************************************************************************************
 * Copyright (c) 2008, 2010 Mirko Raner and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mirko Raner - [196337] Adapted from org.eclipse.ui.externaltools/ProgramLaunchDelegate
 **************************************************************************************************/

package org.eclipse.tm.internal.terminal.local.launch;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tm.internal.terminal.local.LocalTerminalActivator;
import org.eclipse.tm.internal.terminal.local.LocalTerminalUtilities;
import org.eclipse.tm.internal.terminal.local.launch.ui.LocalTerminalStillRunningDialog;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * The class {@link LocalTerminalStillRunningListener} is an {@link IWorkbenchListener} that warns
 * the user about any terminal launches that are still running when the workbench closes. The user
 * might want to take specific action to deal with such left-over processes. Typically, this
 * listener will trigger only on very rare cases because the
 * {@link org.eclipse.tm.internal.terminal.provisional.api.ITerminalConnector} implementation will
 * terminate left-over launches when the workbench window is closed. However, it is possible that
 * a terminal launch does not get automatically terminated, for example, if it was started through
 * an External Tools launch rather than through the terminal.
 *
 * The class {@link LocalTerminalStillRunningListener} is inspired by the
 * <code>ProgramLaunchWindowListener</code> class inside <code>ProgramLaunchDelegate</code> in the
 * <code>org.eclipse.ui.externaltools</code> plug-in, though it works through a slightly different
 * mechanism.
 *
 * @author Mirko Raner
 * @version $Revision: 1.1 $
 */
public class LocalTerminalStillRunningListener implements IWorkbenchListener {

	/**
	 * Creates a new {@link LocalTerminalStillRunningListener}.
	 */
	public LocalTerminalStillRunningListener() {

		super();
	}

	/**
	 * Gets notified when the workbench is closed and informs the user about any left-over
	 * terminal launches.
	 *
	 * @param workbench the {@link IWorkbench}
	 * @param forced <code>true</code> if a forced shutdown occurred, <code>false</code> otherwise
	 * @return <code>true</code> to allow the workbench to proceed with shutdown, <code>false</code>
	 * to prevent a shutdown (only for non-forced shutdown)
	 */
	public boolean preShutdown(IWorkbench workbench, boolean forced) {

		if (forced) {

			return true;
		}
		IPreferenceStore store = LocalTerminalActivator.getDefault().getPreferenceStore();
		if (!store.getBoolean(LocalTerminalActivator.PREF_CONFIRM_TERMINATE_ON_SHUTDOWN)) {

			//return true;
		}
		ILaunchConfigurationType launchType;
		String launchTypeID = LocalTerminalLaunchDelegate.LAUNCH_CONFIGURATION_TYPE_ID;
		launchType = LocalTerminalUtilities.LAUNCH_MANAGER.getLaunchConfigurationType(launchTypeID);
		if (launchType == null) {

			return true;
		}
		List notTerminated = new ArrayList();
		ILaunch launches[] = LocalTerminalUtilities.LAUNCH_MANAGER.getLaunches();
		ILaunchConfigurationType configurationType;
		ILaunchConfiguration configuration;
		for (int launch = 0; launch < launches.length; launch++) {

			try {

				configuration = launches[launch].getLaunchConfiguration();
				if (configuration == null) {

					continue;
				}
				configurationType= configuration.getType();
			}
			catch (CoreException exception) {

				Logger.logException(exception);
				continue;
			}
			if (configurationType.equals(launchType) && !launches[launch].isTerminated()) {

				notTerminated.add(launches[launch]);
			}
		}
		if (!notTerminated.isEmpty()) {

			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			ILaunch[] launch = (ILaunch[])notTerminated.toArray(new ILaunch[notTerminated.size()]);
			return LocalTerminalStillRunningDialog.openDialog(window.getShell(), launch);
		}
		return true;
	}

	/**
	 * <i>Not implemented</i>.
	 * @see IWorkbenchListener#postShutdown(IWorkbench)
	 */
	public void postShutdown(IWorkbench workbench) {

		// Not implemented
	}
}
