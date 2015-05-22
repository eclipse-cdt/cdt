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

import org.eclipse.cdt.arduino.core.ArduinoProjectGenerator;
import org.eclipse.cdt.arduino.core.Board;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.runtime.CoreException;

public class EnvVarSupplier implements IConfigurationEnvironmentVariableSupplier {

	private EnvVar arduinoRoot;
	private EnvVar arduinoHome;
	private EnvVar arduinoLibs;
	private EnvVar path;

	private static final String OUTPUT_DIR = "OUTPUT_DIR"; //$NON-NLS-1$
	private static final String BOARD = "BOARD"; //$NON-NLS-1$

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
		arduinoRoot = new EnvVar();
		arduinoRoot.name = "ARDUINO_ROOT"; //$NON-NLS-1$
		arduinoRoot.value = clean(ArduinoHome.getRootfileDir().getAbsolutePath());

		arduinoHome = new EnvVar();
		arduinoHome.name = "ARDUINO_HOME"; //$NON-NLS-1$
		arduinoHome.value = clean(ArduinoHome.getArduinoDir().getAbsolutePath());

		arduinoLibs = new EnvVar();
		arduinoLibs.name = "ARDUINO_LIBS"; //$NON-NLS-1$
		arduinoLibs.value = clean(ArduinoHome.getArduinoLibsDir().getAbsolutePath());
		File avrPath = new File(ArduinoHome.getArduinoDir(), "hardware/tools/avr/bin"); //$NON-NLS-1$
		String pathStr = avrPath.getAbsolutePath();

		path = new EnvVar();
		path.name = "PATH"; //$NON-NLS-1$
		path.value = pathStr + File.pathSeparator + arduinoRoot.value;
		path.operation = IBuildEnvironmentVariable.ENVVAR_PREPEND;
		path.delimiter = File.pathSeparator;
	}

	private IBuildEnvironmentVariable getOutputDir(IConfiguration configuration) {
		EnvVar outputDir = new EnvVar();
		outputDir.name = OUTPUT_DIR;
		outputDir.value = "build/" + configuration.getName(); //$NON-NLS-1$
		return outputDir;
	}

	private IBuildEnvironmentVariable getBoard(IConfiguration configuration) {
		try {
			Board board = ArduinoProjectGenerator.getBoard(configuration);
			if (board == null)
				return null;

			EnvVar boardVar = new EnvVar();
			boardVar.name = BOARD;
			boardVar.value = board.getId();
			return boardVar;
		} catch (CoreException e) {
			Activator.getPlugin().getLog().log(e.getStatus());
			return null;
		}
	}

	@Override
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		if (variableName.equals(path.name)) {
			return path;
		} else if (variableName.equals(arduinoRoot.name)) {
			return arduinoRoot;
		} else if (variableName.equals(arduinoHome.name)) {
			return arduinoHome;
		} else if (variableName.equals(arduinoLibs.name)) {
			return arduinoLibs;
		} else if (variableName.equals(OUTPUT_DIR)) {
			return getOutputDir(configuration);
		} else if (variableName.equals(BOARD)) {
			return getBoard(configuration);
		}
		return null;
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		List<IBuildEnvironmentVariable> vars = new ArrayList<>();

		vars.add(path);
		vars.add(arduinoRoot);
		vars.add(arduinoHome);
		vars.add(arduinoLibs);

		if (configuration != null) {
			vars.add(getOutputDir(configuration));

			IBuildEnvironmentVariable boardVar = getBoard(configuration);
			if (boardVar != null)
				vars.add(boardVar);
		}

		return vars.toArray(new IBuildEnvironmentVariable[vars.size()]);
	}

}
