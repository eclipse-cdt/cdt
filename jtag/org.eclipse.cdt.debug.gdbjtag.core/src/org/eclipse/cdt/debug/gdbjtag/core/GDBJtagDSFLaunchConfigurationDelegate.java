/*******************************************************************************
 * Copyright (c) 2007 - 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial implementation
 *     Ericsson - Updated for changes in base DSF-GDB launching (Bug 338769)
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
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;

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
	
	protected IDsfDebugServicesFactory newServiceFactory(String version) {
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
}
