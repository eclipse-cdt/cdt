/*******************************************************************************
 * Copyright (c) 2007 - 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial implementation
 *     Ericsson - Updated for changes in base DSF-GDB launching (Bug 338769)
 *     Marc Khouzam (Ericsson) - Make sure non-stop is disabled (bug 348091)
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core;

/**
 * @author Andy Jin
 *
 */

import org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service.GdbJtagDebugServicesFactory;
import org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service.macos.MacOSGdbJtagDebugServicesFactory;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * The launch configuration delegate for the Jtag hardware debugging using
 * the DSF/GDB debugger framework.
 * <p>
 * This delegate only supports the org.eclipse.cdt.debug.gdbjtag.launchConfigurationType
 * launch configuration types.
 * <p>
 * It extends the standard DSF/GDB launch delegate <code>GdbLaunchDelegate</code>
 * but overrides the <code>newServiceFactory</code> method to return the Jtag
 * hardware debugging factory.
 * @since 7.0
 */
@ThreadSafe
public class GDBJtagDSFLaunchConfigurationDelegate extends GdbLaunchDelegate {

	@Override
	protected IDsfDebugServicesFactory newServiceFactory(ILaunchConfiguration config, String version) {
		if (version.contains(LaunchUtils.MACOS_GDB_MARKER)) {
			// The version string at this point should look like
			// 6.3.50-20050815APPLE1346, we extract the gdb version and apple version
			String versions [] = version.split(LaunchUtils.MACOS_GDB_MARKER);
			if (versions.length == 2) {
				return new MacOSGdbJtagDebugServicesFactory(versions[0], versions[1]);
			}
		}

		return new GdbJtagDebugServicesFactory(version);
	}
	
	@Override
	public boolean preLaunchCheck(ILaunchConfiguration config, String mode, IProgressMonitor monitor) throws CoreException {
		// Forcibly turn off non-stop for hardware sessions.
		// Non-stop is not an option we offer for hardware launches.
		// Now that we can have non-stop defaulting to enabled, it will prevent
		// hardware sessions from starting for GDBs <= 6.8 and there is no way to turn if off
		// Bug 348091
		if (LaunchUtils.getIsNonStopMode(config)) {
			ILaunchConfigurationWorkingCopy wcConfig = config.getWorkingCopy();
			wcConfig.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, false);
			wcConfig.doSave();			
		}
		
		return super.preLaunchCheck(config, mode, monitor);
	}
}
