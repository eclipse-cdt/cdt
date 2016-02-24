/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Marc Khouzam (Ericsson) - Listen for IReverseModeChangedDMEvent (Bug 399163)
 *     Intel Corporation - Added Reverse Debugging BTrace support
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.model.IChangeReverseMethodHandler;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl.IReverseModeChangedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl2;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.services.IEvaluationService;

/**
 * Command that toggles the Reverse Debugging feature
 * 
 * @since 2.1
 */
public class GdbReverseToggleCommand extends AbstractDebugCommand implements IChangeReverseMethodHandler {
    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    private final DsfSession fSession;

    private ReverseTraceMethod fTraceMethod = null;
    
    public GdbReverseToggleCommand(DsfSession session) {
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
        fSession = session;
        fTraceMethod = ReverseTraceMethod.STOP_TRACE;
        try {
            fExecutor.execute(new DsfRunnable() {
                @Override
                public void run() {
                	fSession.addServiceEventListener(GdbReverseToggleCommand.this, null);
                }
            });
        } catch(RejectedExecutionException e) {}
    }    

    public void dispose() {
        try {
            fExecutor.execute(new DsfRunnable() {
                @Override
                public void run() {
                	fSession.removeServiceEventListener(GdbReverseToggleCommand.this);
                }
            });
        } catch (RejectedExecutionException e) {
            // Session already gone.
        }
        fTracker.dispose();
    }

    @Override
    protected void doExecute(Object[] targets, IProgressMonitor monitor, final IRequest request) throws CoreException {
    	if (targets.length != 1) {
    		return;
    	}

    	IDMContext dmc = ((IDMVMContext)targets[0]).getDMContext();
        final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);  
        if (controlDmc == null) {
        	return;
        }

      	Query<Object> setReverseMode = new Query<Object>() {
            @Override
            public void execute(final DataRequestMonitor<Object> rm) {
                   final IReverseRunControl2 runControl = fTracker.getService(IReverseRunControl2.class);

                   if (runControl != null) {
                       ReverseTraceMethod traceMethod = fTraceMethod;
                       if (fTraceMethod == ReverseTraceMethod.HARDWARE_TRACE) {
                           if (Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
                                IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE,
                                IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE,
                                null).equals(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_BRANCH_TRACE)) {
                               traceMethod = ReverseTraceMethod.BRANCH_TRACE; // Branch Trace
                        } else if (Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
                                IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_HARDWARE,
                                IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_GDB_TRACE,
                                null).equals(IGdbDebugPreferenceConstants.PREF_REVERSE_TRACE_METHOD_PROCESSOR_TRACE)) {
                            traceMethod = ReverseTraceMethod.PROCESSOR_TRACE; // Processor Trace
                        } else {
                            traceMethod = ReverseTraceMethod.GDB_TRACE; // GDB Selected Option
                        }
                       }
                                  runControl.enableReverseMode(controlDmc, traceMethod, new DataRequestMonitor<Boolean>(fExecutor, rm) {
                                   @Override
                                   public void handleError() {
                                       // Call the parent function
                                       // Since otherwise the status is not updated
                                       super.handleError();
                                       // Here we avoid setting any status other than OK, since we want to
                                       // avoid the default dialog box from eclipse and we propagate the error
                                       // with the plugin specific code of 1, here the ReverseToggleCommandHandler
                                       //  interprets it as, the selected trace method is not available
                                       request.setStatus(new Status(IStatus.OK, GdbPlugin.PLUGIN_ID, 1, Messages.GdbReverseDebugging_HardwareTracingNotAvailable, null));
                                   }
                               });

                   } else {
                       final IReverseRunControl runControl_old = fTracker.getService(IReverseRunControl.class);
                       if (runControl_old != null) {
                           if(fTraceMethod != ReverseTraceMethod.STOP_TRACE && fTraceMethod != ReverseTraceMethod.FULL_TRACE) {
                               runControl_old.enableReverseMode(controlDmc, false, rm); // Swtich Off tracing
                               request.setStatus(new Status(IStatus.OK, GdbPlugin.PLUGIN_ID, 1, Messages.GdbReverseDebugging_HardwareTracingNotAvailable, null));
                               return;
                           }
                           runControl_old.isReverseModeEnabled(controlDmc,
                                                           new DataRequestMonitor<Boolean>(fExecutor, rm) {
                               @Override
                               public void handleSuccess() {
                                   runControl_old.enableReverseMode(controlDmc, !getData(), rm);
                               }
                           });
                       } else {
                           rm.done();
                       }
       			}
       		}
       	};
    	try {
    		fExecutor.execute(setReverseMode);
    		setReverseMode.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
        } catch (RejectedExecutionException e) {
        	// Can be thrown if the session is shutdown
        }
    }

    @Override
	protected boolean isExecutable(Object[] targets, IProgressMonitor monitor, IEnabledStateRequest request)
        throws CoreException 
    {
    	if (targets.length != 1) {
    		return false;
    	}
    	
    	IDMContext dmc = ((IDMVMContext)targets[0]).getDMContext();
        final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
        final IExecutionDMContext execDmc = DMContexts.getAncestorOfType(dmc, IExecutionDMContext.class);
        if (controlDmc == null && execDmc == null) {
        	return false;
        }

        Query<Boolean> canSetReverseMode = new Query<Boolean>() {
        	@Override
        	public void execute(DataRequestMonitor<Boolean> rm) {
        		IReverseRunControl runControl = fTracker.getService(IReverseRunControl.class);

        		// Only allow to toggle reverse if the program is suspended.
        		// When the program is running, GDB will not answer our command
        		// in toggleReverse() and since it is blocking, it will hang the entire UI! 
        		if (runControl != null && 
        			runControl instanceof IRunControl && ((IRunControl)runControl).isSuspended(execDmc)) {
        			runControl.canEnableReverseMode(controlDmc, rm);
        		} else {
        			rm.setData(false);
        			rm.done();
        		}
        	}
        };
        try {
        	fExecutor.execute(canSetReverseMode);
        	return canSetReverseMode.get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (RejectedExecutionException e) {
        	// Can be thrown if the session is shutdown
        }

		return false;
    }
    
    @Override
	protected Object getTarget(Object element) {
    	if (element instanceof IDMVMContext) {
    		return element;
    	}
        return null;
    }

    @Override
	protected boolean isRemainEnabled(IDebugCommandRequest request) {
		return true;
	}
    
    @Override
    public boolean toggleNeedsUpdating() {
    	return true;
    }

    @Override
    public boolean isReverseToggled(Object context) {
    	IDMContext dmc;

    	if (context instanceof IDMContext) {
    		dmc = (IDMContext)context;
    	} else if (context instanceof IDMVMContext) {
    		dmc = ((IDMVMContext)context).getDMContext();
    	} else {
    		return false;
    	}

    	final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
    	if (controlDmc == null) {
    		return false;
    	}

    	Query<Boolean> isToggledQuery = new Query<Boolean>() {
    		@Override
    		public void execute(final DataRequestMonitor<Boolean> rm) {
    			final IReverseRunControl runControl = fTracker.getService(IReverseRunControl.class);

    			if (runControl != null) {
    				runControl.isReverseModeEnabled(controlDmc, rm);
    			} else {
    				rm.setData(false);
    				rm.done();
    			}
    		}
    	};
    	try {
    		fExecutor.execute(isToggledQuery);
    		return isToggledQuery.get();
    	} catch (InterruptedException e) {
    	} catch (ExecutionException e) {
    	} catch (RejectedExecutionException e) {
    		// Can be thrown if the session is shutdown
    	}

    	return false;
    }
    
    /**
     * @noreference This method is not intended to be referenced by clients.
     */
    @DsfServiceEventHandler 
    public void eventDispatched(IReverseModeChangedDMEvent e) {
    	new WorkbenchJob("") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
		        // Request re-evaluation of property "org.eclipse.cdt.debug.ui.isReverseDebuggingEnabled" to update 
			    // visibility of reverse stepping commands.
			    IEvaluationService exprService = PlatformUI.getWorkbench().getService(IEvaluationService.class);
			    if (exprService != null) {
			        exprService.requestEvaluation("org.eclipse.cdt.debug.ui.isReverseDebuggingEnabled"); //$NON-NLS-1$
			    }
			    // Refresh reverse toggle commands with the new state of reverse enabled. 
			    // This is in order to keep multiple toggle actions in UI in sync.
			    ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		        if (commandService != null) {
		           commandService.refreshElements("org.eclipse.cdt.debug.ui.command.reverseToggle", null); //$NON-NLS-1$
		        }
		        
				return Status.OK_STATUS;
			}
		}.schedule();
    }

    @Override
    public void setTraceMethod(ReverseTraceMethod traceMethod) {
        fTraceMethod = traceMethod;
    }

    @Override
    public ReverseTraceMethod getTraceMethod(final Object context) {
        IDMContext dmc;

        if (context instanceof IDMContext) {
            dmc = (IDMContext)context;
        } else if (context instanceof IDMVMContext) {
            dmc = ((IDMVMContext)context).getDMContext();
        } else {
            return ReverseTraceMethod.STOP_TRACE;
        }

        final ICommandControlDMContext controlDmc = DMContexts.getAncestorOfType(dmc, ICommandControlDMContext.class);
        if (controlDmc == null) {
            return ReverseTraceMethod.STOP_TRACE;
        }

        Query<ReverseTraceMethod> ReverseMethodQuery = new Query<ReverseTraceMethod>() {
        @Override
            public void execute(final DataRequestMonitor<ReverseTraceMethod> rm) {
                final IReverseRunControl2 runControl = fTracker.getService(IReverseRunControl2.class);
                if (runControl != null) {
                    	runControl.getReverseTraceMethod(controlDmc, rm);
                    }
                else {
                    rm.setData(ReverseTraceMethod.INVALID);
                    rm.done();
                }
              }
            };
            try {
                fExecutor.execute(ReverseMethodQuery);
                ReverseTraceMethod returnedTrace =  ReverseMethodQuery.get();
                if (returnedTrace == ReverseTraceMethod.INVALID)
                	return isReverseToggled(context) ? ReverseTraceMethod.FULL_TRACE : ReverseTraceMethod.STOP_TRACE ;
                else 
                	return (returnedTrace == ReverseTraceMethod.BRANCH_TRACE ||
                			returnedTrace == ReverseTraceMethod.PROCESSOR_TRACE ||
                			returnedTrace == ReverseTraceMethod.GDB_TRACE ) ? ReverseTraceMethod.HARDWARE_TRACE : returnedTrace;
            } catch (InterruptedException e) {
            } catch (ExecutionException e) {
            } catch (RejectedExecutionException e) {
            }

        return ReverseTraceMethod.STOP_TRACE;
    }
}
