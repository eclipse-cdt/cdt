/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import java.util.List;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.StackFramesVMNode;
import org.eclipse.cdt.dsf.gdb.service.IGDBSynchronizer;
import org.eclipse.cdt.dsf.gdb.service.IGDBSynchronizer.IGDBFocusChangedEvent;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.VMChildrenUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

public class GdbStackFramesVMNode extends StackFramesVMNode {

	public GdbStackFramesVMNode(AbstractDMVMProvider provider, DsfSession session) {
		super(provider, session);
	}

	@Override
	public int getDeltaFlags(Object e) {
		if (e instanceof IGDBFocusChangedEvent) {
        	return IModelDelta.SELECT | IModelDelta.EXPAND;
		}
		
		return super.getDeltaFlags(e);
	}
	
	@Override
	public void buildDelta(final Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor rm) {
		if (e instanceof IGDBFocusChangedEvent) {
			buildDeltaForFocusChangedEvent(parentDelta, nodeOffset, rm);
		}
		else {
			super.buildDelta(e, parentDelta, nodeOffset, rm);
		}
	}
	
	private void buildDeltaForFocusChangedEvent(VMDelta parentDelta, int nodeOffset, RequestMonitor rm) {
		getSession().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				IFrameDMContext found = null;
				// get new frame focus from sync service
				IGDBSynchronizer fGdbSync = getServicesTracker().getService(IGDBSynchronizer.class);
				Object[] elems = fGdbSync.getFocus();
				for (Object elem : elems) {
					if (elem instanceof IFrameDMContext) {
						found = (IFrameDMContext)elem;
						break;
					}
				}
				final IFrameDMContext newFrame = found;
				if (newFrame != null) {
					IExecutionDMContext execDmc = DMContexts.getAncestorOfType(newFrame, IExecutionDMContext.class);
					if (execDmc == null) return;
					
					// is the current thread suspended? 
					isThreadSuspended(execDmc, new DataRequestMonitor<Boolean>(getExecutor(), rm) {
						@Override
						protected void handleSuccess() {
							// is thread is suspended? 
							if (getData()) {
								// find the VMC index for the frame that switched, so we can select it correctly.
								getVMCIndexForDmc(
										GdbStackFramesVMNode.this,
										newFrame,
										parentDelta,
										new DataRequestMonitor<Integer>(getExecutor(), rm) {
											@Override
											protected void handleCompleted() {
												if (isSuccess()) {
													// change to frameOffset
													final int frameOffset = getData();
													
													// Retrieve the list of stack frames
													getVMProvider().updateNode(
															GdbStackFramesVMNode.this,
															new VMChildrenUpdate(
																	parentDelta,
																	getVMProvider().getPresentationContext(), -1, -1,
																	new DataRequestMonitor<List<Object>>(getExecutor(), rm) {
																		@Override
																		public void handleCompleted() {
																			final List<Object> data= getData();
																			if (data != null && data.size() != 0) {
																				// create the delta to select the current stack frame
																				parentDelta.addNode(
																						data.get(frameOffset), 
																						frameOffset, 
																						IModelDelta.SELECT | IModelDelta.FORCE
																				);
																			}
																			rm.done();
																			return;
																		}
																	})
															);
												}
											}
										});
							}
							else {
								// thread is running - no delta to produce for the stack frame node
								rm.done();
							}
						}
					});
				}
			}
    	});
	}
}
