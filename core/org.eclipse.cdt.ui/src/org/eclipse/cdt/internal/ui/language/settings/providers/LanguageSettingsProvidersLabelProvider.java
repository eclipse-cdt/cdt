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
package org.eclipse.cdt.internal.ui.language.settings.providers;

import java.net.URL;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for language settings providers.
 *
 */
public class LanguageSettingsProvidersLabelProvider extends LabelProvider {
	private static final String TEST_PLUGIN_ID_PATTERN = "org.eclipse.cdt.*.tests.*"; //$NON-NLS-1$
	private static final String OOPS = "OOPS"; //$NON-NLS-1$

	/**
	 * Returns base image key (for image without overlay).
	 */
	protected String getBaseKey(ILanguageSettingsProvider provider) {
		String imageKey = null;
		// try id-association
		String id = provider.getId();
		URL url = LanguageSettingsProviderAssociationManager.getImageUrl(id);
		// try class-association
		if (url == null) {
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			if (rawProvider != null) {
				url = LanguageSettingsProviderAssociationManager.getImage(rawProvider.getClass());
			}
		}
		if (url != null) {
			imageKey = url.toString();
		}

		if (imageKey == null) {
			if (id.matches(TEST_PLUGIN_ID_PATTERN)) {
				imageKey = CDTSharedImages.IMG_OBJS_CDT_TESTING;
			} else {
				imageKey = CDTSharedImages.IMG_OBJS_EXTENSION;
			}
		}
		return imageKey;
	}

	/**
	 * Returns keys for image overlays. Returning {@code null} is not allowed.
	 */
	protected String[] getOverlayKeys(ILanguageSettingsProvider provider) {
		String[] overlayKeys = new String[5];
		if (provider.getName() == null) {
			overlayKeys[IDecoration.BOTTOM_LEFT] = CDTSharedImages.IMG_OVR_ERROR;
		}
		return overlayKeys;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof ILanguageSettingsProvider) {
			ILanguageSettingsProvider provider = (ILanguageSettingsProvider) element;
			String imageKey = getBaseKey(provider);
			String[] overlayKeys = getOverlayKeys(provider);
			return CDTSharedImages.getImageOverlaid(imageKey, overlayKeys);
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ILanguageSettingsProvider) {
			ILanguageSettingsProvider provider = (ILanguageSettingsProvider) element;
			String name = provider.getName();
			if (name != null) {
				if (LanguageSettingsManager.isWorkspaceProvider(provider)) {
					name = name + Messages.LanguageSettingsProvidersLabelProvider_TextDecorator_Shared;
				}
				return name;
			}
			return NLS.bind(Messages.GeneralMessages_NonAccessibleID, provider.getId());
		}
		return OOPS;
	}

}