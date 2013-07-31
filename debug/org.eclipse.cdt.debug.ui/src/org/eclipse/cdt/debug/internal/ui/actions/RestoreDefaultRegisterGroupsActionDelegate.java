/*******************************************************************************
 * Copyright (c) 2004, 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Alvaro Sanchez-Leon (Ericsson AB) - Adapted to remove dependencies from Debug model
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;

public class RestoreDefaultRegisterGroupsActionDelegate extends AbstractRegisterGroupActionDelegate {
	@Override
	protected String getErrorDialogMessage() {
		return ActionMessages.getString("RestoreDefaultRegisterGroupsActionDelegate.1"); //$NON-NLS-1$
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
				groupActions.restoreDefaultGroups(getView(), getSelection());
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

			boolean canRestore = false;
			IRegisterGroupActions groupActions = getGroupActions();
			if (groupActions != null) {
				canRestore = groupActions.canRestoreDefaultGroups(getView(), getSelection());
			}

			action.setEnabled(canRestore);
		}
	}
}
