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
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import java.util.ArrayList;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.GetterAndSetterContext.FieldWrapper;

public class GenerateGettersAndSettersInputPage extends UserInputWizardPage {

	private GetterAndSetterContext context;

	public GenerateGettersAndSettersInputPage(GetterAndSetterContext context) {
		super(Messages.GettersAndSetters_Name); 
		this.context = context;
	}

	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE );
		comp.setLayout(new FillLayout());
		createTree(comp);

		setControl(comp);
	}

	private void createTree(Composite comp) {
		final ContainerCheckedTreeViewer variableSelectionView = new ContainerCheckedTreeViewer(comp, SWT.BORDER);
		for(IASTSimpleDeclaration currentField : context.existingFields){
			if(currentField.getDeclarators().length == 0){
				continue;
			}
			
			variableSelectionView.setContentProvider(context);
			variableSelectionView.setAutoExpandLevel(3);
			variableSelectionView.setInput(""); //$NON-NLS-1$
			if(context.selectedName != null) {
				String rawSignature = context.selectedName.getRawSignature();
				for(Object obj : variableSelectionView.getVisibleExpandedElements()) {
					if (obj instanceof FieldWrapper){

						if(obj.toString().contains(rawSignature)){
							variableSelectionView.setSubtreeChecked(obj, true);
						}					
					}
				}
			}
			ArrayList<GetterSetterInsertEditProvider> checkedFunctions = new ArrayList<GetterSetterInsertEditProvider>();
			for(Object currentElement : variableSelectionView.getCheckedElements()){
				if (currentElement instanceof GetterSetterInsertEditProvider) {
					GetterSetterInsertEditProvider editProvider = (GetterSetterInsertEditProvider) currentElement;
					checkedFunctions.add(editProvider);
				}
			}
			context.selectedFunctions = checkedFunctions;
						
			variableSelectionView.addCheckStateListener(new ICheckStateListener(){

				public void checkStateChanged(CheckStateChangedEvent event) {
					ArrayList<GetterSetterInsertEditProvider> checkedFunctions = new ArrayList<GetterSetterInsertEditProvider>();
					for(Object currentElement : variableSelectionView.getCheckedElements()){
						if (currentElement instanceof GetterSetterInsertEditProvider) {
							GetterSetterInsertEditProvider editProvider = (GetterSetterInsertEditProvider) currentElement;
							checkedFunctions.add(editProvider);
						}
					}
					context.selectedFunctions = checkedFunctions;
				}
			});
		}
	}
}
