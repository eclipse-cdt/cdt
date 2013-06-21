/*******************************************************************************
 * Copyright (c) 2013 Ericsson AB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Alvaro Sanchez-Leon - Initial API and implementation (Bug 235747)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.internal.ViewPluginAction;

public abstract class AbstractRegisterGroupActionDelegate extends AbstractViewActionDelegate {
	private IRegisterGroupActions fGroupActions = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate# init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init(IAction action) {
		super.init(action);
		Object element = DebugUITools.getDebugContext();
		setSelection((element != null) ? new StructuredSelection(element) : new StructuredSelection());
		update();
	}

	protected IRegisterGroupActions getGroupActions() {
		IStructuredSelection selection = getSelection();
		if (fGroupActions == null && !selection.isEmpty()) {
			if (selection.getFirstElement() instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) selection.getFirstElement();
				// Resolve IRegisterGroupActions
				fGroupActions = (IRegisterGroupActions) adaptable.getAdapter(IRegisterGroupActions.class);
				if (fGroupActions == null) {
					IAdapterManager adapterManager = Platform.getAdapterManager();
					if (adapterManager.hasAdapter(adaptable, IRegisterGroupActions.class.getName())) {
						fGroupActions = (IRegisterGroupActions) adapterManager.loadAdapter(adaptable,
								IRegisterGroupActions.class.getName());
					}
				}
			}

		}

		return fGroupActions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate# getErrorDialogTitle()
	 */
	@Override
	protected String getErrorDialogTitle() {
		return ActionMessages.getString("RegisterGroupActionDelegate.0"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#
	 * doHandleDebugEvent(org.eclipse.debug.core.DebugEvent)
	 */
	@Override
	protected void doHandleDebugEvent(DebugEvent event) {
	}

	protected IStructuredSelection resolveViewSelection() {
		IStructuredSelection selection = null;
		IAction action = getAction();
		if (action instanceof ViewPluginAction) {
			ISelection sel = ((ViewPluginAction) action).getSelection();
			if (sel instanceof IStructuredSelection) {
				selection = (IStructuredSelection) sel;
			}
		}

		return selection;
	}

}
