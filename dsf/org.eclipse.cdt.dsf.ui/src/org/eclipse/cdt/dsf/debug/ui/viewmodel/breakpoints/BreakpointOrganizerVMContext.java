/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems and others.
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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointContainer;
import org.eclipse.debug.internal.ui.breakpoints.provisional.IBreakpointOrganizer;

/**
 *
 *
 * @since 2.1
 */
public class BreakpointOrganizerVMContext extends AbstractVMContext implements IBreakpointContainer {

	private final IAdaptable fCategory;
	private final IBreakpoint[] fBreakpoints;

	public BreakpointOrganizerVMContext(BreakpointOrganizerVMNode vmNode, IAdaptable category,
			IBreakpoint[] breakpoints) {
		super(vmNode);
		fCategory = category;
		fBreakpoints = breakpoints;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BreakpointOrganizerVMContext
				&& getVMNode().equals(((BreakpointOrganizerVMContext) obj).getVMNode())
				&& getOrganizer().equals(((BreakpointOrganizerVMContext) obj).getOrganizer())
				&& fCategory.equals(((BreakpointOrganizerVMContext) obj).fCategory);
	}

	@Override
	public int hashCode() {
		return getOrganizer().hashCode() + getVMNode().hashCode() + fCategory.hashCode();
	}

	@Override
	public IBreakpointOrganizer getOrganizer() {
		return ((BreakpointOrganizerVMNode) getVMNode()).getOrganizer();
	}

	@Override
	public IAdaptable getCategory() {
		return fCategory;
	}

	@Override
	public boolean contains(IBreakpoint breakpoint) {
		for (IBreakpoint bp : fBreakpoints) {
			if (bp.equals(breakpoint)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IBreakpoint[] getBreakpoints() {
		return fBreakpoints;
	}

	@Override
	public String toString() {
		return fCategory.toString();
	}

}
