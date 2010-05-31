/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.xlc.ui.properties;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IFolderInfo;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.xlc.ui.XLCUIPlugin;
import org.eclipse.cdt.managedbuilder.xlc.ui.preferences.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author crecoskie
 *
 */
public class XLCv8ApplicabiltyCalculator implements IOptionApplicability {

	private boolean isVersion8(IBuildObject configuration) {
		// first we check the preference for this project, if it exists
		if(configuration instanceof IConfiguration) {
			IConfiguration config = (IConfiguration) configuration;
			IManagedProject managedProject = config.getManagedProject();
			
			IProject project = (IProject) managedProject.getOwner();
			
			String currentVersion = null;
			try {
				currentVersion = project.getPersistentProperty(new QualifiedName("",
						PreferenceConstants.P_XLC_COMPILER_VERSION));
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(currentVersion == null) {
				// if the property isn't set, then use the workbench preference
				IPreferenceStore prefStore = XLCUIPlugin.getDefault().getPreferenceStore();
				currentVersion = prefStore.getString(PreferenceConstants.P_XLC_COMPILER_VERSION);
			}
			
		  if(currentVersion.equals(PreferenceConstants.P_XL_COMPILER_VERSION_8))
			  return true;
		}
		
		if(configuration instanceof IFolderInfo) {
			IFolderInfo folderInfo = (IFolderInfo) configuration;
			
			IConfiguration config = folderInfo.getParent();
			
			IManagedProject managedProject = config.getManagedProject();
			
			IProject project = (IProject) managedProject.getOwner();
			
			String currentVersion = null;
			try {
				currentVersion = project.getPersistentProperty(new QualifiedName("",
						PreferenceConstants.P_XLC_COMPILER_VERSION));
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(currentVersion == null) {
				// if the property isn't set, then use the workbench preference
				IPreferenceStore prefStore = XLCUIPlugin.getDefault().getPreferenceStore();
				currentVersion = prefStore.getString(PreferenceConstants.P_XLC_COMPILER_VERSION);
			}
			
		  if(currentVersion.equals(PreferenceConstants.P_XL_COMPILER_VERSION_8))
			  return true;
			
		}
		
		return false;
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionApplicability#isOptionEnabled(org.eclipse.cdt.managedbuilder.core.IBuildObject, org.eclipse.cdt.managedbuilder.core.IHoldsOptions, org.eclipse.cdt.managedbuilder.core.IOption)
	 */
	public boolean isOptionEnabled(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		return isVersion8(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionApplicability#isOptionUsedInCommandLine(org.eclipse.cdt.managedbuilder.core.IBuildObject, org.eclipse.cdt.managedbuilder.core.IHoldsOptions, org.eclipse.cdt.managedbuilder.core.IOption)
	 */
	public boolean isOptionUsedInCommandLine(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		return isVersion8(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IOptionApplicability#isOptionVisible(org.eclipse.cdt.managedbuilder.core.IBuildObject, org.eclipse.cdt.managedbuilder.core.IHoldsOptions, org.eclipse.cdt.managedbuilder.core.IOption)
	 */
	public boolean isOptionVisible(IBuildObject configuration,
			IHoldsOptions holder, IOption option) {
		return isVersion8(configuration);
	}
}
