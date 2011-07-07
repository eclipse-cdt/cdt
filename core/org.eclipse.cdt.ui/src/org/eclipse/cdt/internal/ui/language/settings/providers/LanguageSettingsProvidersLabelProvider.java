/*******************************************************************************
 * Copyright (c) 2010, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.language.settings.providers;

import java.net.URL;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.ui.CDTSharedImages;


/**
 * Label provider for language settings providers.
 *
 */
class LanguageSettingsProvidersLabelProvider extends LabelProvider {
	private static final String TEST_PLUGIN_ID_PATTERN = "org.eclipse.cdt.*.tests.*"; //$NON-NLS-1$
	private static final String OOPS = "OOPS"; //$NON-NLS-1$

	/**
	 * Returns base image key (for image without overlay).
	 */
	protected String getBaseKey(ILanguageSettingsProvider provider) {
		String imageKey = null;
		// try id-association
		String id = provider.getId();
		URL url = LanguageSettingsProviderAssociation.getImageUrl(id);
		// try class-association
		if (url==null) {
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			if (rawProvider!=null) {
				url = LanguageSettingsProviderAssociation.getImage(rawProvider.getClass());
			}
		}
		if (url!=null) {
			imageKey = url.toString();
		}
		
		if (imageKey==null) {
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
		{ // TODO temporary for debugging
//			final String MBS_LANGUAGE_SETTINGS_PROVIDER = "org.eclipse.cdt.managedbuilder.core.LanguageSettingsProvider";
//			boolean isSpecial = provider.getId().equals(MBS_LANGUAGE_SETTINGS_PROVIDER);
			
			ILanguageSettingsProvider rawProvider = LanguageSettingsManager.getRawProvider(provider);
			if (rawProvider instanceof LanguageSettingsSerializable) {
				if (((LanguageSettingsSerializable)rawProvider).isEmpty()) {
					overlayKeys[IDecoration.BOTTOM_RIGHT] = CDTSharedImages.IMG_OVR_EMPTY;
				}
			}

			if (LanguageSettingsManager.isWorkspaceProvider(provider) /*&& !isSpecial*/) {
				overlayKeys[IDecoration.TOP_LEFT] = CDTSharedImages.IMG_OVR_GLOBAL;
//				overlayKeys[IDecoration.TOP_LEFT] = CDTSharedImages.IMG_OVR_REFERENCE;
//				overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_PARENT;
//				overlayKeys[IDecoration.BOTTOM_RIGHT] = CDTSharedImages.IMG_OVR_LINK;
			} else {
//				overlayKeys[IDecoration.TOP_LEFT] = CDTSharedImages.IMG_OVR_CONFIGURATION;
//				overlayKeys[IDecoration.TOP_LEFT] = CDTSharedImages.IMG_OVR_INDEXED;
//				overlayKeys[IDecoration.TOP_LEFT] = CDTSharedImages.IMG_OVR_CONTEXT;
				
//				overlayKeys[IDecoration.TOP_LEFT] = CDTSharedImages.IMG_OVR_PROJECT;
			}
			
		}
		return overlayKeys;
	}
	
	@Override
	public Image getImage(Object element) {
		if (element instanceof ILanguageSettingsProvider) {
			ILanguageSettingsProvider provider = (ILanguageSettingsProvider)element;
			String imageKey = getBaseKey(provider);
			String[] overlayKeys = getOverlayKeys(provider);
			return CDTSharedImages.getImageOverlaid(imageKey, overlayKeys);
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ILanguageSettingsProvider) {
			String name = ((ILanguageSettingsProvider) element).getName();
			if (name!=null)
				return name;
			String id = ((ILanguageSettingsProvider) element).getId();
			return "[ Not accessible id="+id+" ]";
		}
		return OOPS;
	}
}