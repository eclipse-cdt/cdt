/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nokia              - initial API and implementation with some code moved from GDBControl.
 *     Wind River System
 *     Ericsson
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl.InitializationShutdownStep;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.cdt.utils.spawner.Spawner;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.osgi.framework.BundleContext;

/**
 * Implementation of {@link IGDBBackend} for the common case where GDB is launched 
 * in local file system on host PC where Eclipse runs. This also manages some GDB parameters
 * from a given launch configuration.<br>
 * <br> 
 * You can subclass for you special needs.
 * 
 * @since 1.1
 */
public class GDBBackend extends AbstractDsfService implements IGDBBackend {
	
	private ILaunchConfiguration fLaunchConfiguration;
	
	/*
	 * Parameters for launching GDB.
	 */
	private IPath fProgramPath;
	private IPath fGDBWorkingDirectory;
	private String fGDBInitFile;
	private List<String> fSharedLibPaths;
	private String fProgramArguments;
	
	private Properties fEnvVariables;
	private SessionType fSessionType;
    private Boolean fAttach;

	/**
     * Unique ID of this service instance.
     */
	private final String fBackendId; 
    private static int fgInstanceCounter = 0;
	
	/*
     * Service state parameters.
     */
    private MonitorJob fMonitorJob;
    private Process fProcess;
    private int fGDBExitValue;
    private int fGDBLaunchTimeout = 30;
    
    /**
     * A Job that will set a failed status
     * in the proper request monitor, if the interrupt
     * did not succeed after a certain time.
     */
    private MonitorInterruptJob fInterruptFailedJob;

    
	public GDBBackend(DsfSession session, ILaunchConfiguration lc) {
		super(session);
		fBackendId = "gdb[" +Integer.toString(fgInstanceCounter++) + "]";  //$NON-NLS-1$//$NON-NLS-2$
		fLaunchConfiguration = lc;
		
		try {
			// Don't call verifyCProject, because the JUnit tests are not setting a project
			ICProject cproject = LaunchUtils.getCProject(lc);
			fProgramPath = LaunchUtils.verifyProgramPath(lc, cproject);
		} catch (CoreException e) {
			fProgramPath = new Path(""); //$NON-NLS-1$
		}
	}
	
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize( new RequestMonitor(getExecutor(), requestMonitor) {
            @Override
            protected void handleSuccess() {
                doInitialize(requestMonitor);
            }
        });
    }

    public void doInitialize(final RequestMonitor requestMonitor) {

        final Sequence.Step[] initializeSteps = new Sequence.Step[] {
                new GDBProcessStep(InitializationShutdownStep.Direction.INITIALIZING),
                new MonitorJobStep(InitializationShutdownStep.Direction.INITIALIZING),
                new RegisterStep(InitializationShutdownStep.Direction.INITIALIZING),
            };

        Sequence startupSequence = new Sequence(getExecutor(), requestMonitor) {
            @Override public Step[] getSteps() { return initializeSteps; }
        };
        getExecutor().execute(startupSequence);
    }

    @Override
    public void shutdown(final RequestMonitor requestMonitor) {
        final Sequence.Step[] shutdownSteps = new Sequence.Step[] {
                new RegisterStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new MonitorJobStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
                new GDBProcessStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
            };
        Sequence shutdownSequence = 
        	new Sequence(getExecutor(), 
        				 new RequestMonitor(getExecutor(), requestMonitor) {
        					@Override
        					protected void handleCompleted() {
        						GDBBackend.super.shutdown(requestMonitor);
        					}
        				}) {
            @Override public Step[] getSteps() { return shutdownSteps; }
        };
        getExecutor().execute(shutdownSequence);
    }        

    
    private IPath getGDBPath() {
        return LaunchUtils.getGDBPath(fLaunchConfiguration);
    }

	/*
	 * Options for GDB process. 
	 * Allow subclass to override.
	 */
	protected String getGDBCommandLine() {
		StringBuffer gdbCommandLine = new StringBuffer(getGDBPath().toOSString());

		// The goal here is to keep options to an absolute minimum.
		// All configuration should be done in the launch sequence
		// to allow for more flexibility.
		gdbCommandLine.append(" --interpreter"); //$NON-NLS-1$
		// We currently work with MI version 2.  Don't use just 'mi' because it 
		// points to the latest MI version, while we want mi2 specifically.
		gdbCommandLine.append(" mi2"); //$NON-NLS-1$
		// Don't read the gdbinit file here.  It is read explicitly in
		// the LaunchSequence to make it easier to customize.
		gdbCommandLine.append(" --nx"); //$NON-NLS-1$
		
		return gdbCommandLine.toString();
	}

	public String getGDBInitFile() throws CoreException {
		if (fGDBInitFile == null) {
			fGDBInitFile = fLaunchConfiguration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT,
					                                         IGDBLaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT);
		}
		
		return fGDBInitFile;
	}

	public IPath getGDBWorkingDirectory() throws CoreException {
		if (fGDBWorkingDirectory == null) {

			// First try to use the user-specified working directory for the debugged program.
			// This is fine only with local debug. 
			// For remote debug, the working dir of the debugged program will be on remote device
			// and hence not applicable. In such case we may just use debugged program path on host
			// as the working dir for GDB.
			// However, we cannot find a standard/common way to distinguish remote debug from local
			// debug. For instance, a local debug may also use gdbserver+gdb. So it's up to each 
			// debugger implementation to make the distinction.
			//
			IPath path = null;
			String location = fLaunchConfiguration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String)null);

    		if (location != null) {
    			String expandedLocation = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(location);
    			if (expandedLocation.length() > 0) {
    				path = new Path(expandedLocation);
    			}
    		}

    		if (path != null) {
    			// Some validity check. Should have been done by UI code.
    			if (path.isAbsolute()) {
    				File dir = new File(path.toPortableString());
    				if (! dir.isDirectory())
        				path = null; 
    			} else {
    				IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
    				if (res instanceof IContainer && res.exists()) {
    					path = res.getLocation();
    				}
    				else 
    					// Relative but not found in workspace.
    					path = null; 
    			}
    		}
       		
    		if (path == null) {
    			// default working dir is the project if this config has a project
    			ICProject cp = LaunchUtils.getCProject(fLaunchConfiguration);
    			if (cp != null) {
    				IProject p = cp.getProject();
    				path = p.getLocation();
    			}
    			else {
    				// no meaningful value found. Just return null.
    			}
    		} 

    		fGDBWorkingDirectory = path;
		}

   		return fGDBWorkingDirectory;
	}

	public String getProgramArguments() throws CoreException {
		if (fProgramArguments == null) {
			fProgramArguments = fLaunchConfiguration.getAttribute(
									ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
									(String)null);

			if (fProgramArguments != null) {
				fProgramArguments = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(fProgramArguments);
			}
		}
		
		return fProgramArguments;
	}

	public IPath getProgramPath() {
		return fProgramPath;
	}

	@SuppressWarnings("unchecked")
	public List<String> getSharedLibraryPaths() throws CoreException {
		if (fSharedLibPaths == null) {
			fSharedLibPaths = fLaunchConfiguration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, 
																new ArrayList<String>(0));
		}
		
		return fSharedLibPaths;
	}

	/** @since 3.0 */
	public Properties getEnvironmentVariables() throws CoreException {
		if (fEnvVariables == null) {
			fEnvVariables = new Properties();
			
			// if the attribute ATTR_APPEND_ENVIRONMENT_VARIABLES is set,
			// the LaunchManager will return both the new variables and the existing ones.
			// That would force us to delete all the variables in GDB, and then re-create then all
			// that is not very efficient.  So, let's fool the LaunchManager into returning just the
			// list of new variables.
			
			boolean append = fLaunchConfiguration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);

			String[] properties;
			if (append) {
				ILaunchConfigurationWorkingCopy wc = fLaunchConfiguration.copy(""); //$NON-NLS-1$
				// Don't save this change, it is just temporary, and in just a copy of our launchConfig.
				wc.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, false);
				properties = DebugPlugin.getDefault().getLaunchManager().getEnvironment(wc);
			} else {
				// We're getting rid of the environment anyway, so this call will only yield the new variables.
				properties = DebugPlugin.getDefault().getLaunchManager().getEnvironment(fLaunchConfiguration);
			}
			
			if (properties == null) {
				properties = new String[0];
			}
			
			for (String property : properties) {
				int idx = property.indexOf('=');
				if (idx != -1) {
					String key = property.substring(0, idx);
					String value = property.substring(idx + 1);
					fEnvVariables.setProperty(key, value);
				} else {
					fEnvVariables.setProperty(property, ""); //$NON-NLS-1$
				}
			}
		}
		
		return fEnvVariables;
	}
	
	/** @since 3.0 */
	public boolean getClearEnvironment() throws CoreException {
		return !fLaunchConfiguration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
	}
	
	/** @since 3.0 */
	public boolean getUpdateThreadListOnSuspend() throws CoreException {
		return fLaunchConfiguration
				.getAttribute(
						IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
						IGDBLaunchConfigurationConstants.DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND_DEFAULT);
	}
	
	/*
	 * Launch GDB process. 
	 * Allow subclass to override.
	 */
	protected Process launchGDBProcess(String commandLine) throws CoreException {
        Process proc = null;
		try {
			proc = ProcessFactory.getFactory().exec(commandLine);
		} catch (IOException e) {
            String message = "Error while launching command " + commandLine;   //$NON-NLS-1$
            throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, message, e));
		}
		
		return proc;
	}

    public Process getProcess() { 
        return fProcess; 
    }
    
    public OutputStream getMIOutputStream() {
        return fProcess.getOutputStream();
    };
    
    public InputStream getMIInputStream() {
        return fProcess.getInputStream();
    };
    
    public String getId() {
        return fBackendId;
    }

    public void interrupt() {
        if (fProcess instanceof Spawner) {
            Spawner gdbSpawner = (Spawner) fProcess;
           	gdbSpawner.interruptCTRLC();
        }
    }

    /**
	 * @since 3.0
	 */
    public void interruptAndWait(int timeout, RequestMonitor rm) {
        if (fProcess instanceof Spawner) {
            Spawner gdbSpawner = (Spawner) fProcess;
           	gdbSpawner.interruptCTRLC();
            fInterruptFailedJob = new MonitorInterruptJob(timeout, rm);
        } else {
            rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED, "Cannot interrupt.", null)); //$NON-NLS-1$
            rm.done();
        }
    }

    public void destroy() {
		// destroy() should be supported even if it's not spawner. 
    	if (getState() == State.STARTED) {
    		fProcess.destroy();
    	}
    }

    public State getState() {
        if (fMonitorJob == null) {
            return State.NOT_INITIALIZED;
        } else if (fMonitorJob.fExited) {
            return State.TERMINATED;
        } else {
            return State.STARTED;
        }
    }
    
    public int getExitCode() { 
        return fGDBExitValue;
    }
    
    public 	SessionType getSessionType() {
        if (fSessionType == null) {
        	fSessionType = LaunchUtils.getSessionType(fLaunchConfiguration);
        }
        return fSessionType;
    }

	public boolean getIsAttachSession() {
        if (fAttach == null) {
        	fAttach = LaunchUtils.getIsAttach(fLaunchConfiguration);
        }
        return fAttach;
	}

	@Override
	protected BundleContext getBundleContext() {
        return GdbPlugin.getBundleContext();
	}

	protected class GDBProcessStep extends InitializationShutdownStep {
        GDBProcessStep(Direction direction) { super(direction); }
        
        @Override
        public void initialize(final RequestMonitor requestMonitor) {
            class GDBLaunchMonitor {
                boolean fLaunched = false;
                boolean fTimedOut = false;
            }
            final GDBLaunchMonitor fGDBLaunchMonitor = new GDBLaunchMonitor(); 

            final RequestMonitor gdbLaunchRequestMonitor = new RequestMonitor(getExecutor(), requestMonitor) {
                @Override
                protected void handleCompleted() {
                    if (!fGDBLaunchMonitor.fTimedOut) {
                        fGDBLaunchMonitor.fLaunched = true;
                        if (!isSuccess()) {
                            requestMonitor.setStatus(getStatus());
                        }
                        requestMonitor.done();
                    }
                }
            };
            
            final Job startGdbJob = new Job("Start GDB Process Job") { //$NON-NLS-1$
                {
                    setSystem(true);
                }

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    if (gdbLaunchRequestMonitor.isCanceled()) {
                        gdbLaunchRequestMonitor.setStatus(new Status(IStatus.CANCEL, GdbPlugin.PLUGIN_ID, -1, "Canceled starting GDB", null)); //$NON-NLS-1$
                        gdbLaunchRequestMonitor.done();
                        return Status.OK_STATUS;
                    }
                    
                    String commandLine = getGDBCommandLine();
        
                    try {                        
                        fProcess = launchGDBProcess(commandLine);
                    } catch(CoreException e) {
                        gdbLaunchRequestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, e.getMessage(), e));
                        gdbLaunchRequestMonitor.done();
                        return Status.OK_STATUS;
                    }
                    
                    try {
                        Reader r = new InputStreamReader(getMIInputStream());
                        BufferedReader reader = new BufferedReader(r);
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.endsWith("(gdb)")) { //$NON-NLS-1$
                                break;
                            }
                        }
                    } catch (IOException e) {
                        gdbLaunchRequestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Error reading GDB STDOUT", e)); //$NON-NLS-1$
                        gdbLaunchRequestMonitor.done();
                        return Status.OK_STATUS;
                    }

                    gdbLaunchRequestMonitor.done();
                    return Status.OK_STATUS;
                }
            };
            startGdbJob.schedule();
                
            getExecutor().schedule(new Runnable() { 
                public void run() {
                    // Only process the event if we have not finished yet (hit the breakpoint).
                    if (!fGDBLaunchMonitor.fLaunched) {
                        fGDBLaunchMonitor.fTimedOut = true;
                        Thread jobThread = startGdbJob.getThread();
                        if (jobThread != null) {
                            jobThread.interrupt();
                        }
                        requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, DebugException.TARGET_REQUEST_FAILED, "Timed out trying to launch GDB.", null)); //$NON-NLS-1$
                        requestMonitor.done();
                    }
                }},
                fGDBLaunchTimeout, TimeUnit.SECONDS);
        }
        
        @Override
        protected void shutdown(final RequestMonitor requestMonitor) {
            new Job("Terminating GDB process.") {  //$NON-NLS-1$
                {
                    setSystem(true);
                }
                
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    destroy();
        
                    int attempts = 0;
                    while (attempts < 10) {
                        try {
                            // Don't know if we really need the exit value... but what the heck.
                            fGDBExitValue = fProcess.exitValue(); // throws exception if process not exited
        
                            requestMonitor.done();
                            return Status.OK_STATUS;
                        } catch (IllegalThreadStateException ie) {
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                        attempts++;
                    }
                    requestMonitor.setStatus(new Status(
                        IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Process terminate failed", null));      //$NON-NLS-1$
                    requestMonitor.done();
                    return Status.OK_STATUS;
                }
            }.schedule();
        }
    }
    
    protected class MonitorJobStep extends InitializationShutdownStep {
        MonitorJobStep(Direction direction) { super(direction); }

        @Override
        public void initialize(final RequestMonitor requestMonitor) {
            fMonitorJob = new MonitorJob(
                fProcess, 
                new DsfRunnable() {
                    public void run() {
                        requestMonitor.done();
                    }
                });
            fMonitorJob.schedule();
        }

        @Override
        protected void shutdown(RequestMonitor requestMonitor) {
            if (!fMonitorJob.fExited) {
                fMonitorJob.kill();
            }
            requestMonitor.done();
        }
    }

    protected class RegisterStep extends InitializationShutdownStep {
        RegisterStep(Direction direction) { super(direction); }
        @Override
        public void initialize(final RequestMonitor requestMonitor) {
            register(
                new String[]{ IMIBackend.class.getName(), 
                              IGDBBackend.class.getName() }, 
                new Hashtable<String,String>());
            
            getSession().addServiceEventListener(GDBBackend.this, null);

            /*
			 * This event is not consumed by any one at present, instead it's
			 * the GDBControlInitializedDMEvent that's used to indicate that GDB
			 * back end is ready for MI commands. But we still fire the event as
			 * it does no harm and may be needed sometime.... 09/29/2008
			 */
            getSession().dispatchEvent(
                new BackendStateChangedEvent(getSession().getId(), getId(), IMIBackend.State.STARTED), 
                getProperties());
            
            requestMonitor.done();
        }

        @Override
        protected void shutdown(RequestMonitor requestMonitor) {
            unregister();
            getSession().removeServiceEventListener(GDBBackend.this);
            requestMonitor.done();
        }
    }

    /**
     * Monitors a system process, waiting for it to terminate, and
     * then notifies the associated runtime process.
     */
    private class MonitorJob extends Job {
        boolean fExited = false;
        DsfRunnable fMonitorStarted;
        Process fMonProcess;

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            synchronized(fMonProcess) {
                getExecutor().submit(fMonitorStarted);
                while (!fExited) {
                    try {
                        fMonProcess.waitFor();
                        fGDBExitValue = fMonProcess.exitValue();
                    } catch (InterruptedException ie) {
                        // clear interrupted state
                        Thread.interrupted();
                    } finally {
                        fExited = true;
                        getSession().dispatchEvent(
                            new BackendStateChangedEvent(getSession().getId(), getId(), IMIBackend.State.TERMINATED), 
                            getProperties());
                    }
                }
            }
            return Status.OK_STATUS;
        }

        MonitorJob(Process process, DsfRunnable monitorStarted) {
            super("GDB process monitor job.");  //$NON-NLS-1$
            fMonProcess = process; 
            fMonitorStarted = monitorStarted;
            setSystem(true);
        }

        void kill() {
            synchronized(fMonProcess) {
                if (!fExited) {
                    getThread().interrupt();
                }
            }
        }
    }   

    /**
     * Stores the request monitor that must be dealt with for 
     * the result of the interrupt operation.  If the interrupt
     * successfully suspends the backend, the request monitor can
     * be retrieved and completed successfully, and then this job
     * should be canceled.  If this job is not canceled before 
     * the time is up, it will imply the interrupt did not 
     * successfully suspend the backend, and the current job will 
     * indicate this in the request monitor.
     * 
     * The specified timeout is used to indicate how many milliseconds
     * this job should wait for. INTERRUPT_TIMEOUT_DEFAULT indicates
     * to use the default of 500 ms.  The default is also use if the 
     * timeout value is 0 or negative.
     * 
     * @since 3.0
     */
    protected class MonitorInterruptJob extends Job {
    	private final static int TIMEOUT_DEFAULT_VALUE = 500;
        private final RequestMonitor fRequestMonitor;

        public MonitorInterruptJob(int timeout, RequestMonitor rm) {
            super("Interrupt monitor job."); //$NON-NLS-1$
            setSystem(true);
            fRequestMonitor = rm;
            
            if (timeout == INTERRUPT_TIMEOUT_DEFAULT || timeout <= 0) {
            	timeout = TIMEOUT_DEFAULT_VALUE; // default of 0.5 seconds
            }
            
           	schedule(timeout);
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
        	getExecutor().submit(
                    new DsfRunnable() {
                        public void run() {
                        	fInterruptFailedJob = null;
                        	fRequestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED, "Interrupt failed.", null)); //$NON-NLS-1$
                        	fRequestMonitor.done();
                        }
                    });
        	return Status.OK_STATUS;
        }

        public RequestMonitor getRequestMonitor() { return fRequestMonitor; }
    }

	/**
	 * We use this handler to determine if the SIGINT we sent to GDB has been
	 * effective. We must listen for an MI event and not a higher-level
	 * ISuspendedEvent. The reason is that some ISuspendedEvent are not sent
	 * when the target stops, in cases where we don't want to views to update.
	 * For example, if we want to interrupt the target to set a breakpoint, this
	 * interruption is done silently; we will receive the MI event though.
	 * 
	 * <p>
	 * Though we send a SIGINT, we may not specifically get an MISignalEvent.
	 * Typically we will, but not always, so wait for an MIStoppedEvent. See
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=305178#c21
	 * 
	 * @since 3.0
	 * 
	 */
    @DsfServiceEventHandler
    public void eventDispatched(final MIStoppedEvent e) {
    	if (fInterruptFailedJob != null) {
    		if (fInterruptFailedJob.cancel()) {
    			fInterruptFailedJob.getRequestMonitor().done();
    		}
    		fInterruptFailedJob = null;
    	}
    }
}
