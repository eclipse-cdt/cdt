package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

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
			parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.EXPAND);
			parentDelta.addNode(createVMContext(dmc), IModelDelta.SELECT | IModelDelta.FORCE);
			rm.done();
		}
		else {
			super.buildDelta(e, parentDelta, nodeOffset, rm);
		}
	}
	
}
