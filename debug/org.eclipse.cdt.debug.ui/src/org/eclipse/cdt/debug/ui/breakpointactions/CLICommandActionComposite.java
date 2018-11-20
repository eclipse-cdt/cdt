/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia (QNX)- Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This composite show a little text field (multi-line) that allow to enter to enter debugger commands.
 * Interpretation of that depends on the backend
 *
 * @since 8.0
 */
public class CLICommandActionComposite extends Composite {
	private Text command;

	public CLICommandActionComposite(Composite parent, int style, CLICommandActionPage commandActionPage) {
		super(parent, style);
		setLayout(GridLayoutFactory.fillDefaults().create());
		Label messageToLogLabel = new Label(this, SWT.NONE);
		messageToLogLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		messageToLogLabel.setText(Messages.getString("CLICommandActionComposite.0")); //$NON-NLS-1$
		command = new Text(this, SWT.BORDER | SWT.WRAP);
		command.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		command.setText(commandActionPage.getCLICommandAction().getCommand());
	}

	public String getCommand() {
		return command.getText();
	}
}
