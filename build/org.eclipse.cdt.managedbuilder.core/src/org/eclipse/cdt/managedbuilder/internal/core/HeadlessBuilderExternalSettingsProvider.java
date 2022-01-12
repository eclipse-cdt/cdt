/*******************************************************************************
 * Copyright (c) 2010 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * James Blackburn (Broadcom Corp.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.extension.CExternalSettingProvider;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * This class allows extending the set of -D's, -I's and -includes that
 * are passed to projects using the external settings provider mechanism.
 */
public class HeadlessBuilderExternalSettingsProvider extends CExternalSettingProvider {

	private static final String ID = "org.eclipse.cdt.managedbuilder.core.headlessSettings"; //$NON-NLS-1$

	/** List of external settings which should be appended to build */
	static List<ICSettingEntry> additionalSettings = new ArrayList<>();

	public HeadlessBuilderExternalSettingsProvider() {
	}

	@Override
	public CExternalSetting[] getSettings(IProject project, ICConfigurationDescription cfg) {
		if (additionalSettings.isEmpty())
			return new CExternalSetting[0];
		return new CExternalSetting[] { new CExternalSetting(null, null, null,
				additionalSettings.toArray(new ICSettingEntry[additionalSettings.size()])) };
	}

	/**
	 * Hook the external settings provider if the user has added c settings
	 */
	static void hookExternalSettingsProvider() {
		if (additionalSettings.isEmpty())
			return;
		// hook the external settings providers to all projects
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(project);
			if (desc == null)
				continue;
			for (ICConfigurationDescription cfg : desc.getConfigurations()) {
				List<String> settingIds = new ArrayList<>(Arrays.asList(cfg.getExternalSettingsProviderIds()));
				settingIds.add(ID);
				String[] newSettingIds = settingIds.toArray(String[]::new);
				cfg.setExternalSettingsProviderIds(newSettingIds);
			}
			try {
				CoreModel.getDefault().setProjectDescription(project, desc);
			} catch (CoreException e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}
	}

	/**
	 * Unhook the external settings provider if the user has added c settings
	 */
	static void unhookExternalSettingsProvider() {
		if (additionalSettings.isEmpty())
			return;

		// Remove the external settings providers from all the hooked projects
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			ICProjectDescription desc = CCorePlugin.getDefault().getProjectDescription(project);
			if (desc == null)
				continue;
			for (ICConfigurationDescription cfg : desc.getConfigurations()) {
				ArrayList<String> extSettingIds = new ArrayList<>(Arrays.asList(cfg.getExternalSettingsProviderIds()));
				for (Iterator<String> it = extSettingIds.iterator(); it.hasNext();)
					if (ID.equals(it.next()))
						it.remove();
				cfg.setExternalSettingsProviderIds(extSettingIds.toArray(new String[extSettingIds.size()]));
			}
			try {
				CoreModel.getDefault().setProjectDescription(project, desc);
			} catch (CoreException e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}
	}

}
