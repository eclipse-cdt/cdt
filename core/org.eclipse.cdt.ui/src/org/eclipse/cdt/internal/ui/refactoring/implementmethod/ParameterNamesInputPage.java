/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.implementmethod;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

import org.eclipse.cdt.ui.refactoring.CTextFileChange;

import org.eclipse.cdt.internal.ui.preferences.formatter.TranslationUnitPreview;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.dialogs.ValidatingLabeledTextField;
import org.eclipse.cdt.internal.ui.refactoring.utils.DelayedJobRunner;

/**
 * InputPage used by the ImplementMethod refactoring if its necessary to enter additional parameter names.
 * 
 * @author Mirko Stocker
 *
 */
public class ParameterNamesInputPage extends UserInputWizardPage {

	private MethodToImplementConfig config;
	private TranslationUnitPreview translationUnitPreview;
	private DelayedJobRunner delayedPreviewUpdater;
	private ImplementMethodRefactoringWizard wizard;

	public ParameterNamesInputPage(MethodToImplementConfig config, ImplementMethodRefactoringWizard wizard) {
		super(Messages.ParameterNamesInputPage_Title);
		this.config = config;
		this.wizard = wizard;
	}

	public void createControl(Composite parent) {
		
		Composite superComposite = new Composite(parent, SWT.NONE);
		
	    superComposite.setLayout(new GridLayout());
		
		Label label = new Label(superComposite, SWT.NONE);
		label.setText(Messages.ParameterNamesInputPage_CompleteMissingMails); 
		label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
	
		ValidatingLabeledTextField validatingLabeledTextField = new ValidatingLabeledTextField(superComposite);
		validatingLabeledTextField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		for (final ParameterInfo actParameterInfo : config.getParaHandler().getParameterInfos()) {

			String type = actParameterInfo.getTypeName();
			String content = actParameterInfo.getParameterName();
			boolean readOnly = !actParameterInfo.hasNewName();
			
			validatingLabeledTextField.addElement(type, content, readOnly, new ValidatingLabeledTextField.Validator(){

				@Override
				public void hasErrors() {
					setPageComplete(false);
				}

				@Override
				public void hasNoErrors() {
					setPageComplete(true);
				}

				@Override
				public boolean isValidInput(String newName) {
					actParameterInfo.setParameterName(newName);
					updatePreview();
					return true;
				}});
		}

		createPreview(superComposite);
		
		setControl(superComposite);
	}
	
	private InsertEdit getInsertEdit(CompositeChange compositeChange) {
		for(Change actChange : compositeChange.getChildren()) {
			if(actChange instanceof CompositeChange) {
				return getInsertEdit((CompositeChange) actChange);
			} else if (actChange instanceof CTextFileChange) {
				CTextFileChange textFileChange = (CTextFileChange) actChange;
				MultiTextEdit multiEdit = (MultiTextEdit) textFileChange.getEdit();
				if(multiEdit.getChildrenSize() == 0) {
					continue;
				}
				return (InsertEdit) multiEdit.getChildren()[0];
			}
		}
		return null;
	}	
	
	public String createFunctionDefinitionSignature() {
		try {
			ModificationCollector collector = new ModificationCollector();
			((ImplementMethodRefactoring)wizard.getRefactoring()).createDefinition(collector, config, new NullProgressMonitor());
			InsertEdit insertEdit = getInsertEdit(collector.createFinalChange());
			return insertEdit.getText().trim();
		} catch (OperationCanceledException e) {
			return Messages.PreviewGenerationNotPossible;
		} catch (CoreException e) {
			return Messages.PreviewGenerationNotPossible;
		}
	}

	private void createPreview(Composite superComposite) {
		translationUnitPreview = new TranslationUnitPreview(new HashMap<String, String>(), superComposite);
		translationUnitPreview.getControl().setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		Runnable runnable = new Runnable() {
			public void run() {
				setPreviewText(Messages.ImplementMethodRefactoringPage_GeneratingPreview);
				setPreviewText(createFunctionDefinitionSignature());
			}
			private void setPreviewText(final String text) {
				getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						translationUnitPreview.setPreviewText(text);
					}});
			}
		};
		delayedPreviewUpdater = new DelayedJobRunner(runnable, 500);
		delayedPreviewUpdater.start();
		superComposite.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				delayedPreviewUpdater.stop();
			}});
	}
	
	@Override
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}
	
	@Override
	public IWizardPage getNextPage() {
		MethodToImplementConfig nextConfig = ((ImplementMethodRefactoring)wizard.getRefactoring()).getRefactoringData().getNextConfigNeedingParameterNames(config);
		if(nextConfig != null) {
			return wizard.getPageForConfig(nextConfig);
		}else {
			return computeSuccessorPage();
		}
	}

	private void updatePreview() {
		if (translationUnitPreview == null) {
			return;
		}
		delayedPreviewUpdater.runJob();
	}	
	
}
