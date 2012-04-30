/*******************************************************************************
 * Copyright (c) 2010, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language.settings.providers;

import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.cdt.ui.newui.ICPropertyTab;

/**
 * Property page for language settings providers tabs.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class LanguageSettingsProvidersPage extends AbstractPage {
	private Boolean isLanguageSettingsProvidersEnabled = null;

	@Override
	protected boolean isSingle() {
		return false;
	}

	/**
	 * Check if language settings providers functionality is enabled for a current project.
	 *
	 * @noreference This method is temporary and not intended to be referenced by clients.
	 */
	public boolean isLanguageSettingsProvidersEnabled() {
		if (isLanguageSettingsProvidersEnabled == null) {
			isLanguageSettingsProvidersEnabled = ScannerDiscoveryLegacySupport.isLanguageSettingsProvidersFunctionalityEnabled(getProject());
		}
		return isLanguageSettingsProvidersEnabled;
	}

	/**
	 * Enable or disable language settings providers functionality for a current project.
	 *
	 * @noreference This method is temporary and not intended to be referenced by clients.
	 */
	public void setLanguageSettingsProvidersEnabled(boolean enable) {
		isLanguageSettingsProvidersEnabled = enable;
		forEach(ICPropertyTab.UPDATE,getResDesc());
	}
}
