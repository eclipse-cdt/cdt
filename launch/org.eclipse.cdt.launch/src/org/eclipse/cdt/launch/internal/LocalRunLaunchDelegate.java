/*******************************************************************************
 * Copyright (c) 2005, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Marc Khouzam (Ericsson) - Modified to only handle Run mode and modernized (Bug 464636)
 *******************************************************************************/
package org.eclipse.cdt.launch.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate2;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

import com.ibm.icu.text.DateFormat;

/**
 * The launch delegate for Run mode.
 */
public class LocalRunLaunchDelegate extends AbstractCLaunchDelegate2
{
	public LocalRunLaunchDelegate() {
		super(false);
	}
	
	@Override
	public void launch( ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor ) throws CoreException {
		// This delegate is only for Run mode
		assert mode.equals(ILaunchManager.RUN_MODE);
		
		if ( monitor == null ) {
			monitor = new NullProgressMonitor();
		}
		if ( mode.equals(ILaunchManager.RUN_MODE ) ) {
			runLocalApplication( config, launch, monitor );
		}
	}

	private void runLocalApplication(ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(LaunchMessages.LocalCDILaunchDelegate_0, 10); 
		if (monitor.isCanceled()) {
			return;
		}
		monitor.worked(1);
		try {
			IPath exePath = CDebugUtils.verifyProgramPath(config);

			File wd = verifyWorkingDirectory(config);
			if (wd == null) {
				wd = new File(System.getProperty("user.home", ".")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			String args = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ""); //$NON-NLS-1$
			if (args.length() != 0) {
				args = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(args);
			}
			
			String[] arguments = CommandLineUtil.argumentsToArray(args);
			ArrayList<String> command = new ArrayList<>(1 + arguments.length);
			command.add(exePath.toOSString());
			command.addAll(Arrays.asList(arguments));
			monitor.worked(2);
			
			String[] commandArray = command.toArray(new String[command.size()]);
			String[] environment = DebugPlugin.getDefault().getLaunchManager().getEnvironment(config);
			Process process = exec(commandArray, environment, wd);
			monitor.worked(6);

			String timestamp = DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
			String processLabel = String.format("%s (%s)", commandArray[0], timestamp); //$NON-NLS-1$
			DebugPlugin.newProcess(launch, process, processLabel);
		} finally {
			monitor.done();
		}		
	}

	/**
	 * Performs a runtime exec on the given command line in the context of the
	 * specified working directory, and returns the resulting process.
	 * 
	 * @param cmdLine
	 *            the command line
	 * @param environ
	 * @param workingDirectory
	 *            the working directory, or <code>null</code>
	 * @return the resulting process or <code>null</code> if the exec is cancelled
	 * @see Runtime
	 * @since 4.7
	 */
	protected Process exec(String[] cmdLine, String[] environ, File workingDirectory) throws CoreException {
		try {
			if (PTY.isSupported()) {
				return ProcessFactory.getFactory().exec(cmdLine, environ, workingDirectory, new PTY());
			} else {
				return ProcessFactory.getFactory().exec(cmdLine, environ, workingDirectory);
			}
		} catch (IOException e) {
			abort(LaunchMessages.LocalCDILaunchDelegate_8, e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR); 
		}
		return null;
	}


	@Override
    public boolean preLaunchCheck(ILaunchConfiguration config, String mode, IProgressMonitor monitor) throws CoreException {
		return super.preLaunchCheck(config, mode, monitor);
	}

	@Override
	protected String getPluginID() {
		return LaunchUIPlugin.getUniqueIdentifier();
	}
}
