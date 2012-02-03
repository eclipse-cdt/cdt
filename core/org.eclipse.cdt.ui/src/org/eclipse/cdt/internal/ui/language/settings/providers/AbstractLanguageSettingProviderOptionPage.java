package org.eclipse.cdt.internal.ui.language.settings.providers;

import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;

public abstract class AbstractLanguageSettingProviderOptionPage extends AbstractCOptionPage {
	protected LanguageSettingsProviderTab providerTab;
	protected String providerId;

	protected void init(LanguageSettingsProviderTab providerTab, String providerId) {
		this.providerTab = providerTab;
		this.providerId = providerId;
	}
}
