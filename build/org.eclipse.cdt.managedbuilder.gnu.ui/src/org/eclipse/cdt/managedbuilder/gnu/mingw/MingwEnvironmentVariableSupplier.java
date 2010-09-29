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

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * @author Doug Schaefer
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MingwEnvironmentVariableSupplier implements
		IConfigurationEnvironmentVariableSupplier {
	
	private static boolean checked = false;
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
		
		public String getName() {
			return name;
		}
		
		public String getValue() {
			return value;
		}
		
		public int getOperation() {
			return operation;
		}
		
		public String getDelimiter() {
			return ";"; //$NON-NLS-1$
		}
	}
	
	private IBuildEnvironmentVariable path;
	
	public static IPath getBinDir() {
		if (!checked) 
			findBinDir();
		return binDir;
	}

	private static void findBinDir() {
		// 1. Try the mingw directory in the platform install directory
		// CDT distributions like Wascana may distribute MinGW like that
		IPath subPath = new Path("mingw\\bin"); //$NON-NLS-1$
		IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
		IPath binPathTemp = installPath.append(subPath);
		if (binPathTemp.toFile().isDirectory())
			binDir = binPathTemp;
		
		// 2. Try the directory above the install dir (another possible distribution)
		if (binDir == null) {
			binPathTemp = installPath.removeLastSegments(1).append(subPath);
			if (binPathTemp.toFile().isDirectory()) {
				binDir = binPathTemp;
			}
		}
		
		// 3. Look in PATH values. Look for mingw32-gcc.exe
		if (binDir == null) {
			IPath location = PathUtil.findProgramLocation("mingw32-gcc.exe"); //$NON-NLS-1$
			if (location!=null) {
				binDir = location.removeLastSegments(1);
			}
		}
		
		// 4. Try looking if the mingw installer ran
		if (binDir == null) {
			WindowsRegistry registry = WindowsRegistry.getRegistry();
			if (registry != null) {
				String mingwPath = registry.getLocalMachineValue(
						"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\MinGW", //$NON-NLS-1$
						"InstallLocation"); //$NON-NLS-1$
				if (mingwPath != null) {
					binPathTemp = new Path(mingwPath).append("bin"); //$NON-NLS-1$
					if (binPathTemp.toFile().isDirectory())
						binDir = binPathTemp;
				}
			}
		}
		
		// 5. Try the default MinGW install dir
		if (binDir == null) {
			binPathTemp = new Path("C:\\MinGW\\bin"); //$NON-NLS-1$
			if (binPathTemp.toFile().isDirectory())
				binDir = binPathTemp;
		}
		
		checked = true;
	}
	
	public static IPath getMsysBinDir() {
		// Just look in the install location parent dir
		IPath installPath = new Path(Platform.getInstallLocation().getURL().getFile());
		IPath msysBinPath = installPath.append("msys\\bin"); //$NON-NLS-1$
		return msysBinPath.toFile().isDirectory() ? msysBinPath : null;
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
	
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		if (path != null && variableName.equals(path.getName()))
			return path;
		else
			return null;
	}

	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		return path != null
			? new IBuildEnvironmentVariable[] { path }
			: new IBuildEnvironmentVariable[0];
	}

}
