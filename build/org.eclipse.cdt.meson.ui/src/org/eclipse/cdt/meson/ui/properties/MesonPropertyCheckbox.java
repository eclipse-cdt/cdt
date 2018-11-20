/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.meson.ui.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class MesonPropertyCheckbox implements IMesonPropertyPageControl {

	private String name;
	private boolean initialValue;
	protected Button checkbox;

	public MesonPropertyCheckbox(Composite composite, String name, boolean initialValue, String tooltip) {
		this.name = name;
		this.initialValue = initialValue;
		checkbox = new Button(composite, SWT.CHECK);
		GridData data = new GridData(GridData.FILL, GridData.FILL, true, false);
		data.grabExcessHorizontalSpace = true;
		data.horizontalSpan = 1;
		checkbox.setText(name);
		checkbox.setLayoutData(data);
		checkbox.setSelection(initialValue);
		checkbox.setToolTipText(tooltip);
		GridData data2 = new GridData(GridData.FILL, GridData.FILL, true, false);
		data2.grabExcessHorizontalSpace = true;
		data2.horizontalSpan = 1;
		Label label = new Label(composite, SWT.NONE);
		label.setText(tooltip);
		label.setLayoutData(data2);
	}

	@Override
	public String getFieldValue() {
		return Boolean.toString(checkbox.getSelection());
	}

	@Override
	public String getFieldName() {
		return name;
	}

	@Override
	public boolean isValueChanged() {
		return checkbox.getSelection() != initialValue;
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
