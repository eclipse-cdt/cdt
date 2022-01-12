/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import java.util.List;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.StackFramesVMNode;
import org.eclipse.cdt.dsf.gdb.internal.service.IGDBFocusSynchronizer.IGDBFocusChangedEvent;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
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
			return IModelDelta.SELECT;
		}

		return super.getDeltaFlags(e);
	}

	@Override
	public void buildDelta(final Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor rm) {
		if (e instanceof IGDBFocusChangedEvent) {
			buildDeltaForFocusChangedEvent((IGDBFocusChangedEvent) e, parentDelta, rm);
		} else {
			super.buildDelta(e, parentDelta, nodeOffset, rm);
		}
	}

	private void buildDeltaForFocusChangedEvent(IGDBFocusChangedEvent event, VMDelta parentDelta, RequestMonitor rm) {
		getSession().getExecutor().execute(() -> {
			IDMContext ctx = event.getDMContext();

			// Is IGDBFocusChangedEvent pertinent for this VMNode?
			if (ctx instanceof IFrameDMContext) {
				IFrameDMContext newFrameFocus = (IFrameDMContext) ctx;
				IMIExecutionDMContext execDmc = DMContexts.getAncestorOfType(newFrameFocus,
						IMIExecutionDMContext.class);
				if (execDmc == null) {
					rm.done();
					return;
				}

				IRunControl runControl = getServicesTracker().getService(IRunControl.class);
				if (runControl == null) {
					// Required services have not initialized yet.  Ignore the event.
					rm.done();
					return;
				}

				if (runControl.isSuspended(execDmc) || runControl.isStepping(execDmc)) {
					// find the VMC index for the frame that switched, so we can select it correctly.
					getVMCIndexForDmc(GdbStackFramesVMNode.this, newFrameFocus, parentDelta,
							new DataRequestMonitor<Integer>(getExecutor(), rm) {
								@Override
								protected void handleSuccess() {
									// change to frameOffset
									final int frameOffset = getData();

									// Retrieve the list of stack frames
									getVMProvider().updateNode(GdbStackFramesVMNode.this,
											new VMChildrenUpdate(parentDelta, getVMProvider().getPresentationContext(),
													-1, -1, new DataRequestMonitor<List<Object>>(getExecutor(), rm) {
														@Override
														public void handleSuccess() {
															final List<Object> data = getData();
															if (data != null && data.size() != 0) {
																// create the delta to select the
																// current stack frame
																parentDelta.addNode(data.get(frameOffset), frameOffset,
																		IModelDelta.SELECT | IModelDelta.FORCE);
															}
															rm.done();
														}
													}));
								}
							});
				} else {
					// thread is running - no delta to produce for the stack frame node
					rm.done();
				}
			} else {
				// context not a frame  - nothing to do here
				rm.done();
			}
		});
	}
}
