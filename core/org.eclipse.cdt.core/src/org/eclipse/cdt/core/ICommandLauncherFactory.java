/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.core.resources.IProject;

/**
 * @since 6.4
 */
public interface ICommandLauncherFactory {
	
	/**
	 * Get a Command Launcher for a project (based on active configuration)
	 * @param project - IProject to get command launcher for
	 * @return ICommandLauncher
	 */
	public ICommandLauncher getCommandLauncher(IProject project);
	
	/**
	 * Get a Command Launcher for a build configuration descriptor
	 * @param cfgd - ICConfigurationDescription to get command launcher for
	 * @return ICommandLauncher
	 */
	public ICommandLauncher getCommandLauncher(ICConfigurationDescription cfgd);

	/**
	 * Register language setting entries for a project
	 * @param project - IProject used in obtaining language setting entries
	 * @param entries - List of language setting entries
	 */
	public void registerLanguageSettingEntries(IProject project, List<? extends ICLanguageSettingEntry> entries);
	
	/**
	 * Verify language setting entries for a project and change any entries that
	 * have been copied to a local location
	 * @param project - IProject used in obtaining language setting entries
	 * @param entries - List of language setting entries
	 * @return modified List of language setting entries
	 */
	public List<ICLanguageSettingEntry> verifyLanguageSettingEntries(IProject project, List<ICLanguageSettingEntry> entries);

}
