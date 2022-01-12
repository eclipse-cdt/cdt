/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.extension;

import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IProject;

/**
 * Abstract base class for the External Settings Provider extension point.  Contributed
 * external settings are added to the Project's build configuration.
 */
public abstract class CExternalSettingProvider {

	/**
	 * Hook for fetching external settings from the contributed external setting provider
	 * @param project
	 * @param cfg ICConfigurationDescription for which to fetch contributed external settings
	 * @return CExternalSetting[] or contributed external settings
	 */
	public abstract CExternalSetting[] getSettings(IProject project, ICConfigurationDescription cfg);

	/**
	 * Hook for fetching external settings from the contributed external settings provider.
	 * This call-back provides the previous version of the settings as cached by cdt.core
	 *
	 * @param project IProject
	 * @param cfg ICConfigurationDescription for which to fetch contributed external settings
	 * @param previousSettings external settings as cached by cdt.core for this {@link CExternalSettingProvider}
	 *                         or an empty array
	 * @return CExternalSetting[] of contributed external settings
	 * @since 5.2
	 */
	public CExternalSetting[] getSettings(IProject project, ICConfigurationDescription cfg,
			CExternalSetting[] previousSettings) {
		return getSettings(project, cfg);
	}
}
