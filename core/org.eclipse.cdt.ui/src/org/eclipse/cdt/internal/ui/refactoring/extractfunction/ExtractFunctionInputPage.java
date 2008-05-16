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
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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

	public void createControl(final Composite parent) {

		comp = new ExtractFunctionComposite(parent, info, this);
		
		setPageComplete(false);
		
		comp.getMethodNameText().addKeyListener(new KeyListener(){

			public void keyPressed(KeyEvent e) {}

			public void keyReleased(KeyEvent e) {
				info.setMethodName(comp.getMethodName());	
				checkName();
			}
			
		});
		
		
		for (Control buttons : comp.getVisibiltyGroup().getChildren()) {
			buttons.addMouseListener(new MouseListener() {
	
				public void mouseDoubleClick(MouseEvent e) {}
	
				public void mouseDown(MouseEvent e) {}
	
				public void mouseUp(MouseEvent e) {
					String text = ((Button)e.getSource()).getText();
					visibilityChange(text);
				}				
			});
		}
		
		/* Disable until it works again.
		 * 
		 * comp.getReplaceSimilarButton().addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				info.setReplaceDuplicates(comp.getReplaceSimilarButton().isEnabled());	
			}

			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);		
			}
			
		});*/
		
		setControl(comp);
		
	}

	protected void visibilityChange(String text) {
		info.setVisibility(VisibilityEnum.getEnumForStringRepresentation(text));
		
	}

	private void checkName() {

		String methodName = comp.getMethodName();
		IdentifierResult result = IdentifierHelper.checkIdentifierName(methodName);
		if(result.isCorrect()){
			setErrorMessage(null);
			setPageComplete(true);
		}
		else{
			setErrorMessage(Messages.ExtractFunctionInputPage_CheckMethodName + result.getMessage()); 
			setPageComplete(false);
		}
	}
	
	public void errorWithAfterUsedVariable(String variableUsedAfterBlock ) {
		if(variableUsedAfterBlock == null) {
			setErrorMessage(null);
			checkName();	
		}else {
			setErrorMessage("The parameter '" + variableUsedAfterBlock + "' " + Messages.ExtractFunctionInputPage_1);  //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
