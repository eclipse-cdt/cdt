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
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.language.settings.providers;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.internal.ui.language.settings.providers.LanguageSettingsProviderTab;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Abstract class to implement language settings providers Options page.
 *
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class interface is not stable yet as
 * it is not currently clear how it may need to be used in future. Only bare
 * minimum is provided here at this point (CDT 8.1, Juno).
 * There is no guarantee that this API will work or that it will remain the same.
 * Please do not use this API without consulting with the CDT team.
 * </p>
 * @noextend This class is not intended to be subclassed by clients, only internally by CDT.
 *
 * @since 5.4
 */
public abstract class AbstractLanguageSettingProviderOptionPage extends AbstractCOptionPage {
	protected LanguageSettingsProviderTab providerTab;
	protected String providerId;

	/**
	 * Initialize the options page with the owning tab and provider ID.
	 *
	 * @param providerTab - provider tab which owns the options page.
	 * @param providerId - ID of the provider the options page is for.
	 */
	public void init(AbstractCPropertyTab providerTab, String providerId) {
		this.providerTab = (LanguageSettingsProviderTab) providerTab;
		this.providerId = providerId;
	}

	/**
	 * Get provider being displayed on this Options Page.
	 * @return provider.
	 */
	public ILanguageSettingsProvider getProvider() {
		return LanguageSettingsManager.getRawProvider(providerTab.getProvider(providerId));
	}

	/**
	 * Get working copy of the provider to allow its options to be modified.
	 * @return working copy of the provider.
	 */
	public ILanguageSettingsProvider getProviderWorkingCopy() {
		return providerTab.getWorkingCopy(providerId);
	}

	/**
	 * Refresh provider item in the table and update buttons.
	 * This method is intended for use by an Options Page of the provider.
	 *
	 * @param provider - provider item in the table to refresh.
	 */
	public void refreshItem(ILanguageSettingsProvider provider) {
		providerTab.refreshItem(provider);
	}

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		// normally should be handled by LanguageSettingsProviderTab
	}

	@Override
	public void performDefaults() {
		// normally should be handled by LanguageSettingsProviderTab
	}

}
