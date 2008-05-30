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

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.cdt.internal.ui.preferences.formatter.TranslationUnitPreview;
import org.eclipse.cdt.internal.ui.refactoring.dialogs.ValidatingLabeledTextField;
import org.eclipse.cdt.internal.ui.refactoring.utils.DelayedJobRunner;

/**
 * InputPage used by the ImplementMethod refactoring if its necessary to enter additional parameter names.
 * 
 * @author Mirko Stocker
 *
 */
public class ParameterNamesInputPage extends UserInputWizardPage {

	private final ParameterHandler parameterHandler;	
	private TranslationUnitPreview translationUnitPreview;
	private DelayedJobRunner delayedPreviewUpdater;
	
	public ParameterNamesInputPage(ParameterHandler parameterHandler) {
		super(Messages.ParameterNamesInputPage_Title); 
		this.parameterHandler = parameterHandler;
	}

	public void createControl(Composite parent) {
		
		Composite superComposite = new Composite(parent, SWT.NONE);
		
	    superComposite.setLayout(new GridLayout());
		
		Label label = new Label(superComposite, SWT.NONE);
		label.setText(Messages.ParameterNamesInputPage_CompleteMissingMails); 
		label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
	
		ValidatingLabeledTextField validatingLabeledTextField = new ValidatingLabeledTextField(superComposite);
		validatingLabeledTextField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		for (final ParameterInfo actParameterInfo : parameterHandler.getParameterInfos()) {

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

	private void createPreview(Composite superComposite) {
		translationUnitPreview = new TranslationUnitPreview(new HashMap<String, String>(), superComposite);
		translationUnitPreview.getControl().setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		Runnable runnable = new Runnable() {
			public void run() {
				setPreviewText(Messages.ImplementMethodRefactoringPage_GeneratingPreview);
				setPreviewText(parameterHandler.createFunctionDefinitionSignature());
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
	
	private void updatePreview() {
		if (translationUnitPreview == null) {
			return;
		}
		delayedPreviewUpdater.runJob();
	}
}
