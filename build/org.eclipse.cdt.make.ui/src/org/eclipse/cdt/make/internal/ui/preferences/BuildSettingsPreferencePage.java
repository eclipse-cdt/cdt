/*******************************************************************************
 * Copyright (c) 2009 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
