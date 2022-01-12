/*******************************************************************************
 * Copyright (c) 2006, 2012 Red Hat Inc..
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.ui.editors.automake;

public class AutomakefileUtil {

	// Look for Automake conditionals which optionally
	// activate or disable a Makefile rule command.
	public static boolean isAutomakeCommand(String line) {
		return line.matches("^@[a-z_A-Z0-9]+@\\t.*$"); //$NON-NLS-1$
	}

	public static boolean isIfBlock(String line) {
		return line.startsWith("if"); //$NON-NLS-1$
	}

	public static boolean isElseBlock(String line) {
		return line.startsWith("else"); //$NON-NLS-1$
	}

	public static boolean isElseIfBlock(String line) {
		return line.startsWith("else if"); //$NON-NLS-1$
	}

	public static boolean isEndifBlock(String line) {
		return line.startsWith("endif"); //$NON-NLS-1$
	}

	public static boolean isConfigMacro(String line) {
		return line.matches("\\s*@[a-z_A-Z0-9]+@"); //$NON-NLS-1$
	}

}
