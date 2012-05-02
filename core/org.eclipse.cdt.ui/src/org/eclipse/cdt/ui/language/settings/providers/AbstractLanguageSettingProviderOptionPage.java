/*******************************************************************************
 * Copyright (c) 2010, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.language.settings.providers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;

import org.eclipse.cdt.internal.ui.language.settings.providers.LanguageSettingsProviderTab;

/**
 * Abstract class to implement language settings providers Options page.
 *
 * @since 5.4
 */
public abstract class AbstractLanguageSettingProviderOptionPage extends AbstractCOptionPage {
	private LanguageSettingsProviderTab providerTab;
	private String providerId;

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
