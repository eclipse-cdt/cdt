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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;

/**
 * Three radio buttons in a group, labeled according to the corresponding visibility name
 * (public, private, protected).
 * 
 * @author Thomas Corbat
 */
public class VisibilitySelectionPanel extends Composite {
	private Button publicAccessRadioButton;
	private Button protectedAccessRadioButton;
	private Button privateAccessRadioButton;
	private Group accessModifierGroup;

	public VisibilitySelectionPanel(Composite parent, VisibilityEnum defaultVisibility, int style) {
		super(parent, style);
		
		FillLayout compositeLayout = new FillLayout(SWT.HORIZONTAL);
		setLayout(compositeLayout);
		GridData gridData = new GridData(SWT.FILL,SWT.BEGINNING, true, false);
		gridData.horizontalAlignment = GridData.FILL;
		setLayoutData(gridData);
		
		createAccessModifierComposite(this);
		setSelected(defaultVisibility);
	}

	private void createAccessModifierComposite(Composite control) {
		accessModifierGroup = new Group(this, SWT.SHADOW_NONE);
		RowLayout groupLayout = new RowLayout(SWT.HORIZONTAL);
		groupLayout.fill = true;
		accessModifierGroup.setLayout(groupLayout);
		accessModifierGroup.setText(Messages.VisibilitySelectionPanel_AccessModifier); 
		
		publicAccessRadioButton = new Button(accessModifierGroup, SWT.RADIO | SWT.LEFT);
		publicAccessRadioButton.setText(VisibilityEnum.v_public.toString());
		
		protectedAccessRadioButton = new Button(accessModifierGroup, SWT.RADIO | SWT.LEFT);
		protectedAccessRadioButton.setText(VisibilityEnum.v_protected.toString());
		
		privateAccessRadioButton = new Button(accessModifierGroup, SWT.RADIO | SWT.LEFT);
		privateAccessRadioButton.setText(VisibilityEnum.v_private.toString());
	}

	private void setSelected(VisibilityEnum defaultVisibility) {
		switch (defaultVisibility) {
		case v_public:
			publicAccessRadioButton.setSelection(true);
			break;
		case v_protected:
			protectedAccessRadioButton.setSelection(true);
			break;
		case v_private:
			privateAccessRadioButton.setSelection(true);
			break;
		}
	}

	public Group getGroup() {
		return accessModifierGroup;
	}	
	
	@Override
	public void setEnabled(boolean enabled) {
		accessModifierGroup.setEnabled(enabled);
		publicAccessRadioButton.setEnabled(enabled);
		protectedAccessRadioButton.setEnabled(enabled);
		privateAccessRadioButton.setEnabled(enabled);
		super.setEnabled(enabled);
	}
}
