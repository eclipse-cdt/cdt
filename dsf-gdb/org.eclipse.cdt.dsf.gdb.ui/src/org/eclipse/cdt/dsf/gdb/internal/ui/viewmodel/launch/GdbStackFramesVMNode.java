/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.StackFramesVMNode;
import org.eclipse.cdt.dsf.gdb.service.IGDBSynchronizer.IStackFrameSwitchedEvent;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

public class GdbStackFramesVMNode extends StackFramesVMNode {

	public GdbStackFramesVMNode(AbstractDMVMProvider provider, DsfSession session) {
		super(provider, session);
	}

	@Override
	public int getDeltaFlags(Object e) {
		if (e instanceof IStackFrameSwitchedEvent) {
        	return IModelDelta.SELECT | IModelDelta.EXPAND;
		}
		
		return super.getDeltaFlags(e);
	}
	
	@Override
	public void buildDelta(final Object e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor rm) {
		IDMContext dmc = e instanceof IDMEvent<?> ? ((IDMEvent<?>)e).getDMContext() : null;
		
		if (e instanceof IStackFrameSwitchedEvent) {
			buildDeltaForStackFrameSwitchedEvent(dmc, parentDelta, nodeOffset, rm);
		}
		else {
			super.buildDelta(e, parentDelta, nodeOffset, rm);
		}
	}
	
	private void buildDeltaForStackFrameSwitchedEvent(IDMContext dmc, VMDelta parentDelta, int nodeOffset,RequestMonitor rm) {
		// find the VMC index for the frame that switched, so we can select it correctly.
		getVMCIndexForDmc(
				this,
				dmc,
				parentDelta,
				new DataRequestMonitor<Integer>(getExecutor(), rm) {
					@Override
					protected void handleCompleted() {
						if (isSuccess()) {
							parentDelta.addNode(
									createVMContext(dmc), nodeOffset + getData(),
									IModelDelta.SELECT | IModelDelta.FORCE);
						}
						rm.done();
					}
				});
	}
	
}
