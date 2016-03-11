/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia (QNX)- Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * @since 8.0
 */
public class CLICommandActionComposite extends Composite {
	private CLICommandActionPage commandActionPage;
	private Text command;

	public CLICommandActionComposite(Composite parent, int style, CLICommandActionPage gdbCommandActionPage) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);
		this.commandActionPage = gdbCommandActionPage;
		final Label messageToLogLabel = new Label(this, SWT.NONE);
		messageToLogLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
		messageToLogLabel.setText(Messages.getString("CLICommandActionComposite.0")); //$NON-NLS-1$
		command = new Text(this, SWT.BORDER | SWT.WRAP);
		command.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
		command.setText(this.commandActionPage.getCLICommandAction().getCommand());
	}

	@Override
	protected void checkSubclass() {
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public String getCommand() {
		return command.getText();
	}
}
