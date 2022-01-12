/*******************************************************************************
 * Copyright (c) 2005, 2020 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Symbian Software Limited - initial API and implementation
 *     Alexander Fedorov (ArSysOp) - Bug 561993 - Remove dependency to com.ibm.icu from CDT UI
 *******************************************************************************/

package org.eclipse.cdt.ui.templateengine.uitree.uiwidgets;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.ui.templateengine.event.PatternEvent;
import org.eclipse.cdt.ui.templateengine.uitree.InputUIElement;
import org.eclipse.cdt.ui.templateengine.uitree.UIAttributes;
import org.eclipse.cdt.ui.templateengine.uitree.UIElement;
import org.eclipse.cdt.utils.ui.controls.FileListControl;
import org.eclipse.cdt.utils.ui.controls.IFileListChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This gives a Label and StringList Widget.
 *
 */
public class UIStringListWidget extends InputUIElement {
	/**
	 * StringList widget.
	 */
	protected FileListControl fileListControl;

	/**
	 * Label of this widget.
	 */
	protected Label label;

	protected List<String> itemsList;

	/**
	 * Constructor.
	 *
	 * @param attribute
	 *            attribute associated with this widget.
	 */
	public UIStringListWidget(UIAttributes attribute) {
		super(attribute);
		itemsList = new ArrayList<>();
	}

	/**
	 * @return String_List value contained in the String_List Widget.
	 */
	@Override
	public Map<String, String> getValues() {
		Map<String, String> retMap = new HashMap<>();
		String itemString = ""; //$NON-NLS-1$
		for (int i = 0; i < itemsList.size(); i++) {
			itemString = itemString + itemsList.get(i) + "|"; //$NON-NLS-1$
		}
		retMap.put(uiAttributes.get(UIElement.ID), itemString);

		return retMap;
	}

	/**
	 * Set the Text widget with new value.
	 *
	 * @param valueMap
	 */
	@Override
	public void setValues(Map<String, String> valueMap) {
		String items = valueMap.get(uiAttributes.get(UIElement.ID));

		if (items != null) {
			items = items.trim();
			StringTokenizer st = new StringTokenizer(items, "|"); //$NON-NLS-1$
			while (st.hasMoreTokens()) {
				itemsList.add(st.nextToken());
			}
		}
	}

	/**
	 * create a Label and StringList widget, add it to UIComposite. set Layout
	 * for the widgets to be added to UIComposite. set required parameters to
	 * the Widgets.
	 *
	 * @param uiComposite
	 */
	@Override
	public void createWidgets(final UIComposite uiComposite) {
		GridData gridData = null;

		label = new Label(uiComposite, SWT.LEFT);
		label.setText(uiAttributes.get(InputUIElement.WIDGETLABEL));

		GridData gd = new GridData();
		gd.verticalAlignment = SWT.BEGINNING;
		gd.verticalIndent = 5;
		label.setLayoutData(gd);

		if (uiAttributes.get(UIElement.DESCRIPTION) != null) {
			String tipText = uiAttributes.get(UIElement.DESCRIPTION);
			tipText = tipText.replaceAll("\\\\r\\\\n", "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$, $NON-NLS-2$
			label.setToolTipText(tipText);
		}
		Composite flcComposite = new Composite(uiComposite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		flcComposite.setLayout(new GridLayout());
		flcComposite.setLayoutData(gridData);

		fileListControl = new FileListControl(flcComposite, uiAttributes.get(InputUIElement.WIDGETLABEL), 0);
		fileListControl.setList(itemsList.toArray(new String[itemsList.size()]));
		fileListControl.setSelection(0);
		fileListControl.addChangeListener(new IFileListChangeListener() {
			@Override
			public void fileListChanged(FileListControl fileList, String oldValue[], String newValue[]) {
				itemsList.clear();
				itemsList.addAll(Arrays.asList(newValue));
				uiComposite.firePatternEvent(createPatternEvent());
			}
		});

		uiComposite.firePatternEvent(createPatternEvent());
	}

	protected PatternEvent createPatternEvent() {
		String msg = MessageFormat.format("Please add an item to {0}", label.getText()); //$NON-NLS-1$
		return new PatternEvent(this, msg, isValid());
	}

	/**
	 * Based on the stage of this Widget return true or false. This return value
	 * will be used by the UIPage to update its(UIPage) state. Return value
	 * depends on the value contained in String List Widget. If value contained
	 * is null and Mandatory value from attributes.
	 *
	 * @return boolean.
	 */
	@Override
	public boolean isValid() {
		boolean retVal = true;
		String mandatory = uiAttributes.get(InputUIElement.MANDATORY);

		if ((itemsList == null || itemsList.size() == 0)
				&& (mandatory.equalsIgnoreCase(TemplateEngineHelper.BOOLTRUE))) {
			retVal = false;
		}
		return retVal;
	}

	/**
	 * call the dispose method on the widgets. This is to ensure that the
	 * widgets are properly disposed.
	 */
	@Override
	public void disposeWidget() {
		label.dispose();
		fileListControl = null;
	}

}
