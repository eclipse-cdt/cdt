package org.eclipse.cdt.managedbuilder.ui.wizards;

/**********************************************************************
 * Copyright (c) 2002, 2005 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class NewManagedCProjectWizard extends NewManagedProjectWizard {
	// String constants
	private static final String WZ_TITLE = "MngCWizard.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "MngCWizard.description"; //$NON-NLS-1$
	private static final String SETTINGS_TITLE= "MngCWizardSettings.title"; //$NON-NLS-1$
	private static final String SETTINGS_DESC= "MngCWizardSettings.description"; //$NON-NLS-1$

	public NewManagedCProjectWizard() {
		this(ManagedBuilderUIMessages.getResourceString(WZ_TITLE), ManagedBuilderUIMessages.getResourceString(WZ_DESC));
	}

	public NewManagedCProjectWizard(String title, String description) {
		super(title, description);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		// Add the default page for all new managed projects 
		super.addPages();
		
		// support for custom wizard pages
		// publish our nature with the page manager
		MBSCustomPageManager.addPageProperty(CProjectPlatformPage.PAGE_ID, CProjectPlatformPage.NATURE, CProjectNature.C_NATURE_ID);
		
		// load all pages specified via extensions
		try
		{
			MBSCustomPageManager.loadExtensions();
		}
		catch (BuildException e)
		{
			e.printStackTrace();
		}
		
		
		IWizardPage[] customPages = MBSCustomPageManager.getCustomPages();
		
		if (customPages != null)
		{
			for (int k = 0; k < customPages.length; k++)
			{
				addPage(customPages[k]);
			}
		}
	}

}
