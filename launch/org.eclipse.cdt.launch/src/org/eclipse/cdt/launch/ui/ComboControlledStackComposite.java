/*******************************************************************************
 *  Copyright (c) 2009 QNX Software Systems and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      QNX Software Systems - initial API and implementation
 *      Freescale Semiconductor
 *******************************************************************************/
package org.eclipse.cdt.launch.ui;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Stack Composite - Switch between panes controlled by combo box
 * @since 6.0
 */
public class ComboControlledStackComposite extends Composite {
	private Composite fArea;
	private Combo fCombo;
	private Map<String, Composite> tabMap; // label ==> tab
	private StackLayout layout;
	private Label fLabel;

	public ComboControlledStackComposite(Composite parent, int style) {
		super(parent, style);
		tabMap = new LinkedHashMap<>();
		setLayout(new GridLayout(2, false));
		createContents(this);
	}

	public void setLabelText(String label) {
		fLabel.setText(label);
	}

	public void addItem(String label, Composite tab) {
		tabMap.put(label, tab);
		fCombo.add(label);
		if (layout.topControl == null) {
			layout.topControl = tab;
			fCombo.setText(label);
		}
	}

	public void deleteItem(String label) {
		if (fCombo.getText().equals(label)) {
			setSelection(fCombo.getItem(0));
		}
		Composite tab = tabMap.get(label);
		if (tab != null) {
			tab.dispose();
			tabMap.remove(label);
		}
	}

	public void setSelection(String label) {
		fCombo.setText(label);
		setPage(label);
	}

	protected void createContents(Composite parent) {
		fLabel = createLabel(this);
		fCombo = createCombo(this);
		GridData cgd = new GridData(GridData.FILL_HORIZONTAL);

		fCombo.setLayoutData(cgd);
		fArea = createTabArea(this);
		GridData agd = new GridData(GridData.FILL_BOTH);
		agd.horizontalSpan = 2;
		fArea.setLayoutData(agd);
	}

	public Composite getStackParent() {
		return fArea;
	}

	public Label getLabel() {
		return fLabel;
	}

	public Combo getCombo() {
		return fCombo;
	}

	protected Composite createTabArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		layout = new StackLayout();
		comp.setLayout(layout);

		return comp;
	}

	protected Label createLabel(Composite parent) {
		Label label = new Label(parent, SWT.WRAP);
		return label;
	}

	protected Combo createCombo(Composite parent) {
		Combo box = new Combo(parent, SWT.READ_ONLY);
		box.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String name = fCombo.getText();
				comboSelected(name);
			}
		});
		return box;
	}

	protected void comboSelected(String label) {
		setPage(label);
	}

	protected void setPage(String label) {
		layout.topControl = tabMap.get(label);
		getStackParent().layout();
	}

	public Control getTopControl() {
		return layout != null ? layout.topControl : null;
	}
}
