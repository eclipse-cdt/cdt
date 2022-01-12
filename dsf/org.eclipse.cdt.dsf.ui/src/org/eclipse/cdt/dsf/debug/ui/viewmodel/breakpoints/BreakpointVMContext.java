/*******************************************************************************
 * Copyright (c) 2008, 2015 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints;

import org.eclipse.cdt.dsf.ui.viewmodel.AbstractVMContext;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * @since 2.1
 */
public class BreakpointVMContext extends AbstractVMContext {

	private final IBreakpoint fBreakpoint;

	public BreakpointVMContext(BreakpointVMNode node, IBreakpoint breakpoint) {
		super(node);
		fBreakpoint = breakpoint;
	}

	public IBreakpoint getBreakpoint() {
		return fBreakpoint;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isInstance(fBreakpoint)) {
			return (T) fBreakpoint;
		}
		return super.getAdapter(adapter);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BreakpointVMContext && getBreakpoint().equals(((BreakpointVMContext) obj).getBreakpoint())
				&& fBreakpoint.equals(((BreakpointVMContext) obj).fBreakpoint);
	}

	@Override
	public int hashCode() {
		return fBreakpoint.hashCode();
	}

	@Override
	public String toString() {
		return fBreakpoint.toString();
	}
}
