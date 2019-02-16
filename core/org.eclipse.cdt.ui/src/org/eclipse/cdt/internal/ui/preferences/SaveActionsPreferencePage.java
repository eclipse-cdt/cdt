/*******************************************************************************
 * Copyright (c) 2013, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 * 	   Serge Beauchamp (Freescale Semiconductor) - Bug 418810
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import static org.eclipse.cdt.ui.PreferenceConstants.ALIGN_ALL_CONST;
import static org.eclipse.cdt.ui.PreferenceConstants.ENSURE_NEWLINE_AT_EOF;
import static org.eclipse.cdt.ui.PreferenceConstants.FORMAT_SOURCE_CODE;
import static org.eclipse.cdt.ui.PreferenceConstants.FORMAT_SOURCE_CODE_LIMIT_TO_EDITED_LINES;
import static org.eclipse.cdt.ui.PreferenceConstants.REMOVE_TRAILING_WHITESPACE;
import static org.eclipse.cdt.ui.PreferenceConstants.REMOVE_TRAILING_WHITESPACE_LIMIT_TO_EDITED_LINES;

import java.util.ArrayList;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.preferences.OverlayPreferenceStore.OverlayKey;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

/*
 * The page for configuring actions performed when a C/C++ file is saved.
 */
public class SaveActionsPreferencePage extends AbstractPreferencePage {
	private Button fRadioFormatAllLines;
	private Button fRadioFormatEditedLines;
	private Button fRadioTrailingWhitespaceAllLines;
	private Button fRadioTrailingWhitespaceEditedLines;

	public SaveActionsPreferencePage() {
		super();
	}

	@Override
	protected OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
		ArrayList<OverlayKey> overlayKeys = new ArrayList<>();

		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, FORMAT_SOURCE_CODE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				FORMAT_SOURCE_CODE_LIMIT_TO_EDITED_LINES));
		overlayKeys
				.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, REMOVE_TRAILING_WHITESPACE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN,
				REMOVE_TRAILING_WHITESPACE_LIMIT_TO_EDITED_LINES));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ENSURE_NEWLINE_AT_EOF));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, ALIGN_ALL_CONST));

		OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ICHelpContextIds.SAVE_ACTIONS_PREFERENCE_PAGE);
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
		Composite composite = ControlFactory.createComposite(parent, 1);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Button checkboxFormat = addCheckBox(composite, PreferencesMessages.SaveActionsPreferencePage_formatSourceCode,
				FORMAT_SOURCE_CODE, 0);
		Composite group = createRadioContainer(composite);
		fRadioFormatAllLines = addRadioButton(group, PreferencesMessages.SaveActionsPreferencePage_formatAllLines, null,
				0);
		fRadioFormatEditedLines = addRadioButton(group, PreferencesMessages.SaveActionsPreferencePage_formatEditedLines,
				FORMAT_SOURCE_CODE_LIMIT_TO_EDITED_LINES, 0);
		createDependency(checkboxFormat, FORMAT_SOURCE_CODE, fRadioFormatAllLines);
		createDependency(checkboxFormat, FORMAT_SOURCE_CODE, fRadioFormatEditedLines);

		ControlFactory.createEmptySpace(composite, 1);

		Button checkboxTrailingWhitespace = addCheckBox(composite,
				PreferencesMessages.SaveActionsPreferencePage_removeTrailingWhitespace, REMOVE_TRAILING_WHITESPACE, 0);
		group = createRadioContainer(composite);
		fRadioTrailingWhitespaceAllLines = addRadioButton(group,
				PreferencesMessages.SaveActionsPreferencePage_inAllLines, null, 0);
		fRadioTrailingWhitespaceEditedLines = addRadioButton(group,
				PreferencesMessages.SaveActionsPreferencePage_inEditedLines,
				REMOVE_TRAILING_WHITESPACE_LIMIT_TO_EDITED_LINES, 0);
		createDependency(checkboxTrailingWhitespace, REMOVE_TRAILING_WHITESPACE, fRadioTrailingWhitespaceAllLines);
		createDependency(checkboxTrailingWhitespace, REMOVE_TRAILING_WHITESPACE, fRadioTrailingWhitespaceEditedLines);

		ControlFactory.createEmptySpace(composite, 1);

		addCheckBox(composite, PreferencesMessages.SaveActionsPreferencePage_ensureNewline, ENSURE_NEWLINE_AT_EOF, 0);

		ControlFactory.createEmptySpace(composite, 1);

		addCheckBox(composite, PreferencesMessages.SaveActionsPreferencePage_alignConst, ALIGN_ALL_CONST, 0);

		return composite;
	}

	private Composite createRadioContainer(Composite parent) {
		Composite composite = ControlFactory.createComposite(parent, 1);
		GridLayout layout = (GridLayout) composite.getLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		return composite;
	}

	@Override
	protected Control createContents(Composite parent) {
		fOverlayStore.load();
		fOverlayStore.start();

		createConfigurationBlock(parent);

		initializeFields();
		return parent;
	}

	@Override
	protected void initializeFields() {
		super.initializeFields();
		fRadioFormatAllLines.setSelection(!fRadioFormatEditedLines.getSelection());
		fRadioTrailingWhitespaceAllLines.setSelection(!fRadioTrailingWhitespaceEditedLines.getSelection());
	}
}
