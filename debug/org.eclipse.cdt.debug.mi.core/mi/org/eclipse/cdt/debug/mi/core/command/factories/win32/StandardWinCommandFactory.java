/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
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
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetAutoSolib;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetNewConsole;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetSolibSearchPath;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowSolibSearchPath;
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

	public MIEnvironmentCD createMIEnvironmentCD( String pathdir ) {
		return new WinMIEnvironmentCD( getMIVersion(), pathdir );
	}

	public CLIInfoSharedLibrary createCLIInfoSharedLibrary() {
		return new WinCLIInfoSharedLibrary();
	}

	public MIGDBSetAutoSolib createMIGDBSetAutoSolib( boolean set ) {
		// Suppress "set auto-solib" - returns error on Windows
		return new MIGDBSetAutoSolib( getMIVersion(), true ) {

			public String getOperation() {
				return ""; //$NON-NLS-1$
			}

			public String[] getOptions() {
				return new String[0];
			}

			public String[] getParameters() {
				return new String[0];
			}			
		};
	}

	public MIGDBShowSolibSearchPath createMIGDBShowSolibSearchPath() {
		// Suppress "show solib-search-path" - returns error on Windows
		return new MIGDBShowSolibSearchPath( getMIVersion() ) {

			public String getOperation() {
				return ""; //$NON-NLS-1$
			}

			public String[] getOptions() {
				return new String[0];
			}

			public String[] getParameters() {
				return new String[0];
			}			
		};
	}

	public MIGDBSetSolibSearchPath createMIGDBSetSolibSearchPath( String[] params ) {
		// Suppress "set solib-search-path" - returns error on Windows
		return new MIGDBSetSolibSearchPath( getMIVersion(), params ) {

			public String getOperation() {
				return ""; //$NON-NLS-1$
			}

			public String[] getOptions() {
				return new String[0];
			}

			public String[] getParameters() {
				return new String[0];
			}			
		};
	}
	
	public MIGDBSetNewConsole createMIGDBSetNewConsole() {
		// By default in Windows, turn off new console so that the
		// Ctrl-C's get propogated automatically to the inferior.
		return new MIGDBSetNewConsole(getMIVersion(), "off"); //$NON-NLS-1$
	}
}
