/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.StackFramesVMNode;
import org.eclipse.cdt.dsf.gdb.internal.service.IGDBSynchronizer.IGDBFocusChangedEvent;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

public class GdbStackFramesVMNode extends StackFramesVMNode {

	public GdbStackFramesVMNode(AbstractDMVMProvider provider, DsfSession session) {
		super(provider, session);
	}

	@Override
	public int getDeltaFlags(Object e) {
		if (e instanceof IGDBFocusChangedEvent) {
        	return IModelDelta.SELECT;
		}
		
		return super.getDeltaFlags(e);
	}
	
	@Override
	public void buildDelta(final Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor rm) {
		if (e instanceof IGDBFocusChangedEvent) {
			buildDeltaForFocusChangedEvent((IGDBFocusChangedEvent)e, parentDelta, rm);
		}
		else {
			super.buildDelta(e, parentDelta, nodeOffset, rm);
		}
	}
	
	private void buildDeltaForFocusChangedEvent(IGDBFocusChangedEvent event, VMDelta parentDelta, RequestMonitor rm) {
		getSession().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				IFrameDMContext found = null;
				IDMContext ctx = event.getDMContext();
				
				// Is IGDBFocusChangedEvent pertinent for this VMNode? 
				if (ctx instanceof IFrameDMContext) {
					found = (IFrameDMContext) ctx;
				}

				final IFrameDMContext newFrameFocus = found;
				if (newFrameFocus != null) {
					IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(newFrameFocus, IMIExecutionDMContext.class);
					if (execDmc == null) {
						rm.done();
						return;
					}
					// is the current thread suspended? 
					isThreadSuspended(execDmc, new DataRequestMonitor<Boolean>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							// is thread is suspended? 
							if (getData()) {
								// find the VMC index for the frame that switched, so we can select it correctly.
								getVMCIndexForDmc(
										GdbStackFramesVMNode.this,
										newFrameFocus,
										parentDelta,
										new DataRequestMonitor<Integer>(getExecutor(), rm) {
											@Override
											protected void handleSuccess() {
												// change to frameOffset
												final int frameOffset = getData();

												// Retrieve the list of stack frames
												getVMProvider().updateNode(GdbStackFramesVMNode.this,
														new VMChildrenUpdate(parentDelta,
																getVMProvider().getPresentationContext(), -1,
																-1, new DataRequestMonitor<List<Object>>(
																		getExecutor(), rm) {
																	@Override
																	public void handleSuccess() {
																		final List<Object> data = getData();
																		if (data != null && data.size() != 0) {
																			// create the delta to select the
																			// current stack frame
																			parentDelta.addNode(
																					data.get(frameOffset),
																					frameOffset,
																					IModelDelta.SELECT	| IModelDelta.FORCE
																					);
																		}
																		rm.done();
																		return;
																	}
																}));
											}
										});
							}
							else {
								// thread is running - no delta to produce for the stack frame node
								rm.done();
							}
						}
					});
				} else {
					// context not a frame  - nothing to do here
					rm.done();
				}
			}
    	});
	}
	
    /**
     * This method check whether a thread is currently suspended
     * @param ctx thread context
     * @param rm request monitor
     */
    protected void isThreadSuspended(IExecutionDMContext ctx, final DataRequestMonitor<Boolean> rm) {
    	try {
    		getSession().getExecutor().execute(new DsfRunnable() {
    			@Override
    			public void run() {
    				IRunControl runControl = getServicesTracker().getService(IRunControl.class);
    				rm.setData(runControl.isSuspended(ctx) || runControl.isStepping(ctx));
    				rm.done();
    			}});
    	} catch (RejectedExecutionException e) {
    		rm.setStatus(new Status(IStatus.ERROR, GdbUIPlugin.PLUGIN_ID, "Session shut down", e)); //$NON-NLS-1$);
	        rm.done();
    	}
    }
	
}
