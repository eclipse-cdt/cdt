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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class MesonPropertyText implements IMesonPropertyPageControl {

	private String name;
	private String initialValue;
	protected Text text;

	public MesonPropertyText(Composite composite, String name, String initialValue, String tooltip) {
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
	public String getUnconfiguredString() {
		String value = getFieldValue();
		if (value != null && !value.isEmpty()) {
			return "--" + getFieldName() + "=" + getFieldValue(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return "";
	}

	@Override
	public String getErrorMessage() {
		return null;
	}
}
