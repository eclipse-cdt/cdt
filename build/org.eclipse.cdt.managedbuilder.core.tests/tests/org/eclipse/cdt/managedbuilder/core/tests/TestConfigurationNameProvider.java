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
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IConfigurationNameProvider;

public class TestConfigurationNameProvider implements IConfigurationNameProvider {

	private static int configNumber = 0;

	@Override
	public String getNewConfigurationName(IConfiguration configuration, String[] usedConfigurationNames) {

		String configName = configuration.getName();

		if (isArrayContains(usedConfigurationNames, configName) == false)
			return configName;
		else {
			String[] supportedArchList = configuration.getToolChain().getArchList();
			if (supportedArchList.length == 1) {
				String newConfigName = configName + "_" + supportedArchList[0];
				if (isArrayContains(usedConfigurationNames, newConfigName) == false) {
					return newConfigName;
				}
			}

			String[] supportedOsList = configuration.getToolChain().getOSList();
			if (supportedOsList.length == 1) {
				String newConfigName = configName + "_" + supportedOsList[0];
				if (isArrayContains(usedConfigurationNames, newConfigName) == false) {
					return newConfigName;
				}
			}
			configNumber += 1;
			return configName + "_" + configNumber;
		}
	}

	private boolean isArrayContains(String[] usedNames, String name) {
		if (usedNames != null) {
			for (int i = 0; i < usedNames.length; i++) {
				if ((usedNames[i] != null) && (usedNames[i].equals(name))) {
					return true;
				}
			}
		}
		return false;
	}
}
