/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

/**
 * 
 *      -gdb-set
 *
 *   Set an internal GDB variable.
 * 
 */
public class MIGDBSetSolibSearchPath extends MIGDBSet {
	public MIGDBSetSolibSearchPath(String[] paths) {
		super(paths);
		// Overload the parameter
		String sep = System.getProperty("path.separator", ":");
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < paths.length; i++) {
			if (buffer.length() == 0) {
				buffer.append(paths[i]);
			} else {
				buffer.append(sep).append(paths[i]);
			}
		}
		String[] p = new String [] {"solib-search-path", buffer.toString()};
		setParameters(p);
	}
}
