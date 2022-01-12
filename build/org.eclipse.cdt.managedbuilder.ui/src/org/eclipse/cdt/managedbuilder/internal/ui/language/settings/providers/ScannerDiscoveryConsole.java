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
package org.eclipse.cdt.managedbuilder.internal.ui.language.settings.providers;

import java.net.URL;

import org.eclipse.cdt.internal.ui.buildconsole.CBuildConsole;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.ui.language.settings.providers.LanguageSettingsProvidersImages;

/**
 * Console adapter for {@link AbstractBuiltinSpecsDetector}.
 */
public class ScannerDiscoveryConsole extends CBuildConsole {
	/**
	 * {@inheritDoc}
	 *  @param consoleId - a console ID is expected here which then is used as menu context ID.
	 *  @param defaultIconUrl - if {@code LanguageSettingsProviderAssociation} extension point
	 *     defines URL by provider id, {@code defaultIconUrl} will be ignored and the URL from the extension
	 *     point will be used. If not, supplied {@code defaultIconUrl} will be used.
	 */
	@Override
	public void init(String consoleId, String name, URL defaultIconUrl) {
		URL iconUrl = LanguageSettingsProvidersImages.getImageUrl(consoleId);
		if (iconUrl == null) {
			iconUrl = defaultIconUrl;
		}

		super.init(consoleId, name, iconUrl);
	}
}
