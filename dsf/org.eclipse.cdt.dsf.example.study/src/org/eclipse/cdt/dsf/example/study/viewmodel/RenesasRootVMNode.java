package org.eclipse.cdt.dsf.example.study.viewmodel;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;

public class RenesasRootVMNode extends RootDMVMNode {

	public RenesasRootVMNode(AbstractVMProvider provider) {
		super(provider);
	}

	/**
	 * Logic that used to filter the event to make sure that the view does not react to events that relate to objects outside this view.
	 */
	@Override
	public boolean isDeltaEvent(Object rootObject, Object e) {
		if (e instanceof EmployeesVMProvider.RenesasVMChangedEvent) {
			return true;
		}
		return super.isDeltaEvent(rootObject, e);
	}

	/**
	 * Returns a set of IModelDelta delta flags that indicate how elements of this node (type) may be, or are, affected by the given event.
	 */
	@Override
	public int getDeltaFlags(Object e) {
		if (e instanceof EmployeesVMProvider.RenesasVMChangedEvent) {
			return IModelDelta.CONTENT;
		}

		return IModelDelta.NO_CHANGE;
	}

	/**
	 * Creates a delta
	 */
	@Override
	public void createRootDelta(Object rootObject, Object event, final DataRequestMonitor<VMDelta> rm) {
		int flags = IModelDelta.NO_CHANGE;
		if (event instanceof EmployeesVMProvider.RenesasVMChangedEvent) {
			flags |= IModelDelta.CONTENT;
		}
		rm.setData(new VMDelta(rootObject, 0, flags));
		rm.done();
	}
}
