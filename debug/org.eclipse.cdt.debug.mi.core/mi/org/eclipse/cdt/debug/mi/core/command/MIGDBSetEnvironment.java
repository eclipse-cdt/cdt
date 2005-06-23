/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
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
 *      -gdb-set
 *
 *   Set an internal GDB variable.
 * 
 */
public class MIGDBSetEnvironment extends MIGDBSet {

	public MIGDBSetEnvironment(String[] paths) {
		super(paths);
		// Overload the parameter
		String[] newPaths = new String[paths.length + 1];
		newPaths[0] = "environment"; //$NON-NLS-1$
		System.arraycopy(paths, 0, newPaths, 1, paths.length);
		setParameters(newPaths);
	}

	/**
	 * According to the help.:
	 * Set environment variable value to give the program.
	 * Arguments are VAR VALUE where VAR is variable name and VALUE is value.
	 * VALUES of environment variables are uninterpreted strings.
	 * This does not affect the program until the next "run" command.
	 * 
	 * So pass the strings raw without interpretation.
	 */
	protected String parametersToString() {
		StringBuffer buffer = new StringBuffer();
		if (parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				buffer.append(' ').append(parameters[i]);
			}
		}
		return buffer.toString().trim();
	}
	
}
