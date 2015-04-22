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
 *     Marc Khouzam (Ericsson) - Show exit code in console when doing a Run (Bug 463975)
 *******************************************************************************/
package org.eclipse.cdt.launch.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.ICProject;
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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import com.ibm.icu.text.DateFormat;

/**
 * The launch delegate for Run mode.
 */
public class LocalRunLaunchDelegate extends AbstractCLaunchDelegate2
{
	public LocalRunLaunchDelegate() {
		// We support project-less run
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
			IPath exePath = checkBinaryDetails(config);

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
			
			DebugPlugin.newProcess(launch, process, processLabel, createProcessAttributes());
		} finally {
			monitor.done();
		}		
	}

	protected Map<String, String> createProcessAttributes() {
		Map<String, String> attributes = new HashMap<>();
		
		// Specify that the process factory (GdbProcessFactory) should use InferiorRuntimeProcess to wrap
		// the process that we are about to run.
		// Note that GdbProcessFactory is only used for launches created using DSF-GDB not CDI
	    attributes.put("org.eclipse.cdt.dsf.gdb.createProcessType" /* IGdbDebugConstants.PROCESS_TYPE_CREATION_ATTR */, //$NON-NLS-1$
    		    	   "org.eclipse.cdt.dsf.gdb.inferiorProcess" /* IGdbDebugConstants.INFERIOR_PROCESS_CREATION_VALUE */);  //$NON-NLS-1$
	    
	    // Show the exit code of the process in the console title once it has terminated
	    attributes.put("org.eclipse.cdt.dsf.gdb.inferiorExited" /* IGdbDebugConstants.INFERIOR_EXITED_ATTR */,  //$NON-NLS-1$
	    		       "");  //$NON-NLS-1$
		return attributes;
	}

	/**
	 * Method used to check that the project and program are correct.
	 * Can be overridden to avoid checking certain things.
	 */
	protected IPath checkBinaryDetails(final ILaunchConfiguration config) throws CoreException {
		// First verify we are dealing with a proper project.
		ICProject project = verifyCProject(config);
		// Now verify we know the program to run.
		IPath exePath = verifyProgramPath(config, project);
		return exePath;
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
	 */
    protected void setDefaultProcessFactory(ILaunchConfiguration config) throws CoreException {
        if (!config.hasAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID)) {
            ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
            // Use the debug process factory as it provides extra features for the program
            // that is being debugged or in this case run.
            // Effectively, we want to use InferiorRuntimeProcess when doing this Run launch.
            wc.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID,
            				"org.eclipse.cdt.dsf.gdb.GdbProcessFactory"); //$NON-NLS-1$
            wc.doSave();
        }
    }

	@Override
	protected String getPluginID() {
		return LaunchUIPlugin.getUniqueIdentifier();
	}
}
