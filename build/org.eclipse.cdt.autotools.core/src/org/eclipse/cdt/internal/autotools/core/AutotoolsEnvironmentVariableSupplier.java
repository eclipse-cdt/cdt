/*******************************************************************************
 * Copyright (c) 2015 Ericson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core;

import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.runtime.Platform;

/**
 * Supplies some default environment variables for the Autotools toolchain. For
 * example, V=1 to enable verbose output necessary for proper GCC output
 * parsing.
 *
 * @noreference This class is not intended to be referenced by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AutotoolsEnvironmentVariableSupplier implements IConfigurationEnvironmentVariableSupplier {

	private static class VerboseEnvironmentVariable extends EnvironmentVariable implements IBuildEnvironmentVariable {
		private static final String VERBOSE_VAR_NAME = "V";
		private static final String VERBOSE_VAR_VALUE = "1";

		private VerboseEnvironmentVariable(String name, String value, int op, String delimiter) {
			super(name, value, op, delimiter);
		}

		private static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return Platform.getOS().equals(Platform.OS_WIN32)
					? name.equalsIgnoreCase(VerboseEnvironmentVariable.VERBOSE_VAR_NAME)
					: name.equals(VerboseEnvironmentVariable.VERBOSE_VAR_NAME);
		}

		private static IBuildEnvironmentVariable create() {
			return new VerboseEnvironmentVariable(VERBOSE_VAR_NAME, VERBOSE_VAR_VALUE,
					IEnvironmentVariable.ENVVAR_PREPEND, null);
		}
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		IBuildEnvironmentVariable path = VerboseEnvironmentVariable.create();
		return new IBuildEnvironmentVariable[] { path };
	}

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		if (VerboseEnvironmentVariable.isVar(variableName)) {
			return VerboseEnvironmentVariable.create();
		} else {
			return null;
		}
	}
}
