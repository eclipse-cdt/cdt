package org.eclipse.cdt.internal.ui.cview;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */

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
		errorMsg.setText("WIP");
		errorMsg.setMessage ("Work In Progress");
		errorMsg.open();
	}
}
