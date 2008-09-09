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

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.dd.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.gdb.internal.GdbPlugin;
import org.eclipse.dd.gdb.internal.provisional.IGDBLaunchConfigurationConstants;
import org.eclipse.dd.gdb.internal.provisional.actions.IConnect;
import org.eclipse.dd.gdb.internal.provisional.service.SessionType;
import org.eclipse.dd.gdb.internal.provisional.service.command.IGDBControl;
import org.eclipse.dd.mi.service.CSourceLookup;
import org.eclipse.dd.mi.service.IMIProcesses;
import org.eclipse.dd.mi.service.MIBreakpointsManager;
import org.eclipse.dd.mi.service.command.commands.CLISource;
import org.eclipse.dd.mi.service.command.commands.MIEnvironmentCD;
import org.eclipse.dd.mi.service.command.commands.MIFileExecAndSymbols;
import org.eclipse.dd.mi.service.command.commands.MIGDBSetArgs;
import org.eclipse.dd.mi.service.command.commands.MIGDBSetAutoSolib;
import org.eclipse.dd.mi.service.command.commands.MIGDBSetBreakpointApply;
import org.eclipse.dd.mi.service.command.commands.MIGDBSetNonStop;
import org.eclipse.dd.mi.service.command.commands.MIGDBSetSolibSearchPath;
import org.eclipse.dd.mi.service.command.commands.MITargetSelect;
import org.eclipse.dd.mi.service.command.commands.RawCommand;
import org.eclipse.dd.mi.service.command.output.MIInfo;

public class FinalLaunchSequence extends Sequence {

    Step[] fSteps = new Step[] {
        /*
         * Fetch the control service for later use
         */
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            fTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fLaunch.getSession().getId());
            requestMonitor.done();
        }
        @Override
        public void rollBack(RequestMonitor requestMonitor) {
            if (fTracker != null) fTracker.dispose();
            fTracker = null;
            requestMonitor.done();
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            fCommandControl = fTracker.getService(IGDBControl.class);
            fProcService = fTracker.getService(IMIProcesses.class);
            if (fCommandControl == null || fProcService == null) {
        		requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot obtain service", null)); //$NON-NLS-1$
            }

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
        					new CLISource(fCommandControl.getContext(), gdbinitFile), 
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
    		boolean noFileCommand = IGDBLaunchConfigurationConstants.DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP_DEFAULT;
    		try {
    			noFileCommand = fLaunch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP,
    			                                                              IGDBLaunchConfigurationConstants.DEBUGGER_USE_SOLIB_SYMBOLS_FOR_APP_DEFAULT);
    		} catch (CoreException e) {
    			requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Cannot read use solib symbols for app options", e)); //$NON-NLS-1$
    			requestMonitor.done();
    			return;
    		}

        	final IPath execPath = fCommandControl.getExecutablePath();
        	if (!noFileCommand && execPath != null && !execPath.isEmpty()) {
        		fCommandControl.queueCommand(
       				new MIFileExecAndSymbols(fCommandControl.getContext(), 
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
        					new MIGDBSetArgs(fCommandControl.getContext(), args), 
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
        				new MIEnvironmentCD(fCommandControl.getContext(), dir.getAbsolutePath()), 
        				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
        	} else {
        		requestMonitor.done();
        	}
        }},
        /*
         * Tell GDB to have breakpoint affect all processes being debugged.
         * The user should actually make this decision.  See bug 244053
         */
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
            fCommandControl.queueCommand(
                    // This command will fail for GDBs without multi-process support, and that is ok
                    new MIGDBSetBreakpointApply(fCommandControl.getContext(), true),
                    new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
                        @Override
                        protected void handleCompleted() {
                            requestMonitor.done();
                        }
                    });
        }},
    	/*
    	 * Enable non-stop mode if necessary
    	 */
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
        	boolean isNonStop = false;
    		try {
    			isNonStop = fLaunch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
                        IGDBLaunchConfigurationConstants.DEBUGGER_NON_STOP_DEFAULT);
        	} catch (CoreException e) {    		
        	}

        	// GDBs that don't support non-stop don't allow you to set it to false.
        	// We really should set it to false when GDB supports it though.
        	// Something to fix later.
        	if (isNonStop) {
        		// The raw commands should not be necessary in the official GDB release
        		fCommandControl.queueCommand(
        				new RawCommand(fCommandControl.getContext(), "set breakpoint always-inserted"), //$NON-NLS-1$
        				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
        					@Override
        					protected void handleSuccess() {
        						String asyncCommandStr;
        						if (fSessionType == SessionType.REMOTE) {
        							asyncCommandStr = "maint set remote-async 1"; //$NON-NLS-1$
        						} else {
        							asyncCommandStr = "maint set linux-async 1"; //$NON-NLS-1$
        						}

        						fCommandControl.queueCommand(
        								new RawCommand(fCommandControl.getContext(), asyncCommandStr),
        								new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
        									@Override
        									protected void handleSuccess() {
        										fCommandControl.queueCommand(
        												new RawCommand(fCommandControl.getContext(), "set pagination off"),  //$NON-NLS-1$ 
        												new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
        													@Override
        													protected void handleSuccess() {
        														fCommandControl.queueCommand(
        																new MIGDBSetNonStop(fCommandControl.getContext(), true), 
        																new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
        													}
        												});
        									}
        								});
        					}
        								});
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
                	new MIGDBSetAutoSolib(fCommandControl.getContext(), autolib), 
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
        public void execute(final RequestMonitor requestMonitor) {
      		try {
      		    @SuppressWarnings("unchecked")
    			List<String> p = fLaunch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, 
    					                                                       new ArrayList<String>(1));
   				if (p.size() > 0) {
   					String[] paths = p.toArray(new String[p.size()]);
   	                fCommandControl.queueCommand(
   	                	new MIGDBSetSolibSearchPath(fCommandControl.getContext(), paths), 
   	                	new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
   	                		@Override
   	                		protected void handleSuccess() {
// Sysroot is not available in GDB6.6 and will make the launch fail in that case.
// Let's remove it for now
   	                			requestMonitor.done();
//   	                			// If we are able to set the solib-search-path,
//   	                			// we should disable the sysroot variable, as indicated
//   	                			// in the GDB documentation.  This is to avoid the sysroot
//   	                			// variable finding libraries that were not meant to be found.
//   	        	                fCommandControl.queueCommand(
//   	        	   	                	new MIGDBSetSysroot(fCommandControl.getContext()), 
//   	        	   	                	new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
   	                		};
   	                	});
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
            CSourceLookup sourceLookup = fTracker.getService(CSourceLookup.class);

            CSourceLookupDirector locator = (CSourceLookupDirector)fLaunch.getSourceLocator();
            sourceLookup.setSourceLookupPath((ISourceLookupDMContext)fCommandControl.getContext(), 
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
                        		new MITargetSelect(fCommandControl.getContext(), 
                        				           fRemoteTcpHost, fRemoteTcpPort), 
                        	    new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
               		} else {
               			if (!getSerialDevice(requestMonitor)) return;
                    
                        fCommandControl.queueCommand(
                        		new MITargetSelect(fCommandControl.getContext(), 
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
        				fProcService.attachDebuggerToProcess(
        						fProcService.createProcessContext(fCommandControl.getContext(), Integer.toString(pid)),
        						new DataRequestMonitor<IDMContext>(getExecutor(), requestMonitor));
        			} else {
        				IConnect connectCommand = (IConnect)fLaunch.getSession().getModelAdapter(IConnect.class);
        				if (connectCommand != null) {
       						connectCommand.connect(requestMonitor);
        				} else {
    						requestMonitor.done();
        				}
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
            MIBreakpointsManager bpmService = fTracker.getService(MIBreakpointsManager.class);
        	bpmService.startTrackingBreakpoints((IBreakpointsTargetDMContext)fCommandControl.getContext(), requestMonitor);
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
        /*
         * Cleanup
         */
        new Step() {
            @Override
            public void execute(final RequestMonitor requestMonitor) {
            	fTracker.dispose();
                fTracker = null;
                requestMonitor.done();
            }
        },
    };

    GdbLaunch fLaunch;
    SessionType fSessionType;
    boolean fAttach;

    IGDBControl fCommandControl;
    IMIProcesses fProcService;
    DsfServicesTracker fTracker;
        
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

