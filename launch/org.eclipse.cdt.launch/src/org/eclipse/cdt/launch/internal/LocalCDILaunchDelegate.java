/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems) - bugs 205108, 212632, 224187
 * Ken Ryall (Nokia) - bug 188116
 * Marc Khouzam (Ericsson) - Modernize Run launch (bug 464636)
 * Jonah Graham (Kichwa Coders) - Remove CDI - LocalCDILaunchDelegate now only support "run" mode
 *******************************************************************************/
package org.eclipse.cdt.launch.internal; 

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
 
/**
 * The launch configuration delegate for the CDI debugger session types.
 */
public class LocalCDILaunchDelegate extends AbstractCLaunchDelegate {
	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		if (mode.equals(ILaunchManager.RUN_MODE)) {
			// We plan on splitting the Run delegate from the Debug one.
			// For now, to keep backwards-compatibility, we need to keep the same delegate (to keep its id)
			// However, we can just call the new delegate class
			new LocalRunLaunchDelegate().launch(config, mode, launch, monitor);
		}
	}

	@Override
	protected String getPluginID() {
		return LaunchUIPlugin.getUniqueIdentifier();
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration config, String mode, IProgressMonitor monitor) throws CoreException {
		if (mode.equals(ILaunchManager.RUN_MODE)) {
			// We plan on splitting the Run delegate from the Debug one.
			// For now, to keep backwards-compatibility, we need to keep the same delegate (to keep its id)
			// However, we can just call the new delegate class
			new LocalRunLaunchDelegate().preLaunchCheck(config, mode, monitor);
		}

		return super.preLaunchCheck(config, mode, monitor);
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		// Never build for attach. Bug 188116
		String debugMode = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);
		if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH))
			return false;
		
		return super.buildForLaunch(configuration, mode, monitor);
	}
}
