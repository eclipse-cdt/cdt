/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.rse.examples.daytime.ui;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.examples.daytime.DaytimeResources;
import org.eclipse.rse.ui.wizards.AbstractSystemNewConnectionWizardPage;

public class DaytimeNewConnectionWizardPage extends
		AbstractSystemNewConnectionWizardPage {

	
	public DaytimeNewConnectionWizardPage(IWizard wizard, ISubSystemConfiguration parentConfig)
	{
		super(wizard, parentConfig);
	}
	
	public Control createContents(Composite parent) {
		Text field = new Text(parent, SWT.NONE);
		field.setText(DaytimeResources.DaytimeWizard_TestFieldText);
		
		// TODO Auto-generated method stub
		return field;
	}

}
