/*******************************************************************************
 * Copyright (c) 2006, 2007, 2009 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
