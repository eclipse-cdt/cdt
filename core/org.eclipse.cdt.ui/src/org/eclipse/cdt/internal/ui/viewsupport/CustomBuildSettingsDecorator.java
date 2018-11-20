/*******************************************************************************
 * Copyright (c) 2013, 2014 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.List;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvidersKeeper;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.ScannerDiscoveryLegacySupport;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

/**
 * Determines if a file or folder got customized build settings and if so decorates with the "wrench" overlay.
 */
public class CustomBuildSettingsDecorator implements ILightweightLabelDecorator {
	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IFile || element instanceof IFolder) {
			IResource rc = (IResource) element;
			ICProjectDescriptionManager projectDescriptionManager = CoreModel.getDefault()
					.getProjectDescriptionManager();
			ICProjectDescription prjDescription = projectDescriptionManager.getProjectDescription(rc.getProject(),
					ICProjectDescriptionManager.GET_IF_LOADDED);
			if (prjDescription != null) {
				ICConfigurationDescription cfgDescription = prjDescription.getDefaultSettingConfiguration();
				if (cfgDescription != null) {
					if (isCustomizedResource(cfgDescription, rc))
						decoration.addOverlay(CPluginImages.DESC_OVR_SETTING);
				}
			}
		}
	}

	private static boolean isCustomizedResource(ICConfigurationDescription cfgDescription, IResource rc) {
		if (ScannerDiscoveryLegacySupport.isLanguageSettingsProvidersFunctionalityEnabled(rc.getProject())
				&& cfgDescription instanceof ILanguageSettingsProvidersKeeper) {
			IContainer parent = rc.getParent();
			List<String> languages = LanguageSettingsManager.getLanguages(rc, cfgDescription);
			for (ILanguageSettingsProvider provider : ((ILanguageSettingsProvidersKeeper) cfgDescription)
					.getLanguageSettingProviders()) {
				for (String languageId : languages) {
					List<ICLanguageSettingEntry> list = provider.getSettingEntries(cfgDescription, rc, languageId);
					if (list != null) {
						List<ICLanguageSettingEntry> listDefault = provider.getSettingEntries(cfgDescription, parent,
								languageId);
						// != is OK here due as the equal lists will have the same reference in WeakHashSet
						if (list != listDefault)
							return true;
					}
				}
			}
		}

		ICResourceDescription rcDescription = cfgDescription.getResourceDescription(rc.getProjectRelativePath(), true);
		return rcDescription != null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// We don't track state changes
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// We don't track state changes
	}
}
