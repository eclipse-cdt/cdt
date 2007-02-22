/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.managedbuilder.gnu.mingw;

import java.io.File;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * @author Doug Schaefer
 *
 */
public class MingwEnvironmentVariableSupplier implements
		IConfigurationEnvironmentVariableSupplier {

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
			return ";";
		}
	}
	
	private final IBuildEnvironmentVariable path;
	
	public MingwEnvironmentVariableSupplier() {
		// 1. Try the mingw directory in the platform install directory
		String bin = Platform.getInstallLocation().getURL().getFile().substring(1) + "mingw/bin";
		
		if (!new File(bin).exists()) {
			// 2. Try looking if the mingw installer ran
			bin = WindowsRegistry.getRegistry().getLocalMachineValue(
					"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\MinGW",
					"InstallLocation");
			if (bin != null)
				bin += "\\bin";
			
			if (bin == null || !new File(bin).exists()) {
				// 3. Try the standard location
				bin = "C:/MinGW/bin";
			}
		}

		path = new MingwBuildEnvironmentVariable(
			"PATH",
			bin,
			IBuildEnvironmentVariable.ENVVAR_PREPEND);
	}
	
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		if (variableName.equals(path.getName()))
			return path;
		else
			return null;
	}

	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		return new IBuildEnvironmentVariable[] { path };
	}

}
