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
package org.eclipse.cdt.dsf.gdb.internal.ui.actions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.actions.IReverseToggleHandler;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * @since 2.0
 */
@Immutable
public class GdbReverseToggleCommand implements IReverseToggleHandler {

	private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    
    public GdbReverseToggleCommand(DsfSession session) {
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
    }    

    public void dispose() {
        fTracker.dispose();
    }

	public boolean canToggleReverse(ISelection debugContext) {
        final ICommandControlDMContext dmc = getContext(debugContext);
        
        if (dmc == null) {
        	return false;
        }

        Query<Boolean> canSetReverseMode = new Query<Boolean>() {
        	@Override
        	public void execute(DataRequestMonitor<Boolean> rm) {
        		IReverseRunControl runControl = fTracker.getService(IReverseRunControl.class);

        		if (runControl != null) {
        			runControl.canEnableReverseMode(dmc, rm);
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
    
	public void toggleReverse(ISelection debugContext) {
        final ICommandControlDMContext dmc = getContext(debugContext);
        
        if (dmc == null) {
        	return;
        }

      	Query<Object> setReverseMode = new Query<Object>() {
            @Override
            public void execute(final DataRequestMonitor<Object> rm) {
       			final IReverseRunControl runControl = fTracker.getService(IReverseRunControl.class);

       			if (runControl != null) {
       				runControl.isReverseModeEnabled(dmc, 
       				                                new DataRequestMonitor<Boolean>(fExecutor, rm) {
       					@Override
       					public void handleSuccess() {
       	       				runControl.enableReverseMode(dmc, !getData(), rm);
       					}
       				});
       			} else {
       				rm.setData(false);
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
	
	private ICommandControlDMContext getContext(ISelection debugContext) {
        if (debugContext instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) debugContext;
            if (!ss.isEmpty()) {
                Object object = ss.getFirstElement();
                if (object instanceof IDMVMContext) {
                	return DMContexts.getAncestorOfType(((IDMVMContext)object).getDMContext(), ICommandControlDMContext.class);
                }
            }
        }
        
        return null;
	}

	public boolean isReverseToggled(ISelection debugContext) {
        final ICommandControlDMContext dmc = getContext(debugContext);
        return isReverseToggled(dmc);
	}
	
   	public boolean isReverseToggled(final ICommandControlDMContext dmc) {

        if (dmc == null) {
        	return false;
        }

      	Query<Boolean> isToggledQuery = new Query<Boolean>() {
            @Override
            public void execute(final DataRequestMonitor<Boolean> rm) {
       			final IReverseRunControl runControl = fTracker.getService(IReverseRunControl.class);

       			if (runControl != null) {
       				runControl.isReverseModeEnabled(dmc, rm);
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
