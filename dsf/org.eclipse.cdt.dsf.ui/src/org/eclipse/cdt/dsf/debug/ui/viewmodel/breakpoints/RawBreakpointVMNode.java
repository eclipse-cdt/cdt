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

import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Breakpoint node which uses raw breakpoint objects (without a wrapper) as
 * elements which are populated into the view.  The breakpoint objects are
 * responsible for supplying their own label and memento providers, as well
 * as content provider for any children.
 *
 * @since 2.1
 */
public class RawBreakpointVMNode extends AbstractBreakpointVMNode {

	public RawBreakpointVMNode(BreakpointVMProvider provider) {
		super(provider);
	}

	@Override
	protected Object createBreakpiontElement(IBreakpoint bp) {
		return bp;
	}
}
