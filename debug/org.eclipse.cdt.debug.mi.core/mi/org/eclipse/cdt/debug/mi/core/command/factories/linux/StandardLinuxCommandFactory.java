/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.debug.mi.core.command.factories.linux;

import org.eclipse.cdt.debug.mi.core.command.CLIInfoSharedLibrary;
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
	public CLIInfoSharedLibrary createCLIInfoSharedLibrary() {
		return new LinuxCLIInfoSharedLibrary();
	}
}
