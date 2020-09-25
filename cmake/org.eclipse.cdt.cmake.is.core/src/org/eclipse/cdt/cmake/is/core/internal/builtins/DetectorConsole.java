/*******************************************************************************
 * Copyright (c) 2018 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.internal.builtins;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleManager;

/**
 * A console for cmake invocations.
 *
 * @author Martin Weber
 */
public class DetectorConsole extends AbstractConsole {

	private static final String CONSOLE_CONTEXT_MENU_ID = "BuiltinDetectorConsole"; //$NON-NLS-1$

	@Override
	protected IBuildConsoleManager getConsoleManager() {
		return CUIPlugin.getDefault().getConsoleManager(Messages.DetectorConsole_title, CONSOLE_CONTEXT_MENU_ID);
	}

}
