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
package org.eclipse.cdt.core.settings.model;

public interface ICProjectDescriptionPreferences {
	/**
	 * Configuration relation status specifying that the Settings and Build configurations are
	 * independent of each other, i.e. changing the active configuration will NOT change the settings
	 * configuration used by the core and vie a versa
	 *  
	 * @see #getConfigurationRelations()
	 * @see #setConfigurationRelations(int)
	 */
	public static final int CONFIGS_INDEPENDENT = 1;

	/**
	 * Configuration relation status specifying that the Settings and Build configurations are
	 * settings are linked with each other, i.e. changing the active configuration will change the settings
	 * configuration used by the core and vie a versa
	 *  
	 * @see #getConfigurationRelations()
	 * @see #setConfigurationRelations(int)
	 */
	public static final int CONFIGS_LINK_SETTINGS_AND_ACTIVE = 2;

	/**
	 * returns the CONFIG_xxx status for this project description
	 * 
	 * @see #CONFIGS_INDEPENDENT
	 * @see #CONFIGS_LINK_SETTINGS_AND_ACTIVE
	 * 
	 * @return int
	 */
	int getConfigurationRelations();
	
	/**
	 * sets the configuration relation status. can be an integer value containing the
	 * CONFIG_xxx status
	 * 
	 * @see #CONFIGS_INDEPENDENT
	 * @see #CONFIGS_LINK_SETTINGS_AND_ACTIVE
	 * 
	 * @param status
	 */
	void setConfigurationRelations(int status);
	
	/**
	 * specifies that the default configuration relations should be used
	 * When called for the project description, specifies that the workspace preferences settings
	 * should be used
	 * When called for the workspace preferences sets the default relation value
	 * which is CONFIGS_INDEPENDENT
	 */
	void useDefaultConfigurationRelations();
	
	/**
	 * specifies whether default configuration relations are used
	 */
	boolean isDefaultConfigurationRelations();
}
