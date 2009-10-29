/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
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
}
