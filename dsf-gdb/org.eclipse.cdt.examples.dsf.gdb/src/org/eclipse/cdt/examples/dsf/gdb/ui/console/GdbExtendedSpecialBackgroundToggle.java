/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.ui.console;

import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;

/**
 * Action to toggle a special background color for the basic GDB console
 */
public class GdbExtendedSpecialBackgroundToggle extends Action {

	private static final int SPECIAL_BACKGROUND_COLOR = SWT.COLOR_DARK_GREEN;
	private TextConsole fConsole;

	public GdbExtendedSpecialBackgroundToggle(IConsole console) {
		if (console instanceof TextConsole) {
			fConsole = (TextConsole) console;
		} else {
			setEnabled(false);
		}

		setText(GdbExtendedConsoleMessages.Set_Special_Background);
		setToolTipText(GdbExtendedConsoleMessages.Set_Special_Background_Tip);
	}

	@Override
	public void run() {
		int newColor = SPECIAL_BACKGROUND_COLOR;
		Color background = fConsole.getBackground();
		if (background.equals(Display.getDefault().getSystemColor(SPECIAL_BACKGROUND_COLOR))) {
			boolean enabled = Platform.getPreferencesService().getBoolean(GdbPlugin.PLUGIN_ID,
					IGdbDebugPreferenceConstants.PREF_CONSOLE_INVERTED_COLORS, false, null);

			if (enabled) {
				newColor = SWT.COLOR_BLACK;
			} else {
				newColor = SWT.COLOR_WHITE;
			}
		}
		fConsole.setBackground(Display.getDefault().getSystemColor(newColor));
	}
}
