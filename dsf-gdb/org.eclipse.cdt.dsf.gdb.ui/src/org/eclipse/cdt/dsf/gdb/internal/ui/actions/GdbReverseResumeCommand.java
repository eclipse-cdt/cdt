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
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.gdb.actions.IReverseResumeHandler;
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
public class GdbReverseResumeCommand implements IReverseResumeHandler {
    
	private final DsfExecutor fExecutor;
    private final DsfServicesTracker fTracker;
    
    public GdbReverseResumeCommand(DsfSession session) {
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(GdbUIPlugin.getBundleContext(), session.getId());
    }    

    public void dispose() {
        fTracker.dispose();
    }

    public boolean canReverseResume(ISelection debugContext) {
        final IExecutionDMContext dmc = getContext(debugContext);
        
        if (dmc == null) {
        	return false;
        }

        Query<Boolean> canReverseResume = new Query<Boolean>() {
            @Override
            public void execute(DataRequestMonitor<Boolean> rm) {
       			IReverseRunControl runControl = fTracker.getService(IReverseRunControl.class);

       			if (runControl != null) {
       				runControl.canReverseResume(dmc, rm);
       			} else {
       				rm.setData(false);
       				rm.done();
       			}
       		}
       	};
    	try {
    		fExecutor.execute(canReverseResume);
			return canReverseResume.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
        } catch (RejectedExecutionException e) {
        	// Can be thrown if the session is shutdown
        }

		return false;
    }
    
    public void reverseResume(ISelection debugContext) {
        final IExecutionDMContext dmc = getContext(debugContext);
        
        if (dmc == null) {
        	return;
        }

        Query<Object> reverseResume = new Query<Object>() {
            @Override
            public void execute(DataRequestMonitor<Object> rm) {
       			IReverseRunControl runControl = fTracker.getService(IReverseRunControl.class);

       			if (runControl != null) {
       				runControl.reverseResume(dmc, rm);
       			} else {
       				rm.setData(false);
       				rm.done();
       			}
       		}
       	};
    	try {
    		fExecutor.execute(reverseResume);
    		reverseResume.get();
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
        } catch (RejectedExecutionException e) {
        	// Can be thrown if the session is shutdown
        }
    }

	private IExecutionDMContext getContext(ISelection debugContext) {
        if (debugContext instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) debugContext;
            if (!ss.isEmpty()) {
                Object object = ss.getFirstElement();
                if (object instanceof IDMVMContext) {
                	return DMContexts.getAncestorOfType(((IDMVMContext)object).getDMContext(), IExecutionDMContext.class);
                }
            }
        }
        
        return null;
	}
}
