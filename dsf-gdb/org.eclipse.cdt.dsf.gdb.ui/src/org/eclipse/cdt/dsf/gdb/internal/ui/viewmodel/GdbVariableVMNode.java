/*******************************************************************************
 * Copyright (c) 2010 Freescale Semiconductor. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Freescale Semiconductor - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel;

import org.eclipse.cdt.debug.internal.core.ICWatchpointTarget;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMAddress;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.SyncVariableDataAccess;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.Status;

/**
 * Specialization of DSF's VariableVMNode. See
 * {@link GdbVariableVMNode#createVMContext(IDMContext)} for why this is needed.
 */
public class GdbVariableVMNode extends VariableVMNode {

	/**
	 * Specialization of VariableVMNode.VariableExpressionVMC that participates
	 * in the "Add Watchpoint" object contribution action.
	 */
	public class GdbVariableExpressionVMC extends VariableVMNode.VariableExpressionVMC implements ICWatchpointTarget {
        
		/**
		 * Constructor (passthru)
		 */
		public GdbVariableExpressionVMC(IDMContext dmc) {
            super(dmc);
        }
        
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.internal.core.IWatchpointTarget#getSize()
		 */
		public void getSize(final ICWatchpointTarget.GetSizeRequest request) {
			final IExpressionDMContext exprDmc = DMContexts.getAncestorOfType(getDMContext(), IExpressionDMContext.class);
			if (exprDmc != null) {
	            getSession().getExecutor().execute(new Runnable() {
	                public void run() {
	                    final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
	                    if (expressionService != null) {
	                    	final DataRequestMonitor<IExpressionDMAddress> drm = new DataRequestMonitor<IExpressionDMAddress>(getSession().getExecutor(), null) {
                                @Override
								public void handleCompleted() {
                                	if (isSuccess()) {
                                		request.setSize(getData().getSize());
                                	}
                                	request.setStatus(getStatus());
                                    request.done();
                                }
	                    	};
	                    	
	                        expressionService.getExpressionAddressData(exprDmc, drm);
	                    }
	        			else {
	        				request.setStatus(internalError());
	        				request.done();
	        			}
	                }
	            });
			}
			else {
				request.setStatus(internalError());
				request.done();
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.internal.core.IWatchpointTarget#canCreateWatchpoint(org.eclipse.cdt.debug.internal.core.IWatchpointTarget.CanCreateWatchpointRequest)
		 */
		public void canSetWatchpoint(final ICWatchpointTarget.CanCreateWatchpointRequest request) {
			// If the expression is an l-value, then we say it supports a
			// watchpoint. The logic here is basically the same as what's in
			// getSize(), as the same DSF service method tells us (a) if it's an
			// lvalue, and (b) its size.
			final IExpressionDMContext exprDmc = DMContexts.getAncestorOfType(getDMContext(), IExpressionDMContext.class);
			if (exprDmc != null) {
	            getSession().getExecutor().execute(new Runnable() {
	                public void run() {
	                    final IExpressions expressionService = getServicesTracker().getService(IExpressions.class);
	                    if (expressionService != null) {
	                    	final DataRequestMonitor<IExpressionDMAddress> drm = new DataRequestMonitor<IExpressionDMAddress>(getSession().getExecutor(), null) {
                                @Override
								public void handleCompleted() {
                                	if (isSuccess()) {
	                                	assert getData().getSize() > 0;
	                                    request.setCanCreate(true);
                                	}
                                	request.setStatus(getStatus());                                	
	                                request.done();
                                }
	                    	};
	                    	
	                        expressionService.getExpressionAddressData(exprDmc, drm);
	                    }
	        			else {
	        				request.setStatus(internalError());
	        				request.done();
	        			}
	                }
	            });
			}
			else {
				request.setStatus(internalError());
				request.done();
			}
		}
	};
	
	/**
	 * Utility method to create an IStatus object for an internal error 
	 */
	private static Status internalError() {
		return new Status(Status.ERROR, GdbUIPlugin.getUniqueIdentifier(), Messages.Internal_Error);
	}
	/**
	 * Constructor (passthru)
	 */
	public GdbVariableVMNode(AbstractDMVMProvider provider, DsfSession session,
			SyncVariableDataAccess syncVariableDataAccess) {
		super(provider, session, syncVariableDataAccess);
	}

	/**
	 * The primary reason for the specialization of VariableVMNode is to create
	 * a GDB-specific VM context that implements ICWatchpointTarget, so that the
	 * "Add Watchpoint" context menu appears for variables and expressions in
	 * GDB-DSF sessions but not necessarily other DSF-based sessions [bugzilla
	 * 248606]
	 * 
	 * @see org.eclipse.cdt.dsf.debug.ui.viewmodel.variable.VariableVMNode#createVMContext(org.eclipse.cdt.dsf.datamodel.IDMContext)
	 */
    @Override
    protected IDMVMContext createVMContext(IDMContext dmc) {
        return new GdbVariableExpressionVMC(dmc);
    }
}
