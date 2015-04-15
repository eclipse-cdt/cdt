/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching; 

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate2;
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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import com.ibm.icu.text.DateFormat;
 
/**
 * The launch delegate for Run mode for the DSF/GDB debugger.
 * @since 4.7
 */
@ThreadSafe
public class LocalLaunchDelegate extends AbstractCLaunchDelegate2
{
	public LocalLaunchDelegate() {
		// We now fully support project-less debugging
		// See bug 343861
		super(false);
	}
	
	@Override
	public void launch( ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor ) throws CoreException {
		org.eclipse.cdt.launch.LaunchUtils.enableActivity("org.eclipse.cdt.debug.dsfgdbActivity", true); //$NON-NLS-1$
		if ( monitor == null ) {
			monitor = new NullProgressMonitor();
		}
		if ( mode.equals(ILaunchManager.RUN_MODE ) ) {
			runLocalApplication( config, launch, monitor );
		}
	}

	private void runLocalApplication(ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(LaunchMessages.getString("LocalRunLaunchDelegate.Launching_Local_C_Application"), 10);  //$NON-NLS-1$
		if (monitor.isCanceled()) {
			return;
		}
		monitor.worked(1);
		try {
			IPath exePath = CDebugUtils.verifyProgramPath(config);
			
			File wd = LaunchUtils.verifyWorkingDirectory(config);
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
			abort(LaunchMessages.getString("LocalRunLaunchDelegate.Error_starting_process"), e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);  //$NON-NLS-1$
		}
		return null;
	}


	@Override
    public boolean preLaunchCheck(ILaunchConfiguration config, String mode, IProgressMonitor monitor) throws CoreException {
    	// Setup default Process Factory
		setDefaultProcessFactory(config);

		return super.preLaunchCheck(config, mode, monitor);
	}

    /**
     * Modify the ILaunchConfiguration to set the DebugPlugin.ATTR_PROCESS_FACTORY_ID attribute,
     * so as to specify the process factory to use.
     * 
     * This attribute should only be set if it is not part of the configuration already, to allow
     * other code to set it to something else.
	 * @since 4.1
	 */
    protected void setDefaultProcessFactory(ILaunchConfiguration config) throws CoreException {
    	// Bug 210366
        if (!config.hasAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID)) {
            ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
            wc.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, 
            		        IGDBLaunchConfigurationConstants.DEBUGGER_ATTR_PROCESS_FACTORY_ID_DEFAULT);
            wc.doSave();
        }
    }

	@Override
	protected String getPluginID() {
		return GdbPlugin.PLUGIN_ID;
	}
}
