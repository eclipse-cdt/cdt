/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.debug.application;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

public class DebugCoreFile {
	
	public DebugCoreFile() {
	}
	
	public static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * Import given executable into the Executables project then create a launch configuration.
	 * 
	 * @param monitor
	 * @param executable
	 * @param buildLog
	 * @param arguments
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	public static ILaunchConfiguration createLaunchConfig(IProgressMonitor monitor,
			String buildLog, String executable, String coreFile)
					throws CoreException, InterruptedException {
		ILaunchConfiguration config = null;

		//					System.out.println("about to create launch configuration");
		config = createConfiguration(coreFile, executable, true);
		monitor.worked(1);
		return config;
	}

	protected static ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(
				"org.eclipse.cdt.launch.postmortemLaunchType"); //$NON-NLS-1$
	}
	
	protected static ILaunchConfiguration createConfiguration(String corePath, String exePath, boolean save) {
//		System.out.println("creating launch configuration");
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(
					null,
					getLaunchManager().generateLaunchConfigurationName("CDT_DBG_CORE")); //$NON-NLS-1$

			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_COREFILE_PATH, corePath);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, exePath);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					"Executables"); //$NON-NLS-1$
			wc.setAttribute(
					ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
					(String) null);
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
