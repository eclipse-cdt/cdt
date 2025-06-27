/*******************************************************************************
 * Copyright (c) 2025 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Dallaway - Initial implementation (#1175)
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.gnu.macos;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.internal.core.Homebrew;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.internal.envvar.BuildEnvVar;
import org.eclipse.core.runtime.Path;

/**
 * Provides environment variables to support MBS builds using MacOS toolchains
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MacosEnvironmentVariableSupplier implements IConfigurationEnvironmentVariableSupplier {
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		if (Homebrew.ENV_HOMEBREW_HOME.equals(variableName)) {
			IEnvironmentVariable homebrewHomeVar = CCorePlugin.getDefault().getBuildEnvironmentManager()
					.getVariable(Homebrew.ENV_HOMEBREW_HOME, (ICConfigurationDescription) null, false);
			if (homebrewHomeVar == null) { // if HOMEBREW_HOME not already defined
				String homebrewHome = Homebrew.getHomebrewHome();
				if (homebrewHome == null) { // if Homebrew not installed
					homebrewHome = ""; //$NON-NLS-1$
				}
				return new BuildEnvVar(Homebrew.ENV_HOMEBREW_HOME, new Path(homebrewHome).toOSString(),
						IBuildEnvironmentVariable.ENVVAR_REPLACE);
			}
		} else if (variableName.equals(ENV_PATH)) {
			String path = "${" + Homebrew.ENV_HOMEBREW_HOME + "}/bin"; //$NON-NLS-1$ //$NON-NLS-2$
			return new BuildEnvVar(ENV_PATH, path, IBuildEnvironmentVariable.ENVVAR_PREPEND);
		}
		return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		return new IBuildEnvironmentVariable[] { getVariable(Homebrew.ENV_HOMEBREW_HOME, configuration, provider),
				getVariable(ENV_PATH, configuration, provider) };
	}

}
