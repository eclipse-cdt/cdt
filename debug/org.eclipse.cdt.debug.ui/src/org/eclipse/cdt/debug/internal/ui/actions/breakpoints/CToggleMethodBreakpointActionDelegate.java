/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Freescale 	- Add support for conditionally activating an action
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import org.eclipse.cdt.debug.ui.breakpoints.IToggleBreakpointsTargetCExtension;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A toggle method breakpoint action that can be contributed an object
 * contribution. The action will toggle method breakpoints on objects
 * that provide an <code>IToggleBreakpointsTarget</code> adapter.
 * <p>
 * This class is based on {@link org.eclipse.debug.ui.actions.ToggleMethodBreakpointActionDelegate }
 * class.  In addition to the copied functionality, it adds the handling of
 * action-triggering event.
 * </p>
 *
 * @since 7.2
 */
public class CToggleMethodBreakpointActionDelegate extends CToggleBreakpointObjectActionDelegate {

	@Override
	protected void performAction(IToggleBreakpointsTarget target, IWorkbenchPart part, ISelection selection,
			Event event) throws CoreException {
		if ((event.stateMask & SWT.MOD1) != 0 && target instanceof IToggleBreakpointsTargetCExtension
				&& ((IToggleBreakpointsTargetCExtension) target).canCreateFunctionBreakpointInteractive(part,
						selection)) {
			((IToggleBreakpointsTargetCExtension) target).createFunctionBreakpointInteractive(part, selection);
		} else {
			target.toggleMethodBreakpoints(part, selection);
		}
	}

	@Override
	protected boolean canPerformAction(IToggleBreakpointsTarget target, IWorkbenchPart part, ISelection selection) {
		return target.canToggleMethodBreakpoints(part, selection);
	}
}
