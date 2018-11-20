/*******************************************************************************
 * Copyright (c) 2018 Kichwa Coders and others.
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
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.core.refactoring.resource.RenameResourceProcessor;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class CResourceRenameRefactoringInputPage extends UserInputWizardPage {

	/**
	 * Dialog settings key for default setting of update references checkbox.
	 */
	public static final String KEY_UPDATE_REFERENCES = "updateReferences"; //$NON-NLS-1$

	private static final String DIALOG_SETTINGS_KEY = "CResourceRenameRefactoringInputPage"; //$NON-NLS-1$
	private IDialogSettings fDialogSettings;

	private Text fNameField;
	private RenameResourceProcessor fRefactoringProcessor;
	private Button updateReferences;

	public CResourceRenameRefactoringInputPage(RenameResourceProcessor processor) {
		super("CResourceRenameRefactoringInputPage"); //$NON-NLS-1$
		fRefactoringProcessor = processor;
		IDialogSettings ds = CUIPlugin.getDefault().getDialogSettings();
		fDialogSettings = ds.getSection(DIALOG_SETTINGS_KEY);
		if (fDialogSettings == null) {
			fDialogSettings = ds.addNewSection(DIALOG_SETTINGS_KEY);
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setFont(parent.getFont());

		Label label = new Label(composite, SWT.NONE);
		label.setText(RenameMessages.CResourceRenameRefactoringInputPage_new_name);
		label.setLayoutData(new GridData());

		fNameField = new Text(composite, SWT.BORDER);
		String resourceName = fRefactoringProcessor.getNewResourceName();
		fNameField.setText(resourceName);
		fNameField.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
		fNameField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});

		int lastIndexOfDot = resourceName.lastIndexOf('.');
		if ((fRefactoringProcessor.getResource().getType() == IResource.FILE) && (lastIndexOfDot > 0)) {
			fNameField.setSelection(0, lastIndexOfDot);
		} else {
			fNameField.selectAll();
		}

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

		Dialog.applyDialogFont(composite);

		setPageComplete(false);
		setControl(composite);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			fNameField.setFocus();
		}
		super.setVisible(visible);
	}

	private void validatePage() {
		String text = fNameField.getText();
		RefactoringStatus status = fRefactoringProcessor.validateNewElementName(text);
		setPageComplete(status);
	}

	@Override
	protected boolean performFinish() {
		saveRefactoringSettings();
		saveDialogSettings();
		return super.performFinish();
	}

	@Override
	public IWizardPage getNextPage() {
		saveRefactoringSettings();
		saveDialogSettings();
		return super.getNextPage();
	}

	private void saveDialogSettings() {
		fDialogSettings.put(KEY_UPDATE_REFERENCES, updateReferences.getSelection());
	}

	private void saveRefactoringSettings() {
		fRefactoringProcessor.setNewResourceName(fNameField.getText());
		fRefactoringProcessor.setUpdateReferences(updateReferences.getSelection());
	}

}
