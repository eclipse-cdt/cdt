/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.debug.core.model.IReverseToggleHandler;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.IRequest;
import org.eclipse.debug.core.commands.AbstractDebugCommand;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

/**
 * Command that toggles the Reverse Debugging feature
 * 
 * @since 2.1
 */
public class GdbReverseToggleCommand extends AbstractDebugCommand implements IReverseToggleHandler {
    private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    
    public GdbReverseToggleCommand(DsfSession session) {
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
    }    

    public void dispose() {
        fTracker.dispose();
    }

    @Override
	protected void doExecute(Object[] targets, IProgressMonitor monitor, IRequest request) throws CoreException {
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
       			final IReverseRunControl runControl = fTracker.getService(IReverseRunControl.class);

       			if (runControl != null) {
       				runControl.isReverseModeEnabled(controlDmc, 
       				                                new DataRequestMonitor<Boolean>(fExecutor, rm) {
       					@Override
       					public void handleSuccess() {
       	       				runControl.enableReverseMode(controlDmc, !getData(), rm);
       					}
       				});
       			} else {
       				rm.done();
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
}
