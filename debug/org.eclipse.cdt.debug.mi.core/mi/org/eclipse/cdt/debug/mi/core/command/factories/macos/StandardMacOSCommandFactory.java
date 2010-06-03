/*******************************************************************************
 * Copyright (c) 2006, 2010, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 * Marc-Andre Laperle - patch for bug #250037, 294538
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.macos;

import java.io.File;

import org.eclipse.cdt.debug.mi.core.command.CLIInfoProc;
import org.eclipse.cdt.debug.mi.core.command.CLIInfoThreads;
import org.eclipse.cdt.debug.mi.core.command.CLIPType;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentCD;
import org.eclipse.cdt.debug.mi.core.command.MIExecInterrupt;
import org.eclipse.cdt.debug.mi.core.command.MIInfoSharedLibrary;
import org.eclipse.cdt.debug.mi.core.command.MIVarUpdate;
import org.eclipse.cdt.debug.mi.core.command.factories.StandardCommandFactory;

public class StandardMacOSCommandFactory extends StandardCommandFactory {

	/**
	 * Constructor for StandardMacOSCommandFactory.
	 */
	public StandardMacOSCommandFactory() {
		super();
	}

	public String getWorkingDirectory(File cwd) {
		return "--cd=" + '"' + cwd.getAbsolutePath() + '"'; //$NON-NLS-1$
	}

	/**
	 * Constructor for StandardMacOSCommandFactory.
	 */
	public StandardMacOSCommandFactory( String miVersion ) {
		super( miVersion );
	}

	public MIEnvironmentCD createMIEnvironmentCD(String pathdir) {
		return new MacOSMIEnvironmentCD(getMIVersion(), pathdir);
	}

	public CLIPType createCLIPType(String name) {
		return new MacOSCLIPtype(name);
	}

	public MIInfoSharedLibrary createMIInfoSharedLibrary() {
		return new MIInfoSharedLibrary(getMIVersion());
	}

	public MIVarUpdate createMIVarUpdate() {
		return new MacOSMIVarUpdate(getMIVersion());
	}

	public MIVarUpdate createMIVarUpdate(String name) {
		return new MacOSMIVarUpdate(getMIVersion(), name);
	}
	
	public CLIInfoProc createCLIInfoProc() {
		return new MacOSCLIInfoPID();
	}

	public MIExecInterrupt createMIExecInterrupt() {
		return new MIExecInterrupt(getMIVersion());
	}

	public CLIInfoThreads createCLIInfoThreads() {
		return new MacOSCLIInfoThreads();
	}

}
