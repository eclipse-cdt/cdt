/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.xlc.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.managedbuilder.xlc.ui.preferences.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;

/**
 * @author crecoskie
 *
 */
public class XLCSettingsWizardRunnable implements IRunnableWithProgress {

	/**
	 * 
	 */
	public XLCSettingsWizardRunnable() {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		// take the data from the page manager, and set the project properties with it
		String compilerPath = MBSCustomPageManager.getPageProperty(XLCSettingsWizardPage.PAGE_ID, PreferenceConstants.P_XL_COMPILER_ROOT).toString();
		String compilerVersion = MBSCustomPageManager.getPageProperty(XLCSettingsWizardPage.PAGE_ID, PreferenceConstants.P_XLC_COMPILER_VERSION).toString();

		// get a handle to the wizard
		IWizardPage[] pages = MBSCustomPageManager.getPages();
		
		if(pages != null && pages.length > 0) {
		
			CDTCommonProjectWizard wizard = (CDTCommonProjectWizard) pages[0].getWizard();
		
			IProject project = wizard.getLastProject();
			
			try {
				project.setPersistentProperty(new QualifiedName("",
						PreferenceConstants.P_XL_COMPILER_ROOT), compilerPath);
				
				project.setPersistentProperty(new QualifiedName("",
						PreferenceConstants.P_XLC_COMPILER_VERSION), compilerVersion);
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}
		
	}

}
