/*******************************************************************************
 * Copyright (c) 2011 Texas Instruments, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dobrin Alexiev (Texas Instruments) - initial API and implementation (bug 336876)
********************************************************************************/

package org.eclipse.cdt.dsf.debug.internal.ui.debugview.layout.actions;

import java.util.HashSet;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.internal.provisional.service.IExecutionContextTranslator;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.debug.core.commands.IDebugCommandHandler;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;

/**
 * @since 2.2
 */
@SuppressWarnings("restriction")
public abstract class DsfDebugViewLayoutCommand implements IDebugCommandHandler{
	
    protected final DsfExecutor fExecutor;
    protected final DsfServicesTracker fTracker;
    protected static IExecutionDMContext[] EMPTY_ARRAY = new IExecutionDMContext[0];
	
    public DsfDebugViewLayoutCommand(DsfSession session) {
        fExecutor = session.getExecutor();
        fTracker = new DsfServicesTracker(DsfUIPlugin.getBundleContext(), session.getId());
    }    

    public void dispose() {
        fTracker.dispose();
    }

    /**
     * 
     * @param request
     * @return set of IExecutionDMContext if: 
     * 		- all elements are from a DSF session. 
     * 		- all elements are from the same DSF session.
     */
    protected IExecutionDMContext[] getDMContexts( IDebugCommandRequest request) {
    	
    	HashSet<IExecutionDMContext> ret = new HashSet<IExecutionDMContext>();
    	String sessionId = null; 
    	
    	for( Object obj : request.getElements()) {
    		if(!( obj instanceof IDMVMContext ))
    			return EMPTY_ARRAY;
    		
    		IDMContext dmContext = ((IDMVMContext)obj).getDMContext(); 
    		IExecutionDMContext exeContext = DMContexts.getAncestorOfType(dmContext, IExecutionDMContext.class);
    		
    		if( exeContext == null)
    			return EMPTY_ARRAY;

    		// make sure all elements are from the same DSF session. 
    		if( sessionId == null) {
    			sessionId = dmContext.getSessionId();
    		}
    		else {
    			if( !sessionId.equals(dmContext.getSessionId()))
    				return EMPTY_ARRAY;
    		}
    		
    		ret.add(exeContext);	
    	}
    	return ret.toArray(new IExecutionDMContext[0]);
    }
    
	@Override
	public void canExecute(final IEnabledStateRequest request) {
		final IExecutionDMContext[] executionContexts = getDMContexts( request);
		if( executionContexts.length > 0 && !fExecutor.isTerminated()) {
	        fExecutor.submit(new DsfRunnable() {
				@Override
				public void run() {
					IExecutionContextTranslator translator = fTracker.getService(IExecutionContextTranslator.class);
					if( translator != null) {
						canExecuteOnDsfThread(translator, executionContexts, 
		                    new DataRequestMonitor<Boolean>(fExecutor, null) {
			                    @Override
			                    protected void handleCompleted() {
			                    	boolean canExecute = isSuccess() && getData();
			                    	request.setEnabled(canExecute);
			                    	request.done();
			                    }
			                });
						
					} else {
						request.setEnabled(false);
						request.done();
					}
				} 
	        });
		} 
		else {
			request.setEnabled(false);
			request.done();
		}
	}

	@Override
	public boolean execute(final IDebugCommandRequest request) {
		final IExecutionDMContext[] executionContexts = getDMContexts( request);
		if( executionContexts.length > 0 && !fExecutor.isTerminated()) {
	        fExecutor.submit(new DsfRunnable() {
				@Override
				public void run() {
					IExecutionContextTranslator translator = fTracker.getService(IExecutionContextTranslator.class);
					if( translator != null) {
						executeOnDsfThread(translator, executionContexts, 
		                    new RequestMonitor(fExecutor, null) {
			                    @Override
			                    protected void handleCompleted() {
			                    	request.done();
			                    }
			                });
					}
					else
						request.done();
				} 
	        });
			return false;
		}
        request.done();
		return true;
	}
	
	abstract void executeOnDsfThread( IExecutionContextTranslator translator, IExecutionDMContext[] contexts, RequestMonitor requestMonitor);
	abstract void canExecuteOnDsfThread( IExecutionContextTranslator translator, IExecutionDMContext[] contexts, DataRequestMonitor<Boolean> rm);
}
