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
package org.eclipse.dd.tests.gdb.launching; 

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.gdb.internal.provisional.launching.FinalLaunchSequence;
import org.eclipse.dd.gdb.internal.provisional.launching.GdbLaunch;
import org.eclipse.dd.gdb.internal.provisional.launching.LaunchUtils;
import org.eclipse.dd.gdb.internal.provisional.launching.ServicesLaunchSequence;
import org.eclipse.dd.gdb.internal.provisional.service.GdbDebugServicesFactory;
import org.eclipse.dd.gdb.internal.provisional.service.SessionType;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
 
/**
 * The launch configuration delegate for the DSF GDB JUnit tests.
 */
@ThreadSafe
public class TestLaunchDelegate extends LaunchConfigurationDelegate 
    implements ILaunchConfigurationDelegate2
{
    public final static String GDB_DEBUG_MODEL_ID = "org.eclipse.dd.tests.gdb"; //$NON-NLS-1$

    public void launch( ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor ) throws CoreException {
		if ( monitor == null ) {
			monitor = new NullProgressMonitor();
		}
		if ( mode.equals( ILaunchManager.DEBUG_MODE ) ) {
			launchDebugger( config, launch, monitor );
		}
	}

	private void launchDebugger( ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor ) throws CoreException {
		monitor.beginTask("Launching debugger session", 10); //$NON-NLS-1$
		if ( monitor.isCanceled() ) {
			return;
		}
		try {
			String debugMode = config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
			if ( debugMode.equals( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN ) ) {
				launchLocalDebugSession( config, launch, monitor );
			}
		}
		finally {
			monitor.done();
		}		
	}

	private void launchLocalDebugSession( final ILaunchConfiguration config, ILaunch l, IProgressMonitor monitor ) throws CoreException {
		if ( monitor.isCanceled() ) {
			return;
		}
        final GdbLaunch launch = (GdbLaunch)l;

        monitor.subTask("DSF GDB/MI reference JUnit tests"); //$NON-NLS-1$
		IPath exePath = new Path(getProgramName(config));
		verifyBinary(exePath);
		
        monitor.worked( 1 );  
        
        launch.setServiceFactory(new GdbDebugServicesFactory(LaunchUtils.getGDBVersion(config)));

        final ServicesLaunchSequence servicesLaunchSequence = 
            new ServicesLaunchSequence(launch.getSession(), launch);
        launch.getSession().getExecutor().execute(servicesLaunchSequence);
        try {
            servicesLaunchSequence.get();
        } catch (InterruptedException e1) {
            throw new DebugException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
        } catch (ExecutionException e1) {
            throw new DebugException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Error in services launch sequence", e1.getCause())); //$NON-NLS-1$
        }
        
        launch.initializeControl();

        // Add the CLI and "inferior" process objects to the launch.
        launch.addCLIProcess("gdb"); //$NON-NLS-1$
        launch.addInferiorProcess(exePath.lastSegment());

        // Create and invoke the final launch sequence to setup GDB
        final FinalLaunchSequence finalLaunchSequence = 
        	new FinalLaunchSequence(launch.getSession().getExecutor(), launch, SessionType.LOCAL, false);
        launch.getSession().getExecutor().execute(finalLaunchSequence);
        try {
        	finalLaunchSequence.get();
        } catch (InterruptedException e1) {
            throw new DebugException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
        } catch (ExecutionException e1) {
            throw new DebugException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Error in final launch sequence", e1.getCause())); //$NON-NLS-1$
        }
	}

	/*
	 * Verify that the specified file exists on the file system.
	 */
	private void verifyBinary(IPath exePath) throws CoreException {
		try {
			new FileReader(exePath.toFile());
		} catch (Exception e) {
			Throwable exception = new FileNotFoundException(exePath.toOSString() + " does not exist"); //$NON-NLS-1$
			int code = ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_BINARY;
			throw new CoreException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, code, exception == null ? "" : exception.getLocalizedMessage(), //$NON-NLS-1$
					exception));
		}
	}

	@Override
    public boolean preLaunchCheck( ILaunchConfiguration config, String mode, IProgressMonitor monitor ) throws CoreException {
		return super.preLaunchCheck( config, mode, monitor );
	}

    @Override
    public boolean buildForLaunch(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
        return false;
    }

    @Override
    public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
        return true;
    }

    @Override
    public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
    	GdbLaunch launch = new GdbLaunch(configuration, mode, null);
        launch.initialize();
        return launch;
    }
    
    private static String getProgramName(ILaunchConfiguration configuration) throws CoreException {
    	return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, (String)null);
    }
}
