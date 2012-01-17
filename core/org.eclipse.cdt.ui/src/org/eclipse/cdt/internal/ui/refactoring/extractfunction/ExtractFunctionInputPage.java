/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierResult;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

public class ExtractFunctionInputPage extends UserInputWizardPage {
	private final ExtractFunctionInformation info;
	private ExtractFunctionComposite comp;
	protected final String NO_NAME_ERROR_LABEL = Messages.ExtractFunctionInputPage_EnterName; 

	public ExtractFunctionInputPage(String name, ExtractFunctionInformation info) {
		super(name);
		this.info = info;
	}

	@Override
	public void createControl(final Composite parent) {
		comp = new ExtractFunctionComposite(parent, info, this);
		
		setPageComplete(false);
		
		comp.getMethodNameText().addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				info.setMethodName(comp.getMethodName());	
				checkName();
			}
		});
		
		for (Control buttons : comp.getVisibiltyGroup().getChildren()) {
			buttons.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent e) {
					String text = ((Button)e.getSource()).getText();
					visibilityChange(text);
				}				
			});
		}
		
		comp.getReplaceSimilarButton().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				info.setReplaceDuplicates(comp.getReplaceSimilarButton().isEnabled());	
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);		
			}
		});
		
		setControl(comp);
	}

	protected void visibilityChange(String text) {
		info.setVisibility(VisibilityEnum.getEnumForStringRepresentation(text));
	}

	private void checkName() {
		String methodName = comp.getMethodName();
		IdentifierResult result = IdentifierHelper.checkIdentifierName(methodName);
		if (result.isCorrect()) {
			setErrorMessage(null);
			setPageComplete(true);
		} else {
			setErrorMessage(Messages.ExtractFunctionInputPage_CheckFunctionName + " " + result.getMessage());  //$NON-NLS-1$
			setPageComplete(false);
		}
	}
	
	public void errorWithAfterUsedVariable(String variableUsedAfterBlock ) {
		if (variableUsedAfterBlock == null) {
			setErrorMessage(null);
			checkName();	
		} else {
			setErrorMessage("The parameter '" + variableUsedAfterBlock + "' " + Messages.ExtractFunctionInputPage_1);  //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
