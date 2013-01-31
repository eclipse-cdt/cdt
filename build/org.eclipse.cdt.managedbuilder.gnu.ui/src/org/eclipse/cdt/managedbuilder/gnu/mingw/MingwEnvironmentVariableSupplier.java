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
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$

	private static String envPathValueCached = null;
	private static String envMingwHomeValueCached = null;
	private static IPath binDir = null;
	private static IPath msysBinDir = null;

	private static class MingwBuildEnvironmentVariable implements IBuildEnvironmentVariable {
		private static final String PATH_SEPARATOR = ";"; //$NON-NLS-1$
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
			return PATH_SEPARATOR;
		}
	}

	/**
	 * @return location of $MINGW_HOME/bin folder on the file-system.
	 *
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 */
	public static IPath getBinDir() {
		locateMingw();
		return binDir;
	}

	/**
	 * @return location of $MINGW_HOME/msys/bin folder on the file-system.
	 *
	 * If you use this do not cache results to ensure user preferences are accounted for.
	 * Please rely on internal caching.
	 */
	public static IPath getMsysBinDir() {
		locateMingw();
		return msysBinDir;
	}

	/**
	 * Locate MinGW directories. The results are judicially cached so it is reasonably cheap to call.
	 * The reason to call it each time is to check if user changed environment in preferences.
	 */
	private static void locateMingw() {
		IEnvironmentVariable varPath = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable(ENV_PATH, null, true);
		String envPathValue = varPath != null ? varPath.getValue() : null;
		IEnvironmentVariable varMingwHome = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariable("MINGW_HOME", null, true); //$NON-NLS-1$
		String envMingwHomeValue = varMingwHome != null ? varMingwHome.getValue() : null;

		if (CDataUtil.objectsEqual(envPathValue, envPathValueCached) && CDataUtil.objectsEqual(envMingwHomeValue, envMingwHomeValueCached)) {
			return;
		}

		envPathValueCached = envPathValue;
		envMingwHomeValueCached = envMingwHomeValue;

		binDir = locateBinDir();
		msysBinDir = locateMsysBinDir(binDir);
	}

	private static IPath locateBinDir() {
		// Check $MINGW_HOME
		IPath mingwBinDir = new Path(envMingwHomeValueCached + "\\bin"); //$NON-NLS-1$
		if (mingwBinDir.toFile().isDirectory()) {
			return mingwBinDir;
		}

		// Try the mingw directory in the platform install directory
		// CDT distributions like Wascana may distribute MinGW like that
		IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
		mingwBinDir = installPath.append("mingw\\bin"); //$NON-NLS-1$
		if (mingwBinDir.toFile().isDirectory()) {
			return mingwBinDir;
		}

		// Check for MinGW-w64 on Windows 64 bit, see http://mingw-w64.sourceforge.net/
		if (Platform.ARCH_X86_64.equals(Platform.getOSArch())) {
			IPath gcc64Loc = PathUtil.findProgramLocation("x86_64-w64-mingw32-gcc.exe", envPathValueCached); //$NON-NLS-1$
			if (gcc64Loc != null) {
				return gcc64Loc.removeLastSegments(1);
			}
		}

		// Look in PATH values. Look for mingw32-gcc.exe
		// TODO: Since this dir is already in the PATH, why are we adding it here?
		// This is really only to support isToolchainAvail. Must be a better way.
		// AG: Because otherwise the toolchain won't be shown in the list of "supported" toolchains in UI
		// when MinGW installed in custom location even if it is in the PATH
		IPath gccLoc = PathUtil.findProgramLocation("mingw32-gcc.exe", envPathValueCached); //$NON-NLS-1$
		if (gccLoc != null) {
			return gccLoc.removeLastSegments(1);
		}

		// Try the default MinGW install dir
		mingwBinDir = new Path("C:\\MinGW\\bin"); //$NON-NLS-1$
		if (mingwBinDir.toFile().isDirectory()) {
			return mingwBinDir;
		}

		return null;
	}

	private static IPath locateMsysBinDir(IPath binPath) {
		if (binPath != null) {
			// Just look in the install location parent dir
			IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
			IPath msysBinPath = installPath.append("msys\\bin"); //$NON-NLS-1$
			if (msysBinPath.toFile().isDirectory()) {
				return msysBinPath;
			}

			if (envMingwHomeValueCached != null) {
				msysBinPath = new Path(envMingwHomeValueCached + "\\msys\\1.0\\bin"); //$NON-NLS-1$
				if (msysBinPath.toFile().isDirectory()) {
					return msysBinPath;
				}
			}

			// Try the new MinGW msys bin dir
			msysBinPath = new Path("C:\\MinGW\\msys\\1.0\\bin"); //$NON-NLS-1$
			if (msysBinPath.toFile().isDirectory()) {
				return msysBinPath;
			}
		}
		return null;
	}

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration, IEnvironmentVariableProvider provider) {
		if (variableName.equals(ENV_PATH)) {
			locateMingw();
			if (binDir != null) {
				String pathStr = binDir.toOSString();
				if (msysBinDir != null) {
					pathStr += MingwBuildEnvironmentVariable.PATH_SEPARATOR + msysBinDir.toOSString();
				}
				return new MingwBuildEnvironmentVariable(ENV_PATH, pathStr, IBuildEnvironmentVariable.ENVVAR_PREPEND);
			}
		}

		return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration, IEnvironmentVariableProvider provider) {
		IBuildEnvironmentVariable path = getVariable(ENV_PATH, configuration, provider);
		return path != null
			? new IBuildEnvironmentVariable[] { path }
			: new IBuildEnvironmentVariable[0];
	}

}
