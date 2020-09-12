/*******************************************************************************
 * Copyright (c) 2007, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.msw.build;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.cdt.internal.msw.build.MSVCToolChainInfo;
import org.eclipse.cdt.internal.msw.build.VSInstallation;
import org.eclipse.cdt.internal.msw.build.VSInstallationRegistry;
import org.eclipse.cdt.internal.msw.build.VSVersionNumber;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.envvar.IProjectEnvironmentVariableSupplier;
import org.eclipse.cdt.utils.envvar.EnvVarOperationProcessor;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author DSchaefer
 *
 */
public class WinEnvironmentVariableSupplier
		implements IConfigurationEnvironmentVariableSupplier, IProjectEnvironmentVariableSupplier {
	private static Map<String, IBuildEnvironmentVariable> envvars;

	private static class WindowsBuildEnvironmentVariable implements IBuildEnvironmentVariable {

		private final String name;
		private final String value;
		private final int operation;

		public WindowsBuildEnvironmentVariable(String name, String value, int operation) {
			this.name = name;
			this.value = value;
			this.operation = operation;
		}

		@Override
		public String getDelimiter() {
			return ";"; //$NON-NLS-1$
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public int getOperation() {
			return operation;
		}

	}

	public WinEnvironmentVariableSupplier() {
		initvars();
	}

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IManagedProject project,
			IEnvironmentVariableProvider provider) {
		return envvars.get(variableName);
	}

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		return envvars.get(variableName);
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IManagedProject project, IEnvironmentVariableProvider provider) {
		return envvars.values().toArray(new IBuildEnvironmentVariable[envvars.size()]);
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		return envvars.values().toArray(new IBuildEnvironmentVariable[envvars.size()]);
	}

	public static IPath[] getIncludePath() {
		// Include paths
		IBuildEnvironmentVariable var = envvars.get("INCLUDE"); //$NON-NLS-1$
		if (var == null)
			return new IPath[0];
		return EnvVarOperationProcessor.convertToList(var.getValue(), var.getDelimiter()).stream()
				.map(val -> Path.fromOSString(val)).collect(Collectors.toList()).toArray(new IPath[0]);
	}

	private static void addvar(IBuildEnvironmentVariable var) {
		envvars.put(var.getName(), var);
	}

	private static synchronized void initvars() {
		if (envvars != null)
			return;
		envvars = new HashMap<>();

		Entry<VSVersionNumber, VSInstallation> vsInstallationEntry = VSInstallationRegistry.getVsInstallations()
				.lastEntry();
		if (vsInstallationEntry != null) {
			List<MSVCToolChainInfo> toolchains = vsInstallationEntry.getValue().getToolchains();
			if (toolchains.size() != 0) {
				//TODO: Support more toolchains/architectures (host and target) when we start giving the choice to the user.
				MSVCToolChainInfo toolChainInfo = toolchains.get(0);
				addvar(new WindowsBuildEnvironmentVariable("INCLUDE", toolChainInfo.getIncludeEnvVar(), //$NON-NLS-1$
						IBuildEnvironmentVariable.ENVVAR_PREPEND));
				addvar(new WindowsBuildEnvironmentVariable("PATH", toolChainInfo.getPathEnvVar(), //$NON-NLS-1$
						IBuildEnvironmentVariable.ENVVAR_PREPEND));
				addvar(new WindowsBuildEnvironmentVariable("LIB", toolChainInfo.getLibEnvVar(), //$NON-NLS-1$
						IBuildEnvironmentVariable.ENVVAR_PREPEND));
			}
		}

	}

}
