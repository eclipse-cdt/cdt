/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.cmake.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CMakePropertyText implements ICMakePropertyPageControl {
	
	private String name;
	private String initialValue;
	protected Text text;
	
	public CMakePropertyText(Composite composite, String name, String initialValue, String tooltip) {
		this.name = name;
		if (initialValue == null) {
			initialValue = ""; //$NON-NLS-1$
		}
		this.initialValue = initialValue;
		Label label = new Label(composite, SWT.NONE);
		label.setText(name);
		label.setLayoutData(new GridData());
		text = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		text.setLayoutData(data);
		text.setText(initialValue);
		text.setToolTipText(tooltip);
	}

	@Override
	public String getFieldValue() {
		return text.getText();
	}

	@Override
	public String getFieldName() {
		return name;
	}
	
	@Override
	public boolean isValueChanged() {
		return !text.getText().equals(initialValue);
	}
	
	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}
}
