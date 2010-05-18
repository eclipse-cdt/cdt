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
	 * Notifies the listener that external settings in a particular container have changed.
	 * The CExternalSettingsManager will try to reconcile changes into the project + config
	 * specified by the call-back. If these are null (which they currently always are)
	 * the external settings manager will check all projects and configurations to see
	 * if there are any referencing configs which need reconciling.
	 *
	 * @param project or null indicating all projects should be considered
	 * @param cfgId or null indicating all configurations should be considered
	 * @param event CExternalSettingsChangeEvent
	 */
	void settingsChanged(IProject project, String cfgId, CExternalSettingChangeEvent event);
}
