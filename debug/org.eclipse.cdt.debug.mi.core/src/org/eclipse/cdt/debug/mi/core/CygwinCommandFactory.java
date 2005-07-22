/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core;

import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentCD;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentDirectory;

/**
 * Cygwin Command Factory overrides the regular Command Factory to allow for
 * commands to take into account the cygwin environment.
 */
public class CygwinCommandFactory extends CommandFactory {

	public CygwinCommandFactory(String miVersion) {
		super(miVersion);
	}

	public MIEnvironmentDirectory createMIEnvironmentDirectory(boolean reset, String[] pathdirs) {
		return new CygwinMIEnvironmentDirectory(getMIVersion(), reset, pathdirs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.CommandFactory#createMIEnvironmentCD(java.lang.String)
	 */
	public MIEnvironmentCD createMIEnvironmentCD(String pathdir) {
		return new CygwinMIEnvironmentCD(getMIVersion(), pathdir);
	}
}
