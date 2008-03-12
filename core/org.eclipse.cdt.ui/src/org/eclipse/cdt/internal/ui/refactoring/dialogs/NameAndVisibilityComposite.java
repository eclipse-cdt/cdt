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
package org.eclipse.cdt.internal.ui.refactoring.dialogs;

/**
 * @author Thomas Corbat
 *
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

public class NameAndVisibilityComposite extends Composite {
	
	private LabeledTextField constantName;
	private final String labelName;
	private final VisibilitySelectionPanel visibilityPanel;
	
	public NameAndVisibilityComposite(Composite parent, String labelName) {
		this(parent, labelName, VisibilityEnum.v_public);
	}
	
	public NameAndVisibilityComposite(Composite parent, String labelName, VisibilityEnum defaultVisibility){

		super(parent, SWT.NONE);
		
		this.labelName = labelName;
		
		setLayout(new GridLayout());
		
		createNewMethodNameComposite(this);
		visibilityPanel = new VisibilitySelectionPanel(this, defaultVisibility,SWT.NONE);
	}
	

	public Text getConstantNameText() {
		return constantName.getText();
	}
	
	public Group getVisibiltyGroup() {
		return visibilityPanel.getGroup();
	}
	
	public void visibilityPanelsetVisible(boolean visible) {
		visibilityPanel.setVisible(visible);
	}


	private void createNewMethodNameComposite(Composite control) {
		Composite methodNameComposite = new Composite(control, SWT.NONE);
		FillLayout compositeLayout = new FillLayout(SWT.HORIZONTAL);
		GridData gridData = new GridData(SWT.FILL,SWT.BEGINNING, true, false);
		gridData.horizontalAlignment = GridData.FILL;
		methodNameComposite.setLayoutData(gridData);
		methodNameComposite.setLayout(compositeLayout);
		constantName = new LabeledTextField(methodNameComposite, labelName);
	}
}
