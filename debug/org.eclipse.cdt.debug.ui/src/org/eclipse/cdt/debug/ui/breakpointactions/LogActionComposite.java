/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class LogActionComposite extends Composite {

	private Button expressionButton;
	private LogActionPage logActionPage;
	private Text message;

	public LogActionComposite(Composite parent, int style, LogActionPage logActionPage) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		this.logActionPage = logActionPage;

		final Label messageToLogLabel = new Label(this, SWT.NONE);
		messageToLogLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 2, 1));
		messageToLogLabel.setText(Messages.getString("LogActionComposite.0")); //$NON-NLS-1$

		message = new Text(this, SWT.BORDER | SWT.WRAP);
		message.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));

		expressionButton = new Button(this, SWT.CHECK);
		expressionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
			}
		});
		expressionButton.setText(Messages.getString("LogActionComposite.1")); //$NON-NLS-1$
		//

		message.setText(this.logActionPage.getLogAction().getMessage());
		expressionButton.setSelection(this.logActionPage.getLogAction().isEvaluateExpression());
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public boolean getIsExpression() {
		return expressionButton.getSelection();
	}

	public String getMessage() {
		return message.getText();
	}

}
