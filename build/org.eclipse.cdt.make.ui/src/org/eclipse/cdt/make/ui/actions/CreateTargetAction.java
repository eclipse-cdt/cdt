/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.actions;

import org.eclipse.cdt.make.ui.dialogs.MakeTargetDialog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;

public class CreateTargetAction extends AbstractTargetAction {

	public void run(IAction action) {
		if ( getSelectedContainer() != null ) {
			MakeTargetDialog dialog;
			try {
				dialog = new MakeTargetDialog(getShell(), getSelectedContainer());
				dialog.open();
			} catch (CoreException e) {
			}
		}
	}
}
