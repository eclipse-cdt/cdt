/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

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
	@Override
	public String getNewConfigurationName(IConfiguration configuration, String[] usedConfigurationNames) {
		// TODO Auto-generated method stub
		return null;
	}

	public static ConfigurationNameProvider getConfigurationNameProvider() {
		if (configurationNameProvider == null)
			configurationNameProvider = new ConfigurationNameProvider();
		return configurationNameProvider;
	}
}
