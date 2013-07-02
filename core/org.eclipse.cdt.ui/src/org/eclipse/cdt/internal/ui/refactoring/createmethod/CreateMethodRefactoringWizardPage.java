/*******************************************************************************
 * Copyright (c) 2013 - Xdin AB
 * This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Erik Johansson
 ******************************************************************************/

package org.eclipse.cdt.internal.ui.refactoring.createmethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierResult;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

@SuppressWarnings("restriction")
public final class CreateMethodRefactoringWizardPage extends UserInputWizardPage {

	private static final String PAGE_NAME = "InputPage"; //$NON-NLS-1$
	private HashMap<Text, IASTParameterDeclaration> textToParameterMap;

	public CreateMethodRefactoringWizardPage() {
		super(PAGE_NAME);
		textToParameterMap = new HashMap<Text, IASTParameterDeclaration>();
	}
	
	public CreateMethodRefactoring getRefactoring() {
		RefactoringWizard wiz = getRefactoringWizard();
		if (wiz instanceof CreateMethodRefactoringWizard) {
			CreateMethodRefactoringWizard wizz = (CreateMethodRefactoringWizard) wiz;
			Refactoring ref = wizz.getRefactoring();
			if (ref instanceof CreateMethodRefactoring) {
				return (CreateMethodRefactoring) ref;
			}
		}
		return null;
	}

	@Override
	public void createControl(Composite parent) {
		
		IASTFunctionDefinition definition = getRefactoring().getFunctionDefinition();
		IASTStandardFunctionDeclarator declarator = (IASTStandardFunctionDeclarator) definition.getDeclarator();
		IASTParameterDeclaration[] parameters = declarator.getParameters();
		Composite control = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		control.setLayout(layout);
				
		Label label = new Label(control, SWT.NONE);
		label.setText(NLS.bind(Messages.CreateMethodRefactoringWizardPage_ParameterNamesLabel, declarator.getName().toString()));
		GridData labelLayout = new GridData();
		labelLayout.horizontalSpan = 2;
		labelLayout.verticalSpan = 2;
		label.setLayoutData(labelLayout);
		
		for (IASTParameterDeclaration param : parameters) {
			Label paramLabel = new Label(control, SWT.NONE);
			paramLabel.setText(Helpers.getParameterTypeName(param));
			Text paramText = new Text(control, SWT.BORDER | SWT.SINGLE);
			GridData textLayout = new GridData();
			textLayout.horizontalAlignment = GridData.FILL;
			textLayout.grabExcessHorizontalSpace = true;
			paramText.setLayoutData(textLayout);
			paramText.setText(param.getDeclarator().getName().toString());
			paramText.selectAll();
			paramText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					if (checkNames()) {
						Text text = (Text) e.getSource();
						updateParameterNameInAST(text, text.getText());
					}
				}
			});
			textToParameterMap.put(paramText, param);
		}
		
		super.setControl(control);
	}
	
	private void updateParameterNameInAST(Text text, String newName) {
		getRefactoring().updateParameterName(textToParameterMap.get(text), newName);
	}
	
	/**
	 * Basically copied from org.eclipse.cdt.internal.ui.refactoring.extractlocalvariable.InputPage
	 * 
	 */
	private boolean checkNames() {
		List<String> used = new ArrayList<String>();
		for (Text text : textToParameterMap.keySet()) {
			final String name = text.getText();
			IdentifierResult result = IdentifierHelper.checkIdentifierName(name);
			if (result.isCorrect()) {
				if (used.contains(name)) {
					setErrorMessage(NLS.bind(Messages.CreateMethodRefactoringWizardPage_ParameterNameUsedMultipleTimes, name));
					setPageComplete(false);
					return false;
				}
				else
					used.add(name);
			} else {
				setErrorMessage(result.getMessage());
				setPageComplete(false);
				return false;
			}	
		}
		setErrorMessage(null);
		setPageComplete(true);
		return true;
	}
}