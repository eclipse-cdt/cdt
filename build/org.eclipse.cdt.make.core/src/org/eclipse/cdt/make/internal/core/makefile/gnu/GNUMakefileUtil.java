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

/**
 * GNUMakefile
 */
public class GNUMakefileUtil {

	public static boolean isIncludeDirective(String line) {
		line = line.trim();
		boolean isInclude = line.startsWith("include") && line.length() > 7 && Character.isWhitespace(line.charAt(7));
		boolean isDashInclude = line.startsWith("-include") && line.length() > 8 && Character.isWhitespace(line.charAt(8));
		boolean isSInclude = line.startsWith("sinclude") && line.length() > 8 && Character.isWhitespace(line.charAt(8));
		return isInclude || isDashInclude || isSInclude;
	}

	public static boolean isVPathDirective(String line) {
		line = line.trim();
		return line.equals("vpath") ||
			line.startsWith("vpath") && line.length() > 5 && Character.isWhitespace(line.charAt(5));
	}

	public static boolean isExport(String line) {
		line = line.trim();
		return line.equals("export") ||
			line.startsWith("export") && line.length() > 6 && Character.isWhitespace(line.charAt(6));
	}

	public static boolean isUnExport(String line) {
		line = line.trim();
		return line.equals("unexport") ||
			line.startsWith("unexport") && line.length() > 8 && Character.isWhitespace(line.charAt(8));
	}

	public static boolean isDefine(String line) {
		line = line.trim();
		return line.equals("define") ||
			line.startsWith("define") && line.length() > 6 && Character.isWhitespace(line.charAt(6));
	}

	public static boolean isEndef(String line) {
		line = line.trim();
		return line.equals("endef") ||
			line.startsWith("endef") && line.length() > 5 && Character.isWhitespace(line.charAt(5));
	}

	public static boolean isOverride(String line) {
		line = line.trim();
		return line.equals("override") ||
			line.startsWith("override") && line.length() > 8 && Character.isWhitespace(line.charAt(8));
	}

	public static boolean isIfeq(String line) {
		line = line.trim();
		return line.equals("ifeq") ||
			line.startsWith("ifeq") && line.length() > 4 && Character.isWhitespace(line.charAt(4));
	}

	public static boolean isIfneq(String line) {
		line = line.trim();
		return line.equals("ifneq") ||
			line.startsWith("ifneq") && line.length() > 5 && Character.isWhitespace(line.charAt(5));
	}

	public static boolean isIfdef(String line) {
		line = line.trim();
		return line.equals("ifdef") ||
			line.startsWith("ifdef") && line.length() > 5 && Character.isWhitespace(line.charAt(5));
	}

	public static boolean isIfndef(String line) {
		line = line.trim();
		return line.equals("ifndef") ||
			line.startsWith("ifndef") && line.length() > 5 && Character.isWhitespace(line.charAt(5));
	}

	public static boolean isElse(String line) {
		line = line.trim();
		return line.equals("else") ||
			line.startsWith("else") && line.length() > 4 && Character.isWhitespace(line.charAt(4));
	}

	public static boolean isEndif(String line) {
		line = line.trim();
		return line.equals("endif") ||
			line.startsWith("endif") && line.length() > 5 && Character.isWhitespace(line.charAt(5));
	}

}
