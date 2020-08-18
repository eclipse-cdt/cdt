/*******************************************************************************
 * Copyright (c) 2015, 2020 Ericson and others.
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
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;

/**
 * Supplies some default environment variables for the Autotools toolchain. For
 * example, V=1 to enable verbose output necessary for proper GCC output
 * parsing.
 *
 * @noreference This class is not intended to be referenced by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AutotoolsEnvironmentVariableSupplier implements IConfigurationEnvironmentVariableSupplier {
	private static final String VERBOSE_VAR_NAME = "V"; //$NON-NLS-1$
	private static final String VERBOSE_VAR_VALUE = "1"; //$NON-NLS-1$

	private static IEnvironmentVariable create() {
		return new EnvironmentVariable(VERBOSE_VAR_NAME, VERBOSE_VAR_VALUE, IEnvironmentVariable.ENVVAR_PREPEND, null);
	}

	@Override
	public IEnvironmentVariable[] getVariables(IConfiguration configuration, IEnvironmentVariableProvider provider) {
		IEnvironmentVariable path = create();
		return new IEnvironmentVariable[] { path };
	}

	@Override
	public IEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		if (variableName.equals(VERBOSE_VAR_NAME)) {
			return create();
		} else {
			return null;
		}
	}
}
