/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.linux;

import org.eclipse.cdt.debug.mi.core.command.CLIInfoSharedLibrary;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetNewConsole;
import org.eclipse.cdt.debug.mi.core.command.factories.StandardCommandFactory;

/**
 * Command factory for the standard gdb/mi protocol for Linux.
 */
public class StandardLinuxCommandFactory extends StandardCommandFactory {

	/**
	 * Constructor for StandardLinuxCommandFactory.
	 */
	public StandardLinuxCommandFactory() {
		super();
	}

	/**
	 * Constructor for StandardLinuxCommandFactory.
	 */
	public StandardLinuxCommandFactory( String miVersion ) {
		super( miVersion );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.CommandFactory#createCLIInfoSharedLibrary()
	 */
	@Override
	public CLIInfoSharedLibrary createCLIInfoSharedLibrary() {
		return new LinuxCLIInfoSharedLibrary();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.CommandFactory#createMIGDBSetNewConsole()
	 */
	@Override
	public MIGDBSetNewConsole createMIGDBSetNewConsole() {
		// Suppress "set new-console" - returns error on Linux
		return new MIGDBSetNewConsole( getMIVersion() ) {

			/* (non-Javadoc)
			 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#getOperation()
			 */
			@Override
			public String getOperation() {
				return ""; //$NON-NLS-1$
			}

			/* (non-Javadoc)
			 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#getOptions()
			 */
			@Override
			public String[] getOptions() {
				return new String[0];
			}

			/* (non-Javadoc)
			 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#getParameters()
			 */
			@Override
			public String[] getParameters() {
				return new String[0];
			}			
		};
	}
}
