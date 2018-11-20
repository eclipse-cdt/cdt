/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
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
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class DefaultAction extends Action {

	Shell shell;

	DefaultAction(Shell shell, String s) {
		super(s);
		this.shell = shell;
	}

	@Override
	public void run() {
		MessageBox errorMsg = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		errorMsg.setText(CViewMessages.DefaultAction_WIP);
		errorMsg.setMessage(CViewMessages.DefaultAction_workInProgress);
		errorMsg.open();
	}
}
