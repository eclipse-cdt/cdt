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

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.runtime.Platform;

public class CrossEnvironmentVariableSupplier implements IConfigurationEnvironmentVariableSupplier {

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		if (PathEnvironmentVariable.isVar(variableName))
			return PathEnvironmentVariable.create(configuration);
		else
			return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		IBuildEnvironmentVariable path = PathEnvironmentVariable.create(configuration);
		return path != null ? new IBuildEnvironmentVariable[] { path } : new IBuildEnvironmentVariable[0];
	}

	private static class PathEnvironmentVariable implements IBuildEnvironmentVariable {

		public static String name = "PATH"; //$NON-NLS-1$

		private File path;

		private PathEnvironmentVariable(File path) {
			this.path = path;
		}

		public static PathEnvironmentVariable create(IConfiguration configuration) {
			IToolChain toolchain = configuration.getToolChain();
			IOption option = toolchain.getOptionBySuperClassId("cdt.managedbuild.option.gnu.cross.path"); //$NON-NLS-1$
			String path = (String) option.getValue();
			File sysroot = new File(path);
			File bin = new File(sysroot, "bin"); //$NON-NLS-1$
			if (bin.isDirectory())
				sysroot = bin;
			return new PathEnvironmentVariable(sysroot);
		}

		public static boolean isVar(String name) {
			// Windows has case insensitive env var names
			return Platform.getOS().equals(Platform.OS_WIN32) ? name.equalsIgnoreCase(PathEnvironmentVariable.name)
					: name.equals(PathEnvironmentVariable.name);
		}

		@Override
		public String getDelimiter() {
			return Platform.getOS().equals(Platform.OS_WIN32) ? ";" : ":"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getOperation() {
			return IBuildEnvironmentVariable.ENVVAR_PREPEND;
		}

		@Override
		public String getValue() {
			return path.getPath();
		}

	}
}
