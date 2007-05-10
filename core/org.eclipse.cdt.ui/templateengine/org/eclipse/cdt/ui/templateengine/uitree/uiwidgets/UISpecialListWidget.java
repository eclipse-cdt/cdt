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

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.cdt.ui.templateengine.uitree.InputUIElement;
import org.eclipse.cdt.ui.templateengine.uitree.UIAttributes;
import org.eclipse.cdt.ui.templateengine.uitree.UIElement;
import org.eclipse.cdt.utils.ui.controls.FileListControl;
import org.eclipse.cdt.utils.ui.controls.IFileListChangeListener;


/**
 * This gives a Label and UISpecialList Widget.
 * 
 */
public class UISpecialListWidget extends UIStringListWidget {

	/**
	 * Constructor.
	 * 
	 * @param attribute
	 *            attribute associated with this widget.
	 */
	public UISpecialListWidget(UIAttributes/*<String, String>*/ attribute) {
		super(attribute);
		uiAttribute = attribute;
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

		GridData gd = new GridData();
		gd.verticalAlignment = SWT.BEGINNING;
		gd.verticalIndent = 5;
		label.setLayoutData(gd);

		if (uiAttribute.get(InputUIElement.DESCRIPTION) != null){
			String tipText = (String) uiAttribute.get(UIElement.DESCRIPTION);
			tipText = tipText.replaceAll("\\\\r\\\\n", "\r\n"); //$NON-NLS-1$, //$NON-NLS-2$
			label.setToolTipText(tipText);
		}
		Composite flcComposite = new Composite(composite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		flcComposite.setLayout(new GridLayout());
		flcComposite.setLayoutData(gridData);

		fileListControl = new FileListControl(flcComposite, (String) uiAttribute.get(InputUIElement.WIDGETLABEL), 1);
		fileListControl.setList((String[])itemsList.toArray());
		fileListControl.setSelection(0);
		fileListControl.addChangeListener(new IFileListChangeListener(){
			public void fileListChanged(FileListControl fileList, String oldValue[], String newValue[]) {
				itemsList.addAll(Arrays.asList(newValue));
			}
		});
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
