/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui.wizards;


import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 */
public abstract class NewMakeProjectWizard extends NewCProjectWizard {
	
	protected MakeProjectWizardOptionPage fOptionPage;
	
	public NewMakeProjectWizard(String title, String desc) {
		super(title, desc);
	}

	protected void doRunPrologue(IProgressMonitor monitor) {
	}

	protected void doRunEpilogue(IProgressMonitor monitor) {
	}

	protected void doRun(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(MakeUIPlugin.getResourceString("MakeCWizard.task_name"), 10); //$NON-NLS-1$

        // super.doRun() just creates the project and does not assign a builder to it.
		super.doRun(new SubProgressMonitor(monitor, 5));

		MakeProjectNature.addNature(getProjectHandle(), new SubProgressMonitor(monitor, 1));
		ScannerConfigNature.addScannerConfigNature(getProjectHandle());
		        
        // Modify the project based on what the user has selected
		if (newProject != null) {
			fOptionPage.performApply(new SubProgressMonitor(monitor, 4));
			monitor.done();
		}
	}
	
	public String getProjectID() {
		return MakeCorePlugin.MAKE_PROJECT_ID;
	}
	
	public void createPageControls(Composite pageContainer) {
		super.createPageControls( pageContainer );
		
		IWizardPage [] pages = getPages();
		
		if( pages != null && pages.length == 2 ){
			MakeUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(pages[0].getControl(), IMakeHelpContextIds.MAKE_PROJ_WIZ_NAME_PAGE);
	
			MakeProjectWizardOptionPage optionPage = (MakeProjectWizardOptionPage) pages[1];
			optionPage.setupHelpContextIds();
		}
	}
}
