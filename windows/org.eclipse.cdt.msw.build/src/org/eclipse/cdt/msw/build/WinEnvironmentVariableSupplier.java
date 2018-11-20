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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.managedbuilder.envvar.IProjectEnvironmentVariableSupplier;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author DSchaefer
 *
 */
public class WinEnvironmentVariableSupplier
		implements IConfigurationEnvironmentVariableSupplier, IProjectEnvironmentVariableSupplier {

	private static Map<String, IBuildEnvironmentVariable> envvars;
	private static String sdkDir;
	private static String vcDir;

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
			return ";";
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

	private static String getSoftwareKey(WindowsRegistry reg, String subkey, String name) {
		String value = reg.getLocalMachineValue("SOFTWARE\\" + subkey, name);
		// Visual Studio is a 32 bit application so on Windows 64 the keys will be in Wow6432Node
		if (value == null) {
			value = reg.getLocalMachineValue("SOFTWARE\\Wow6432Node\\" + subkey, name);
		}
		return value;
	}

	// Current support is for Windows SDK 8.0 with Visual C++ 11.0
	// or Windows SDK 7.1 with Visual C++ 10.0
	// or Windows SDK 7.0 with Visual C++ 9.0
	private static String getSDKDir() {
		WindowsRegistry reg = WindowsRegistry.getRegistry();
		String sdkDir = getSoftwareKey(reg, "Microsoft\\Microsoft SDKs\\Windows\\v8.0", "InstallationFolder");
		if (sdkDir != null)
			return sdkDir;
		sdkDir = getSoftwareKey(reg, "Microsoft\\Microsoft SDKs\\Windows\\v7.1", "InstallationFolder");
		if (sdkDir != null)
			return sdkDir;
		return getSoftwareKey(reg, "Microsoft SDKs\\Windows\\v7.0", "InstallationFolder");
	}

	private static String getVCDir() {
		WindowsRegistry reg = WindowsRegistry.getRegistry();
		String vcDir = getSoftwareKey(reg, "Microsoft\\VisualStudio\\SxS\\VC7", "11.0");
		if (vcDir != null)
			return vcDir;
		vcDir = getSoftwareKey(reg, "Microsoft\\VisualStudio\\SxS\\VC7", "10.0");
		if (vcDir != null)
			return vcDir;
		return getSoftwareKey(reg, "Microsoft\\VisualStudio\\SxS\\VC7", "9.0");
	}

	public static IPath[] getIncludePath() {
		// Include paths
		List<IPath> includePaths = new ArrayList<>();
		if (sdkDir != null) {
			includePaths.add(new Path(sdkDir.concat("Include")));
			includePaths.add(new Path(sdkDir.concat("Include\\gl")));
		}

		if (vcDir != null) {
			includePaths.add(new Path(vcDir.concat("Include")));
		}
		return includePaths.toArray(new IPath[0]);
	}

	private static void addvar(IBuildEnvironmentVariable var) {
		envvars.put(var.getName(), var);
	}

	private static synchronized void initvars() {
		if (envvars != null)
			return;
		envvars = new HashMap<>();

		// The SDK Location
		sdkDir = getSDKDir();
		vcDir = getVCDir();

		if (sdkDir == null && vcDir == null) {
			return;
		}

		// INCLUDE
		StringBuilder buff = new StringBuilder();
		IPath includePaths[] = getIncludePath();
		for (IPath path : includePaths) {
			buff.append(path.toOSString()).append(';');
		}
		addvar(new WindowsBuildEnvironmentVariable("INCLUDE", buff.toString(),
				IBuildEnvironmentVariable.ENVVAR_PREPEND));

		// LIB
		buff = new StringBuilder();
		if (vcDir != null)
			buff.append(vcDir).append("Lib;");
		if (sdkDir != null) {
			buff.append(sdkDir).append("Lib;");
			buff.append(sdkDir).append("Lib\\win8\\um\\x86;");
		}

		addvar(new WindowsBuildEnvironmentVariable("LIB", buff.toString(), IBuildEnvironmentVariable.ENVVAR_PREPEND));

		// PATH
		buff = new StringBuilder();
		if (vcDir != null) {
			buff.append(vcDir).append("..\\Common7\\IDE;");
			buff.append(vcDir).append("..\\Common7\\Tools;");
			buff.append(vcDir).append("Bin;");
			buff.append(vcDir).append("vcpackages;");
		}
		if (sdkDir != null) {
			buff.append(sdkDir).append("Bin;");
		}
		addvar(new WindowsBuildEnvironmentVariable("PATH", buff.toString(), IBuildEnvironmentVariable.ENVVAR_PREPEND));
	}

}
