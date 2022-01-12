/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.DisassemblyMessages;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

public final class ActionOpenPreferences extends Action {
	private final static String PREF_PAGE_ID = "org.eclipse.cdt.dsf.debug.ui.disassembly.preferencePage"; //$NON-NLS-1$
	private final Shell fShell;

	public ActionOpenPreferences(Shell shell) {
		fShell = shell;
		setText(DisassemblyMessages.Disassembly_action_OpenPreferences_label);
	}

	@Override
	public void run() {
		PreferencesUtil.createPreferenceDialogOn(fShell, PREF_PAGE_ID, new String[] { PREF_PAGE_ID }, null).open();
	}
}
