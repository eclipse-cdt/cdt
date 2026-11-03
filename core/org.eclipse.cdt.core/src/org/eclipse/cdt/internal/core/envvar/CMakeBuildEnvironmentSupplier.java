/*******************************************************************************
 * Copyright (c) 2025 Renesas Electronics Europe and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.envvar;

import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * This is the Environment Variable Supplier used  to supply CMake Tools variables
 * defined through CMake preference
 */
public class CMakeBuildEnvironmentSupplier implements ICoreEnvironmentVariableSupplier {

	public static final String NODENAME = "cmake_environment"; //$NON-NLS-1$
	public static final String ENABLE_USE_CMAKE_LOCATION = "enable_use_cmake_location"; //$NON-NLS-1$
	public static final String CMAKE_LOCATION = "cmake_location"; //$NON-NLS-1$
	public static final String CMAKE_GENERATOR_LOCATION = "cmake_generator_locations"; //$NON-NLS-1$
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$
	public static final String LOCATION_NODE = "location.%d"; //$NON-NLS-1$

	@Override
	public IEnvironmentVariable getVariable(String name, Object context) {
		if (context != null && "PATH".equals(name)) { //$NON-NLS-1$
			return getVariable();
		}
		return null;
	}

	@Override
	public IEnvironmentVariable[] getVariables(Object context) {
		if (context != null) {
			IEnvironmentVariable var = getVariable();
			if (var != null)
				return new IEnvironmentVariable[] { var };
		}
		return new IEnvironmentVariable[0];
	}

	@Override
	public boolean appendEnvironment(Object context) {
		return true;
	}

	private IEnvironmentVariable getVariable() {
		try {
			ArrayList<String> locations = new ArrayList<>();
			boolean useCmakeToolLocation = getPreferences().getBoolean(ENABLE_USE_CMAKE_LOCATION, false);
			if (useCmakeToolLocation) {
				// Get CMake location
				String cmakeLoc = resolveVariableValue(getPreferences().get(CMAKE_LOCATION, EMPTY_STRING));
				if (cmakeLoc != null && !cmakeLoc.isBlank()) {
					locations.add(cmakeLoc);
				}
				// Get CMake Generators' locations
				String[] keys = getPreferences().node(CMakeBuildEnvironmentSupplier.CMAKE_GENERATOR_LOCATION).keys();
				int index;
				for (index = 0; index < keys.length; index++) {
					locations.add(getPreferences().node(CMAKE_GENERATOR_LOCATION)
							.get(String.format(CMakeBuildEnvironmentSupplier.LOCATION_NODE, index), EMPTY_STRING));
				}
				String value = String.join(EnvironmentVariableManager.getDefault().getDefaultDelimiter(), locations);
				return new EnvironmentVariable("PATH", value, IEnvironmentVariable.ENVVAR_PREPEND, //$NON-NLS-1$
						EnvironmentVariableManager.getDefault().getDefaultDelimiter());
			}
		} catch (BackingStoreException e) {
		}
		return null;
	}

	private Preferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node(NODENAME);
	}

	private String resolveVariableValue(String value) {
		try {
			ICdtVariableManager vm = CCorePlugin.getDefault().getCdtVariableManager();
			return vm.resolveValue(value, null, CMakeBuildEnvironmentSupplier.EMPTY_STRING, null);
		} catch (CdtVariableException e) {
		}
		return null;
	}
}
