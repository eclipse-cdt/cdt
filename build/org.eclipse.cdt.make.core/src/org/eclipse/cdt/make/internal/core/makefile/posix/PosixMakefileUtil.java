/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.posix;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.internal.core.makefile.MakeFileConstants;
import org.eclipse.cdt.make.internal.core.makefile.Util;

/**
 */
public class PosixMakefileUtil {

	public static String[] findPrerequisites(String line) {
		return findTargets(line);
	}

	public static String[] findTargets(String line) {
		List aList = new ArrayList();
		int space;
		// Trim away trailing and prepending spaces.
		line = line.trim();
		while ((space = Util.indexOf(line, " \t")) != -1) { //$NON-NLS-1$
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
		return Util.indexOf(line, '=') != -1;
	}

	public static boolean isTargetRule(String line) {
		return Util.indexOf(line, ':') != -1;
	}

	public static boolean isCommand(String line) {
		return line.length() > 1 && line.charAt(0) == '\t';
	}

	public static boolean isEmptyLine(String line) {
		return line.trim().length() == 0;
	}

	public static boolean isInferenceRule(String line) {
		line = line.trim();
		if (line.startsWith(".")) { //$NON-NLS-1$
			int index = Util.indexOf(line, ':');
			if (index > 1) {
				line = line.substring(index + 1).trim();
				if (line.length() == 0 || line.equals(";")) { //$NON-NLS-1$
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isDefaultRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(MakeFileConstants.RULE_DEFAULT);
		}
		return false;
	}

	public static boolean isIgnoreRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(MakeFileConstants.RULE_IGNORE);
		}
		return false;
	}

	public static boolean isPosixRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(MakeFileConstants.RULE_POSIX);
		}
		return false;
	}

	public static boolean isPreciousRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(MakeFileConstants.RULE_PRECIOUS);
		}
		return false;
	}

	public static boolean isSccsGetRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(MakeFileConstants.RULE_SCCS_GET);
		}
		return false;
	}

	public static boolean isSilentRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(MakeFileConstants.RULE_SILENT);
		}
		return false;
	}

	public static boolean isSuffixesRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(MakeFileConstants.RULE_SUFFIXES);
		}
		return false;
	}

	public static boolean isLibraryTarget(String line) {
		char prev = 0;
		int paren = 0;

		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);
			if (ch == '(' && prev != '$' && prev != '\\') {
				paren++;
			} else if (ch == ')' && prev != '\\') {
				if (paren > 0) {
					return true;
				}
			}
			prev = ch;
		}
		return false;
	}

}
