/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launch;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Utility methods.
 */
public class LaunchUtils {

	/**
	 * For given launch configuration returns the program arguments as 
	 * an array of individual arguments.
	 */
	public static String[] getProgramArgumentsArray(ILaunchConfiguration config) throws CoreException {
		return parseArguments(getProgramArguments(config));
	}

	/**
	 * Returns the program arguments as a String.
	 */
	public static String getProgramArguments(ILaunchConfiguration config) throws CoreException {
		String args = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String) null);
		if (args != null) {
			args = getStringVariableManager().performStringSubstitution(args);
		}
		return args;
	}

	/**
	 * Convenience method.
	 */
	public static IStringVariableManager getStringVariableManager() {
		return VariablesPlugin.getDefault().getStringVariableManager();
	}

	private static String[] parseArguments(String args) {
		return CommandLineUtil.argumentsToArray(args);
	}
}
