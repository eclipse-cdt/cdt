/*******************************************************************************
 * Copyright (c) 2005, 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Symbian Software Limited - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.templateengine.uitree.uiwidgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.ui.templateengine.uitree.InputUIElement;
import org.eclipse.cdt.ui.templateengine.uitree.UIAttributes;
import org.eclipse.cdt.ui.templateengine.uitree.UIElement;
import org.eclipse.cdt.utils.ui.controls.FileListControl;
import org.eclipse.cdt.utils.ui.controls.IFileListChangeListener;


/**
 * This gives a Label and StringList Widget.
 * 
 */

public class UIStringListWidget extends InputUIElement {

	/**
	 * Attributes associated with this widget.
	 */
	protected UIAttributes/*<String, String>*/ uiAttribute;

	/**
	 * StringList widget.
	 */
	protected FileListControl fileListControl;

	/**
	 * Label of this widget.
	 */
	protected Label label;

	/**
	 * Composite to which this widget control is added.
	 */
	protected UIComposite uiComposite;
	
	protected List itemsList;

	/**
	 * Constructor.
	 * 
	 * @param attribute
	 *            attribute associated with this widget.
	 */
	public UIStringListWidget(UIAttributes/*<String, String>*/ attribute) {
		super(attribute);
		uiAttribute = attribute;
		itemsList = new ArrayList();
	}

	/**
	 * @return String_List value contained in the String_List Widget.
	 */
	public Map/*<String, String>*/ getValues() {
		Map/*<String, String>*/ retMap = new HashMap/*<String, String>*/();
		String itemString = new String();
		for (int i = 0; i < itemsList.size(); i++) {
			itemString = itemString + itemsList.get(i) + "|"; //$NON-NLS-1$
		}
		retMap.put(uiAttribute.get(InputUIElement.ID), itemString);

		return retMap;
	}

	/**
	 * Set the Text widget with new value.
	 * 
	 * @param valueMap
	 */
	public void setValues(Map/*<String, String>*/ valueMap) {
		String items = (String) valueMap.get(uiAttribute.get(InputUIElement.ID));

		if (items != null) {
			items = items.trim();
			StringTokenizer st = new StringTokenizer(items, "|"); //$NON-NLS-1$
			for (int i = 0; st.hasMoreTokens(); i++) {
				itemsList.add(st.nextToken());
			}
		}
	}

	/**
	 * create a Label and StringList widget, add it to UIComposite. set Layout
	 * for the widgets to be added to UIComposite. set required parameters to
	 * the Widgets.
	 * 
	 * @param composite
	 */
	public void createWidgets(UIComposite composite) {
		GridData gridData = null;
		uiComposite = composite;

		label = new Label(composite, SWT.LEFT);
		label.setText((String) uiAttribute.get(InputUIElement.WIDGETLABEL));

		GridData gd = new GridData();
		gd.verticalAlignment = SWT.BEGINNING;
		gd.verticalIndent = 5;
		label.setLayoutData(gd);

		if (uiAttribute.get(InputUIElement.DESCRIPTION) != null){
			String tipText = (String) uiAttribute.get(UIElement.DESCRIPTION);
			tipText = tipText.replaceAll("\\\\r\\\\n", "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$, $NON-NLS-2$
			label.setToolTipText(tipText);
		}
		Composite flcComposite = new Composite(uiComposite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		flcComposite.setLayout(new GridLayout());
		flcComposite.setLayoutData(gridData);

		fileListControl = new FileListControl(flcComposite, (String) uiAttribute.get(InputUIElement.WIDGETLABEL), 0);
		fileListControl.setList((String[])itemsList.toArray());
		fileListControl.setSelection(0);
		fileListControl.addChangeListener(new IFileListChangeListener(){
			public void fileListChanged(FileListControl fileList, String oldValue[], String newValue[]) {
				itemsList.addAll(Arrays.asList(newValue));
			}
		});

	}

	/**
	 * Based on the stae of this Widget return true or false. This return value
	 * will be used by the UIPage to update its(UIPage) state. Return value
	 * depends on the value contained in String List Widget. If value contained
	 * is null and Mandatory value from attributes.
	 * 
	 * @return boolean.
	 */
	public boolean isValid() {
		boolean retVal = true;
		String mandatory = (String) uiAttribute.get(InputUIElement.MANDATORY);

		if ((itemsList == null || itemsList.size() == 0) && (mandatory.equalsIgnoreCase(TemplateEngineHelper.BOOLTRUE))) {
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
		fileListControl = null;
	}

}
