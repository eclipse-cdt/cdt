/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.debug.application;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

public class DebugRemoteExecutable {

	public DebugRemoteExecutable() {
	}

	public static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	public static ILaunchConfiguration createLaunchConfig(IProgressMonitor monitor, String buildLog, String executable,
			String address, String port, boolean attach) throws CoreException, InterruptedException {
		ILaunchConfiguration config = null;

		config = createConfiguration(executable, address, port, attach, true);
		monitor.worked(1);
		return config;
	}

	protected static ILaunchConfigurationType getLaunchConfigType(boolean attach) {
		return getLaunchManager()
				.getLaunchConfigurationType(attach ? ICDTLaunchConfigurationConstants.ID_LAUNCH_C_ATTACH
						: ICDTLaunchConfigurationConstants.ID_LAUNCH_C_REMOTE_APP);
	}

	protected static ILaunchConfiguration createConfiguration(String exePath, String address, String port,
			boolean attach, boolean save) {
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getLaunchConfigType(attach);
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
					getLaunchManager().generateLaunchConfigurationName(attach ? "CDT_REMOTE_ATTACH" : "CDT_REMOTE")); //$NON-NLS-1$ //$NON-NLS-2$

			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					attach ? IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE_ATTACH
							: IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE);

			if (exePath != null && exePath.length() > 0) {
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, exePath);
			} else {
				assert attach;
			}
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "Executables"); //$NON-NLS-1$
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);

			wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP, true);
			wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_HOST, address);
			wc.setAttribute(IGDBLaunchConfigurationConstants.ATTR_PORT, port);

			if (save) {
				config = wc.doSave();
			} else {
				config = wc;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return config;
	}
}
