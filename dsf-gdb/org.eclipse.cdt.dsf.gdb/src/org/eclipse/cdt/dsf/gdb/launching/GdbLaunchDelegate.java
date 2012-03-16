/*******************************************************************************
 * Copyright (c) 2008, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems   - Initial API and implementation
 * Windriver and Ericsson - Updated for DSF
 * IBM Corporation 
 * Ericsson               - Added support for Mac OS
 * Ericsson               - Added support for post-mortem trace files
 * Abeer Bagul (Tensilica) - Allow to better override GdbLaunch (bug 339550)
 * Anton Gorenkov         - Need to use a process factory (Bug 210366)
 * Marc Khouzam (Ericsson) - Cleanup the launch if it is cancelled (Bug 374374)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching; 

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.debug.sourcelookup.DsfSourceLookupDirector;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.GdbDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.service.GdbDebugServicesFactoryNS;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.gdb.service.macos.MacOSGdbDebugServicesFactory;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ISourceLocator;
 
/**
 * The shared launch configuration delegate for the DSF/GDB debugger.
 * This delegate supports all configuration types (local, remote, attach, etc)
 */
@ThreadSafe
public class GdbLaunchDelegate extends AbstractCLaunchDelegate2
{
    public final static String GDB_DEBUG_MODEL_ID = "org.eclipse.cdt.dsf.gdb"; //$NON-NLS-1$

    private final static String NON_STOP_FIRST_VERSION = "6.8.50"; //$NON-NLS-1$
    
    // Can be removed once we remove the deprecated newServiceFactory(String)
	private boolean fIsNonStopSession = false;
	
    private final static String TRACING_FIRST_VERSION = "7.1.50"; //$NON-NLS-1$
	
    private GdbLaunch fGdbLaunch;
    
	public GdbLaunchDelegate() {
		// We now fully support project-less debugging
		// See bug 343861
		this(false);
	}

	/**
	 * @since 4.0
	 */
	public GdbLaunchDelegate(boolean requireCProject) {
		super(requireCProject);
	}

	@Override
	public void launch( ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor ) throws CoreException {
		org.eclipse.cdt.launch.LaunchUtils.enableActivity("org.eclipse.cdt.debug.dsfgdbActivity", true); //$NON-NLS-1$
		if ( monitor == null ) {
			monitor = new NullProgressMonitor();
		}
		if ( mode.equals( ILaunchManager.DEBUG_MODE ) ) {
			launchDebugger( config, launch, monitor );
		}
	}

	private void launchDebugger( ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor ) throws CoreException {
		monitor.beginTask(LaunchMessages.getString("GdbLaunchDelegate.0"), 10);  //$NON-NLS-1$
		if ( monitor.isCanceled() ) {
			cleanupLaunch();
			return;
		}

		try {
    		launchDebugSession( config, launch, monitor );
		}
		finally {
			monitor.done();
		}		
	}

	private void launchDebugSession( final ILaunchConfiguration config, ILaunch l, IProgressMonitor monitor ) throws CoreException {
		if ( monitor.isCanceled() ) {
			cleanupLaunch();
			return;
		}
		
		SessionType sessionType = LaunchUtils.getSessionType(config);
		boolean attach = LaunchUtils.getIsAttach(config);
		
        final GdbLaunch launch = (GdbLaunch)l;

        if (sessionType == SessionType.REMOTE) {
            monitor.subTask( LaunchMessages.getString("GdbLaunchDelegate.1") );  //$NON-NLS-1$
        } else if (sessionType == SessionType.CORE) {
            monitor.subTask( LaunchMessages.getString("GdbLaunchDelegate.2") );  //$NON-NLS-1$
        } else {
        	assert sessionType == SessionType.LOCAL : "Unexpected session type: " + sessionType.toString(); //$NON-NLS-1$
            monitor.subTask( LaunchMessages.getString("GdbLaunchDelegate.3") );  //$NON-NLS-1$
        }
        
        // An attach session does not need to necessarily have an
        // executable specified.  This is because:
        // - In remote multi-process attach, there will be more than one executable
        //   In this case executables need to be specified differently.
        //   The current solution is to use the solib-search-path to specify
        //   the path of any executable we can attach to.
        // - In local single process, GDB has the ability to find the executable
        //   automatically.
        if (!attach) {
        	checkBinaryDetails(config);
        }
    	
        monitor.worked( 1 );

        // Must set this here for users that call directly the deprecated newServiceFactory(String)
        fIsNonStopSession = LaunchUtils.getIsNonStopMode(config);

        String gdbVersion = getGDBVersion(config);
        
        // First make sure non-stop is supported, if the user want to use this mode
        if (LaunchUtils.getIsNonStopMode(config) && !isNonStopSupportedInGdbVersion(gdbVersion)) {
			cleanupLaunch();
            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Non-stop mode is only supported starting with GDB " + NON_STOP_FIRST_VERSION, null)); //$NON-NLS-1$        	
        }

        if (LaunchUtils.getIsPostMortemTracing(config) && !isPostMortemTracingSupportedInGdbVersion(gdbVersion)) {
			cleanupLaunch();
            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Post-mortem tracing is only supported starting with GDB " + TRACING_FIRST_VERSION, null)); //$NON-NLS-1$        	
        }

        launch.setServiceFactory(newServiceFactory(config, gdbVersion));

        // Create and invoke the launch sequence to create the debug control and services
        IProgressMonitor subMon1 = new SubProgressMonitor(monitor, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK); 
        final ServicesLaunchSequence servicesLaunchSequence = 
            new ServicesLaunchSequence(launch.getSession(), launch, subMon1);
        
        launch.getSession().getExecutor().execute(servicesLaunchSequence);
        boolean succeed = false;
        try {
            servicesLaunchSequence.get();
            succeed = true;
        } catch (InterruptedException e1) {
            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
        } catch (ExecutionException e1) {
            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Error in services launch sequence", e1.getCause())); //$NON-NLS-1$
        } catch (CancellationException e1) {
        	// Launch aborted, so exit cleanly
        	return;
        } finally {
        	if (!succeed) {
        		cleanupLaunch();
        	}
        }
        
        if (monitor.isCanceled()) {
			cleanupLaunch();
			return;
        }
        
        // The initializeControl method should be called after the ICommandControlService
        // is initialized in the ServicesLaunchSequence above.  This is because it is that
        // service that will trigger the launch cleanup (if we need it during this launch)
        // through an ICommandControlShutdownDMEvent
        launch.initializeControl();

        // Add the GDB process object to the launch.
        launch.addCLIProcess("gdb"); //$NON-NLS-1$

        monitor.worked(1);
        
        // Create and invoke the final launch sequence to setup GDB
        final IProgressMonitor subMon2 = new SubProgressMonitor(monitor, 4, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK); 

        Query<Object> completeLaunchQuery = new Query<Object>() {
            @Override
            protected void execute(final DataRequestMonitor<Object> rm) {
            	DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), launch.getSession().getId());
            	IGDBControl control = tracker.getService(IGDBControl.class);
            	tracker.dispose();
            	control.completeInitialization(new RequestMonitorWithProgress(ImmediateExecutor.getInstance(), subMon2) {
            		@Override
            		protected void handleCompleted() {
            			if (isCanceled()) {
            				rm.cancel();
            			} else {
            				rm.setStatus(getStatus());
            			}
            			rm.done();
            		}
            	});
            }
        };

        launch.getSession().getExecutor().execute(completeLaunchQuery);
        succeed = false;
        try {
        	completeLaunchQuery.get();
        	succeed = true;
        } catch (InterruptedException e1) {
            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "Interrupted Exception in dispatch thread", e1)); //$NON-NLS-1$
        } catch (ExecutionException e1) {
            throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Error in final launch sequence", e1.getCause())); //$NON-NLS-1$
        } catch (CancellationException e1) {
        	// Launch aborted, so exit cleanly
        	return;
        } finally {
            if (!succeed) {
                // finalLaunchSequence failed. Shutdown the session so that all started
                // services including any GDB process are shutdown. (bug 251486)
                cleanupLaunch();
            }        
        }
	}

	/** 
	 * This method takes care of cleaning up any resources allocated by the launch, as early as
	 * the call to getLaunch(), whenever the launch is cancelled or does not complete properly.
	 * @since 4.1 */
	protected void cleanupLaunch() throws DebugException {
		if (fGdbLaunch != null) {
            Query<Object> launchShutdownQuery = new Query<Object>() {
                @Override
                protected void execute(DataRequestMonitor<Object> rm) {
                	fGdbLaunch.shutdownSession(rm);
                }
            };
                
            fGdbLaunch.getSession().getExecutor().execute(launchShutdownQuery);
            
            // wait for the shutdown to finish.
            // The Query.get() method is a synchronous call which blocks until the 
            // query completes.  
            try {
                launchShutdownQuery.get();
            } catch (InterruptedException e) { 
                throw new DebugException( new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.INTERNAL_ERROR, "InterruptedException while shutting down debugger launch " + fGdbLaunch, e)); //$NON-NLS-1$ 
            } catch (ExecutionException e) {
                throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.REQUEST_FAILED, "Error in shutting down debugger launch " + fGdbLaunch, e)); //$NON-NLS-1$
            }
		}
	}
	
	/**
	 * Method used to check that the project, program and binary are correct.
	 * Can be overridden to avoid checking certain things.
	 * @since 3.0
	 */
	protected IPath checkBinaryDetails(final ILaunchConfiguration config) throws CoreException {
		// First verify we are dealing with a proper project.
		ICProject project = verifyCProject(config);
		// Now verify we know the program to debug.
		IPath exePath = LaunchUtils.verifyProgramPath(config, project);
		// Finally, make sure the program is a proper binary.
		LaunchUtils.verifyBinary(config, exePath);
		return exePath;
	}

	/**
	 * Returns the GDB version. 
	 * Subclass can override for special need.
     *
	 * @since 2.0
	 */
	protected String getGDBVersion(ILaunchConfiguration config) throws CoreException {
		return LaunchUtils.getGDBVersion(config);
	}

	@Override
    public boolean preLaunchCheck(ILaunchConfiguration config, String mode, IProgressMonitor monitor) throws CoreException {
    	// Setup default GDB Process Factory
    	// Bug 210366
		setDefaultProcessFactory(config);

		// Forcibly turn off non-stop for post-mortem sessions.
		// Non-stop does not apply to post-mortem sessions.
		// Now that we can have non-stop defaulting to enabled, it will prevent
		// post-mortem sessions from starting for GDBs <= 6.8 and there is no way to turn it off
		// Bug 348091
		if (LaunchUtils.getSessionType(config) == SessionType.CORE) {
			if (LaunchUtils.getIsNonStopMode(config)) {
				ILaunchConfigurationWorkingCopy wcConfig = config.getWorkingCopy();
				wcConfig.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP, false);
				wcConfig.doSave();			
			}
			
			// no further prelaunch check for core files
			return true;
		}
		
		boolean result = super.preLaunchCheck(config, mode, monitor);
		if (!result) {
			// The launch will not proceed!  We must cleanup.
			cleanupLaunch();
		}
		
		return result;
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		boolean result = super.finalLaunchCheck(configuration, mode, monitor);
		if (!result) {
			// The launch will not proceed!  We must cleanup.
			cleanupLaunch();
		}
		
		return result;
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

    // This is the first method to be called in the launch sequence, even before preLaunchCheck()
    // If we cancel the launch, we need to cleanup what is allocated in this method.  The cleanup
    // can be performed by GdbLaunch.shutdownSession()
    @Override
    public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
        // Need to configure the source locator before creating the launch
        // because once the launch is created and added to launch manager, 
        // the adapters will be created for the whole session, including 
        // the source lookup adapter.
        
    	fGdbLaunch = createGdbLaunch(configuration, mode, null);
    	fGdbLaunch.initialize();
    	fGdbLaunch.setSourceLocator(getSourceLocator(configuration, fGdbLaunch.getSession()));
        return fGdbLaunch;
    }
    
    /**
     * Creates an object of GdbLaunch.
     * Subclasses who wish to just replace the GdbLaunch object with a sub-classed GdbLaunch
     * should override this method.
     * Subclasses who wish to replace the GdbLaunch object as well as change the 
     * initialization sequence of the launch, should override getLaunch() as well as this method.
     * Subclasses who wish to create a launch class which does not subclass GdbLaunch, 
     * are advised to override getLaunch() directly.
     * 
     * @param configuration The launch configuration
     * @param mode The launch mode - "run", "debug", "profile"
     * @param locator The source locator.  Can be null.
     * @return The GdbLaunch object, or a sub-classed object
     * @throws CoreException
     * @since 4.1
     */
    protected GdbLaunch createGdbLaunch(ILaunchConfiguration configuration, String mode, ISourceLocator locator) throws CoreException {
    	return new GdbLaunch(configuration, mode, locator);
    }

    /**
     * Creates and initializes the source locator for the given launch configuration and dsf session.
     * @since 4.1
     */
    protected ISourceLocator getSourceLocator(ILaunchConfiguration configuration, DsfSession session) throws CoreException {
        DsfSourceLookupDirector locator = createDsfSourceLocator(configuration, session);
        String memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String)null);
        if (memento == null) {
            locator.initializeDefaults(configuration);
        } else {
            locator.initializeFromMemento(memento, configuration);
        }
        return locator;
    }
    
    /**
     * Creates an object of DsfSourceLookupDirector with the given DsfSession.
     * Subclasses who wish to just replace the source locator object with a sub-classed source locator
     * should override this method. 
     * Subclasses who wish to replace the source locator object as well as change the 
     * initialization sequence of the source locator, should override getSourceLocator()
     * as well as this method.
     * Subclasses who wish to create a source locator which does not subclass DsfSourceLookupDirector,
     * are advised to override getSourceLocator() directly.
     * @since 4.1
     */
    protected DsfSourceLookupDirector createDsfSourceLocator(ILaunchConfiguration configuration, DsfSession session) throws CoreException {
    	return new DsfSourceLookupDirector(session);
    }
	
	/**
	 * Returns true if the specified version of GDB supports
	 * non-stop mode.
	 * @since 4.0
	 */
	protected boolean isNonStopSupportedInGdbVersion(String version) {
		if (version.contains(LaunchUtils.MACOS_GDB_MARKER)) {
			// Mac OS's GDB does not support Non-Stop
			return false;
		}
		
		if (NON_STOP_FIRST_VERSION.compareTo(version) <= 0) {
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the specified version of GDB supports
	 * post-mortem tracing.
	 * @since 4.0
	 */
	protected boolean isPostMortemTracingSupportedInGdbVersion(String version) {
		if (version.contains(LaunchUtils.MACOS_GDB_MARKER)) {
			// Mac OS's GDB does not support post-mortem tracing
			return false;
		}
		
		if (TRACING_FIRST_VERSION.compareTo(version) <= 0
			// This feature will be available for GDB 7.2. But until that GDB is itself available
			// there is a pre-release that has a version of 6.8.50.20090414
			|| "6.8.50.20090414".equals(version)) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	/**
	 * @deprecated Replaced by newServiceFactory(ILaunchConfiguration, String)
	 */
	@Deprecated
	protected IDsfDebugServicesFactory newServiceFactory(String version) {

		if (fIsNonStopSession && isNonStopSupportedInGdbVersion(version)) {
			return new GdbDebugServicesFactoryNS(version);
		}
		
		if (version.contains(LaunchUtils.MACOS_GDB_MARKER)) {
			// The version string at this point should look like
			// 6.3.50-20050815APPLE1346, we extract the gdb version and apple version
			String versions [] = version.split(LaunchUtils.MACOS_GDB_MARKER);
			if (versions.length == 2) {
				return new MacOSGdbDebugServicesFactory(versions[0], versions[1]);
			}
		}

		return new GdbDebugServicesFactory(version);
	}

	/**
	 * Method called to create the services factory for this debug session.
	 * A subclass can override this method and provide its own ServiceFactory.
	 * @since 4.1
	 */
	protected IDsfDebugServicesFactory newServiceFactory(ILaunchConfiguration config, String version) {
		// Call the deprecated one for now to avoid code duplication.
		// Once we get rid of the deprecated one, we can also get rid of fIsNonStopSession
		fIsNonStopSession = LaunchUtils.getIsNonStopMode(config);
		return newServiceFactory(version);
	}

	@Override
	protected String getPluginID() {
		return GdbPlugin.PLUGIN_ID;
	}
}
