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

import java.util.Vector;

import org.eclipse.jface.wizard.IWizard;

import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.ui.view.SubSystemConfigurationAdapter;
import org.eclipse.rse.ui.wizards.ISystemNewConnectionWizardPage;


public class DaytimeSubSystemConfigurationAdapter extends SubSystemConfigurationAdapter
{
	Vector _additionalActions;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.ui.view.SubSystemConfigurationAdapter#getNewConnectionWizardPages(org.eclipse.rse.core.subsystems.ISubSystemConfiguration, org.eclipse.jface.wizard.IWizard)
	 */
	public ISystemNewConnectionWizardPage[] getNewConnectionWizardPages(ISubSystemConfiguration factory, IWizard wizard)
	{
		ISystemNewConnectionWizardPage[] basepages = super.getNewConnectionWizardPages(factory, wizard);

		if (true)
		{
		  DaytimeNewConnectionWizardPage page = new DaytimeNewConnectionWizardPage(wizard, factory);		  
		  ISystemNewConnectionWizardPage[] newPages = new ISystemNewConnectionWizardPage[basepages.length + 1];
		  newPages[0] = page;
		  for (int i = 0; i < basepages.length; i++)
		  {
		  	newPages[i+1] = basepages[i];
		  }
		  basepages = newPages;
		}
		return basepages;
	}
	
 
}