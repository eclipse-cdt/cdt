/*******************************************************************************
 * Copyright (c) 2007, 2012 Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpoints;

import org.eclipse.cdt.debug.core.ICWatchpointTarget;
import org.eclipse.cdt.debug.internal.core.CRequest;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;

/**
 * Invoked when user right clicks on an element in the Variables or Expressions
 * view and selects 'Add Watchpoint (C/C++)'  Clients can register this action for
 * their specific element type which adapts to {@link ICWatchpointTarget}.
 *
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 7.2
 */
public class AddWatchpointOnVariableActionDelegate extends AddWatchpointActionDelegate {

	/** The target variable/expression */
	private ICWatchpointTarget fVar;

	/**
	 * Constructor
	 */
	public AddWatchpointOnVariableActionDelegate() {
		super();
	}

	private class CanCreateWatchpointRequest extends CRequest implements ICWatchpointTarget.CanCreateWatchpointRequest {
		boolean fCanCreate;

		@Override
		public boolean getCanCreate() {
			return fCanCreate;
		}

		@Override
		public void setCanCreate(boolean value) {
			fCanCreate = value;
		}
	}

	/**
	 * Record the target variable/expression
	 *
	 * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(final IAction action, ISelection selection) {
		super.selectionChanged(action, selection);
		fVar = null;
		if (selection == null || selection.isEmpty()) {
			action.setEnabled(false);
			return;
		}
		if (selection instanceof TreeSelection) {
			Object obj = ((TreeSelection) selection).getFirstElement();
			fVar = (ICWatchpointTarget) DebugPlugin.getAdapter(obj, ICWatchpointTarget.class);
			if (fVar != null) {
				final ICWatchpointTarget.CanCreateWatchpointRequest request = new CanCreateWatchpointRequest() {
					@Override
					public void done() {
						action.setEnabled(getCanCreate());
					}
				};
				fVar.canSetWatchpoint(request);
				return;
			}
			assert false : "action should not have been available for object " + obj; //$NON-NLS-1$
		} else if (selection instanceof StructuredSelection) {
			// Not sure why, but sometimes we get an extraneous empty StructuredSelection. Seems harmless enough
			assert ((StructuredSelection) selection)
					.getFirstElement() == null : "action installed in unexpected type of view/part"; //$NON-NLS-1$
		} else {
			assert false : "action installed in unexpected type of view/part"; //$NON-NLS-1$
		}
		action.setEnabled(false);
	}
}
