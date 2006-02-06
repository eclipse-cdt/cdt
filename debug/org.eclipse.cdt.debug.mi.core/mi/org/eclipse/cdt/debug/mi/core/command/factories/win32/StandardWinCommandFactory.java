/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.win32; 

import org.eclipse.cdt.debug.mi.core.command.CLIInfoSharedLibrary;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentCD;
import org.eclipse.cdt.debug.mi.core.command.factories.StandardCommandFactory;
 
/**
 * Command factory for the standard gdb/mi protocol for Windows.
 */
public class StandardWinCommandFactory extends StandardCommandFactory {

	/** 
	 * Constructor for StandardWinCommandFactory. 
	 */
	public StandardWinCommandFactory() {
		super();
	}

	/** 
	 * Constructor for StandardWinCommandFactory. 
	 */
	public StandardWinCommandFactory( String miVersion ) {
		super( miVersion );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.CommandFactory#createMIEnvironmentCD(java.lang.String)
	 */
	public MIEnvironmentCD createMIEnvironmentCD( String pathdir ) {
		return new WinMIEnvironmentCD( getMIVersion(), pathdir );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.CommandFactory#createCLIInfoSharedLibrary()
	 */
	public CLIInfoSharedLibrary createCLIInfoSharedLibrary() {
		return new WinCLIInfoSharedLibrary();
	}
}
