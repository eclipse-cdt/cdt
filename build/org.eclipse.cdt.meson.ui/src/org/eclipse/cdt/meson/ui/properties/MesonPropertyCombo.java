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
package org.eclipse.cdt.meson.ui.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class MesonPropertyCombo implements IMesonPropertyPageControl {
	
	private String name;
	private String initialValue;
	private Combo combo;
	
	public MesonPropertyCombo(Composite composite, String name, String[] values, String initialValue, String tooltip) {
		this.name = name;
		this.initialValue = initialValue;
		Label label = new Label(composite, SWT.NONE);
		label.setText(name);
		label.setLayoutData(new GridData());
		combo = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		combo.setLayoutData(data);
		combo.setItems(values);
		combo.setText(initialValue);
		combo.setToolTipText(tooltip);
	}


	@Override
	public String getFieldValue() {
		return combo.getText();
	}

	@Override
	public String getFieldName() {
		return name;
	}

	@Override
	public boolean isValueChanged() {
		return !combo.getText().equals(initialValue);
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
