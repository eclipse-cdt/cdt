/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.actions;

import org.eclipse.cdt.make.ui.dialogs.MakeTargetDialog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CreateTargetAction extends AbstractTargetAction {

	@Override
	public void run(IAction action) {
		if (getSelectedContainer() != null) {
			MakeTargetDialog dialog;
			try {
				dialog = new MakeTargetDialog(getShell(), getSelectedContainer());
				dialog.open();
			} catch (CoreException e) {
			}
		}
	}
}
