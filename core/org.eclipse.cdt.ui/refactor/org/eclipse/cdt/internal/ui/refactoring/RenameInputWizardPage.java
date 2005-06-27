/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import org.eclipse.cdt.internal.corext.refactoring.IReferenceUpdating;
import org.eclipse.cdt.internal.corext.refactoring.ITextUpdating;
import org.eclipse.cdt.internal.corext.refactoring.base.Refactoring;
import org.eclipse.cdt.internal.ui.util.RowLayouter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.help.WorkbenchHelp;

abstract class RenameInputWizardPage extends TextInputWizardPage {

	private String fHelpContextID;
	private Button fUpdateReferences;
	private Button fUpdateComments;
	private Button fUpdateStrings;
	private Button fUpdateQualifiedNames;
	private static final String UPDATE_REFERENCES= "updateReferences"; //$NON-NLS-1$
	private static final String UPDATE_COMMENTS= "updateComments"; //$NON-NLS-1$
	private static final String UPDATE_STRINGS= "updateStrings"; //$NON-NLS-1$
	private static final String UPDATE_QUALIFIED_NAMES= "updateQualifiedNames"; //$NON-NLS-1$
	
	/**
	 * Creates a new text input page.
	 * @param isLastUserPage <code>true</code> if this page is the wizard's last
	 *  user input page. Otherwise <code>false</code>.
	 * @param initialSetting the initialSetting.
	 */
	public RenameInputWizardPage(String description, String contextHelpId, boolean isLastUserPage, String initialValue) {
		super(description, isLastUserPage, initialValue);
		fHelpContextID= contextHelpId;
	}
	
	/* non java-doc
	 * @see DialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite superComposite= new Composite(parent, SWT.NONE);
		setControl(superComposite);
		initializeDialogUnits(superComposite);
		
		superComposite.setLayout(new GridLayout());
		Composite composite= new Composite(superComposite, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));	
		
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.verticalSpacing= 8;
		composite.setLayout(layout);
		RowLayouter layouter= new RowLayouter(2);

		Label oldNameLabel= new Label(composite, SWT.NONE);
		oldNameLabel.setText(getOldNameLabelText());

		Label oldName= new Label(composite, SWT.NONE);
		oldName.setText(fInitialValue);

		Label label= new Label(composite, SWT.NONE);
		label.setText(getLabelText());
		
		Text text= createTextInputField(composite);
		text.selectAll();
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint= convertWidthInCharsToPixels(25);
		text.setLayoutData(gd);

				
		layouter.perform(label, text, 1);
		
		addOptionalUpdateReferencesCheckbox(composite, layouter);
		
		Dialog.applyDialogFont(superComposite);
		WorkbenchHelp.setHelp(getControl(), fHelpContextID);

	}
	
	protected boolean saveSettings() {
		if (getContainer() instanceof Dialog)
			return ((Dialog)getContainer()).getReturnCode() == IDialogConstants.OK_ID;
		return true;
	}
	
	public void dispose() {
		if (saveSettings()) {
			saveBooleanSetting(UPDATE_REFERENCES, fUpdateReferences);
			saveBooleanSetting(UPDATE_COMMENTS, fUpdateComments);
			saveBooleanSetting(UPDATE_STRINGS, fUpdateStrings);
			saveBooleanSetting(UPDATE_QUALIFIED_NAMES, fUpdateQualifiedNames);
		}
		super.dispose();
	}
	
	private void addOptionalUpdateReferencesCheckbox(Composite result, RowLayouter layouter) {
		final IReferenceUpdating ref= (IReferenceUpdating)getRefactoring().getAdapter(IReferenceUpdating.class);
		if (ref == null || !ref.canEnableUpdateReferences())	
			return;
		String title= RefactoringMessages.getString("RenameInputWizardPage.update_references"); //$NON-NLS-1$
		boolean defaultValue= getBooleanSetting(UPDATE_REFERENCES, ref.getUpdateReferences());
		fUpdateReferences= createCheckbox(result, title, defaultValue, layouter);
		ref.setUpdateReferences(fUpdateReferences.getSelection());
		fUpdateReferences.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				ref.setUpdateReferences(fUpdateReferences.getSelection());
			}
		});		
	}
		
	private void addOptionalUpdateCommentsAndStringCheckboxes(Composite result, RowLayouter layouter) {
		ITextUpdating refactoring= (ITextUpdating)getRefactoring().getAdapter(ITextUpdating.class);
		
		if (refactoring == null || !refactoring.canEnableTextUpdating())
			return;
		
		addUpdateCommentsCheckbox(result, layouter, refactoring);
		addUpdateStringsCheckbox(result, layouter, refactoring);
	}
	
	private void  addUpdateCommentsCheckbox(Composite result, RowLayouter layouter, final ITextUpdating refactoring) {
		String title= RefactoringMessages.getString("RenameInputWizardPage.update_comment_references"); //$NON-NLS-1$
		boolean defaultValue= getBooleanSetting(UPDATE_COMMENTS, refactoring.getUpdateComments());
		fUpdateComments= createCheckbox(result, title, defaultValue, layouter);
		refactoring.setUpdateComments(fUpdateComments.getSelection());
		fUpdateComments.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				refactoring.setUpdateComments(fUpdateComments.getSelection());
				updateForcePreview();
			}
		});		
	}

	private void addUpdateStringsCheckbox(Composite result, RowLayouter layouter, final ITextUpdating refactoring) {
		String title= RefactoringMessages.getString("RenameInputWizardPage.ppdate_string_references"); //$NON-NLS-1$
		boolean defaultValue= getBooleanSetting(UPDATE_STRINGS, refactoring.getUpdateStrings());
		fUpdateStrings= createCheckbox(result, title, defaultValue, layouter);
		refactoring.setUpdateStrings(fUpdateStrings.getSelection());
		fUpdateStrings.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				refactoring.setUpdateStrings(fUpdateStrings.getSelection());
				updateForcePreview();
			}
		});		
	}
	
	protected String getLabelText() {
		return RefactoringMessages.getString("RenameInputWizardPage.enter_name"); //$NON-NLS-1$
	}

	protected String getOldNameLabelText() {
		return RefactoringMessages.getString("RenameInputWizardPage.old_name"); //$NON-NLS-1$
	}

	protected boolean getBooleanSetting(String key, boolean defaultValue) {
		String update= getRefactoringSettings().get(key);
		if (update != null)
			return Boolean.valueOf(update).booleanValue();
		else
			return defaultValue;
	}
	
	protected void saveBooleanSetting(String key, Button checkBox) {
		if (checkBox != null)
			getRefactoringSettings().put(key, checkBox.getSelection());
	}

	private static Button createCheckbox(Composite parent, String title, boolean value, RowLayouter layouter) {
		Button checkBox= new Button(parent, SWT.CHECK);
		checkBox.setText(title);
		checkBox.setSelection(value);
		layouter.perform(checkBox);
		return checkBox;		
	}
	
	private void updateForcePreview() {
		boolean forcePreview= false;
		Refactoring refactoring= getRefactoring();
		ITextUpdating tu= (ITextUpdating)refactoring.getAdapter(ITextUpdating.class);
		if (tu != null) {
			forcePreview= tu.getUpdateComments() || tu.getUpdateJavaDoc() || tu.getUpdateStrings();
		}
		getRefactoringWizard().setPreviewReview(forcePreview);
	}
}
