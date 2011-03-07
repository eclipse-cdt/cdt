/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service.macos;

import org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service.GDBJtagControl;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.service.macos.MacOSCommandFactory;
import org.eclipse.cdt.dsf.gdb.service.macos.MacOSGdbDebugServicesFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

/** @since 8.0 */
public class MacOSGdbJtagDebugServicesFactory extends MacOSGdbDebugServicesFactory {

	public MacOSGdbJtagDebugServicesFactory(String gdbVersion, String appleVersion) {
		super(gdbVersion, appleVersion);
	}

	@Override
	protected ICommandControl createCommandControl(DsfSession session, ILaunchConfiguration config) {
		return new GDBJtagControl(session, config, new MacOSCommandFactory());
	}
}
