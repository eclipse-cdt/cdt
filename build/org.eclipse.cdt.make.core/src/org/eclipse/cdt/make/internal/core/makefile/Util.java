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
package org.eclipse.cdt.make.internal.core.makefile;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class Util {

	private Util() {
	}

	public static String readLine(BufferedReader br) throws IOException {
		boolean done = false;
		StringBuffer buffer = new StringBuffer();
		boolean escaped = false;
		while (!done) {
			String line = br.readLine();
			if (line == null) {
				return null;
			}

			if (escaped) {
				line = line.trim();
			}

			// Eat the spaces at the beginning.
			if (line.length() > 0 && line.charAt(0) == ' ') {
				int i = 1;
				while (i < line.length() && line.charAt(i) == ' ') {
					i++;
				}
				line = line.substring(i);
			}

			if (line.endsWith("\\")) {
				escaped = true;
				int index = line.indexOf('\\');
				if (index > 0) {
					buffer.append(line.substring(0, index));
					buffer.append(' ');
				}
			} else {
				done = true;
				escaped = false;
				buffer.append(line);
			}
		}
		return buffer.toString();
	}

	public static String[] findTargets(String line) {
		List aList = new ArrayList();
		int space;
		while ((space = indexOf(line, ' ')) != -1) {
			aList.add(line.substring(0, space).trim());
			line = line.substring(space + 1).trim();
		}
		if (line.length() > 0) {
			aList.add(line);
		}
		return (String[]) aList.toArray(new String[0]);
	}

	public static String[] findPrerequisites(String line) {
		return findTargets(line);
	}

	public static boolean isMacroDefinition(char[] line) {
		return indexOf(line, '=') != -1;
	}

	public static boolean isRule(char[] line) {
		return indexOf(line, ':') != -1;
	}

	public static int indexOf(String s, char c) {
		return indexOf(s.toCharArray(), c);
	}

	public static int indexOf(char[] line, char c) {
		int level = 0;
		for (int i = 0; i < line.length; i++) {
			if (line[i] == '(' || line[i] == '{') {
				level++;
			} else if (line[i] == ')' || line[i] == '}') {
				level--;
			} else if (line[i] == c) {
				if (level == 0) {
					return i;
				}
			}
		}
		return -1;
	}

}
