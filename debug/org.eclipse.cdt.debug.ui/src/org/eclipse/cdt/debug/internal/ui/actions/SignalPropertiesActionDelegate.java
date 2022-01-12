/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 * Signal Properties action delegate.
 */
public class SignalPropertiesActionDelegate extends ActionDelegate implements IObjectActionDelegate {

	private ICSignal fSignal;

	private IWorkbenchPart fTargetPart;

	/**
	 * Constructor for SignalPropertiesActionDelegate.
	 *
	 */
	public SignalPropertiesActionDelegate() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fTargetPart = targetPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof ICSignal) {
				action.setEnabled(true);
				setSignal((ICSignal) element);
				return;
			}
		}
		action.setEnabled(false);
		setSignal(null);
	}

	protected ICSignal getSignal() {
		return this.fSignal;
	}

	private void setSignal(ICSignal signal) {
		this.fSignal = signal;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		PropertyDialogAction propertyAction = new PropertyDialogAction(getActivePart().getSite(),
				new ISelectionProvider() {

					@Override
					public void addSelectionChangedListener(ISelectionChangedListener listener) {
					}

					@Override
					public ISelection getSelection() {
						return new StructuredSelection(getSignal());
					}

					@Override
					public void removeSelectionChangedListener(ISelectionChangedListener listener) {
					}

					@Override
					public void setSelection(ISelection selection) {
					}
				});
		propertyAction.run();
	}

	protected IWorkbenchPart getActivePart() {
		return fTargetPart;
	}
}
