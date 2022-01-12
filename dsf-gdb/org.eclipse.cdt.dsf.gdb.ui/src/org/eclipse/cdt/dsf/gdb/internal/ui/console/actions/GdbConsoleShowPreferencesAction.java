/*******************************************************************************
 * Copyright (c) 2016 Ericsson AB and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.console.actions;

import org.eclipse.cdt.dsf.gdb.internal.ui.console.ConsoleMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class GdbConsoleShowPreferencesAction extends Action {
	private final static String PREF_PAGE_ID = "org.eclipse.cdt.dsf.gdb.ui.preferences.console.GdbConsolePreferencePage"; //$NON-NLS-1$
	private final Shell fShell;

	public GdbConsoleShowPreferencesAction(Shell shell) {
		fShell = shell;
		setText(ConsoleMessages.GdbConsolePreferences_name);
	}

	@Override
	public void run() {
		PreferencesUtil.createPreferenceDialogOn(fShell, PREF_PAGE_ID, new String[] { PREF_PAGE_ID }, null).open();
	}
}
