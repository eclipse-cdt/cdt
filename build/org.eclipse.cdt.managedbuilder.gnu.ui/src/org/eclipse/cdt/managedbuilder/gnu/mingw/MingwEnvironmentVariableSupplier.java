/*******************************************************************************
 * Copyright (c) 2006, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer, QNX Software Systems - Initial API and implementation
 *     Andrew Gvozdev                      - Ability to use different MinGW versions in different cfg
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.gnu.mingw;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.MinGW;
import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.envvar.BuildEnvVar;
import org.eclipse.core.runtime.Path;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MingwEnvironmentVariableSupplier implements IConfigurationEnvironmentVariableSupplier {
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$
	private static final String BACKSLASH = java.io.File.separator;
	private static final String PATH_DELIMITER = EnvironmentVariableManager.getDefault().getDefaultDelimiter();

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		if (variableName.equals(MinGW.ENV_MINGW_HOME)) {
			IEnvironmentVariable varMinGWHome = CCorePlugin.getDefault().getBuildEnvironmentManager()
					.getVariable(MinGW.ENV_MINGW_HOME, (ICConfigurationDescription) null, false);
			if (varMinGWHome == null) {
				// Contribute if the variable does not already come from workspace environment
				String minGWHome = MinGW.getMinGWHome();
				if (minGWHome == null) {
					// If the variable is not defined still show it in the environment variables list as a hint to user
					minGWHome = ""; //$NON-NLS-1$
				}
				return new BuildEnvVar(MinGW.ENV_MINGW_HOME, new Path(minGWHome).toOSString(),
						IBuildEnvironmentVariable.ENVVAR_REPLACE);
			}
			return null;

		} else if (variableName.equals(MinGW.ENV_MSYS_HOME)) {
			IEnvironmentVariable varMsysHome = CCorePlugin.getDefault().getBuildEnvironmentManager()
					.getVariable(MinGW.ENV_MSYS_HOME, (ICConfigurationDescription) null, false);
			if (varMsysHome == null) {
				// Contribute if the variable does not already come from workspace environment
				String msysHome = MinGW.getMSysHome();
				if (msysHome == null) {
					// If the variable is not defined still show it in the environment variables list as a hint to user
					msysHome = ""; //$NON-NLS-1$
				}
				return new BuildEnvVar(MinGW.ENV_MSYS_HOME, new Path(msysHome).toOSString(),
						IBuildEnvironmentVariable.ENVVAR_REPLACE);
			}
			return null;

		} else if (variableName.equals(ENV_PATH)) {
			@SuppressWarnings("nls")
			String path = "${" + MinGW.ENV_MINGW_HOME + "}" + BACKSLASH + "bin" + PATH_DELIMITER + "${"
					+ MinGW.ENV_MSYS_HOME + "}" + BACKSLASH + "bin" + PATH_DELIMITER + "${" + MinGW.ENV_MSYS_HOME + "}"
					+ BACKSLASH + "usr" + BACKSLASH + "bin";
			return new BuildEnvVar(ENV_PATH, path, IBuildEnvironmentVariable.ENVVAR_PREPEND);
		}

		return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		return new IBuildEnvironmentVariable[] { getVariable(MinGW.ENV_MINGW_HOME, configuration, provider),
				getVariable(MinGW.ENV_MSYS_HOME, configuration, provider),
				getVariable(ENV_PATH, configuration, provider), };
	}

}
