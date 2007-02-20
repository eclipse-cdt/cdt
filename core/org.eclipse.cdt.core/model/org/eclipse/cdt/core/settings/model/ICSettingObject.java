/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

/**
 * this is the common super-class for all ICProjectDescription model elements 
 *
 */
public interface ICSettingObject extends ICSettingBase{
	/**
	 * 
	 * @return the unique id of this element
	 */
	String getId();
	
	/**
	 * 
	 * @return the name of this element
	 */
	String getName();
	
	/**
	 * 
	 * @return constant representing the setting type
	 * can be one of the following:
	 * {@link ICSettingBase#SETTING_PROJECT}
	 * {@link ICSettingBase#SETTING_CONFIGURATION}
	 * {@link ICSettingBase#SETTING_FOLDER}
	 * {@link ICSettingBase#SETTING_FILE}
	 * {@link ICSettingBase#SETTING_LANGUAGE}
	 * {@link ICSettingBase#SETTING_TARGET_PLATFORM}
	 * {@link ICSettingBase#SETTING_BUILD}
	 */
	int getType();
	
	/**
	 * 
	 * @return true if the given object is valid, false - otherwise
	 * 
	 * the object can be invalid, e.g. in case it was removed
	 */
	boolean isValid();
	
	/**
	 * 
	 * @return the configuration description this object belongs to
	 */
	ICConfigurationDescription getConfiguration();
	
	/**
	 * 
	 * @return the object parent
	 */
	ICSettingContainer getParent();
	
	/**
	 * 
	 * @return true if the object is read-only, false - otherwise
	 */
	boolean isReadOnly();
}
