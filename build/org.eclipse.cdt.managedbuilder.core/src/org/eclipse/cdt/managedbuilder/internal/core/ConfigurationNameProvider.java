/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.managedbuilder.internal.core;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IConfigurationNameProvider;

public class ConfigurationNameProvider implements IConfigurationNameProvider {

	private static ConfigurationNameProvider configurationNameProvider;
	
	protected ConfigurationNameProvider() {
		configurationNameProvider = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfigurationNameProvider#getUniqueConfigurationName(org.eclipse.cdt.managedbuilder.core.IConfiguration, java.lang.String[])
	 */
	public String getNewConfigurationName(IConfiguration configuration,
			String[] usedConfigurationNames) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static ConfigurationNameProvider getConfigurationNameProvider() {
		if( configurationNameProvider == null)
			configurationNameProvider = new ConfigurationNameProvider();
		return configurationNameProvider;
	}
}
