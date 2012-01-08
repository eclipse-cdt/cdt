/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *    Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.dialogs;


import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.cdt.internal.ui.refactoring.NameNVisibilityInformation;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierResult;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * Holds a NameAndVisibilityComposite and deals with the extract refactoring
 * specific implementation and propagates the inputs made in the wizard ui back
 * to the refactoring via the NameNVisibilityInformation object.
 *	
 * @author Emanuel Graf
 */
public abstract class ExtractInputPage extends UserInputWizardPage {

	protected NameAndVisibilityComposite control;
	protected NameNVisibilityInformation info;
	protected String label = Messages.ExtractInputPage_ReplaceInSubclass; 
	protected String errorLabel = Messages.ExtractInputPage_EnterName; 

	public ExtractInputPage(String name, NameNVisibilityInformation info) {
		super(name);
		this.info = info;
	}

	@Override
	public void createControl(Composite parent) {
		control = new NameAndVisibilityComposite(parent, label, info.getName());
		setTitle(getName());
		setPageComplete(false);
		control.getConstantNameText().addModifyListener(new ModifyListener() {
	
			@Override
			public void modifyText(ModifyEvent e) {
				info.setName(control.getConstantNameText().getText());
				checkName();
			}
			
		});
		
		for (Control buttons : control.getVisibiltyGroup().getChildren()) {
			buttons.addMouseListener(new MouseAdapter() {
	
				@Override
				public void mouseUp(MouseEvent e) {
					String text = ((Button)e.getSource()).getText();
					visibilityChange(text);
				}
				
			});
		}

		checkName();
		setControl(control);
	}

	protected void checkName() {
		String methodName = control.getConstantNameText().getText();
		IdentifierResult result = IdentifierHelper.checkIdentifierName(methodName);
		if(result.isCorrect()){
			setErrorMessage(null);
			setPageComplete(true);
			verifyName(methodName);
		}
		else{
			setErrorMessage(NLS.bind(Messages.ExtractInputPage_CheckName, result.getMessage())); 
			setPageComplete(false);
		}
	}
	
	abstract protected void verifyName(String name);

	protected void visibilityChange(String visibilityText) {
		info.setVisibility(VisibilityEnum.getEnumForStringRepresentation(visibilityText));
	} 

}
