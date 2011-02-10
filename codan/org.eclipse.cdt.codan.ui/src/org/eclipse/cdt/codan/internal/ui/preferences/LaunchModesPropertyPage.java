/*******************************************************************************
 * Copyright (c) 2010 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class LaunchModesPropertyPage extends FieldEditorPreferencePage {
	private Button useParentSettingsButton;
	private Button useLocalSettingsButton;
	private Button configureButton;
	private ArrayList<FieldEditor> editors;
	private Group useLocalGroup;

	/**
	 * @param prefStore
	 * 
	 */
	public LaunchModesPropertyPage(PreferenceStore prefStore) {
		super(GRID);
		setPreferenceStore(prefStore);
		editors = new ArrayList<FieldEditor>();
	}

	@Override
	public void noDefaultAndApplyButton() {
		super.noDefaultAndApplyButton();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors
	 * ()
	 */
	@Override
	protected void createFieldEditors() {
		createSelectionGroup(getFieldEditorParent());
		addField(new BooleanFieldEditor(
				CheckerLaunchMode.RUN_ON_FULL_BUILD.name(),
				"Run on full build", useLocalGroup));
		addField(new BooleanFieldEditor(
				CheckerLaunchMode.RUN_ON_INC_BUILD.name(),
				"Run on incremental build", useLocalGroup));
		addField(new BooleanFieldEditor(
				CheckerLaunchMode.RUN_AS_YOU_TYPE.name(),
				"Run as you type", useLocalGroup));
		updateFieldEditors();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.FieldEditorPreferencePage#addField(org.eclipse
	 * .jface.preference.FieldEditor)
	 */
	@Override
	protected void addField(FieldEditor editor) {
		editors.add(editor);
		super.addField(editor);
	}

	/**
	 * Creates and initializes a selection group with two choice buttons and one
	 * push button.
	 * 
	 * @param parent
	 *        - the parent composite
	 */
	private void createSelectionGroup(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Composite radioGroup = new Composite(comp, SWT.NONE);
		radioGroup.setLayout(new GridLayout(2, false));
		radioGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		useParentSettingsButton = createRadioButton(radioGroup,
				CodanUIMessages.OverlayPage_Use_Project_Settings);
		configureButton = new Button(radioGroup, SWT.PUSH);
		configureButton.setText("Configure...");
		configureButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				configureProjectSettings();
			}
		});
		useLocalSettingsButton = createRadioButton(radioGroup,
				"Use checker specific settings");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		useLocalSettingsButton.setLayoutData(gd);
		// Set workspace/project radio buttons
		try {
			Boolean useParent = getPreferenceStore().getBoolean(
					CheckerLaunchMode.USE_PARENT.name());
			if (useParent) {
				useParentSettingsButton.setSelection(true);
			} else {
				useLocalSettingsButton.setSelection(true);
				configureButton.setEnabled(false);
			}
		} catch (Exception e) {
			useParentSettingsButton.setSelection(true);
		}
		useLocalGroup = new Group(radioGroup, SWT.NONE);
		GridLayout layout2 = new GridLayout(2, false);
		useLocalGroup.setLayout(layout2);
		GridData gd2 = new GridData(GridData.FILL_BOTH);
		gd2.horizontalSpan = 2;
		useLocalGroup.setLayoutData(gd2);
	}

	/**
	 * 
	 */
	protected void configureProjectSettings() {
		// TODO Auto-generated method stub
	}

	/**
	 * Convenience method creating a radio button
	 * 
	 * @param parent
	 *        - the parent composite
	 * @param label
	 *        - the button label
	 * @return - the new button
	 */
	private Button createRadioButton(Composite parent, String label) {
		final Button button = new Button(parent, SWT.RADIO);
		button.setText(label);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean useParent = button == useParentSettingsButton;
				configureButton.setEnabled(useParent);
				getPreferenceStore().setValue(
						CheckerLaunchMode.USE_PARENT
								.name(), useParent);
				updateFieldEditors();
			}
		});
		return button;
	}

	private void updateFieldEditors() {
		// We iterate through all field editors
		boolean enabled = useLocalSettingsButton.getSelection();
		updateFieldEditors(enabled);
	}

	/**
	 * Enables or disables the field editors and buttons of this page Subclasses
	 * may override.
	 * 
	 * @param enabled
	 *        - true if enabled
	 */
	protected void updateFieldEditors(boolean enabled) {
		Composite parent = useLocalGroup;
		Iterator it = editors.iterator();
		while (it.hasNext()) {
			FieldEditor editor = (FieldEditor) it.next();
			editor.setEnabled(enabled, parent);
		}
	}

	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		return result;
	}
}
