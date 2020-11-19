/*******************************************************************************
 * Copyright (c) 2020, Simeon Andreev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Simeon Andreev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Set whether to show the build console on activity.
 */
public class BringToTopOnBuild extends Action {

	public BringToTopOnBuild() {
		super(ConsoleMessages.BringToTopOnBuild);
		propertyChange();
		setToolTipText(ConsoleMessages.BringToTopOnBuild);
		setImageDescriptor(CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_OBJS_BRING_CONSOLE_TO_TOP_ON_BUILD));
	}

	@Override
	public void run() {
		super.run();
		boolean isSet = isChecked();
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(BuildConsolePreferencePage.PREF_CONSOLE_ON_TOP, isSet);
	}

	public void propertyChange() {
		boolean isChecked = BuildConsolePreferencePage.isConsoleOnTop();
		setChecked(isChecked);
	}

}
