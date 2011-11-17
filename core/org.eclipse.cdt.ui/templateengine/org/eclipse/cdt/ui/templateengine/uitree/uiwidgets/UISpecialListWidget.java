/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
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
	public UISpecialListWidget(UIAttributes attribute) {
		super(attribute);
	}

	/**
	 * create a Label and Text widget, add it to UIComposite. set Layout for the
	 * widgets to be added to UIComposite. set required parameters to the
	 * Widgets.
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

		if (uiAttributes.get(InputUIElement.DESCRIPTION) != null){
			String tipText = uiAttributes.get(UIElement.DESCRIPTION);
			tipText = tipText.replaceAll("\\\\r\\\\n", "\r\n"); //$NON-NLS-1$, //$NON-NLS-2$
			label.setToolTipText(tipText);
		}
		Composite flcComposite = new Composite(uiComposite, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		flcComposite.setLayout(new GridLayout());
		flcComposite.setLayoutData(gridData);

		fileListControl = new FileListControl(flcComposite, uiAttributes.get(InputUIElement.WIDGETLABEL), 1);
		fileListControl.setList(itemsList.toArray(new String[itemsList.size()]));
		fileListControl.setSelection(0);
		fileListControl.addChangeListener(new IFileListChangeListener(){
			@Override
			public void fileListChanged(FileListControl fileList, String oldValue[], String newValue[]) {
				itemsList.clear();
				itemsList.addAll(Arrays.asList(newValue));
				uiComposite.firePatternEvent(createPatternEvent());
			}
		});
		uiComposite.firePatternEvent(createPatternEvent());
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
