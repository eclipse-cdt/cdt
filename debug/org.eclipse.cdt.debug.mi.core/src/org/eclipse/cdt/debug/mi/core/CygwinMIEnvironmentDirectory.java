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

import java.io.ByteArrayOutputStream;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.debug.mi.core.command.MIEnvironmentDirectory;
import org.eclipse.core.runtime.Path;

/**
 * Cygwin implementation of the MIEnvironmentDirectory command.  In the cygwin
 * environment, the paths are DOS paths and need to be converted to cygwin
 * style paths before passing them to gdb.
 */
public class CygwinMIEnvironmentDirectory extends MIEnvironmentDirectory {

	final static private int BUFFER_SIZE = 1000;

	/**
	 * To prevent to call "cygpath" for each folder (see //bugs.eclipse.org/bugs/show_bug.cgi?id=107202)
	 * we use the "-p" option of "cygpath".
	 * We can not convert the whole path in one shot because the size of the spawner's buffer is limited to 2049, 
	 * so we divide the path's folders into groups. 
	 */
	CygwinMIEnvironmentDirectory(String miVersion, boolean reset, String[] paths) {
		super(miVersion, reset, paths);

		String[] newpaths = new String[paths.length];
		int index = 0;
		while(index < paths.length) {
			int length = 0;
			StringBuffer sb = new StringBuffer(BUFFER_SIZE);
			for (int i = index; i < paths.length; i++) {
				if (length + paths[i].length() < BUFFER_SIZE) {
					length += paths[i].length();
					newpaths[i] = paths[i];
					sb.append(paths[i]).append(';');
					++length;
				}
				else {
					convertPath(sb, newpaths, index);
					index = i;
					break;
				}
				if (i == paths.length - 1) {
					convertPath(sb, newpaths, index);
					index = paths.length;
					break;
				}
			}			
		}
		setParameters(newpaths);
	}

	/**
	 * Converts a path to the cygwin path and stores the resulting 
	 * folders into the given array starting from <code>index</code>. 
	 */
	private void convertPath(StringBuffer sb, String[] paths, int index) {
		if (sb.charAt(sb.length() - 1) == ';')
			sb.deleteCharAt(sb.length() - 1);
		String result = convertPath0(sb.toString());
		StringTokenizer st = new StringTokenizer(result, ":"); //$NON-NLS-1$
		int j = index;
		while(st.hasMoreTokens()) {
			if (j >= paths.length)
				break;
			paths[j++] = st.nextToken();
		}
	}

	/**
	 * Converts a windows type path into the cygwin type path using "cygpath" 
	 * with the "-p" option.
	 */
	private String convertPath0(String path) {
		String result = path;
		CommandLauncher launcher = new CommandLauncher();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		launcher.execute(
			new Path("cygpath"), //$NON-NLS-1$
			new String[] { "-p", "-u", path }, //$NON-NLS-1$ //$NON-NLS-2$
			new String[0],
			new Path(".")); //$NON-NLS-1$
		if (launcher.waitAndRead(out, out) == CommandLauncher.OK)
			result = out.toString().trim();
		return result;
	}
}
