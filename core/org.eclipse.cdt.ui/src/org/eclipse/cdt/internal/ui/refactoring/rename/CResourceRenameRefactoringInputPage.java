/*******************************************************************************
 * Copyright (c) 2018, 2020 Kichwa Coders Ltd and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.rename;

import org.eclipse.cdt.internal.ui.preferences.OrganizeIncludesPreferencePage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ltk.core.refactoring.participants.IRenameResourceProcessor;
import org.eclipse.ltk.ui.refactoring.resource.RenameResourceWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class CResourceRenameRefactoringInputPage
		extends RenameResourceWizard.RenameResourceRefactoringConfigurationPage {

	/**
	 * Dialog settings key for default setting of update references checkbox.
	 */
	public static final String KEY_UPDATE_REFERENCES = "updateReferences"; //$NON-NLS-1$

	private static final String DIALOG_SETTINGS_KEY = "CResourceRenameRefactoringInputPage"; //$NON-NLS-1$
	private IDialogSettings fDialogSettings;

	private Button updateReferences;

	public CResourceRenameRefactoringInputPage(IRenameResourceProcessor renameResourceProcessor) {
		super(renameResourceProcessor);
		IDialogSettings ds = CUIPlugin.getDefault().getDialogSettings();
		fDialogSettings = ds.getSection(DIALOG_SETTINGS_KEY);
		if (fDialogSettings == null) {
			fDialogSettings = ds.addNewSection(DIALOG_SETTINGS_KEY);
		}
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Control control = getControl();
		Assert.isTrue(control instanceof Composite,
				"super class has changed from using composite to something else for the main control."); //$NON-NLS-1$
		Composite composite = (Composite) control;

		updateReferences = new Button(composite, SWT.CHECK);
		updateReferences.setText(RenameMessages.CResourceRenameRefactoringInputPage_update_references);
		String value = fDialogSettings.get(KEY_UPDATE_REFERENCES);
		boolean updateRefs = value != null ? Boolean.parseBoolean(value) : true;

		updateReferences.setSelection(updateRefs);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		updateReferences.setLayoutData(gridData);

		// link to open preference page
		Link link = new Link(composite, SWT.NONE);
		link.setText("<a>" + RenameMessages.CResourceRenameRefactoringInputPage_open_preferences + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
		link.setToolTipText(RenameMessages.CResourceRenameRefactoringInputPage_open_preferences_tooltip);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		link.setLayoutData(gridData);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getShell(),
						OrganizeIncludesPreferencePage.PREF_ID, null, null);
				dialog.open();
			}
		});
	}

	@Override
	protected void storeSettings() {
		super.storeSettings();
		fDialogSettings.put(KEY_UPDATE_REFERENCES, updateReferences.getSelection());
	}

	@Override
	protected void initializeRefactoring() {
		super.initializeRefactoring();
		getProcessor().setUpdateReferences(updateReferences.getSelection());
	}
}
