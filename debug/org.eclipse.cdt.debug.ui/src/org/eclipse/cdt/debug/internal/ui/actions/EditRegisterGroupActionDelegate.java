/*******************************************************************************
 * Copyright (c) 2004, 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Alvaro Sanchez-Leon (Ericsson AB) - remove dependencies from Debug model (Bug 235747)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;

public class EditRegisterGroupActionDelegate extends AbstractRegisterGroupActionDelegate {
	@Override
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("EditRegisterGroupActionDelegate.0"); //$NON-NLS-1$
	}

	@Override
	protected void doAction() throws DebugException {
		IAction action = getAction();
		if (action != null) {

			IRegisterGroupActionsTarget groupActions = getGroupActions();
			if (groupActions != null) {
				groupActions.editRegisterGroup(getView(), getSelection());
			}
		}
	}

	@Override
	protected void update() {
		IAction action = getAction();
		if (action != null) {

			boolean canEdit = false;
			IRegisterGroupActionsTarget groupActions = getGroupActions();
			if (groupActions != null) {
				canEdit = groupActions.canEditRegisterGroup(getView(), getSelection());
			}

			action.setEnabled(canEdit);
		}
	}

}
