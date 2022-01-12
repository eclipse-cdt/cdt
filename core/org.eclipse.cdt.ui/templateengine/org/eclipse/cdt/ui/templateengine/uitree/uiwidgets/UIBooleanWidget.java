/*******************************************************************************
 * Copyright (c) 2007, 2016 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.uitree.uiwidgets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.ui.templateengine.uitree.InputUIElement;
import org.eclipse.cdt.ui.templateengine.uitree.UIAttributes;
import org.eclipse.cdt.ui.templateengine.uitree.UIElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This gives a Label and Boolean widget.
 */
public class UIBooleanWidget extends InputUIElement {
	/**
	 * Boolean widget.
	 */
	protected Button button;

	/**
	 * Label of this widget.
	 */
	protected Label label;

	/**
	 * Composite to which this widget control is added.
	 */
	protected UIComposite uiComposite;

	private boolean booleanValue;

	/**
	 * Constructor.
	 *
	 * @param uiAttributes
	 *            attribute associated with this widget.
	 */
	public UIBooleanWidget(UIAttributes uiAttributes, boolean defaultValue) {
		super(uiAttributes);
		this.booleanValue = defaultValue;
	}

	/**
	 * @return HashMap which contains the values in the Boolean Widget.
	 */
	@Override
	public Map<String, String> getValues() {
		Map<String, String> values = new HashMap<>();
		values.put(uiAttributes.get(UIElement.ID), Boolean.toString(booleanValue));
		return values;
	}

	/*
	 * @see org.eclipse.cdt.ui.templateengine.uitree.UIElement#setValues(java.util.Map)
	 */
	@Override
	public void setValues(Map<String, String> valueMap) {
		booleanValue = Boolean.parseBoolean(valueMap.get(uiAttributes.get(UIElement.ID)));
	}

	/*
	 * @see org.eclipse.cdt.ui.templateengine.uitree.UIElement#createWidgets(org.eclipse.cdt.ui.templateengine.uitree.uiwidgets.UIComposite)
	 */
	@Override
	public void createWidgets(UIComposite uiComposite) {
		GridData gridData = null;
		this.uiComposite = uiComposite;

		label = new Label(uiComposite, SWT.LEFT);
		label.setText(uiAttributes.get(InputUIElement.WIDGETLABEL));

		if (uiAttributes.get(UIElement.DESCRIPTION) != null) {
			String tipText = uiAttributes.get(UIElement.DESCRIPTION);
			tipText = tipText.replaceAll("\\\\r\\\\n", "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
			label.setToolTipText(tipText);
		}
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		Composite booleanContainer = new Composite(uiComposite, SWT.NONE);
		GridData gridcData = new GridData(GridData.FILL_HORIZONTAL);
		booleanContainer.setLayout(new GridLayout());
		booleanContainer.setLayoutData(gridcData);
		button = new Button(booleanContainer, SWT.CHECK);
		button.setData(".uid", uiAttributes.get(UIElement.ID)); //$NON-NLS-1$
		button.setSelection(booleanValue);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				booleanValue = button.getSelection();
			}
		});
	}

	/*
	 * @see org.eclipse.cdt.ui.templateengine.uitree.UIElement#isValid()
	 */
	@Override
	public boolean isValid() {
		boolean retVal = true;
		String mandatory = uiAttributes.get(InputUIElement.MANDATORY);
		if (!booleanValue && Boolean.parseBoolean(mandatory)) {
			retVal = false;
		}
		return retVal;
	}

	/*
	 * @see org.eclipse.cdt.ui.templateengine.uitree.UIElement#disposeWidget()
	 */
	@Override
	public void disposeWidget() {
		label.dispose();
		button.dispose();
	}
}
