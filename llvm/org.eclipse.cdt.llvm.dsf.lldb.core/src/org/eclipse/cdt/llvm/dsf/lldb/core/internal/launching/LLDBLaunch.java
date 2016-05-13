/*******************************************************************************
 * Copyright (c) 2016 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.launching;

import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.llvm.dsf.lldb.core.ILLDBDebugPreferenceConstants;
import org.eclipse.cdt.llvm.dsf.lldb.core.ILLDBLaunchConfigurationConstants;
import org.eclipse.cdt.llvm.dsf.lldb.core.internal.LLDBCorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;

/**
 * LLDB specific launch. It mostly deals with setting up the paths correctly.
 */
public class LLDBLaunch extends GdbLaunch {

	/**
	 * Constructs a launch.
	 *
	 * @param launchConfiguration
	 *            the launch configuration
	 * @param mode
	 *            the launch mode, i.e., debug, profile, etc.
	 * @param locator
	 */
	public LLDBLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
		super(launchConfiguration, mode, locator);
	}

	/*
	 * TODO: GdbLaunch.getGDBPath() and setGDBPath() should reference each other
	 * in the javadoc to make sure extenders override both.
	 */
	public IPath getGDBPath() {
		String lldbPath = getAttribute(ILLDBLaunchConfigurationConstants.ATTR_DEBUG_NAME);
		if (lldbPath != null) {
			return new Path(lldbPath);
		}

		return getLLDBPath(getLaunchConfiguration());
	}

	public void setGDBPath(String path) {
		setAttribute(ILLDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, path);
	}

	/**
	 * Get the LLDB path based on a launch configuration.
	 *
	 * @param configuration
	 *            the launch configuration.
	 * @return the LLDB path
	 */
	public static IPath getLLDBPath(ILaunchConfiguration configuration) {
		String defaultLLdbCommand = getDefaultLLDBPath();

		IPath retVal = new Path(defaultLLdbCommand);
		try {
			String lldbPath = configuration.getAttribute(ILLDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, defaultLLdbCommand);
			lldbPath = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(lldbPath, false);
			retVal = new Path(lldbPath);
		} catch (CoreException e) {
			LLDBCorePlugin.getDefault().getLog().log(e.getStatus());
		}
		return retVal;
	}

	protected String getDefaultGDBPath() {
		return getDefaultLLDBPath();
	}

	private static String getDefaultLLDBPath() {
		return Platform.getPreferencesService().getString(LLDBCorePlugin.PLUGIN_ID,
				ILLDBDebugPreferenceConstants.PREF_DEFAULT_LLDB_COMMAND,
				ILLDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT, null);
	}

	@Override
	public String getGDBInitFile() throws CoreException {
		// Not supported by LLDB-MI right now. There is also no MI command in
		// GDB to source a file. We should look into adding this in GDB first.
		return null;
	}

	@Override
	public String getProgramPath() throws CoreException {
		IPath path = new Path(super.getProgramPath());

		// FIXME: LLDB-MI only accepts absolute paths for the program. But this
		// seems to work with the latest version in SVN trunk (Mac) so we will
		// need to find out in which version this was or will be fixed and stop
		// doing this work-around if possible.
		if (!path.isAbsolute()) {
			path = getGDBWorkingDirectory().append(path);
		}
		return path.toOSString();
	}
}
