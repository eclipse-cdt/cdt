package org.eclipse.cdt.managedbuilder.ui.wizards;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;

public class NewManagedCProjectWizard extends NewManagedProjectWizard {
	// String constants
	private static final String WZ_TITLE = "MngCWizard.title";
	private static final String WZ_DESC = "MngCWizard.description";
	private static final String SETTINGS_TITLE= "MngCWizardSettings.title"; //$NON-NLS-1$
	private static final String SETTINGS_DESC= "MngCWizardSettings.description"; //$NON-NLS-1$

	public NewManagedCProjectWizard() {
		this(ManagedBuilderUIPlugin.getResourceString(WZ_TITLE), ManagedBuilderUIPlugin.getResourceString(WZ_DESC));
	}

	public NewManagedCProjectWizard(String title, String description) {
		super(title, description);
	}

	public void addPages() {
		// Add the default page for all new managed projects 
		super.addPages();
	}

}
