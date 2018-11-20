/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.memory.traditional;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ColorAndEffectFieldEditor extends FieldEditor {

	private final String nameBold;
	private final String nameItalic;
	private final String nameBox;

	private ColorSelector colorSelector;
	private Button checkBold;
	private Button checkItalic;
	private Button checkBox; // :)

	public ColorAndEffectFieldEditor(String name, String nameBold, String nameItalic, String nameBox, String labelText,
			Composite parent) {
		super(name, labelText, parent);
		this.nameBold = nameBold;
		this.nameItalic = nameItalic;
		this.nameBox = nameBox;
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		((GridData) checkItalic.getLayoutData()).horizontalSpan = numColumns - 4;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		control.setLayoutData(new GridData());

		colorSelector = new ColorSelector(parent);
		colorSelector.getButton().setLayoutData(new GridData());

		checkBold = new Button(parent, SWT.CHECK);
		checkBold.setText(TraditionalRenderingMessages.getString("ColorAndEffectFieldEditor.bold"));
		checkBold.setLayoutData(new GridData());

		checkItalic = new Button(parent, SWT.CHECK);
		checkItalic.setText(TraditionalRenderingMessages.getString("ColorAndEffectFieldEditor.italic"));
		checkItalic.setLayoutData(new GridData());

		checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(TraditionalRenderingMessages.getString("ColorAndEffectFieldEditor.box"));
		checkBox.setLayoutData(new GridData());
	}

	@Override
	protected void doLoad() {
		IPreferenceStore store = getPreferenceStore();
		colorSelector.setColorValue(PreferenceConverter.getColor(store, getPreferenceName()));
		checkBold.setSelection(store.getBoolean(nameBold));
		checkItalic.setSelection(store.getBoolean(nameItalic));
		checkBox.setSelection(store.getBoolean(nameBox));
	}

	@Override
	protected void doLoadDefault() {
		IPreferenceStore store = getPreferenceStore();
		colorSelector.setColorValue(PreferenceConverter.getDefaultColor(store, getPreferenceName()));
		checkBold.setSelection(store.getDefaultBoolean(nameBold));
		checkItalic.setSelection(store.getDefaultBoolean(nameItalic));
		checkBox.setSelection(store.getDefaultBoolean(nameBox));
	}

	@Override
	protected void doStore() {
		IPreferenceStore store = getPreferenceStore();
		PreferenceConverter.setValue(store, getPreferenceName(), colorSelector.getColorValue());
		store.setValue(nameBold, checkBold.getSelection());
		store.setValue(nameItalic, checkItalic.getSelection());
		store.setValue(nameBox, checkBox.getSelection());
	}

	@Override
	public int getNumberOfControls() {
		return 5;
	}

}
