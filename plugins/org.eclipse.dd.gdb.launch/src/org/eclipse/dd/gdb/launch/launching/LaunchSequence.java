/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.gdb.launch.launching;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.debug.mi.core.IGDBServerMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.debug.service.StepQueueManager;
import org.eclipse.dd.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.launch.internal.GdbLaunchPlugin;
import org.eclipse.dd.gdb.service.GDBRunControl;
import org.eclipse.dd.gdb.service.command.GDBControl;
import org.eclipse.dd.gdb.service.command.GDBControl.SessionType;
import org.eclipse.dd.mi.service.CSourceLookup;
import org.eclipse.dd.mi.service.ExpressionService;
import org.eclipse.dd.mi.service.MIBreakpoints;
import org.eclipse.dd.mi.service.MIBreakpointsManager;
import org.eclipse.dd.mi.service.MIDisassembly;
import org.eclipse.dd.mi.service.MIMemory;
import org.eclipse.dd.mi.service.MIModules;
import org.eclipse.dd.mi.service.MIRegisters;
import org.eclipse.dd.mi.service.MIStack;
import org.eclipse.dd.mi.service.command.commands.MIBreakInsert;
import org.eclipse.dd.mi.service.command.commands.MICommand;
import org.eclipse.dd.mi.service.command.commands.MIExecContinue;
import org.eclipse.dd.mi.service.command.commands.MIExecRun;
import org.eclipse.dd.mi.service.command.commands.MITargetSelect;
import org.eclipse.dd.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.dd.mi.service.command.output.MIInfo;
import org.eclipse.debug.core.DebugException;

public class LaunchSequence extends Sequence {

    Step[] fSteps = new Step[] {
        // Create and initialize the Connection service.
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
            	String debugMode = ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN;
            	try {
            		debugMode = fLaunch.getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
            				                                                  ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN );
            	} catch (CoreException e) {
            	}

            	if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN)) {
            		fSessionType = SessionType.RUN;
            	} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_ATTACH)) {
            		fSessionType = SessionType.ATTACH;
            	} else if (debugMode.equals(ICDTLaunchConfigurationConstants.DEBUGGER_MODE_CORE)) {
            		fSessionType = SessionType.CORE;
            	} else if (debugMode.equals(IGDBLaunchConfigurationConstants.DEBUGGER_MODE_REMOTE)) {
            		fSessionType = SessionType.REMOTE;
            	} else {
                	fSessionType = SessionType.RUN;
            	}

                //
                // Create the connection.
                //
                fCommandControl = new GDBControl(fSession, getGDBPath(), fExecPath, fSessionType, 30);
                fCommandControl.initialize(requestMonitor);
            }
        },
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	new GDBRunControl(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new StepQueueManager(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new MIMemory(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new MIModules(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new MIStack(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new ExpressionService(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            fSourceLookup = new CSourceLookup(fSession);
            fSourceLookup.initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
        	CSourceLookupDirector locator = (CSourceLookupDirector)fLaunch.getSourceLocator();
        	
            fSourceLookup.setSourceLookupDirector(fCommandControl.getGDBDMContext(), locator);
            fSourceLookup.setSourceLookupPath(fCommandControl.getGDBDMContext(), 
            		                          locator.getSourceContainers(), requestMonitor);
        }},
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
            // Create the low-level breakpoint service 
            final MIBreakpoints bpService = new MIBreakpoints(fSession);
            bpService.initialize(new RequestMonitor(getExecutor(), requestMonitor));
        }},
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
            // Create high-level breakpoint service and install breakpoints 
            // for the GDB debug context.
            fBpmService = new MIBreakpointsManager(fSession, CDebugCorePlugin.PLUGIN_ID);
            fBpmService.initialize(new RequestMonitor(getExecutor(), requestMonitor)); 
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new MIRegisters(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new MIDisassembly(fSession).initialize(requestMonitor);
        }},
        /* If remote debugging, connect to target */
        new Step() {
        	private boolean fTcpConnection;
            private String fRemoteTcpHost;
            private String fRemoteTcpPort;
            private String fSerialDevice;
            
            private boolean checkConnectionType(RequestMonitor requestMonitor) {
                try {
                	fTcpConnection = fLaunch.getLaunchConfiguration().getAttribute(
                                    IGDBServerMILaunchConfigurationConstants.ATTR_REMOTE_TCP,
                                    false);
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbLaunchPlugin.PLUGIN_ID, -1, "Cannot retrieve connection mode", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }
                return true;
            }
            
            private boolean getSerialDevice(RequestMonitor requestMonitor) {
                try {
                    fSerialDevice = fLaunch.getLaunchConfiguration().getAttribute(
                                    			IGDBServerMILaunchConfigurationConstants.ATTR_DEV, "invalid");
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbLaunchPlugin.PLUGIN_ID, -1, "Cannot retrieve serial device", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }
                return true;
            }
            
            private boolean getTcpHost(RequestMonitor requestMonitor) {
                try {
                    fRemoteTcpHost = fLaunch.getLaunchConfiguration().getAttribute(
                    							IGDBServerMILaunchConfigurationConstants.ATTR_HOST, "invalid");
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbLaunchPlugin.PLUGIN_ID, -1, "Cannot retrieve remote TCP host", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }
                return true;
            }

            private boolean getTcpPort(RequestMonitor requestMonitor) {
                try {
                    fRemoteTcpPort = fLaunch.getLaunchConfiguration().getAttribute(
                                    			IGDBServerMILaunchConfigurationConstants.ATTR_PORT, "invalid");
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbLaunchPlugin.PLUGIN_ID, -1, "Cannot retrieve remote TCP port", e)); //$NON-NLS-1$
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
                        		new MITargetSelect((IContainerDMContext)fCommandControl.getControlDMContext(), 
                        				            fRemoteTcpHost, fRemoteTcpPort), 
                        	    new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
               		} else {
               			if (!getSerialDevice(requestMonitor)) return;
                    
                        fCommandControl.queueCommand(
                        		new MITargetSelect((IContainerDMContext)fCommandControl.getControlDMContext(), 
                        				           fSerialDevice), 
                        	    new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
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
        	fBpmService.startTrackingBreakpoints(fCommandControl.getGDBDMContext(), requestMonitor);
        }},
        /*
         * If needed, insert breakpoint at main and run to it.
         */
        new Step() {
            private boolean fStopInMain = false;
            private String fStopSymbol = null;

            /**
             * @return The return value actually indicates whether the get operation succeeded, 
             * not whether to stop.
             */
            private boolean readStopAtMain(RequestMonitor requestMonitor) {
                try {
                    fStopInMain = fLaunch.getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false );
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbLaunchPlugin.PLUGIN_ID, -1, "Cannot retrieve the entry point symbol", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }
                return true;
            }
            
            private boolean readStopSymbol(RequestMonitor requestMonitor) {
                try {
                    fStopSymbol = fLaunch.getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT );
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, GdbLaunchPlugin.PLUGIN_ID, DebugException.CONFIGURATION_INVALID, "Cannot retrieve the entry point symbol", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }                
                return true;
            }
            
            @Override
            public void execute(final RequestMonitor requestMonitor) {
            	final MICommand<MIInfo> execCommand;
            	if (fSessionType == SessionType.REMOTE) {
            		// When doing remote debugging, we use -exec-continue instead of -exec-run 
            	    execCommand = new MIExecContinue((IContainerDMContext)fCommandControl.getControlDMContext());
            	} else {
            		execCommand = new MIExecRun((IContainerDMContext)fCommandControl.getControlDMContext(), new String[0]);	
            	}
            	
                if (!readStopAtMain(requestMonitor)) return;
                if (!fStopInMain) {
                	// Just start the program.
    				fCommandControl.queueCommand(execCommand, new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
                } else {
                    if (!readStopSymbol(requestMonitor)) return;
                
                    // Insert a breakpoint at the requested stop symbol.
                    fCommandControl.queueCommand(
                    		new MIBreakInsert(
                    				(IBreakpointsTargetDMContext)fCommandControl.getControlDMContext(), 
                    				true, false, null, 0, fStopSymbol, 0), 
                    				new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), requestMonitor) { 
                    			@Override
                    			protected void handleOK() {

                    				// After the break-insert is done, execute the -exec-run or -exec-continue command.
                    				fCommandControl.queueCommand(execCommand, new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
                    			}
                    		});
                }
            }
        },
    };

    DsfSession fSession;
    GdbLaunch fLaunch;
    IPath fExecPath;
    SessionType fSessionType;

    GDBControl fCommandControl;
    CSourceLookup fSourceLookup;
    MIBreakpointsManager fBpmService;

    public LaunchSequence(DsfSession session, GdbLaunch launch, IPath execPath) {
        super(session.getExecutor());
        fSession = session;
        fLaunch = launch;
        fExecPath = execPath;
    }
    
    @Override
    public Step[] getSteps() {
        return fSteps;
    }
    
    private IPath getGDBPath() {
        IPath retVal = new Path("gdb.exe"); //$NON-NLS-1$
        try {
            retVal = new Path( fLaunch.getLaunchConfiguration().getAttribute( IMILaunchConfigurationConstants.ATTR_DEBUG_NAME, IMILaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT ) );
        } catch (CoreException e) {
        }
        return retVal;
    }

}
