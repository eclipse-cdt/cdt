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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.internal.ui.refactoring.MethodContext;
import org.eclipse.cdt.internal.ui.refactoring.dialogs.NameAndVisibilityComposite;
import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

public class ExtractFunctionComposite extends Composite {
	private Button replaceSimilar;
	private ChooserComposite comp;
	private NameAndVisibilityComposite nameVisiComp;
	private final ExtractFunctionInformation info;
	
	public ExtractFunctionComposite(Composite parent, ExtractFunctionInformation info,
			ExtractFunctionInputPage page) {
		super(parent, SWT.NONE);
		this.info = info;
		setLayout(new GridLayout());

		createNewMethodNameComposite(this);

		Group returnGroup = createReturnGroup(nameVisiComp);
		createReturnValueChooser(returnGroup, info, page);		

		createReplaceCheckBox(nameVisiComp);
		
		if (info.getMethodContext().getType() == MethodContext.ContextType.METHOD) {
			visibilityPanelSetVisible(true);
		} else {
			visibilityPanelSetVisible(false);
		}
		layout();
	}

	private Group createReturnGroup(Composite parent) {
		Group returnGroup = new Group(parent,SWT.NONE);
	
		returnGroup.setText(Messages.ExtractFunctionComposite_ReturnValue); 
		returnGroup.setLayout(new GridLayout());
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		returnGroup.setLayoutData(gridData);
		return returnGroup;
	}

	private void createReturnValueChooser(Composite parent, ExtractFunctionInformation info,
			ExtractFunctionInputPage page) {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		comp = new ChooserComposite(parent, info, page);
		comp.setLayoutData(gridData);
		comp.redraw();
	}

	public Text getMethodNameText() {
		return nameVisiComp.getConstantNameText();
	}
	
	public Button getReplaceSimilarButton() {
		return replaceSimilar;
	}
	
	public void visibilityPanelSetVisible(boolean visible) {
		nameVisiComp.visibilityPanelsetVisible(visible);
	}

	private void createNewMethodNameComposite(Composite parent) {
		String label;
		if (info.getMethodContext().getType() == MethodContext.ContextType.METHOD) {
			label = Messages.ExtractFunctionComposite_MethodName;
		} else {
			label = Messages.ExtractFunctionComposite_FunctionName;
		}
		nameVisiComp = new NameAndVisibilityComposite(parent, label, VisibilityEnum.v_private, "");  //$NON-NLS-1$
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		nameVisiComp.setLayoutData(gridData);
		final Button virtual = new Button(nameVisiComp, SWT.CHECK);
		virtual.setText(Messages.ExtractFunctionComposite_Virtual);
		virtual.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				info.setVirtual(virtual.getSelection());
			}
		});
	}
	
	private void createReplaceCheckBox(Composite parent) {
		replaceSimilar = new Button(parent, SWT.CHECK | SWT.LEFT);
		GridData buttonLayoutData = new GridData(SWT.None);
		buttonLayoutData.verticalIndent = 5;
		replaceSimilar.setLayoutData(buttonLayoutData);
		replaceSimilar.setText(Messages.ExtractFunctionComposite_ReplaceDuplicates);
	}
	
	public ChooserComposite getReturnChooser() {
		return comp;
	}
	
	public String getMethodName(){
		return nameVisiComp.getConstantNameText().getText();
	}

	public Composite getVisibiltyGroup() {
		return nameVisiComp.getVisibiltyGroup();
	}
}
