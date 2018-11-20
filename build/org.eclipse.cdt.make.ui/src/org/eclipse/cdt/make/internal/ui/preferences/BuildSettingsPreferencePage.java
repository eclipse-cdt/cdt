/*******************************************************************************
 * Copyright (c) 2009 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.preferences;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.newui.AbstractPrefPage;
import org.eclipse.cdt.ui.newui.ICPropertyTab;

/**
 * Preference page for Build Settings.
 *
 */
public class BuildSettingsPreferencePage extends AbstractPrefPage {

	@Override
	protected String getHeader() {
		return MakefilePreferencesMessages.getString("BuildPreferencePage.description"); //$NON-NLS-1$
	}

	/*
	 * All affected settings are stored in preferences. Tabs are responsible for
	 * saving, after OK signal. No need to affect Project Description somehow.
	 */
	@Override
	public boolean performOk() {
		forEach(ICPropertyTab.OK, null);
		return true;
	}

	@Override
	public ICResourceDescription getResDesc() {
		return null;
	}

	@Override
	protected boolean isSingle() {
		return false;
	}
}
