/*******************************************************************************
 * Copyright (c) 2007, 2020 QNX Software Systems and others.
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

import org.eclipse.cdt.core.envvar.EnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
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

	private static Map<String, IEnvironmentVariable> envvars;
	private static String sdkDir;
	private static String vcDir;

	public WinEnvironmentVariableSupplier() {
		initvars();
	}

	@Override
	public IEnvironmentVariable getVariable(String variableName, IManagedProject project,
			IEnvironmentVariableProvider provider) {
		return envvars.get(variableName);
	}

	@Override
	public IEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		return envvars.get(variableName);
	}

	@Override
	public IEnvironmentVariable[] getVariables(IManagedProject project, IEnvironmentVariableProvider provider) {
		return envvars.values().toArray(new IEnvironmentVariable[envvars.size()]);
	}

	@Override
	public IEnvironmentVariable[] getVariables(IConfiguration configuration, IEnvironmentVariableProvider provider) {
		return envvars.values().toArray(new IEnvironmentVariable[envvars.size()]);
	}

	private static String getSoftwareKey(WindowsRegistry reg, String subkey, String name) {
		String value = reg.getLocalMachineValue("SOFTWARE\\" + subkey, name); //$NON-NLS-1$
		// Visual Studio is a 32 bit application so on Windows 64 the keys will be in Wow6432Node
		if (value == null) {
			value = reg.getLocalMachineValue("SOFTWARE\\Wow6432Node\\" + subkey, name); //$NON-NLS-1$
		}
		return value;
	}

	// Current support is for Windows SDK 8.0 with Visual C++ 11.0
	// or Windows SDK 7.1 with Visual C++ 10.0
	// or Windows SDK 7.0 with Visual C++ 9.0
	private static String getSDKDir() {
		WindowsRegistry reg = WindowsRegistry.getRegistry();
		String sdkDir = getSoftwareKey(reg, "Microsoft\\Microsoft SDKs\\Windows\\v8.0", "InstallationFolder"); //$NON-NLS-1$ //$NON-NLS-2$
		if (sdkDir != null)
			return sdkDir;
		sdkDir = getSoftwareKey(reg, "Microsoft\\Microsoft SDKs\\Windows\\v7.1", "InstallationFolder"); //$NON-NLS-1$ //$NON-NLS-2$
		if (sdkDir != null)
			return sdkDir;
		return getSoftwareKey(reg, "Microsoft SDKs\\Windows\\v7.0", "InstallationFolder"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static String getVCDir() {
		WindowsRegistry reg = WindowsRegistry.getRegistry();
		String vcDir = getSoftwareKey(reg, "Microsoft\\VisualStudio\\SxS\\VC7", "11.0"); //$NON-NLS-1$ //$NON-NLS-2$
		if (vcDir != null)
			return vcDir;
		vcDir = getSoftwareKey(reg, "Microsoft\\VisualStudio\\SxS\\VC7", "10.0"); //$NON-NLS-1$ //$NON-NLS-2$
		if (vcDir != null)
			return vcDir;
		return getSoftwareKey(reg, "Microsoft\\VisualStudio\\SxS\\VC7", "9.0"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static IPath[] getIncludePath() {
		// Include paths
		List<IPath> includePaths = new ArrayList<>();
		if (sdkDir != null) {
			includePaths.add(new Path(sdkDir.concat("Include"))); //$NON-NLS-1$
			includePaths.add(new Path(sdkDir.concat("Include\\gl"))); //$NON-NLS-1$
		}

		if (vcDir != null) {
			includePaths.add(new Path(vcDir.concat("Include"))); //$NON-NLS-1$
		}
		return includePaths.toArray(new IPath[0]);
	}

	private static void addvar(IEnvironmentVariable var) {
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
		addvar(new EnvironmentVariable("INCLUDE", buff.toString(), //$NON-NLS-1$
				IEnvironmentVariable.ENVVAR_PREPEND));

		// LIB
		buff = new StringBuilder();
		if (vcDir != null)
			buff.append(vcDir).append("Lib;"); //$NON-NLS-1$
		if (sdkDir != null) {
			buff.append(sdkDir).append("Lib;"); //$NON-NLS-1$
			buff.append(sdkDir).append("Lib\\win8\\um\\x86;"); //$NON-NLS-1$
		}

		addvar(new EnvironmentVariable("LIB", buff.toString(), IEnvironmentVariable.ENVVAR_PREPEND)); //$NON-NLS-1$

		// PATH
		buff = new StringBuilder();
		if (vcDir != null) {
			buff.append(vcDir).append("..\\Common7\\IDE;"); //$NON-NLS-1$
			buff.append(vcDir).append("..\\Common7\\Tools;"); //$NON-NLS-1$
			buff.append(vcDir).append("Bin;"); //$NON-NLS-1$
			buff.append(vcDir).append("vcpackages;"); //$NON-NLS-1$
		}
		if (sdkDir != null) {
			buff.append(sdkDir).append("Bin;"); //$NON-NLS-1$
		}
		addvar(new EnvironmentVariable("PATH", buff.toString(), IEnvironmentVariable.ENVVAR_PREPEND)); //$NON-NLS-1$
	}

}
