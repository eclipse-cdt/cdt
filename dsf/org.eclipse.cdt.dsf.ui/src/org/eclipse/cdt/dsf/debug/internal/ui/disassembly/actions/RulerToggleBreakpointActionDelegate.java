/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
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
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions;

import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.debug.ui.actions.ToggleBreakpointAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;

/**
 * Ruler toggle breakpoint action delegate for disassembly parts.
 *
 * @since 2.1
 *
 * @see org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate
 */
public class RulerToggleBreakpointActionDelegate extends AbstractDisassemblyRulerActionDelegate {

	private ToggleBreakpointAction fDelegate;

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.AbstractDisassemblyRulerActionDelegate#createAction(org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart, org.eclipse.jface.text.source.IVerticalRulerInfo)
	 */
	@Override
	protected IAction createAction(IDisassemblyPart disassemblyPart, IVerticalRulerInfo rulerInfo) {
		if (fDelegate != null) {
			fDelegate.dispose();
		}
		return fDelegate = new ToggleBreakpointAction(disassemblyPart, disassemblyPart.getTextViewer().getDocument(),
				rulerInfo);
	}

	/*
	 * @see org.eclipse.ui.actions.ActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
		if (fDelegate != null) {
			fDelegate.dispose();
			fDelegate = null;
		}
		super.dispose();
	}
}
