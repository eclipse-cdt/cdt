/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.runtime.Platform;

public class EnvVarSupplier implements IConfigurationEnvironmentVariableSupplier {

	private EnvVar arduinoHome;
	private EnvVar arduinoLibs;
	private EnvVar path;

	private static final String OUTPUT_DIR = "OUTPUT_DIR"; //$NON-NLS-1$

	private static final class EnvVar implements IBuildEnvironmentVariable {
		String name;
		String value;
		int operation = IBuildEnvironmentVariable.ENVVAR_REPLACE;
		String delimiter = null;

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
			return delimiter;
		}
	}

	private String clean(String str) {
		return str.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public EnvVarSupplier() {
		arduinoHome = new EnvVar();
		arduinoHome.name = "ARDUINO_HOME"; //$NON-NLS-1$
		arduinoHome.value = clean(ArduinoPreferences.getArduinoHome().toString());

		arduinoLibs = new EnvVar();
		arduinoLibs.name = "ARDUINO_USER_LIBS"; //$NON-NLS-1$
		arduinoLibs.value = clean(System.getProperty("user.home") + "/Documents/Arduino/libraries"); //$NON-NLS-1$ //$NON-NLS-2$

		String avrDir = ArduinoPreferences.getArduinoHome().toString() + "/hardware/tools/avr/bin"; //$NON-NLS-1$
		String installDir = Platform.getInstallLocation().getURL().getPath();
		path = new EnvVar();
		path.name = "PATH"; //$NON-NLS-1$
		path.value = avrDir + File.pathSeparator + installDir;
		path.operation = IBuildEnvironmentVariable.ENVVAR_PREPEND;
		path.delimiter = File.pathSeparator;
	}

	private IBuildEnvironmentVariable getOutputDir(IConfiguration configuration) {
		EnvVar outputDir = new EnvVar();
		outputDir.name = OUTPUT_DIR;
		outputDir.value = "build/" + configuration.getName(); //$NON-NLS-1$
		return outputDir;
	}

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		if (variableName.equals(path.name)) {
			return path;
		} else if (variableName.equals(arduinoHome.name)) {
			return arduinoHome;
		} else if (variableName.equals(arduinoLibs.name)) {
			return arduinoLibs;
		} else if (variableName.equals(OUTPUT_DIR)) {
			return getOutputDir(configuration);
		}
		return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration,
			IEnvironmentVariableProvider provider) {
		List<IBuildEnvironmentVariable> vars = new ArrayList<>();

		vars.add(path);
		vars.add(arduinoHome);
		vars.add(arduinoLibs);

		if (configuration != null) {
			vars.add(getOutputDir(configuration));
		}

		return vars.toArray(new IBuildEnvironmentVariable[vars.size()]);
	}

}
