/*******************************************************************************
 * Copyright (c) 2010, 2012 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language.settings.providers;

import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.cdt.ui.newui.ICPropertyTab;
import org.eclipse.core.resources.IProject;

/**
 * Property page for language settings providers tabs.
 * The handling of isLanguageSettingsProvidersEnabled is temporary, this control is to be removed.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class LanguageSettingsProvidersPage extends AbstractPage {
	private static boolean isLanguageSettingsProvidersEnabled = false;
	private static IProject project = null;

	@Override
	protected boolean isSingle() {
		return false;
	}

	/**
	 * Check if language settings providers functionality is enabled for the project.
	 * Need this method as another page could be inquiring before this page gets initialized.
	 *
	 * @noreference This method is temporary and not intended to be referenced by clients.
	 */
	public static boolean isLanguageSettingsProvidersEnabled(IProject prj) {
		if (prj != null) {
			if (prj.equals(project)) {
				return isLanguageSettingsProvidersEnabled;
			} else {
				return ScannerDiscoveryLegacySupport.isLanguageSettingsProvidersFunctionalityEnabled(project);
			}
		}
		return false;
	}

	/**
	 * Check if language settings providers functionality is enabled for the current project.
	 *
	 * @noreference This method is temporary and not intended to be referenced by clients.
	 */
	public boolean isLanguageSettingsProvidersEnabled() {
		IProject prj = getProject();
		if (prj != null) {
			if (!prj.equals(project)) {
				project = prj;
				isLanguageSettingsProvidersEnabled = ScannerDiscoveryLegacySupport
						.isLanguageSettingsProvidersFunctionalityEnabled(project);
			}
			return isLanguageSettingsProvidersEnabled;
		}
		return false;
	}

	/**
	 * Enable or disable language settings providers functionality for the current project.
	 * Triggers update of all the property pages.
	 *
	 * Note that this method only sets property for the current editing session.
	 * Use {@link #applyLanguageSettingsProvidersEnabled()} to apply to the project.
	 *
	 * @noreference This method is temporary and not intended to be referenced by clients.
	 */
	public void setLanguageSettingsProvidersEnabled(boolean enable) {
		isLanguageSettingsProvidersEnabled = enable;
		project = getProject();
		forEach(ICPropertyTab.UPDATE, getResDesc());
	}

	/**
	 * Apply enablement of language settings providers functionality to the current project.
	 *
	 * @noreference This method is temporary and not intended to be referenced by clients.
	 */
	public void applyLanguageSettingsProvidersEnabled() {
		ScannerDiscoveryLegacySupport.setLanguageSettingsProvidersFunctionalityEnabled(getProject(),
				isLanguageSettingsProvidersEnabled);
	}

	@Override
	public void dispose() {
		isLanguageSettingsProvidersEnabled = false;
		project = null;
		super.dispose();
	}
}
