/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.settings.model;

import org.eclipse.core.resources.IProject;

/**
 * Listener for external settings changes
 */
public interface ICExternalSettingsListener {

	/**
	 * Notifies the listener that the configuration with id cfgId has changed in the project
	 * project.
	 * @param project or null indicating all projects should be considered
	 * @param cfgId or null indicating all configurations should be considered
	 * @param event CExternalSettingsChangeEvent
	 */
	void settingsChanged(IProject project, String cfgId, CExternalSettingChangeEvent event);
}
