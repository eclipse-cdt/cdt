/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
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
public class MIGDBSetSolibSearchPath extends MIGDBSet {
	public MIGDBSetSolibSearchPath(String miVersion, String[] paths) {
		super(miVersion, paths);
		// Overload the parameter
		String sep = System.getProperty("path.separator", ":"); //$NON-NLS-1$ //$NON-NLS-2$
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < paths.length; i++) {
			if (buffer.length() == 0) {
				buffer.append(paths[i]);
			} else {
				buffer.append(sep).append(paths[i]);
			}
		}
		String[] p = new String [] {"solib-search-path", buffer.toString()}; //$NON-NLS-1$
		setParameters(p);
	}
}
