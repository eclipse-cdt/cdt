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

import org.eclipse.cdt.make.internal.core.makefile.Util;
import org.eclipse.cdt.make.internal.core.makefile.posix.PosixMakefileUtil;

/**
 * GNUMakefile
 */
public class GNUMakefileUtil extends PosixMakefileUtil {

	public static boolean isInclude(String line) {
		line = line.trim();
		boolean isInclude = line.startsWith("include") && line.length() > 7 && Character.isWhitespace(line.charAt(7));
		boolean isDashInclude = line.startsWith("-include") && line.length() > 8 && Character.isWhitespace(line.charAt(8));
		boolean isSInclude = line.startsWith("sinclude") && line.length() > 8 && Character.isWhitespace(line.charAt(8));
		return isInclude || isDashInclude || isSInclude;
	}

	public static boolean isVPath(String line) {
		line = line.trim();
		return line.equals("vpath") || line.startsWith("vpath") && line.length() > 5 && Character.isWhitespace(line.charAt(5));
	}

	public static boolean isExport(String line) {
		line = line.trim();
		return line.equals("export") || line.startsWith("export") && line.length() > 6 && Character.isWhitespace(line.charAt(6));
	}

	public static boolean isUnExport(String line) {
		line = line.trim();
		return line.startsWith("unexport") && line.length() > 8 && Character.isWhitespace(line.charAt(8));
	}

	public static boolean isDefine(String line) {
		line = line.trim();
		return line.startsWith("define") && line.length() > 6 && Character.isWhitespace(line.charAt(6));
	}

	public static boolean isEndef(String line) {
		return line.trim().equals("endef");
	}

	public static boolean isOverride(String line) {
		line = line.trim();
		return line.startsWith("override") && line.length() > 8 && Character.isWhitespace(line.charAt(8));
	}

	public static boolean isIfeq(String line) {
		line = line.trim();
		return line.startsWith("ifeq") && line.length() > 4 && Character.isWhitespace(line.charAt(4));
	}

	public static boolean isIfneq(String line) {
		line = line.trim();
		return line.startsWith("ifneq") && line.length() > 5 && Character.isWhitespace(line.charAt(5));
	}

	public static boolean isIfdef(String line) {
		line = line.trim();
		return line.startsWith("ifdef") && line.length() > 5 && Character.isWhitespace(line.charAt(5));
	}

	public static boolean isIfndef(String line) {
		line = line.trim();
		return line.startsWith("ifndef") && line.length() > 5 && Character.isWhitespace(line.charAt(5));
	}

	public static boolean isElse(String line) {
		return line.trim().equals("else");
	}

	public static boolean isEndif(String line) {
		return line.trim().equals("endif");
	}

	public static boolean isOverrideDefine(String line) {
		line = line.trim();
		if (line.startsWith("override")) {
			int i = 8;
			for (; i < line.length() && Character.isWhitespace(line.charAt(i)); i++);
			if (line.startsWith("define", i)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isTargetVariable(String line) {
		line = line.trim();
		int index = Util.indexOf(line, ':');
		if (index > 1) {
			line = line.substring(index + 1).trim();
			int equal = Util.indexOf(line, '=');
			if (equal > 1) {
				return true;
			}
		}
		return false;
	}

	public static boolean isVariableDefinition(String line) {
		return isOverrideDefine(line)
			|| isTargetVariable(line)
			|| isDefine(line)
			|| isOverride(line)
			|| isExport(line)
			|| isMacroDefinition(line);
	}

	/**
	 * @param line
	 * @return
	 */
	public static boolean isStaticTargetRule(String line) {
		line = line.trim();
		int colon1 = Util.indexOf(line, ':');
		if (colon1 > 0) {
			// move pass colon1
			line = line.substring(colon1 + 1);
			int colon2 =  Util.indexOf(line, ':');
			// Catch operator "::" not a static pattern rule
			return (colon2 > 0);
		}
		return false;
	}

	/**
	 * @param line
	 * @return
	 */
	public static boolean isGNUTargetRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			colon++;
			// Catch VariableDefiniton with operator ":="
			if (colon < line.length()) {
				return line.charAt(colon) != '=';
			}
			return true;
		}
		return false;
	}

}
