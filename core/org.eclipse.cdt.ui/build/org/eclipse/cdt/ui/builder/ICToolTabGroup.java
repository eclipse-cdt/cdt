/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.ui.builder;

import org.eclipse.cdt.core.builder.model.ICBuildConfig;
import org.eclipse.cdt.core.builder.model.ICBuildConfigWorkingCopy;

/**
 * A tool settings configuration tab group is used to edit/view
 * parameters passed to a tool as part of a C/'C++ build step.
 * CToolPoint settings are presented in a dialog with a tab folder.
 * Each tab presents UI elements appropriate for manipulating
 * a set of parameters for a tool.
 * <p>
 * The tab group controls which tabs are displayed for a specific
 * tool, and provides a mechanism for overriding configuration
 * initialization performed by tabs.
 * <p>
 * This interface is intended to be implemented by clients.
 * <p>
 * (Mercilessly modeled on the Eclipse launch mechanism.)
 */
public interface ICToolTabGroup {

	/**
	 * Creates the tabs contained in this tab group. The tabs control's
	 * are not created. This is the first method called in the lifecycle
	 * of a tab group.
	 * 
	 * @param dialog the tool settings dialog this tab group is contained in
	 */
	public void createTabs(ICBuildConfigDialog dialog);

	/**
	 * Returns the tabs contained in this tab group.
	 * 
	 * @return the tabs contained in this tab group
	 */
	public ICToolTab[] getTabs();

	/**
	 * Notifies this tab group that it has been disposed, and disposes
	 * of this group's tabs. Marks the end of this tab group's lifecycle,
	 * allowing this tab group to perform any cleanup required.
	 */
	public void dispose();

	/**
	 * Initializes the given build configuration with default values
	 * for this tab group. This method is called when a new build
	 * configuration is created such that the configuration can be
	 * initialized with meaningful values. This method may be called
	 * before tab controls are created.
	 * 
	 * @param configuration build configuration
	 */
	public void setDefaults(ICBuildConfigWorkingCopy configuration);

	/**
	 * Initializes this group's tab controls with values from the given
	 * build configuration. This method is called when a configuration
	 * is selected to view or edit.
	 * 
	 * @param configuration build configuration
	 */
	public void initializeFrom(ICBuildConfig configuration);

	/**
	 * Copies values from this group's tabs into the given build configuration.
	 * 
	 * @param configuration build configuration
	 */
	public void performApply(ICBuildConfigWorkingCopy configuration);
}
