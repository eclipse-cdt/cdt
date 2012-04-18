/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.launcher;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Collects the data from the Tests Runner provider plug-in extension points and
 * provides the convenient access to it.
 */
public class TestsRunnerProvidersManager {
	
	/** Tests Runner Plug-ins extension point ID. */
	private static final String TESTS_RUNNER_EXTENSION_POINT_ID = "org.eclipse.cdt.testsrunner.TestsRunner"; //$NON-NLS-1$

	/** Tests Runner Plug-ins information collection. */
	private TestsRunnerProviderInfo[] testsRunnerProviders = null;

	
	/**
	 * Provides access to information about all registered Tests Runner
	 * Plug-ins.
	 * 
	 * @return array of tests runner plug-ins descriptors
	 */
	public TestsRunnerProviderInfo[] getTestsRunnersProviderInfo() {
		if (testsRunnerProviders == null) {
			// Initialize tests runners info
			List<TestsRunnerProviderInfo> testsRunnerProvidersList = new ArrayList<TestsRunnerProviderInfo>();
			for (IConfigurationElement element : Platform.getExtensionRegistry().getConfigurationElementsFor(TESTS_RUNNER_EXTENSION_POINT_ID)) {
				testsRunnerProvidersList.add(new TestsRunnerProviderInfo(element));
			}
			testsRunnerProviders = testsRunnerProvidersList.toArray(new TestsRunnerProviderInfo[testsRunnerProvidersList.size()]);
		}
		return testsRunnerProviders;
	}

	/**
	 * Provides access to information about Tests Runner Plug-in referred in the
	 * specified launch configuration.
	 * 
	 * @return tests runner plug-in descriptor
	 */
	public TestsRunnerProviderInfo getTestsRunnerProviderInfo(ILaunchConfiguration launchConf) throws CoreException {
		String testsRunnerId = launchConf.getAttribute(ITestsLaunchConfigurationConstants.ATTR_TESTS_RUNNER, (String)null);
		return getTestsRunnerProviderInfo(testsRunnerId);
	}
	
	/**
	 * Provides access to information about Tests Runner Plug-in with the
	 * specified ID.
	 * 
	 * @return tests runner plug-in descriptor
	 */
	private TestsRunnerProviderInfo getTestsRunnerProviderInfo(String testsRunnerProviderId) {
		if (testsRunnerProviderId != null) {
			for (TestsRunnerProviderInfo testsRunnerProvider : getTestsRunnersProviderInfo()) {
				if (testsRunnerProvider.getId().equals(testsRunnerProviderId)) {
					return testsRunnerProvider;
				}
			}
		}
		return null;
	}
	
}
