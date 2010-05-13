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

import org.eclipse.cdt.core.settings.model.CExternalSetting;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Root of the External Settings Provider Factory hierarchy. These
 * are responsible for creating {@link CExternalSettingsContainer}s
 * for a given settings provider id.  The container
 * is a simple container for external settings accessible via: 
 * {@link CExternalSettingsContainer#getExternalSettings()}
 *
 * There are two concrete implementation of this in CDT:
 * <ul>
 * <li>{@link CfgExportSettingContainerFactory} responsible for settings propagated
 * referenced configurations.</li>
 * <li>{@link ExtensionContainerFactory} responsible for settings contributed
 * using the external settings extension point.</li>
 * </ul>  
 */
public abstract class CExternalSettingContainerFactory {

	/**
	 * Create a settings container for fetching External Settings from the External
	 * Settings provider with the given id on the specified project and configuration
	 *
	 * @param id of the external settings provider
	 * @param project project to fetch settings for
	 * @param cfgDes configuration to fetch settings for
	 * @param previousSettings settings as previously cached by cdt.core
	 * @return {@link CExternalSettingsContainer}
	 * @throws CoreException
	 */
	public abstract CExternalSettingsContainer createContainer(
			String id,
			IProject project,
			ICConfigurationDescription cfgDes, CExternalSetting[] previousSettings) throws CoreException;

	public void addListener(ICExternalSettingsListener listener){
	}
	
	public void removeListener(ICExternalSettingsListener listener){
	}

	public void startup(){
	}
	
	public void shutdown(){
	}
}
