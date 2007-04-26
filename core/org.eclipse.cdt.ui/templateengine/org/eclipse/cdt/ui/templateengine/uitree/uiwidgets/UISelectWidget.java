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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.ui.templateengine.uitree.InputUIElement;
import org.eclipse.cdt.ui.templateengine.uitree.UIAttributes;
import org.eclipse.cdt.ui.templateengine.uitree.UIElement;


/**
 * This gives a Label and Combo widget.
 */
public class UISelectWidget extends InputUIElement {
	/**
	 * Attributes associated with this widget.
	 */
	UIAttributes/*<String, String>*/ uiAttribute;

	/**
	 * Select widget.
	 */
	Combo combo;

	/**
	 * Label of this widget.
	 */
	Label label;

	/**
	 * Composite to which this widget control is added.
	 */
	UIComposite uiComposite;

	/**
	 * Map contains the values of Select Widget
	 */
	HashMap/*<String, String>*/ itemMap;

	/**
	 * Default value of Select Widget
	 */
	String itemSelected;

	/**
	 * Constructor for Select Widget.
	 * 
	 * @param attribute
	 *            attribute associated with this widget.
	 */
	public UISelectWidget(UIAttributes/*<String, String>*/ attribute, HashMap/*<String, String>*/ itemMap,
			String itemSelected) {

		super(attribute);
		uiAttribute = attribute;
		this.itemMap = itemMap;
		this.itemSelected = itemSelected;
	}

	/**
	 * @return Boolean value contained in the Boolean Widget.
	 */
	public Map/*<String, String>*/ getValues() {

		Map/*<String, String>*/ retMap = new HashMap/*<String, String>*/();
		retMap.put(uiAttribute.get(InputUIElement.ID), itemMap.get(combo.getItem(combo.getSelectionIndex())));

		return retMap;
	}

	/**
	 * Set the Text widget with new value.
	 * 
	 * @param valueMap
	 */
	public void setValues(Map/*<String, String>*/ valueMap) {

		String val = (String) valueMap.get(uiAttribute.get(InputUIElement.ID));
		if (val != null)
			val = val.trim();
		String[] items = combo.getItems();
		int index = 0;
		for (int i = 0; i < items.length; i++)
			if (itemMap.get(items[i]).equals(val))
				index = i;

		combo.select(index);
	}

	/**
	 * create a Label and Text widget, add it to UIComposite. set Layout for the
	 * widgets to be added to UIComposite. set required parameters to the
	 * Widgets.
	 * 
	 * @param composite
	 */
	public void createWidgets(UIComposite composite) {
		GridData gridData = null;
		uiComposite = composite;

		label = new Label(composite, SWT.LEFT);
		label.setText((String) uiAttribute.get(InputUIElement.WIDGETLABEL));

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;

		Composite comboComposite = new Composite(composite, SWT.NONE);

		gridData = new GridData(GridData.FILL_HORIZONTAL);
		comboComposite.setLayout(new GridLayout());
		comboComposite.setLayoutData(gridData);

		combo = new Combo(comboComposite, SWT.DROP_DOWN | SWT.READ_ONLY);

		Set keySet = itemMap.keySet();
		Iterator mapIterator = keySet.iterator();

		int index = 0;
		int i = 0;
		while (mapIterator.hasNext()) {

			String key = (String) mapIterator.next();
			combo.add(key);
			if (itemSelected.equals(key))
				index = i;
			i++;
		}

		combo.select(index);
		combo.setData(".uid", uiAttribute.get(UIElement.ID)); //$NON-NLS-1$
	}

	/**
	 * Based on the stae of this Widget return true or false. This return value
	 * will be used by the UIPage to update its(UIPage) state. Return value
	 * depends on the value contained in Select Widget. If value contained is
	 * null, "" and Mandatory value from attributes.
	 * 
	 * @return boolean.
	 */
	public boolean isValid() {
		boolean retVal = true;
		String mandatory = (String) uiAttribute.get(InputUIElement.MANDATORY);

		if (((combo.getText() == null) || (combo.getText().equals("")) //$NON-NLS-1$
		|| (combo.getText().trim().length() < 1)) && (mandatory.equalsIgnoreCase(TemplateEngineHelper.BOOLTRUE))) {

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
		combo.dispose();
	}

}
