/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *
 *      -environment-cd PATHDIR
 *
 *   Set GDB's working directory.
 *
 * 
 */
public class MIEnvironmentCD extends MICommand 
{
	public MIEnvironmentCD(String miVersion, String path) {
		super(miVersion, "-environment-cd", new String[]{path}); //$NON-NLS-1$
	}

	/**
	 * !@*^%^$( sigh ... gdb for this command does not make any interpretation
	 * So we must past the command verbatim without any changes. 
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.command.MICommand#parametersToString()
	 */
	protected String parametersToString() {
		String[] parameters = getParameters();
		if (parameters != null && parameters.length == 1) {
			return parameters[0];
		}
		return super.parametersToString();		
	}
}
