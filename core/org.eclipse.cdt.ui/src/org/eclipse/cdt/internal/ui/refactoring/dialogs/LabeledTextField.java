/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A text field with an associated label, displayed side-by-side.
 *
 * @author Mirko Stocker
 */
public class LabeledTextField extends Composite {
	private final Text textField;

	public LabeledTextField(Composite parent, String labelName, String textContent) {
		super(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		setLayout(layout);

		Label label = new Label(this, SWT.NONE);
		label.setText(labelName);
		label.setLayoutData(new GridData());

		textField = new Text(this, SWT.BORDER | SWT.SINGLE);
		textField.setText(textContent);
		textField.selectAll();
		GridData textData = new GridData(GridData.FILL_HORIZONTAL);
		textData.grabExcessHorizontalSpace = true;
		textField.setLayoutData(textData);
	}

	public LabeledTextField(Composite parent, String labelName) {
		this(parent, labelName, ""); //$NON-NLS-1$
	}

	public Text getText() {
		return textField;
	}

	public String getFieldContent() {
		return textField.getText();
	}
}
