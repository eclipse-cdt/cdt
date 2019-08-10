/*******************************************************************************
 * Copyright (c) 2006, 2010, 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *     Randy Rohrbach (Wind River Systems, Inc.) - Copied and modified to create the floating point plugin
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.floatingpoint;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class FPRenderingPreferenceAction extends ActionDelegate implements IViewActionDelegate {

	@Override
	public void run(IAction action) {
		IPreferencePage page = new FPRenderingPreferencePage();
		showPreferencePage("org.eclipse.cdt.debug.ui.memory.floatingpoint.FPRenderingPreferencePage", page); //$NON-NLS-1$
	}

	@Override
	public void init(IViewPart view) {
	}

	protected void showPreferencePage(String id, IPreferencePage page) {
		BusyIndicator.showWhile(FPRenderingPlugin.getStandardDisplay(), () -> PreferencesUtil
				.createPreferenceDialogOn(FPRenderingPlugin.getShell(), id, new String[] { id }, null).open());
	}
}
