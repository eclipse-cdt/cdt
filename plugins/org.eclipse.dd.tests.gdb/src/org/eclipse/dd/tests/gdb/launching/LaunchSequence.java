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
package org.eclipse.dd.tests.gdb.launching;

import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
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
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.launch.launching.GdbLaunch;
import org.eclipse.dd.gdb.service.GDBRunControl;
import org.eclipse.dd.gdb.service.command.GDBControl;
import org.eclipse.dd.mi.service.CSourceLookup;
import org.eclipse.dd.mi.service.ExpressionService;
import org.eclipse.dd.mi.service.MIBreakpoints;
import org.eclipse.dd.mi.service.MIBreakpointsManager;
import org.eclipse.dd.mi.service.MIDisassembly;
import org.eclipse.dd.mi.service.MIMemory;
import org.eclipse.dd.mi.service.MIRegisters;
import org.eclipse.dd.mi.service.MIStack;
import org.eclipse.dd.mi.service.command.commands.MIBreakInsert;
import org.eclipse.dd.mi.service.command.commands.MIExecRun;
import org.eclipse.dd.mi.service.command.events.MIStoppedEvent;
import org.eclipse.dd.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.dd.mi.service.command.output.MIInfo;
import org.eclipse.debug.core.DebugException;

public class LaunchSequence extends Sequence {

    public class EntryPointHitEventListener {
        boolean fAborted = false;
        boolean fFinished = false;
        final RequestMonitor fRequestMonitor; 
        
        EntryPointHitEventListener(RequestMonitor requestMonitor) {
            fRequestMonitor = requestMonitor;
        }
        
        @DsfServiceEventHandler 
        public void eventDispatched(@SuppressWarnings("unused")
        MIStoppedEvent e) {
            fFinished = true;
            if (!fAborted) {
                fSession.removeServiceEventListener(this);
                fRequestMonitor.done();
            }
        }
    }

    
    Step[] fSteps = new Step[] {
        // Create and initialize the Connection service.
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                //
                // Create the connection.
                //
                fCommandControl = new GDBControl(
                    fSession, getGDBPath(), fExecPath, GDBControl.SessionType.RUN, 30);
                fCommandControl.initialize(requestMonitor);
            }
        },
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
                    requestMonitor.setStatus(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, -1, "Cannot retrieve the entry point symbol", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }
                return true;
            }
            
            private boolean readStopSymbol(RequestMonitor requestMonitor) {
                try {
                    fStopSymbol = fLaunch.getLaunchConfiguration().getAttribute( ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT );
                } catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, DebugException.CONFIGURATION_INVALID, "Cannot retrieve the entry point symbol", e)); //$NON-NLS-1$
                    requestMonitor.done();
                    return false;
                }                
                return true;
            }
            
            @Override
            public void execute(final RequestMonitor requestMonitor) {
                if (!readStopAtMain(requestMonitor)) return;
                if (!fStopInMain) {
                    requestMonitor.done();
                    return;
                }                    
                
                if (!readStopSymbol(requestMonitor)) return;
                
                // Create a listener to wait for the stopped event, and register as even handler.
                // This handler will execute the requestMonitor.
                final EntryPointHitEventListener entryPointHitListener = new EntryPointHitEventListener(requestMonitor);
                fSession.addServiceEventListener(entryPointHitListener, null);
                
                // Create a time-out, to abort if breakpoint not hit.
                fSession.getExecutor().schedule(
                    new Runnable() { public void run() {
                        // Only process the event if we have not finished yet (hit the breakpoint).
                        if (!entryPointHitListener.fFinished) {
                            // Mark the listener as aborted, and unregister it as event listener.
                            entryPointHitListener.fAborted = true;
                            fSession.removeServiceEventListener(entryPointHitListener);
                            
                            // Submit the error result for the step.
                            requestMonitor.setStatus(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, DebugException.TARGET_REQUEST_FAILED, "Timed out running to entry point.", null)); //$NON-NLS-1$
                            requestMonitor.done();
                        }
                    }},
                    60, TimeUnit.SECONDS);
                
                // Insert a breakpoint at the requested stop symbol.
                fCommandControl.queueCommand(
                    new MIBreakInsert(fCommandControl.getGDBDMContext(), true, false, null, 0, fStopSymbol, 0), 
                    new DataRequestMonitor<MIBreakInsertInfo>(getExecutor(), requestMonitor) { 
                        @Override
                        protected void handleSuccess() {
    
                            // After the break-insert is done, execute the -exec-run command.
                            fCommandControl.queueCommand(
                                new MIExecRun(fCommandControl.getGDBDMContext(), new String[0]), 
                                new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
                                    @Override
                                    protected void handleSuccess() {
                                        // Note : Do we not need to do something with the original requestMonitor?
                                        // Do nothing.  Execution was resumed and the EntryPointHitEventListener
                                        // will resume execution
                                    }
                                }
                            );
                        }
                    });            
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
            fSourceLookup.setSourceLookupDirector(
                fCommandControl.getGDBDMContext(), 
                ((CSourceLookupDirector)fLaunch.getSourceLocator()));
            requestMonitor.done();
        }},
        new Step() { @Override
        public void execute(final RequestMonitor requestMonitor) {
            // Create the low-level breakpoint service 
            final MIBreakpoints bpService = new MIBreakpoints(fSession);
            bpService.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
                @Override
                protected void handleSuccess() {
                	requestMonitor.done();
                }
            }); 
        }},
        new Step() { @Override
            public void execute(final RequestMonitor requestMonitor) {
                // Create high-level breakpoint service and install breakpoints 
                // for the GDB debug context.
                final MIBreakpointsManager bpmService = new MIBreakpointsManager(fSession, CDebugCorePlugin.PLUGIN_ID);
                bpmService.initialize(new RequestMonitor(getExecutor(), requestMonitor) {
                    @Override
                    protected void handleSuccess() {
                        bpmService.startTrackingBreakpoints(fCommandControl.getGDBDMContext(), requestMonitor);
                    }
                }); 
            }},
        new Step() { @Override
        public void execute(RequestMonitor requestMonitor) {
            new MIRegisters(fSession).initialize(requestMonitor);
        }},
        new Step() { @Override
            public void execute(RequestMonitor requestMonitor) {
                new MIDisassembly(fSession).initialize(requestMonitor);
            }},
        /*new Step() { public void execute(RequestMonitor requestMonitor) {
            new GDBVariables(fSession).initialize(requestMonitor);
        }},*/
    };

    final DsfSession fSession;
    final GdbLaunch fLaunch;
    final IPath fExecPath;

    GDBControl fCommandControl;
    CSourceLookup fSourceLookup;

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
