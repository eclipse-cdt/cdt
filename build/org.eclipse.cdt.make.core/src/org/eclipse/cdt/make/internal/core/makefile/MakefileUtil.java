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

import java.util.ArrayList;
import java.util.List;

/**
 */
public class MakefileUtil {

	private MakefileUtil() {
	}

	public static String[] findPrerequisites(String line) {
		return findTargets(line);
	}
	public static String[] findTargets(String line) {
		List aList = new ArrayList();
		int space;
		// Trim away trailing and prepending spaces.
		line = line.trim();
		while ((space = indexOf(line, ' ')) != -1) {
			aList.add(line.substring(0, space).trim());
			line = line.substring(space + 1).trim();
		}
		// The last target.
		if (line.length() > 0) {
			aList.add(line);
		}
		return (String[]) aList.toArray(new String[0]);
	}

	public static boolean isMacroDefinition(String line) {
		return isMacroDefinition(line.toCharArray());
	}
	public static boolean isMacroDefinition(char[] line) {
		return indexOf(line, '=') != -1;
	}

	public static boolean isTargetRule(String line) {
		return isTargetRule(line.toCharArray());
	}
	public static boolean isTargetRule(char[] line) {
		int colon = indexOf(line, ':');
		if (colon != -1) {
			colon++;
			// Things like := are not targets but :: is
			if (colon < line.length) {
				char c = line[colon];
				if (c == '=') {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean isCommand(String line) {
		return line.length() > 1 && line.startsWith("\t");
	}
	public static boolean isCommand(char[] line) {
		return (line.length > 1 && line[0] == '\t');
	}

	public static boolean isEscapedLine(String line) {
		return (line.endsWith("\\") && !line.endsWith("\\\\"));
	}

	public static boolean isEmptyLine(String line) {
		return isEmptyLine(line.toCharArray());
	}
	public static boolean isEmptyLine(char[] line) {
		boolean empty = true;
		for (int i = 0; i < line.length; i++) {
			if (!isSpace(line[i])) {
				empty = false;
				break;
			}
		}
		return empty;
	}

	public static boolean isInferenceRule(String line) {
		return line.startsWith(".") && line.indexOf(':') != -1;
	}
	public static boolean isInferenceRule(char[] line) {
		boolean period = false;
		boolean colon = false;
		for (int i = 0; i < line.length; i++) {
			if (line[0] == '.') {
				period = true;
			}
			if (line[i] == ':') {
				colon = true;
			}
		}
		return period && colon;
	}

	public static boolean isSpace(char c) {
		return (c == ' '  || c == '\t' || c == '\r' || c == '\n');
	}


	public static int indexOfComment(String line) {
		return indexOfComment(line.toCharArray());
	}
	public static int indexOfComment(char[] line) {
		boolean escaped = false;
		for (int i = 0; i < line.length; i++) {
			if (line[i] == '#' && !escaped) {
				return i;
			}
			escaped = line[i] == '\\';
		}
		return -1;
	} 

	public static int indexOf(String line, char c) {
		return indexOf(line.toCharArray(), c);
	}

	/**
	 * Special indexOf() method that makes sure that what we are searching
	 * is not between parentheses, brackets or quotes
	 */
	public static int indexOf(char[] line, char c) {
		int paren = 0;
		int bracket = 0;
		boolean escaped = false;

		for (int i = 0; i < line.length; i++) {
			if (line[i] == '(' && !escaped) {
				paren++;
			} else if (line[i] == '{' && !escaped) {
				bracket++;
			} else if (line[i] == ')' && !escaped) {
				paren--;
			} else if (line[i] == '}' && !escaped) {
				bracket--;
			} else if (line[i] == c) {
				if (paren == 0 && bracket == 0) {
					return i;
				}
			}
			escaped = line[i] == '\\';
		}
		return -1;
	}

}
