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
import java.text.MessageFormat;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
/**
 * @version 	1.0
 * @author
 */
public class WizardCheckboxTablePart extends CheckboxTablePart {
	public static final String KEY_SELECT_ALL = MakeUIPlugin.getResourceString("WizardCheckboxTablePart.WizardCheckboxTablePart.selectAll"); //$NON-NLS-1$
	public static final String KEY_DESELECT_ALL = MakeUIPlugin.getResourceString("WizardCheckboxTablePart.WizardCheckboxTablePart.deselectAll"); //$NON-NLS-1$
	public static final String KEY_COUNTER = MakeUIPlugin.getResourceString("WizardCheckboxTablePart.WizardCheckboxTablePart.counter"); //$NON-NLS-1$

	private final String EMPTY_STRING = ""; //$NON-NLS-1$
	private int selectAllIndex = -1;
	private int deselectAllIndex = -1;
	private String tableName;
	private int counter;
	private Label counterLabel;

	/**
	 * Constructor for WizardCheckboxTablePart.
	 * @param buttonLabels
	 */
	public WizardCheckboxTablePart(String tableName, String[] buttonLabels) {
		super(buttonLabels);
		this.tableName = tableName;
	}

	public WizardCheckboxTablePart(String mainLabel) {
		this(
			mainLabel,
			new String[] { MakeUIPlugin.getResourceString(KEY_SELECT_ALL), MakeUIPlugin.getResourceString(KEY_DESELECT_ALL)});
		setSelectAllIndex(0);
		setDeselectAllIndex(1);
	}

	public void setSelectAllIndex(int index) {
		this.selectAllIndex = index;
	}
	public void setDeselectAllIndex(int index) {
		this.deselectAllIndex = index;
	}

	protected void buttonSelected(Button button, int index) {
		if (index == selectAllIndex) {
			handleSelectAll(true);
		}
		if (index == deselectAllIndex) {
			handleSelectAll(false);
		}
	}

	public Object[] getSelection() {
		CheckboxTableViewer viewer = getTableViewer();
		return viewer.getCheckedElements();
	}

	public void setSelection(Object[] selected) {
		CheckboxTableViewer viewer = getTableViewer();
		viewer.setCheckedElements(selected);
		updateCounter(selected.length);
	}

	public void createControl(Composite parent) {
		createControl(parent, SWT.NULL, 2);
		counterLabel = new Label(parent, SWT.NULL);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		counterLabel.setLayoutData(gd);
		updateCounter(0);
	}

	protected Button createButton(Composite parent, String label, int index) {
		Button button = super.createButton(parent, label, index);
		return button;
	}

	protected StructuredViewer createStructuredViewer(Composite parent, int style) {
		StructuredViewer viewer = super.createStructuredViewer(parent, style);
		return viewer;
	}

	protected void createMainLabel(Composite parent, int span) {
		if (tableName == null)
			return;
		Label label = new Label(parent, SWT.NULL);
		label.setText(tableName);
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		label.setLayoutData(gd);
	}

	protected void updateCounter(int amount) {
		counter = amount;
		updateCounterLabel();
	}

	protected void updateCounterLabel() {
		String number = EMPTY_STRING + getSelectionCount();
		String totalNumber = EMPTY_STRING + getTotalCount();
		String message = MessageFormat.format(MakeUIPlugin.getResourceString(KEY_COUNTER), new String[] { number, totalNumber });
		counterLabel.setText(message);
	}

	public int getSelectionCount() {
		return counter;
	}

	public void selectAll(boolean select) {
		handleSelectAll(select);
	}

	private int getTotalCount() {
		CheckboxTableViewer viewer = getTableViewer();
		return viewer.getTable().getItemCount();
	}

	protected void handleSelectAll(boolean select) {
		CheckboxTableViewer viewer = getTableViewer();
		viewer.setAllChecked(select);
		int selected;
		if (!select) {
			selected = 0;
		} else {
			selected = getTotalCount();
		}
		updateCounter(selected);
	}
	protected void elementChecked(Object element, boolean checked) {
		int count = getSelectionCount();
		updateCounter(checked ? count + 1 : count - 1);
	}
}
