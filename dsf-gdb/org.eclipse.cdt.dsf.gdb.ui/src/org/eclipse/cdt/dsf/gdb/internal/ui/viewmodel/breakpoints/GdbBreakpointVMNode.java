/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.breakpoints;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints.BreakpointVMProvider;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints.RawBreakpointVMNode;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * BreakpointVMNode for GDB which supports aggressive breakpoint filtering.
 * @since 2.4
 */
public class GdbBreakpointVMNode extends RawBreakpointVMNode {

	public GdbBreakpointVMNode(BreakpointVMProvider provider) {
		super(provider);
	}

	@Override
	public int getDeltaFlags(Object event) {
		if (event instanceof PropertyChangeEvent) {
			String property = ((PropertyChangeEvent) event).getProperty();
			if (IGdbDebugPreferenceConstants.PREF_AGGRESSIVE_BP_FILTER.equals(property)) {
				return IModelDelta.CONTENT;
			}
		}

		return super.getDeltaFlags(event);
	}

	@Override
	public void buildDelta(Object event, VMDelta parent, int nodeOffset, RequestMonitor rm) {
		if (event instanceof PropertyChangeEvent) {
			String property = ((PropertyChangeEvent) event).getProperty();
			if (IGdbDebugPreferenceConstants.PREF_AGGRESSIVE_BP_FILTER.equals(property)) {
				parent.setFlags(parent.getFlags() | IModelDelta.CONTENT);
				rm.done();
				return;
			}
		}

		super.buildDelta(event, parent, nodeOffset, rm);
	}
}
