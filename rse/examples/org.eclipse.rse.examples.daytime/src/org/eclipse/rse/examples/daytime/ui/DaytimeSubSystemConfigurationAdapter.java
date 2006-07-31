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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;

import org.eclipse.rse.filters.ISystemFilter;
import org.eclipse.rse.filters.ISystemFilterPool;
import org.eclipse.rse.model.ISystemRegistry;
import org.eclipse.rse.ui.RSEUIPlugin;
import org.eclipse.rse.ui.actions.SystemPasteFromClipboardAction;
import org.eclipse.rse.ui.view.SubsystemConfigurationAdapter;
import org.eclipse.rse.ui.wizards.ISystemNewConnectionWizardPage;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Shell;


public class DaytimeSubSystemConfigurationAdapter extends SubsystemConfigurationAdapter
{
	

	
	Vector _additionalActions;
	
	// -----------------------------------
	// WIZARD PAGE CONTRIBUTION METHODS... (defects 43194 and 42780)
	// -----------------------------------
	/**
	 * Optionally return one or more wizard pages to append to the New Connection Wizard if
	 *  the user selects a system type that this subsystem factory supports.
	 * <p>
	 * Tip: consider extending AbstractSystemWizardPage for your wizard page class.
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