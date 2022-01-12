/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.StandardProcessVMNode;
import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;
import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.jface.viewers.TreePath;

/**
 * Layout node for the standard platform debug model IProcess object. This
 * node requires that an ILaunch object be found as an ancestor of this node.
 * It does not implement the label provider functionality, so the default
 * adapters should be used to retrieve the label.
 *
 * This version is specific to DSF-GDB to no longer show the inferiors.
 */
public class GdbStandardProcessVMNode extends StandardProcessVMNode {

	public GdbStandardProcessVMNode(AbstractVMProvider provider) {
		super(provider);
	}

	@Override
	public String toString() {
		return "GdbStandardProcessVMNode"; //$NON-NLS-1$
	}

	@Override
	public void update(IChildrenUpdate[] updates) {
		for (IChildrenUpdate update : updates) {
			ILaunch launch = findLaunch(update.getElementPath());
			if (launch == null) {
				// There is no launch in the parent of this node.  This means that the
				// layout is misconfigured.
				assert false;
				update.done();
				continue;
			}

			/*
			 * Assume that the process objects are stored within the launch, and
			 * retrieve them on dispatch thread.
			 */
			int count = 0;
			for (IProcess process : launch.getProcesses()) {
				if (!(process instanceof InferiorRuntimeProcess)) {
					update.setChild(process, count++);
				}
			}
			update.done();
		}
	}

	@Override
	public void update(final IChildrenCountUpdate[] updates) {
		for (IChildrenCountUpdate update : updates) {
			if (!checkUpdate(update))
				continue;
			ILaunch launch = findLaunch(update.getElementPath());
			if (launch == null) {
				assert false;
				update.setChildCount(0);
				update.done();
				return;
			}

			int count = 0;
			for (IProcess process : launch.getProcesses()) {
				if (!(process instanceof InferiorRuntimeProcess)) {
					count++;
				}
			}
			update.setChildCount(count);
			update.done();
		}
	}

	// @see org.eclipse.cdt.dsf.ui.viewmodel.IViewModelLayoutNode#hasElements(org.eclipse.cdt.dsf.ui.viewmodel.IVMContext, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	@Override
	public void update(IHasChildrenUpdate[] updates) {
		for (IHasChildrenUpdate update : updates) {
			ILaunch launch = findLaunch(update.getElementPath());
			if (launch == null) {
				assert false;
				update.setHasChilren(false);
				update.done();
				return;
			}

			boolean hasChildren = false;
			for (IProcess process : launch.getProcesses()) {
				if (!(process instanceof InferiorRuntimeProcess)) {
					hasChildren = true;
					break;
				}
			}

			update.setHasChilren(hasChildren);
			update.done();
		}
	}

	/**
	 * Recursively searches the VMC for Launch VMC, and returns its ILaunch.
	 * Returns null if an ILaunch is not found.
	 */
	private ILaunch findLaunch(TreePath path) {
		for (int i = path.getSegmentCount() - 1; i >= 0; i--) {
			if (path.getSegment(i) instanceof ILaunch) {
				return (ILaunch) path.getSegment(i);
			}
		}
		return null;
	}

	@Override
	public int getDeltaFlags(Object e) {
		int myFlags = 0;
		if (e instanceof DebugEvent) {
			DebugEvent de = (DebugEvent) e;
			if (!(de.getSource() instanceof InferiorRuntimeProcess) && (de.getKind() == DebugEvent.CHANGE
					|| de.getKind() == DebugEvent.CREATE || de.getKind() == DebugEvent.TERMINATE)) {
				myFlags = IModelDelta.STATE;
			}
		}
		return myFlags;
	}

	@Override
	public void buildDelta(Object e, VMDelta parent, int nodeOffset, RequestMonitor requestMonitor) {
		if (e instanceof DebugEvent && !(((DebugEvent) e).getSource() instanceof InferiorRuntimeProcess)) {
			DebugEvent de = (DebugEvent) e;
			if (de.getKind() == DebugEvent.CHANGE) {
				handleChange(de, parent);
			} else if (de.getKind() == DebugEvent.CREATE) {
				handleCreate(de, parent);
			} else if (de.getKind() == DebugEvent.TERMINATE) {
				handleTerminate(de, parent);
			}
			/*
			 * No other node should need to process events related to process.
			 * Therefore, just invoke the request monitor without calling super.buildDelta().
			 */
		}
		requestMonitor.done();
	}
}
