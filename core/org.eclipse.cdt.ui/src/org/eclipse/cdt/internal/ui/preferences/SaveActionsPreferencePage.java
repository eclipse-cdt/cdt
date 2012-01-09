/*******************************************************************************
 * Copyright (c) 2009 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.preferences.OverlayPreferenceStore.OverlayKey;

/*
 * The page for configuring actions performed when a C/C++ file is saved.
 */
public class SaveActionsPreferencePage extends AbstractPreferencePage {
	private Button fRadioEditedLines;
	private Button fRadioAllLines;

	public SaveActionsPreferencePage() {
		super();
	}

	@Override
	protected OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
		ArrayList<OverlayKey> overlayKeys = new ArrayList<OverlayKey>();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.REMOVE_TRAILING_WHITESPACE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.REMOVE_TRAILING_WHITESPACE_LIMIT_TO_EDITED_LINES));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				PreferenceConstants.ENSURE_NEWLINE_AT_EOF));

        OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				ICHelpContextIds.SAVE_ACTIONS_PREFERENCE_PAGE);
	}

	/**
	 * Sets enabled flag for a control and all its sub-tree.
	 */
	protected static void setEnabled(Control control, boolean enable) {
		control.setEnabled(enable);
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			Control[] children = composite.getChildren();
			for (Control element : children) {
				setEnabled(element, enable);
			}
		}
	}

	private Control createConfigurationBlock(Composite parent) {
		Composite composite= ControlFactory.createComposite(parent, 1);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		String label = PreferencesMessages.SaveActionsPreferencePage_removeTrailingWhitespace;
		Button checkboxTrailingWhitespace = addCheckBox(composite, label,
				PreferenceConstants.REMOVE_TRAILING_WHITESPACE, 0);
		fRadioEditedLines = addRadioButton(composite, PreferencesMessages.SaveActionsPreferencePage_inEditedLines,
				PreferenceConstants.REMOVE_TRAILING_WHITESPACE_LIMIT_TO_EDITED_LINES, 0);
		fRadioAllLines = addRadioButton(composite, PreferencesMessages.SaveActionsPreferencePage_inAllLines,
				null, 0);
		createDependency(checkboxTrailingWhitespace,
				PreferenceConstants.REMOVE_TRAILING_WHITESPACE, fRadioEditedLines);
		createDependency(checkboxTrailingWhitespace,
				PreferenceConstants.REMOVE_TRAILING_WHITESPACE, fRadioAllLines);

		ControlFactory.createEmptySpace(composite, 1);

		label = PreferencesMessages.SaveActionsPreferencePage_ensureNewline;
		addCheckBox(composite, label, PreferenceConstants.ENSURE_NEWLINE_AT_EOF, 0);
		
		return composite;
	}

	@Override
	protected Control createContents(Composite parent) {
		fOverlayStore.load();
		fOverlayStore.start();

		createConfigurationBlock(parent);
		
		initialize();
		return parent;
	}

	private void initialize() {
		initializeFields();
		fRadioAllLines.setSelection(!fRadioEditedLines.getSelection());
	}
}
