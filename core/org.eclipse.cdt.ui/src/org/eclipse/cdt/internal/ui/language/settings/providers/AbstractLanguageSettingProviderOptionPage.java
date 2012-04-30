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
package org.eclipse.cdt.internal.ui.language.settings.providers;

import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;

/**
 * Abstract class to implement language settings providers Options page.
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
	protected void init(LanguageSettingsProviderTab providerTab, String providerId) {
		this.providerTab = providerTab;
		this.providerId = providerId;
	}
}
