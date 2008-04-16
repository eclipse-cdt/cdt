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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;

import org.eclipse.cdt.internal.ui.preferences.formatter.TranslationUnitPreview;
import org.eclipse.cdt.internal.ui.refactoring.dialogs.ValidatingLabeledTextField;
import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;

/**
 * InputPage used by the ImplementMethod refactoring if its necessary to enteraditional parameter names.
 * 
 * @author Mirko Stocker
 *
 */
public class ParameterNamesInputPage extends UserInputWizardPage {

	private final ImplementMethodRefactoring refactoring;	

	private TranslationUnitPreview translationUnitPreview; 

	public ParameterNamesInputPage(ImplementMethodRefactoring implementMethodRefactoring) {
		super(Messages.ParameterNamesInputPage_Title); 
		this.refactoring = implementMethodRefactoring;
	}

	public void createControl(Composite parent) {
		
		Composite superComposite = new Composite(parent, SWT.NONE);
		
	    superComposite.setLayout(new GridLayout());
		
		Label label = new Label(superComposite, SWT.NONE);
		label.setText(Messages.ParameterNamesInputPage_CompleteMissingMails); 
		label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
	
		ValidatingLabeledTextField validatingLabeledTextField = new ValidatingLabeledTextField(superComposite);
		validatingLabeledTextField.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		
		for (final IASTParameterDeclaration parameterDeclaration : refactoring.getParameters()) {

			String type = parameterDeclaration.getDeclSpecifier().getRawSignature();
			String content = String.valueOf(parameterDeclaration.getDeclarator().getName().toCharArray());
			boolean enabled = parameterDeclaration.getDeclarator().getName().toCharArray().length > 0;
			
			validatingLabeledTextField.addElement(type, content, enabled, new ValidatingLabeledTextField.Validator(){

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
					boolean isValid = NameHelper.isValidLocalVariableName(newName);
					
					if(isValid) {
						parameterDeclaration.getDeclarator().setName(new CPPASTName(newName.toCharArray()));
						translationUnitPreview.setPreviewText(refactoring.createFunctionDefinition().getRawSignature());
					}
					
					return isValid;
				}});
		}

		translationUnitPreview = new TranslationUnitPreview(new HashMap<String, String>(), superComposite);
		translationUnitPreview.getControl().setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		translationUnitPreview.setPreviewText(refactoring.createFunctionDefinition().getRawSignature());
		
		setControl(superComposite);
		
		setPageComplete(false);
	}
}
