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
 *     Wind River Systems - added support for IToggleBreakpointsTargetFactory
 *     Freescale 	- Add support for conditionally activating an action
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A toggle breakpoint action that can be contributed to an object. The action
 * will perform a toggle breakpoint operation for a selected object.
 * <p>
 * This class is based on {@link org.eclipse.debug.internal.ui.actions.breakpoints.ToggleBreakpointObjectActionDelegate }
 * class.  In addition to the copied functionality, it adds the handling of
 * action-triggering event.
 * </p>
 *
 * @since 7.2
 */
public abstract class CToggleBreakpointObjectActionDelegate implements IObjectActionDelegate, IActionDelegate2 {

	private IWorkbenchPart fPart;
	private IStructuredSelection fSelection;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fPart = targetPart;
	}

	@Override
	public void run(IAction action) {
		runWithEvent(action, null);
	}

	@Override
	public void runWithEvent(IAction action, Event event) {
		IToggleBreakpointsTarget target = DebugUITools.getToggleBreakpointsTargetManager()
				.getToggleBreakpointsTarget(fPart, fSelection);
		if (target != null) {
			try {
				performAction(target, fPart, fSelection, event);
			} catch (CoreException e) {
				DebugPlugin.log(e);
			}
		}
	}

	/**
	 * Performs the operation specific to this action.
	 *
	 * @param target adapter to toggle breakpoints
	 * @param part the active part
	 * @param selection the selection in the active part
	 * @param event that triggered this action
	 * @exception CoreException if an exception occurs
	 */
	protected abstract void performAction(IToggleBreakpointsTarget target, IWorkbenchPart part, ISelection selection,
			Event event) throws CoreException;

	/**
	 * Returns whether the specific operation is supported.
	 *
	 * @param target the target adapter
	 * @param selection the selection to verify the operation on
	 * @param part the part the operation has been requested on
	 * @return whether the operation can be performed
	 */
	protected abstract boolean canPerformAction(IToggleBreakpointsTarget target, IWorkbenchPart part,
			ISelection selection);

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		boolean enabled = false;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			this.fSelection = ss;
			// selectionChagned() can sometimes be called before setActivePart().
			// Guard here against that possibility.
			if (fPart != null) {
				IToggleBreakpointsTarget target = DebugUITools.getToggleBreakpointsTargetManager()
						.getToggleBreakpointsTarget(fPart, fSelection);
				enabled = target != null && canPerformAction(target, fPart, fSelection);
			}
		}
		action.setEnabled(enabled);
	}

	@Override
	public void init(IAction action) {
	}

	@Override
	public void dispose() {
		fSelection = null;
		fPart = null;
	}
}
