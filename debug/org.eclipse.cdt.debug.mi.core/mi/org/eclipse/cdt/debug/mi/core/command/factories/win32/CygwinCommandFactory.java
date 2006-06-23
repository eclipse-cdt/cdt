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

import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentDirectory;

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
}
