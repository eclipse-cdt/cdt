/*******************************************************************************
 * Copyright (c) 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc-Andre Laperle - Added support for Mac OS (separate factory)
 *     					  - fix for bug 265483
 *     Ericsson           - Added a field for the specific Mac OS version scheme
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.macos;

import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.gdb.service.GdbDebugServicesFactory;
import org.eclipse.cdt.dsf.service.DsfSession;

/** @since 2.1 */
public class MacOSGdbDebugServicesFactory extends GdbDebugServicesFactory {

	// Mac OS has it's own version of GDB, which does not follow the standard GDB version
	// We have to be careful not to compare that specific version number scheme with the 
	// FSF GDB version scheme.
	// Use this variable when needing to differentiate between different Mac OS GDBs
	private final String fAppleVersion;

	public MacOSGdbDebugServicesFactory(String gdbVersion, String appleVersion) {
		super(gdbVersion);
		fAppleVersion = appleVersion;
	}

	@Override
	protected IExpressions createExpressionService(DsfSession session) {
		return new MacOSGDBExpressions(session);
	}

	@Override
	protected IRunControl createRunControlService(DsfSession session) {
		return new MacOSGDBRunControl(session);
	}
}
