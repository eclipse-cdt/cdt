package org.eclipse.cdt.ui.build.wizards;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.TabFolder;

/**
 * Wizard that creates a new C++ project that uses the managed make system
 */
public class ManagedCCWizard extends ManagedProjectWizard {
	
	private static final String WZ_TITLE = "MngCCWizard.title";
	private static final String WZ_DESC = "MngCCWizard.description";
	private static final String SETTINGS_TITLE= "MngCCWizardSettings.title"; //$NON-NLS-1$
	private static final String SETTINGS_DESC= "MngCCWizardSettings.description"; //$NON-NLS-1$

	public ManagedCCWizard() {
		this(CUIPlugin.getResourceString(WZ_TITLE), CUIPlugin.getResourceString(WZ_DESC));
	}

	public ManagedCCWizard(String title, String desc) {
		super(title, desc);
	}

	public void addTabItems(TabFolder folder) {
		super.addTabItems(folder);
		fTabFolderPage.setTitle(CUIPlugin.getResourceString(SETTINGS_TITLE));
		fTabFolderPage.setDescription(CUIPlugin.getResourceString(SETTINGS_DESC));
	}
	
	protected void doRun(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Creating Generated C++ Make Project", 4);
		super.doRun(monitor);
		// Add C++ and managed build natures
		if (newProject != null) {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			// Add C++ Nature to the newly created project.
			monitor.subTask("Adding C++ Nature");
			CCorePlugin.getDefault().convertProjectFromCtoCC(newProject, monitor);
			monitor.worked(1);
			
			// Add the managed build nature to the project
			monitor.subTask("Adding makefile generator");
			addManagedBuildNature(newProject, monitor);
			monitor.worked(1);
			
			monitor.done();
		}
	}
}
