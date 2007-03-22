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
package org.eclipse.cdt.core.settings.model.extension;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * the class is to be implemented by the Configuration data provider contributed via
 * a org.eclipse.cdt.core.CConfigurationDataProvider extension point 
 *
 */
public abstract class CConfigurationDataProvider {
	/**
	 * requests the Configuration Data to be loadded for the given ConfigurationDescription
	 */
	public abstract CConfigurationData loadConfiguration(ICConfigurationDescription des, IProgressMonitor monitor) throws CoreException;

	/**
	 * requests the Configuration Data to be created for the given ConfigurationDescription
	 * The method can be called in several caces:
	 * 1. When the new configuration is being created based upon the already existing one via 
	 * theICProjectDescription.createConfiguration method call
	 * 2. When the configuration copy (clone) is being created for the copy description

	 * @param des
	 * @param base
	 * @param clone true indicates that the configuration copy (clone) is being created for 
	 * the copy description.
	 * false indicates that the new configuration is being created based upon the already existing one via 
	 * theICProjectDescription.createConfiguration method call
	 * @return
	 * @throws CoreException
	 */
	public abstract CConfigurationData createConfiguration(ICConfigurationDescription des, 
			ICConfigurationDescription baseDescription,
			CConfigurationData baseData, boolean clone,
			IProgressMonitor monitor) throws CoreException;
	
	/**
	 * called to notify the provider that the configuration is removed
	 * 
	 * @param des
	 * @param data
	 */
	public abstract void removeConfiguration(ICConfigurationDescription des, CConfigurationData data, IProgressMonitor monitor);

	/**
	 * called during the setProjectDescription operation to notify the provider that the configuration data
	 * is being applied.
	 * Provider would typically store all the necessary data configuration during this call.
	 * 
	 * @param des
	 * @param base
	 * @return
	 * @throws CoreException
	 */
	public abstract CConfigurationData applyConfiguration(ICConfigurationDescription des, 
			ICConfigurationDescription baseDescription,
			CConfigurationData baseData,
			IProgressMonitor monitor) throws CoreException;
	
	/**
	 * called to notify that the configuration data was cached
	 * implementors can do any necessary cleaning, etc.
	 * Default implementation is empty
	 * 
	 * @param cfgDes
	 * @param data
	 */
	public void dataCached(ICConfigurationDescription cfgDes, CConfigurationData data, IProgressMonitor monitor){
	}
}
