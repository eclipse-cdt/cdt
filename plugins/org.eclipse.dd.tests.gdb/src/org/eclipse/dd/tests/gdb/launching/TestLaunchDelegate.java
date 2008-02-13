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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.utils.pty.PTY;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.debug.model.DsfMemoryBlockRetrieval;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.gdb.launching.GdbLaunch;
import org.eclipse.dd.gdb.service.command.GDBControl;
import org.eclipse.dd.mi.service.command.AbstractCLIProcess;
import org.eclipse.dd.mi.service.command.MIInferiorProcess;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.IPersistableSourceLocator2;
 
/**
 * The launch configuration delegate for the CDI debugger session types.
 */
@ThreadSafe
public class TestLaunchDelegate extends AbstractCLaunchDelegate 
    implements ILaunchConfigurationDelegate2
{
    public final static String GDB_DEBUG_MODEL_ID = "org.eclipse.dd.tests.gdb"; //$NON-NLS-1$
    
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.launch.AbstractCLaunchDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
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

	private void launchLocalDebugSession( ILaunchConfiguration config, ILaunch l, IProgressMonitor monitor ) throws CoreException {
		if ( monitor.isCanceled() ) {
			return;
		}
        final GdbLaunch launch = (GdbLaunch)l;

        monitor.subTask("Debugging using GDB/MI reference for DSF"); //$NON-NLS-1$
		IPath exePath = new Path(getProgramName(config));
		verifyBinary(exePath);
		
		setDefaultSourceLocator(launch, config);

        monitor.worked( 1 );  
        
        // Create and invoke the launch sequence to create the debug control and services
        final LaunchSequence launchSequence = 
            new LaunchSequence(launch.getSession(), launch, exePath);
        launch.getSession().getExecutor().execute(launchSequence);
        try {
            launchSequence.get();
        } catch (InterruptedException e1) {
            throw new DebugException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
        } catch (ExecutionException e1) {
            throw new DebugException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Error in launch sequence", e1.getCause())); //$NON-NLS-1$
        }
        
        launch.initializeControl();

        // Add the CLI and "inferior" process objects to the launch.
        final AtomicReference<AbstractCLIProcess> cliProcessRef = new AtomicReference<AbstractCLIProcess>();
        final AtomicReference<MIInferiorProcess> inferiorProcessRef = new AtomicReference<MIInferiorProcess>();
        try {
            launch.getDsfExecutor().submit( new Callable<Object>() {
                public Object call() throws CoreException {
                    DsfServicesTracker tracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), launch.getSession().getId());
                    GDBControl gdb = tracker.getService(GDBControl.class);
                    if (gdb != null) {
                        cliProcessRef.set(gdb.getCLIProcess());
                        inferiorProcessRef.set(gdb.getInferiorProcess());
                    }
                    tracker.dispose();
                    return null;
                }
            }).get();
            launch.addProcess(DebugPlugin.newProcess(launch, cliProcessRef.get(), "gdb")); //$NON-NLS-1$
            launch.addProcess(DebugPlugin.newProcess(launch, inferiorProcessRef.get(), exePath.lastSegment()));
        } catch (InterruptedException e) {
            throw new CoreException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, 0, "Interrupted while waiting for get process callable.", e)); //$NON-NLS-1$
        } catch (ExecutionException e) {
            throw (CoreException)e.getCause();
        } catch (RejectedExecutionException e) {
            throw new CoreException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, 0, "Debugger shut down before launch was completed.", e)); //$NON-NLS-1$
        }            
        
        // Create a memory retrieval and register it with session 
        try {
            launch.getDsfExecutor().submit( new Callable<Object>() {
                public Object call() throws CoreException {
                    DsfServicesTracker tracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), launch.getSession().getId());
                    GDBControl gdbControl = tracker.getService(GDBControl.class);
                    if (gdbControl != null) {
                        IMemoryBlockRetrieval memRetrieval = new DsfMemoryBlockRetrieval(
                            GDB_DEBUG_MODEL_ID, gdbControl.getGDBDMContext());
                        launch.getSession().registerModelAdapter(IMemoryBlockRetrieval.class, memRetrieval);
                    }
                    tracker.dispose();
                    return null;
                }
            }).get();
        } catch (InterruptedException e) {
            throw new CoreException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, 0, "Interrupted while waiting for get process callable.", e)); //$NON-NLS-1$
        } catch (ExecutionException e) {
            throw (CoreException)e.getCause();
        } catch (RejectedExecutionException e) {
            throw new CoreException(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, 0, "Debugger shut down before launch was completed.", e)); //$NON-NLS-1$
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
			throw new CoreException(new Status(IStatus.ERROR, getPluginID(), code, exception == null ? "" : exception.getLocalizedMessage(), //$NON-NLS-1$
					exception));
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.launch.AbstractCLaunchDelegate#getPluginID()
	 */
	@Override
    protected String getPluginID() {
		return LaunchUIPlugin.getUniqueIdentifier();
	}

	/**
	 * Performs a runtime exec on the given command line in the context of the
	 * specified working directory, and returns the resulting process. If the
	 * current runtime does not support the specification of a working
	 * directory, the status handler for error code
	 * <code>ERR_WORKING_DIRECTORY_NOT_SUPPORTED</code> is queried to see if
	 * the exec should be re-executed without specifying a working directory.
	 * 
	 * @param cmdLine
	 *            the command line
	 * @param workingDirectory
	 *            the working directory, or <code>null</code>
	 * @return the resulting process or <code>null</code> if the exec is
	 *         cancelled
	 * @see Runtime
	 */
	protected Process exec( String[] cmdLine, String[] environ, File workingDirectory, boolean usePty ) throws CoreException {
		Process p = null;
		try {
			if ( workingDirectory == null ) {
				p = ProcessFactory.getFactory().exec( cmdLine, environ );
			}
			else {
				if ( usePty && PTY.isSupported() ) {
					p = ProcessFactory.getFactory().exec( cmdLine, environ, workingDirectory, new PTY() );
				}
				else {
					p = ProcessFactory.getFactory().exec( cmdLine, environ, workingDirectory );
				}
			}
		}
		catch( IOException e ) {
			if ( p != null ) {
				p.destroy();
			}
			abort( "Error starting process.", e, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR ); //$NON-NLS-1$
		}
		catch( NoSuchMethodError e ) {
			// attempting launches on 1.2.* - no ability to set working
			// directory
			IStatus status = new Status( IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(), ICDTLaunchConfigurationConstants.ERR_WORKING_DIRECTORY_NOT_SUPPORTED, LaunchMessages.getString( "LocalDsfLaunchDelegate.9" ), e ); //$NON-NLS-1$
			IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler( status );
			if ( handler != null ) {
				Object result = handler.handleStatus( status, this );
				if ( result instanceof Boolean && ((Boolean)result).booleanValue() ) {
					p = exec( cmdLine, environ, null, usePty );
				}
			}
		}
		return p;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.launch.AbstractCLaunchDelegate#preLaunchCheck(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
    public boolean preLaunchCheck( ILaunchConfiguration config, String mode, IProgressMonitor monitor ) throws CoreException {
		// no pre launch check for core file
		if ( mode.equals( ILaunchManager.DEBUG_MODE ) ) {
			if ( ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE.equals( config.getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE, ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN ) ) )
					return true; 
		}
		return super.preLaunchCheck( config, mode, monitor );
//		return true;
	}

    ///////////////////////////////////////////////////////////////////////////
    // ILaunchConfigurationDelegate2
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
        // Need to configure the source locator before creating the launch
        // because once the launch is created and added to launch manager, 
        // the adapters will be created for the whole session, including 
        // the source lookup adapter.
        ISourceLocator locator = getSourceLocator(configuration);
        
        return  new GdbLaunch(configuration, mode, locator);
    }

    private ISourceLocator getSourceLocator(ILaunchConfiguration configuration) throws CoreException {
        String type = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
        if (type == null) {
            type = configuration.getType().getSourceLocatorId();
        }
        if (type != null) {
            IPersistableSourceLocator locator = DebugPlugin.getDefault().getLaunchManager().newSourceLocator(type);
            String memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String)null);
            if (memento == null) {
                locator.initializeDefaults(configuration);
            } else {
                if(locator instanceof IPersistableSourceLocator2)
                    ((IPersistableSourceLocator2)locator).initializeFromMemento(memento, configuration);
                else
                    locator.initializeFromMemento(memento);
            }
            return locator;
        }
        return null;
    }
}
