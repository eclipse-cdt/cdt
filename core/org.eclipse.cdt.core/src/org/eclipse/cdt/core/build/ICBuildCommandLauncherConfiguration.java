/*******************************************************************************
 * Copyright (c) 2017 Intel, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import java.util.List;

import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;

/**
 * This is an adapter interface for ICBuildConfiguration instances that signals the
 * ability to use ICommandLaunchers in project build.  Nope that the three methods
 * implemented by this interface allow the ICommandLauncherFactory to delegate 
 * all of it's methods to the ICBuildConfiguration instance that implements this adapter.
 * @since 6.3
 * 
 */
public interface ICBuildCommandLauncherConfiguration {

	/**
	 * Note:  registerLanguageSettingEntries and verifyLanguageSettingEntries are here because 
	 * ICommandLauncherFactory has corresponding methods...e.g. 
	 * ICommandLauncherFactory.registerLanguageSettingsEntries(IProject, entries) method.  
	 * see ICommandLauncherFactory.
	 * 
	 * If the core builder
	 * does not need to have these two methods called, then they can/will be removed
	 */
	void registerLanguageSettingEntries(
			List<? extends ICLanguageSettingEntry> entries);

	List<ICLanguageSettingEntry> verifyLanguageSettingEntries(
			List<ICLanguageSettingEntry> entries);

	/**
	 * Get the configured and enabled ICommandLauncher of appropriate type to use for this 
	 * building this project.  
	 * 
	 * @return ICommandLauncher to use for this project.  If <code>null</code> there is no
	 * enabled command launcher.  If non-null, then the given ICommandLauncher should be used
	 * to launch build/clean commands for this project.
	 */
	ICommandLauncher getCommandLauncher();

}
