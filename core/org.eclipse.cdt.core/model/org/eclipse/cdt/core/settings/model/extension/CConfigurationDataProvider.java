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
package org.eclipse.cdt.core.settings.model.extension;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.IModificationContext;
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
	 * The method can be called in several cases:
	 * 1. When the new configuration is being created based upon the already existing one via 
	 * theICProjectDescription.createConfiguration method call
	 * 2. When the configuration copy (clone) is being created for the copy description

	 * @param des
	 * @param baseDescription
	 * @param baseData
	 * @param clone true indicates that the configuration copy (clone) is being created for 
	 * the copy description.
	 * @param monitor
	 * @return {@code false} indicates that the new configuration is being created based upon the already existing one via 
	 * theICProjectDescription.createConfiguration method call, {@code true} otherwise
	 * @throws CoreException
	 */
	public abstract CConfigurationData createConfiguration(ICConfigurationDescription des, 
			ICConfigurationDescription baseDescription,
			CConfigurationData baseData, boolean clone,
			IProgressMonitor monitor) throws CoreException;
	
	/**
	 * called to notify the provider that the configuration is removed
	 */
	public abstract void removeConfiguration(ICConfigurationDescription des, CConfigurationData data, IProgressMonitor monitor);

	/**
	 * the method is called in case the implementer does NOT override 
	 * the {@link #applyConfiguration(ICConfigurationDescription, ICConfigurationDescription, CConfigurationData, IModificationContext, IProgressMonitor)}
	 * method. See {@link #applyConfiguration(ICConfigurationDescription, ICConfigurationDescription, CConfigurationData, IModificationContext, IProgressMonitor)}
	 * for detail
	 * 
	 * @throws CoreException
	 */
	public CConfigurationData applyConfiguration(ICConfigurationDescription des, 
			ICConfigurationDescription baseDescription,
			CConfigurationData baseData,
			IProgressMonitor monitor) throws CoreException{
		return baseData;
	}

	/**
	 * called during the setProjectDescription operation to notify the provider that the configuration data
	 * is being applied.
	 * Provider would typically store all the necessary data configuration during this call.
	 * 
	 * @param des
	 * @param baseDescription
	 * @param baseData
	 * @param context the {@link IModificationContext} allows registering workspace runnables to be run
	 * as a single batch workspace operation.
	 * If possible the runnables will be run directly in the apply context(thread) after all
	 * configuration datas get applied. Otherwise runnables will be run as a separate job.  
	 * This allows to perform all workspace modifications registered by different configurations 
	 * to be run as a single batch peration together with the workspace modifications performed by the 
	 * ICProjectDesacription framework
	 * @param monitor
	 * 
	 * @throws CoreException
	 */
	public CConfigurationData applyConfiguration(ICConfigurationDescription des, 
			ICConfigurationDescription baseDescription,
			CConfigurationData baseData,
			IModificationContext context,
			IProgressMonitor monitor) throws CoreException{
		return applyConfiguration(des, baseDescription, baseData, monitor);
	}

	/**
	 * called to notify that the configuration data was cached
	 * implementors can do any necessary cleaning, etc.
	 * Default implementation is empty
	 */
	public void dataCached(ICConfigurationDescription cfgDes, CConfigurationData data, IProgressMonitor monitor){
	}
}
