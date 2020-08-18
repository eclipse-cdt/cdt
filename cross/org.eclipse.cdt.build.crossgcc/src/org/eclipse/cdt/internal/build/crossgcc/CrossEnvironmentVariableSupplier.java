/*******************************************************************************
 * Copyright (c) 2009, 2015 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Doug Schaefer - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.build.crossgcc;

import java.io.File;

import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;

public class CrossEnvironmentVariableSupplier implements IConfigurationEnvironmentVariableSupplier {
	private static String PATH = "PATH"; //$NON-NLS-1$

	@Override
	public IEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		if (variableName.equals(CrossEnvironmentVariableSupplier.PATH))
			return create(configuration);
		else
			return null;
	}

	@Override
	public IEnvironmentVariable[] getVariables(IConfiguration configuration, IEnvironmentVariableProvider provider) {
		IEnvironmentVariable path = create(configuration);
		return path != null ? new IEnvironmentVariable[] { path } : new IEnvironmentVariable[0];
	}

	private static IEnvironmentVariable create(IConfiguration configuration) {
		IToolChain toolchain = configuration.getToolChain();
		IOption option = toolchain.getOptionBySuperClassId("cdt.managedbuild.option.gnu.cross.path"); //$NON-NLS-1$
		String path = (String) option.getValue();
		File sysroot = new File(path);
		File bin = new File(sysroot, "bin"); //$NON-NLS-1$
		if (bin.isDirectory())
			sysroot = bin;
		return new EnvironmentVariable(PATH, sysroot.getPath(), IEnvironmentVariable.ENVVAR_PREPEND);
	}
}
