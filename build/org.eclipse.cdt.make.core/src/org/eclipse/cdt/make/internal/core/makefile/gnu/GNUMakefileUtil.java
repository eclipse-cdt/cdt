/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.makefile.gnu;

import org.eclipse.cdt.make.internal.core.makefile.Util;
import org.eclipse.cdt.make.internal.core.makefile.posix.PosixMakefileUtil;

/**
 * GNUMakefile
 */
public class GNUMakefileUtil extends PosixMakefileUtil {

	public static boolean isInclude(String line) {
		line = line.trim();
		boolean isInclude = line.startsWith(GNUMakefileConstants.DIRECTIVE_INCLUDE) && line.length() > 7 && Character.isWhitespace(line.charAt(7));
		boolean isDashInclude = line.startsWith("-" + GNUMakefileConstants.DIRECTIVE_INCLUDE) && line.length() > 8 && Character.isWhitespace(line.charAt(8)); //$NON-NLS-1$
		boolean isSInclude = line.startsWith("s" + GNUMakefileConstants.DIRECTIVE_INCLUDE) && line.length() > 8 && Character.isWhitespace(line.charAt(8)); //$NON-NLS-1$
		return isInclude || isDashInclude || isSInclude;
	}

	public static boolean isVPath(String line) {
		line = line.trim();
		return line.equals(GNUMakefileConstants.DIRECTIVE_VPATH) || line.startsWith(GNUMakefileConstants.DIRECTIVE_VPATH) && line.length() > 5 && Character.isWhitespace(line.charAt(5));
	}

	public static boolean isExport(String line) {
		line = line.trim();
		return line.equals(GNUMakefileConstants.VARIABLE_EXPORT) || line.startsWith(GNUMakefileConstants.VARIABLE_EXPORT) && line.length() > 6 && Character.isWhitespace(line.charAt(6));
	}

	public static boolean isUnExport(String line) {
		line = line.trim();
		return line.startsWith(GNUMakefileConstants.DIRECTIVE_UNEXPORT) && line.length() > 8 && Character.isWhitespace(line.charAt(8));
	}

	public static boolean isDefine(String line) {
		line = line.trim();
		return line.startsWith(GNUMakefileConstants.VARIABLE_DEFINE) && line.length() > 6 && Character.isWhitespace(line.charAt(6));
	}

	public static boolean isEndef(String line) {
		return line.trim().equals(GNUMakefileConstants.TERMINAL_ENDEF);
	}

	public static boolean isOverride(String line) {
		line = line.trim();
		return line.startsWith(GNUMakefileConstants.VARIABLE_OVERRIDE) && line.length() > 8 && Character.isWhitespace(line.charAt(8));
	}

	public static boolean isIfeq(String line) {
		line = line.trim();
		return line.startsWith(GNUMakefileConstants.CONDITIONAL_IFEQ) && line.length() > 4 && Character.isWhitespace(line.charAt(4));
	}

	public static boolean isIfneq(String line) {
		line = line.trim();
		return line.startsWith(GNUMakefileConstants.CONDITIONAL_IFNEQ) && line.length() > 5 && Character.isWhitespace(line.charAt(5));
	}

	public static boolean isIfdef(String line) {
		line = line.trim();
		return line.startsWith(GNUMakefileConstants.CONDITIONAL_IFDEF) && line.length() > 5 && Character.isWhitespace(line.charAt(5));
	}

	public static boolean isIfndef(String line) {
		line = line.trim();
		return line.startsWith(GNUMakefileConstants.CONDITIONAL_IFNDEF) && line.length() > 6 && Character.isWhitespace(line.charAt(6));
	}

	public static boolean isElse(String line) {
		return line.trim().equals(GNUMakefileConstants.CONDITIONAL_ELSE);
	}

	public static boolean isEndif(String line) {
		return line.trim().equals(GNUMakefileConstants.TERMINAL_ENDIF);
	}

	public static boolean isOverrideDefine(String line) {
		line = line.trim();
		if (line.startsWith(GNUMakefileConstants.VARIABLE_OVERRIDE)) {
			int i = 8;
			while(i < line.length() && Character.isWhitespace(line.charAt(i))) {
				i++;
			}
			if (line.startsWith(GNUMakefileConstants.VARIABLE_DEFINE, i)) {
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

	public static boolean isPhonyRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(GNUMakefileConstants.RULE_PHONY);
		}
		return false;
	}

	public static boolean isIntermediateRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(GNUMakefileConstants.RULE_INTERMEDIATE);
		}
		return false;
	}

	public static boolean isSecondaryRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(GNUMakefileConstants.RULE_SECONDARY);
		}
		return false;
	}

	public static boolean isDeleteOnErrorRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(GNUMakefileConstants.RULE_DELETE_ON_ERROR);
		}
		return false;
	}

	public static boolean isLowResolutionTimeRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(GNUMakefileConstants.RULE_LOW_RESOLUTION_TIME);
		}
		return false;
	}

	public static boolean isExportAllVariablesRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(GNUMakefileConstants.RULE_EXPORT_ALL_VARIABLES);
		}
		return false;
	}

	public static boolean isNotParallelRule(String line) {
		line = line.trim();
		int colon = Util.indexOf(line, ':');
		if (colon > 0) {
			line = line.substring(0, colon).trim();
			return line.equals(GNUMakefileConstants.RULE_NOT_PARALLEL);
		}
		return false;
	}

}
