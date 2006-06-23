/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.CommandFactory#createMIGDBSetAutoSolib(boolean)
	 */
	public MIGDBSetAutoSolib createMIGDBSetAutoSolib( boolean set ) {
		// Suppress "set auto-solib" - returns error on Windows
		return new MIGDBSetAutoSolib( getMIVersion(), true ) {

			/* (non-Javadoc)
			 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#getOperation()
			 */
			public String getOperation() {
				return ""; //$NON-NLS-1$
			}

			/* (non-Javadoc)
			 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#getOptions()
			 */
			public String[] getOptions() {
				return new String[0];
			}

			/* (non-Javadoc)
			 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#getParameters()
			 */
			public String[] getParameters() {
				return new String[0];
			}			
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.CommandFactory#createMIGDBShowSolibSearchPath()
	 */
	public MIGDBShowSolibSearchPath createMIGDBShowSolibSearchPath() {
		// Suppress "show solib-search-path" - returns error on Windows
		return new MIGDBShowSolibSearchPath( getMIVersion() ) {

			/* (non-Javadoc)
			 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#getOperation()
			 */
			public String getOperation() {
				return ""; //$NON-NLS-1$
			}

			/* (non-Javadoc)
			 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#getOptions()
			 */
			public String[] getOptions() {
				return new String[0];
			}

			/* (non-Javadoc)
			 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#getParameters()
			 */
			public String[] getParameters() {
				return new String[0];
			}			
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.CommandFactory#createMIGDBSetSolibSearchPath(java.lang.String[])
	 */
	public MIGDBSetSolibSearchPath createMIGDBSetSolibSearchPath( String[] params ) {
		// Suppress "set solib-search-path" - returns error on Windows
		return new MIGDBSetSolibSearchPath( getMIVersion(), params ) {

			/* (non-Javadoc)
			 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#getOperation()
			 */
			public String getOperation() {
				return ""; //$NON-NLS-1$
			}

			/* (non-Javadoc)
			 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#getOptions()
			 */
			public String[] getOptions() {
				return new String[0];
			}

			/* (non-Javadoc)
			 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#getParameters()
			 */
			public String[] getParameters() {
				return new String[0];
			}			
		};
	}
}
