/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.uitree.uiwidgets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.ui.templateengine.uitree.InputUIElement;
import org.eclipse.cdt.ui.templateengine.uitree.UIAttributes;
import org.eclipse.cdt.ui.templateengine.uitree.UIElement;


/**
 * This gives a Label and Boolean widget.
 */

public class UIBooleanWidget extends InputUIElement {

	/**
	 * Attributes associated with this widget.
	 */
	protected UIAttributes/*<String, String>*/ uiAttribute;

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
	 * @param uiAttribute
	 *            attribute associated with this widget.
	 */
	public UIBooleanWidget(UIAttributes/*<String, String>*/ uiAttribute) {
		super(uiAttribute);
		this.uiAttribute = uiAttribute;
		this.booleanValue = false;
	}

	/**
	 * @return HashMap which contains the values in the Boolean Widget.
	 */
	public Map/*<String, String>*/ getValues() {

		Map/*<String, String>*/ retMap = new HashMap/*<String, String>*/();
		retMap.put(uiAttribute.get(InputUIElement.ID), new Boolean(booleanValue).toString());

		return retMap;
	}

	/**
	 * Set the Boolean widget with new value.
	 * 
	 * @param valueMap
	 */
	public void setValues(Map/*<String, String>*/ valueMap) {
		booleanValue = new Boolean((String) valueMap.get(uiAttribute.get(InputUIElement.ID))).booleanValue();
	}

	/**
	 * create a Label and Boolean widget, add it to UIComposite. set Layout for
	 * the widgets to be added to UIComposite. set required parameters to the
	 * Widgets.
	 * 
	 * @param uiComposite
	 */
	public void createWidgets(UIComposite uiComposite) {
		GridData gridData = null;
		this.uiComposite = uiComposite;

		label = new Label(uiComposite, SWT.LEFT);
		label.setText((String) uiAttribute.get(InputUIElement.WIDGETLABEL));

		if (uiAttribute.get(InputUIElement.DESCRIPTION) != null){
			String tipText = (String) uiAttribute.get(UIElement.DESCRIPTION);
			tipText = tipText.replaceAll("\\\\r\\\\n", "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
			label.setToolTipText(tipText);
		}
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		Composite booleanConatiner = new Composite(uiComposite, SWT.NONE);
		GridData gridcData = new GridData(GridData.FILL_HORIZONTAL);
		booleanConatiner.setLayout(new GridLayout());
		booleanConatiner.setLayoutData(gridcData);
		button = new Button(booleanConatiner, SWT.CHECK);
		button.setData(".uid", uiAttribute.get(UIElement.ID)); //$NON-NLS-1$
		button.setSelection(new Boolean(booleanValue).booleanValue());
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				booleanValue = button.getSelection();
			}
		});
	}

	/**
	 * Based on the stae of this Widget return true or false. This return value
	 * will be used by the UIPage to update its(UIPage) state. Return value
	 * depends on the value contained in Boolean Widget. If boolean value
	 * contained is false and Mandatory value from attributes.
	 * 
	 * @return boolean.
	 */
	public boolean isValid() {
		boolean retVal = true;
		String mandatory = (String) uiAttribute.get(InputUIElement.MANDATORY);

		if (!booleanValue && mandatory.equalsIgnoreCase(TemplateEngineHelper.BOOLTRUE)) {
			retVal = false;
		}
		return retVal;
	}

	/**
	 * call the dispose method on the widgets. This is to ensure that the
	 * widgets are properly disposed.
	 */
	public void disposeWidget() {
		label.dispose();
		button.dispose();
	}

}
