/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Alvaro Sanchez-Leon (Ericsson AB) - Remove dependencies from Debug model (Bug 235747)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;

/**
 * A delegate for the "Add register group" action.
 */
public class AddRegisterGroupActionDelegate extends AbstractRegisterGroupActionDelegate {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#getErrorDialogMessage()
	 */
	@Override
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("AddRegisterGroupActionDelegate.1"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#doAction()
	 */
	@Override
	protected void doAction() throws DebugException {
		IAction action = getAction();
		if (action != null) {
			IRegisterGroupActions groupActions = getGroupActions();
			if (groupActions != null && getView() != null) {
				groupActions.addRegisterGroup(getView(), getSelection());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractViewActionDelegate#update()
	 */
	@Override
	protected void update() {
		IAction action = getAction();
		if (action != null) {

			boolean canAdd = false;
			IRegisterGroupActions groupActions = getGroupActions();
			if (groupActions != null) {
				canAdd = groupActions.canAddRegisterGroup(getView(), getSelection());
			}

			action.setEnabled(canAdd);
		}
	}
}
