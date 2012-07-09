/*******************************************************************************
 * Copyright (c) 2006, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.gnu.mingw;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * @author Doug Schaefer
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MingwEnvironmentVariableSupplier implements IConfigurationEnvironmentVariableSupplier {
	private static String envPathValueCached = null;
	private static String envMingwHomeValueCached = null;
	private static IPath binDir = null;

	private static class MingwBuildEnvironmentVariable implements IBuildEnvironmentVariable {
		private final String name;
		private final String value;
		private final int operation;

		public MingwBuildEnvironmentVariable(String name, String value, int operation) {
			this.name = name;
			this.value = value;
			this.operation = operation;
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

		@Override
		public String getDelimiter() {
			return ";"; //$NON-NLS-1$
		}
	}

	private IBuildEnvironmentVariable path;

	/**
	 * @return location of $MINGW_HOME/bin folder on the file-system.
	 *
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 */
	public static IPath getBinDir() {
		IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable("PATH", null, true); //$NON-NLS-1$
		String envPathValue = varPath != null ? varPath.getValue() : null;
		IEnvironmentVariable varMingwHome = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable("MINGW_HOME", null, true); //$NON-NLS-1$
		String envMingwHomeValue = varMingwHome != null ? varMingwHome.getValue() : null;

		if (CDataUtil.objectsEqual(envPathValue, envPathValueCached) && CDataUtil.objectsEqual(envMingwHomeValue, envMingwHomeValueCached)) {
			return binDir;
		}

		envPathValueCached = envPathValue;
		envMingwHomeValueCached = envMingwHomeValue;
		binDir = null;

		// Check $MINGW_HOME
		IPath mingwBinDir = new Path(envMingwHomeValue + "\\bin"); //$NON-NLS-1$
		if (mingwBinDir.toFile().isDirectory()) {
			binDir = mingwBinDir;
			return binDir;
		}

		// Try the mingw directory in the platform install directory
		// CDT distributions like Wascana may distribute MinGW like that
		IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
		mingwBinDir = installPath.append("mingw\\bin"); //$NON-NLS-1$
		if (mingwBinDir.toFile().isDirectory()) {
			binDir = mingwBinDir;
			return binDir;
		}

		// Look in PATH values. Look for mingw32-gcc.exe
		// TODO: Since this dir is already in the PATH, why are we adding it here?
		// This is really only to support isToolchainAvail. Must be a better way.
		// AG: Because otherwise the toolchain won't be shown in the list of "supported" toolchains in UI
		// when MinGW installed in custom location even if it is in the PATH
		IPath gccLoc = PathUtil.findProgramLocation("mingw32-gcc.exe", envPathValue); //$NON-NLS-1$
		if (gccLoc != null) {
			binDir = gccLoc.removeLastSegments(1);
			return binDir;
		}

		// Try the default MinGW install dir
		mingwBinDir = new Path("C:\\MinGW\\bin"); //$NON-NLS-1$
		if (mingwBinDir.toFile().isDirectory()) {
			binDir = mingwBinDir;
			return binDir;
		}

		return binDir;
	}

	public static IPath getMsysBinDir() {
		// Just look in the install location parent dir
		IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
		IPath msysBinPath = installPath.append("msys\\bin"); //$NON-NLS-1$
		if (msysBinPath.toFile().isDirectory())
			return msysBinPath;

		String mingwHome = envMingwHomeValueCached;
		if (mingwHome != null) {
			msysBinPath = new Path(mingwHome + "\\msys\\1.0\\bin"); //$NON-NLS-1$
			if (msysBinPath.toFile().isDirectory())
				return msysBinPath;
		}

		// Try the new MinGW msys bin dir
		msysBinPath = new Path("C:\\MinGW\\msys\\1.0\\bin"); //$NON-NLS-1$
		if (msysBinPath.toFile().isDirectory())
			return msysBinPath;
		return null;
	}

	public MingwEnvironmentVariableSupplier() {
		IPath binPath = getBinDir();
		if (binPath != null) {
			String pathStr = binPath.toOSString();
			IPath msysBinPath = getMsysBinDir();
			if (msysBinPath != null)
				pathStr += ';' + msysBinPath.toOSString();

			path = new MingwBuildEnvironmentVariable("PATH", pathStr, IBuildEnvironmentVariable.ENVVAR_PREPEND); //$NON-NLS-1$
		}
	}

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		if (path != null && variableName.equals(path.getName()))
			return path;
		else
			return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		return path != null
			? new IBuildEnvironmentVariable[] { path }
			: new IBuildEnvironmentVariable[0];
	}

}
