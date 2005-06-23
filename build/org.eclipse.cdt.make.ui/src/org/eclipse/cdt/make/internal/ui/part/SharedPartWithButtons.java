/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.part;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @version 	1.0
 * @author
 */
public abstract class SharedPartWithButtons extends SharedPart {
	private String[] buttonLabels;
	private Control[] controls;
	private Composite buttonContainer;

	private class SelectionHandler implements SelectionListener {
		public void widgetSelected(SelectionEvent e) {
			buttonSelected(e);
		}
		public void widgetDefaultSelected(SelectionEvent e) {
			buttonSelected(e);
		}
		private void buttonSelected(SelectionEvent e) {
			Integer index = (Integer) e.widget.getData();
			SharedPartWithButtons.this.buttonSelected((Button) e.widget, index.intValue());
		}
	}

	public SharedPartWithButtons(String[] buttonLabels) {
		this.buttonLabels = buttonLabels;
	}

	public void setButtonEnabled(int index, boolean enabled) {
		if (controls != null && index >= 0 && controls.length > index) {
			Control c = controls[index];
			if (c instanceof Button)
				c.setEnabled(enabled);
		}
	}

	protected abstract void createMainControl(Composite parent, int style, int span);
	protected abstract void buttonSelected(Button button, int index);

	/*
	 * @see SharedPart#createControl(Composite, FormWidgetFactory)
	 */
	public void createControl(Composite parent, int style, int span) {
		createMainLabel(parent, span);
		createMainControl(parent, style, span - 1);
		if (buttonLabels != null && buttonLabels.length > 0) {
			buttonContainer = createComposite(parent);
			GridData gd = new GridData(GridData.FILL_VERTICAL);
			buttonContainer.setLayoutData(gd);
			buttonContainer.setLayout(createButtonsLayout());

			controls = new Control[buttonLabels.length];
			SelectionHandler listener = new SelectionHandler();
			for (int i = 0; i < buttonLabels.length; i++) {
				String label = buttonLabels[i];
				if (label != null) {
					Button button = createButton(buttonContainer, label, i);
					button.addSelectionListener(listener);
					controls[i] = button;
				} else {
					createEmptySpace(buttonContainer, 1);
				}
			}
		}
	}

	protected GridLayout createButtonsLayout() {
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		return layout;
	}

	protected Button createButton(Composite parent, String label, int index) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		button.setLayoutData(gd);
		button.setData(new Integer(index));
		return button;
	}

	protected void updateEnabledState() {
		for (int i = 0; i < controls.length; i++) {
			Control c = controls[i];
			if (c instanceof Button)
				c.setEnabled(isEnabled());
		}
	}

	protected void createMainLabel(Composite parent, int span) {
	}
}
