/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.cdt.make.internal.core.makefile.Statement;

public class VPath extends Statement {

	String pattern;
	String[] directories;

	public VPath(String line) {
		parse(line);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("vpath");
		if (pattern != null && pattern.length() > 0) {
			sb.append(' ').append(pattern);
		}
		for (int i = 0; i < directories.length; i++) {
			sb.append(' ').append(directories[i]);
		}
		return sb.toString();
	}

	public String[] getDirectories() {
		return directories;
	}

	public String getPattern() {
		return pattern;
	}

	/**
	 * There are three forms of the "vpath" directive:
	 *	"vpath PATTERN DIRECTORIES"
	 * Specify the search path DIRECTORIES for file names that match PATTERN.
	 * 
	 * The search path, DIRECTORIES, is a list of directories to be
	 * searched, separated by colons (semi-colons on MS-DOS and
	 * MS-Windows) or blanks, just like the search path used in the `VPATH' variable.
	 * 
	 *	"vpath PATTERN"
	 * Clear out the search path associated with PATTERN.
	 * 
	 *	"vpath"
	 * Clear all search paths previously specified with `vpath' directives.
	 */
	protected void parse(String line) {
		StringTokenizer st = new StringTokenizer(line);
		int count = st.countTokens();
		List dirs = new ArrayList(count);
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				if (count == 0) {
					// ignore the "vpath" directive
					st.nextToken();
				} else if (count == 1) {
					pattern = st.nextToken();
				} else if (count == 3) {
					String delim =  " \t\n\r\f" + GNUMakefile.PATH_SEPARATOR;
					dirs.add(st.nextToken(delim));
				} else {
					dirs.add(st.nextToken());
				}
			}
		}
		directories = (String[]) dirs.toArray(new String[0]);
		if (pattern == null) {
			pattern = new String();
		}
	}
}
