/*******************************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.win32; 

import java.io.IOException;

import org.eclipse.cdt.debug.mi.core.CygwinMIProcessAdapter;
import org.eclipse.cdt.debug.mi.core.MIProcess;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentDirectory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetNewConsole;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Command factory for the gdb/mi protocol for CygWin environment.
 */
public class CygwinCommandFactory extends StandardWinCommandFactory {

	/** 
	 * Constructor for CygwinCommandFactory. 
	 */
	public CygwinCommandFactory() {
		super();
	}

	/** 
	 * Constructor for CygwinCommandFactory. 
	 */
	public CygwinCommandFactory( String miVersion ) {
		super( miVersion );
	}

	public MIEnvironmentDirectory createMIEnvironmentDirectory(boolean reset, String[] pathdirs) {
		return new CygwinMIEnvironmentDirectory( getMIVersion(), reset, pathdirs );
	}
	
	public MIGDBSetNewConsole createMIGDBSetNewConsole() {
		// With cygwin, the Ctrl-C isn't getting propagated to the
		// inferior. Thus we need to have the inferior in it's own
		// console so that the fall back of sending it the interrupt
		// signal works.
		return new MIGDBSetNewConsole(getMIVersion(), "on");
	}
	
	public MIProcess createMIProcess(String[] args, int launchTimeout,
			IProgressMonitor monitor) throws IOException {
		return new CygwinMIProcessAdapter(args, launchTimeout, monitor);
	}
}
