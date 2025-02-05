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
import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

/**
 * This is the Environment Variable Supplier used  to supply CMake Tools variables
 * defined through CMake preference
 */
public class CMakeBuildEnvironmentSupplier implements ICoreEnvironmentVariableSupplier {

	private static final String NODENAME = "cmake_environment"; //$NON-NLS-1$
	private static final String ENABLE_USE_CMAKE_LOCATION = "enable_use_cmake_location"; //$NON-NLS-1$
	private static final String CMAKE_LOCATION = "cmake_location"; //$NON-NLS-1$
	private static final String CMAKE_GENERATOR_LOCATION = "cmake_generator_locations"; //$NON-NLS-1$

	private boolean useCmakeToolLocation;
	private String cmakeLocation;
	private String[] generatorLocations;

	public CMakeBuildEnvironmentSupplier() {
		updateSupplier();
	}

	@Override
	public IEnvironmentVariable getVariable(String name, Object context) {
		updateSupplier();
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
		return null;
	}

	@Override
	public boolean appendEnvironment(Object context) {
		return true;
	}

	private IEnvironmentVariable getVariable() {
		// Need to update supplier in case Preferences are changed
		updateSupplier();
		if (!useCmakeToolLocation) {
			return null;
		}
		ArrayList<String> locations = new ArrayList<>();
		locations.add(cmakeLocation);
		for (String genLoc : generatorLocations) {
			locations.add(genLoc);
		}
		String value = String.join(EnvironmentVariableManager.getDefault().getDefaultDelimiter(), locations);
		return new EnvironmentVariable("PATH", value, IEnvironmentVariable.ENVVAR_APPEND, //$NON-NLS-1$
				EnvironmentVariableManager.getDefault().getDefaultDelimiter());
	}

	private Preferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).node(NODENAME);
	}

	private void updateSupplier() {
		useCmakeToolLocation = getPreferences().getBoolean(ENABLE_USE_CMAKE_LOCATION, false);
		cmakeLocation = getPreferences().get(CMAKE_LOCATION, ""); //$NON-NLS-1$
		String genLocations = getPreferences().get(CMAKE_GENERATOR_LOCATION, ""); //$NON-NLS-1$
		generatorLocations = genLocations.length() > 0 ? genLocations.split(" \\|\\| ") : new String[0]; //$NON-NLS-1$
	}
}
