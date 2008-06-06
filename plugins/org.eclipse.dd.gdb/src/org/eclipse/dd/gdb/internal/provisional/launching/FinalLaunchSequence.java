/*******************************************************************************
 * Copyright (c) 2008 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation          
 *******************************************************************************/
package org.eclipse.dd.gdb.internal.provisional.launching;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IProcessInfo;
import org.eclipse.cdt.core.IProcessList;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
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
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.gdb.internal.GdbPlugin;
import org.eclipse.dd.gdb.internal.provisional.IGDBLaunchConfigurationConstants;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl;
import org.eclipse.dd.gdb.internal.provisional.service.command.GDBControl.SessionType;
import org.eclipse.dd.mi.service.CSourceLookup;
import org.eclipse.dd.mi.service.MIBreakpointsManager;
import org.eclipse.dd.mi.service.command.commands.CLIAttach;
import org.eclipse.dd.mi.service.command.commands.CLIMonitorListProcesses;
import org.eclipse.dd.mi.service.command.commands.CLISource;
import org.eclipse.dd.mi.service.command.commands.MIEnvironmentCD;
import org.eclipse.dd.mi.service.command.commands.MIFileExecAndSymbols;
import org.eclipse.dd.mi.service.command.commands.MIGDBSetArgs;
import org.eclipse.dd.mi.service.command.commands.MIGDBSetAutoSolib;
import org.eclipse.dd.mi.service.command.commands.MIGDBSetSolibSearchPath;
import org.eclipse.dd.mi.service.command.commands.MITargetSelect;
import org.eclipse.dd.mi.service.command.output.CLIMonitorListProcessesInfo;
import org.eclipse.dd.mi.service.command.output.MIInfo;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;

public class FinalLaunchSequence extends Sequence {

    Step[] fSteps = new Step[] {
        /*
         * Fetch the control service for later use
         */
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fLaunch.getSession().getId());
            fCommandControl = tracker.getService(GDBControl.class);
            if (fCommandControl == null) {
        		requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain GDBControl service", null)); //$NON-NLS-1$
            }
            tracker.dispose();

            requestMonitor.done();
        }},
    	/*
    	 * Source the gdbinit file specified in the launch
    	 */
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
        	try {
        		final String gdbinitFile = fLaunch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT, 
        				                                                           IGDBLaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT );
        		if (gdbinitFile != null && gdbinitFile.length() > 0) {
        			fCommandControl.queueCommand(
        					new CLISource(fCommandControl.getControlDMContext(), gdbinitFile), 
        					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
        						@Override
        						protected void handleCompleted() {
        							// If the gdbinitFile is the default, then it may not exist and we
        							// should not consider this an error.
        							// If it is not the default, then the user must have specified it and
        							// we want to warn the user if we can't find it.
        							if (!gdbinitFile.equals(IGDBLaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT )) {
        								requestMonitor.setStatus(getStatus());
        							}
        							requestMonitor.done();
        						}
        					});
        		} else {
        			requestMonitor.done();
        		}
        	} catch (CoreException e) {
        		requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot get gdbinit option", e)); //$NON-NLS-1$
        		requestMonitor.done();
        	}
        }},
    	/*
    	 * Specify the executable file to be debugged and read the symbol table.
    	 */
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
        	final IPath execPath = fCommandControl.getExecutablePath();
        	if (execPath != null && !execPath.isEmpty()) {
        		fCommandControl.queueCommand(
       				new MIFileExecAndSymbols(fCommandControl.getControlDMContext(), 
       						                 execPath.toOSString()), 
       				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
        	} else {
        		requestMonitor.done();
        	}
        }},        
    	/*
    	 * Specify the arguments to the executable file
    	 */
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
    		try {
    			String args = fLaunch.getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
    					                                                    (String)null);
        		if (args != null) {
        			args = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(args);

        			fCommandControl.queueCommand(
        					new MIGDBSetArgs(fCommandControl.getControlDMContext(), args), 
        					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
        		} else {
        			requestMonitor.done();
        		}
    		} catch (CoreException e) {
    			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot get inferior arguments", e)); //$NON-NLS-1$
    			requestMonitor.done();
    		}    		
        }},
    	/*
    	 * Specify GDB's working directory
    	 */
        new Step() {
        	
        	private File getWorkingDirectory(RequestMonitor requestMonitor) {
       			IPath path = null;
           		try {
        			String location = fLaunch.getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, 
        																		    (String)null);
            		if (location != null) {
            			String expandedLocation = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(location);
            			if (expandedLocation.length() > 0) {
            				path = new Path(expandedLocation);
            			}
            		}

            		if (path == null) {
            			// default working dir is the project if this config has a project
            			ICProject cp = LaunchUtils.getCProject(fLaunch.getLaunchConfiguration());
            			if (cp != null) {
            				IProject p = cp.getProject();
            				return p.getLocation().toFile();
            			}
            		} else {
            			if (path.isAbsolute()) {
            				File dir = new File(path.toOSString());
            				if (dir.isDirectory()) {
            					return dir;
            				}
            			} else {
            				IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
            				if (res instanceof IContainer && res.exists()) {
            					return res.getLocation().toFile();
            				}
            			}

            			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, 
        						LaunchMessages.getString("AbstractCLaunchDelegate.Working_directory_does_not_exist"), //$NON-NLS-1$
        						new FileNotFoundException(LaunchMessages.getFormattedString(
        								"AbstractCLaunchDelegate.WORKINGDIRECTORY_PATH_not_found", path.toOSString())))); //$NON-NLS-1$
        				requestMonitor.done();
            		}
           		} catch (CoreException e) {
           			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot get working directory", e)); //$NON-NLS-1$
           			requestMonitor.done();
           		}

        		return null;
        	}

        @Override
        public void execute(final RequestMonitor requestMonitor) {
        	File dir = getWorkingDirectory(requestMonitor);
        	if (dir != null) {
        		fCommandControl.queueCommand(
        				new MIEnvironmentCD(fCommandControl.getControlDMContext(), dir.getAbsolutePath()), 
        				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
        	} else {
        		requestMonitor.done();
        	}
        }},
        /*
         * Tell GDB to automatically load or not the shared library symbols
         */
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
    		try {
    			boolean autolib = fLaunch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB,
    					                                                        IGDBLaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT);
                fCommandControl.queueCommand(
                	new MIGDBSetAutoSolib(fCommandControl.getControlDMContext(), autolib), 
                	new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
    		} catch (CoreException e) {
    			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot set shared library option", e)); //$NON-NLS-1$
    			requestMonitor.done();
    		}
        }},
        /*
         * Set the shared library paths
         */
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
      		try {
      		    @SuppressWarnings("unchecked")
    			List<String> p = fLaunch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, 
    					                                                       new ArrayList<String>(1));
   				if (p.size() > 0) {
   					String[] paths = p.toArray(new String[p.size()]);
   	                fCommandControl.queueCommand(
   	                	new MIGDBSetSolibSearchPath(fCommandControl.getControlDMContext(), paths), 
   	                	new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
   				} else {
   	                requestMonitor.done();
   				}
    		} catch (CoreException e) {
                requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot set share library paths", e)); //$NON-NLS-1$
                requestMonitor.done();
    		}
    	}},
    	/*
    	 * Setup the source paths
    	 */
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fLaunch.getSession().getId());
            CSourceLookup sourceLookup = tracker.getService(CSourceLookup.class);
            tracker.dispose();

            CSourceLookupDirector locator = (CSourceLookupDirector)fLaunch.getSourceLocator();
            sourceLookup.setSourceLookupPath(fCommandControl.getGDBDMContext(), 
               	                             locator.getSourceContainers(), requestMonitor);
        }},
        /* 
         * If remote debugging, connect to target.
         */
        new Step() {
        	private boolean fTcpConnection;
            private String fRemoteTcpHost;
            private String fRemoteTcpPort;
            private String fSerialDevice;
            
            private boolean checkConnectionType(RequestMonitor requestMonitor) {
                try {
                	fTcpConnection = fLaunch.getLaunchConfiguration().getAttribute(
                                    IGDBLaunchConfigurationConstants.ATTR_REMOTE_TCP,
                                    false);
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot retrieve connection mode", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }
                return true;
            }
            
            private boolean getSerialDevice(RequestMonitor requestMonitor) {
                try {
                    fSerialDevice = fLaunch.getLaunchConfiguration().getAttribute(
                                    			IGDBLaunchConfigurationConstants.ATTR_DEV, "invalid"); //$NON-NLS-1$
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot retrieve serial device", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }
                return true;
            }
            
            private boolean getTcpHost(RequestMonitor requestMonitor) {
                try {
                    fRemoteTcpHost = fLaunch.getLaunchConfiguration().getAttribute(
                    							IGDBLaunchConfigurationConstants.ATTR_HOST, "invalid"); //$NON-NLS-1$
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot retrieve remote TCP host", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }
                return true;
            }

            private boolean getTcpPort(RequestMonitor requestMonitor) {
                try {
                    fRemoteTcpPort = fLaunch.getLaunchConfiguration().getAttribute(
                                    			IGDBLaunchConfigurationConstants.ATTR_PORT, "invalid"); //$NON-NLS-1$
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot retrieve remote TCP port", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }
                return true;
            }
            
            @Override
            public void execute(final RequestMonitor requestMonitor) {
               	if (fSessionType == SessionType.REMOTE) {
               		if (!checkConnectionType(requestMonitor)) return;
               		
               		if (fTcpConnection) {
                   		if (!getTcpHost(requestMonitor)) return;
                        if (!getTcpPort(requestMonitor)) return;
                    
                        fCommandControl.queueCommand(
                        		new MITargetSelect(fCommandControl.getControlDMContext(), 
                        				           fRemoteTcpHost, fRemoteTcpPort), 
                        	    new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
               		} else {
               			if (!getSerialDevice(requestMonitor)) return;
                    
                        fCommandControl.queueCommand(
                        		new MITargetSelect(fCommandControl.getControlDMContext(), 
                        				           fSerialDevice), 
                        	    new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
               		}
            	} else {
            		requestMonitor.done();
            	}
            }
        },
        /*
        * If attach session, perform the attach. 
        */
       new Step() {
        	private void promptForProcessID(final ILaunchConfiguration config, final DataRequestMonitor<Integer> rm) {
        			IStatus promptStatus = new Status(IStatus.INFO, "org.eclipse.debug.ui", 200/*STATUS_HANDLER_PROMPT*/, "", null); //$NON-NLS-1$//$NON-NLS-2$
        			final IStatus processPromptStatus = new Status(IStatus.INFO, "org.eclipse.dd.gdb.ui", 100, "", null); //$NON-NLS-1$//$NON-NLS-2$

        			final IStatusHandler prompter = DebugPlugin.getDefault().getStatusHandler(promptStatus);
        			
					final Status noPidStatus = new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1,
								                          LaunchMessages.getString("LocalAttachLaunchDelegate.No_Process_ID_selected"), //$NON-NLS-1$
								                          null);

        			if (prompter == null) {
        				rm.setStatus(noPidStatus);
        				rm.done();
        				return;
        			}
        				
        			// Need a job because prompter.handleStatus will block
        			final Job promptForPid = new Job("Prompt for Process") { //$NON-NLS-1$
        				@Override
        				protected IStatus run(IProgressMonitor monitor) {
        					try {
        						Object result = prompter.handleStatus(processPromptStatus, fProcessList);
        						if (result instanceof Integer) {
        							rm.setData((Integer)result);
        						} else {
        							rm.setStatus(noPidStatus);
        						}
        					} catch (CoreException e) {
    							rm.setStatus(noPidStatus);
        					}
        					rm.done();

        					return Status.OK_STATUS;
        				}
        			};

        			if (fSessionType == SessionType.LOCAL) {
        				try {
        					IProcessList list = CCorePlugin.getDefault().getProcessList();
        					if (list != null) fProcessList = list.getProcessList();
        				} catch (CoreException e) {
						} finally {
							// If the list is null, the prompter will deal with it
        					promptForPid.schedule();
        				}
        			} else {
        				fCommandControl.queueCommand(
        						new CLIMonitorListProcesses(fCommandControl.getControlDMContext()), 
        						new DataRequestMonitor<CLIMonitorListProcessesInfo>(getExecutor(), rm) {
        							@Override
        							protected void handleCompleted() {
        								if (isSuccess()) {
        									fProcessList = getData().getProcessList();
        								} else {
        									// The monitor list command is not supported.
        									// Just set the list to empty and let the user
        									// put in the pid directly.
        									fProcessList = new IProcessInfo[0];
        								}

        								promptForPid.schedule();
        							}
        						});
        			}
        	}

        	@Override
        	public void execute(final RequestMonitor requestMonitor) {
        		if (fAttach) {
        			// If we are attaching, get the process id.
        			int pid = -1;
        			try {
        				// have we already been given the pid (maybe from a JUnit test launch or something)
        				pid = fLaunch.getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_ATTACH_PROCESS_ID, -1);
        			} catch (CoreException e) { 
        				// do nothing and fall to below
        			}

        			if (pid != -1) {
						fCommandControl.queueCommand(
								new CLIAttach(fCommandControl.getControlDMContext(), pid), 
								new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));        				
        			} else {
        				promptForProcessID(fLaunch.getLaunchConfiguration(), 
        						           new DataRequestMonitor<Integer>(getExecutor(), requestMonitor) {
        					@Override
        					protected void handleSuccess() {
        						fCommandControl.queueCommand(
        								new CLIAttach(fCommandControl.getControlDMContext(), getData()), 
        								new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
        					}
        				});
        			}
        		} else {
        			requestMonitor.done();
        		}
        	}
        },
        /* 
         * Start tracking the breakpoints once we know we are connected to the target (necessary for remote debugging) 
         */
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
            DsfServicesTracker tracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fLaunch.getSession().getId());
            MIBreakpointsManager bpmService = tracker.getService(MIBreakpointsManager.class);
            tracker.dispose();
        	bpmService.startTrackingBreakpoints(fCommandControl.getGDBDMContext(), requestMonitor);
        }},
        /*
         * Start the program.
         */
        new Step() {
            @Override
            public void execute(final RequestMonitor requestMonitor) {
            	fCommandControl.start(fLaunch, requestMonitor);
            }
        },
    };

    GdbLaunch fLaunch;
    SessionType fSessionType;
    boolean fAttach;

    GDBControl fCommandControl;
    // The list of processes used in the case of an ATTACH session
    IProcessInfo[] fProcessList = null;
	
    public FinalLaunchSequence(DsfExecutor executor, GdbLaunch launch, SessionType sessionType, boolean attach) {
        super(executor);
        fLaunch = launch;
        fSessionType = sessionType;
        fAttach = attach;
    }

    @Override
    public Step[] getSteps() {
        return fSteps;
    }
}

