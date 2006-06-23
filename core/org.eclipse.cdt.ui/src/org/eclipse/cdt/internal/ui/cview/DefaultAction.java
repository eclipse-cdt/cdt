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
package org.eclipse.cdt.internal.ui.cview;


import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

public class DefaultAction extends Action {

	Shell shell;

	DefaultAction (Shell shell, String s) {
		super (s);
		this.shell = shell;
	}

	public void run() {
		MessageBox errorMsg = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		errorMsg.setText(CViewMessages.getString("DefaultAction.WIP")); //$NON-NLS-1$
		errorMsg.setMessage (CViewMessages.getString("DefaultAction.workInProgress")); //$NON-NLS-1$
		errorMsg.open();
	}
}
