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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;


public class NewManagedProjectWizard extends NewCProjectWizard {

	/* (non-Javadoc)
	 * String constants
	 */
	protected static final String PREFIX = "MngMakeProjectWizard";	//$NON-NLS-1$
	protected static final String OP_ERROR = PREFIX + ".op_error";	//$NON-NLS-1$
	protected static final String WZ_TITLE = PREFIX + ".title";	//$NON-NLS-1$
	protected static final String WZ_DESC = PREFIX + ".description";	//$NON-NLS-1$
	protected static final String CONF_TITLE = PREFIX + ".config.title";	//$NON-NLS-1$
	protected static final String CONF_DESC = PREFIX + ".config.desc";	//$NON-NLS-1$
	protected static final String OPTIONS_TITLE = PREFIX + ".options.title";	//$NON-NLS-1$
	protected static final String OPTIONS_DESC = PREFIX + ".options.desc";	//$NON-NLS-1$
	protected static final String MSG_ADD_NATURE = PREFIX + ".message.add_nature";	//$NON-NLS-1$
	protected static final String MSG_ADD_BUILDER = PREFIX + ".message.add_builder";	//$NON-NLS-1$
	protected static final String MSG_SAVE = PREFIX + ".message.save";	//$NON-NLS-1$
	protected static final String SETTINGS_TITLE = "MngMakeWizardSettings.title"; //$NON-NLS-1$
	protected static final String SETTINGS_DESC = "MngMakeWizardSettings.description";	//$NON-NLS-1$
	
	// Wizard pages
	protected CProjectPlatformPage targetConfigurationPage;
	protected NewManagedProjectOptionPage optionPage;

	public NewManagedProjectWizard() {
		this(ManagedBuilderUIPlugin.getResourceString(WZ_TITLE), ManagedBuilderUIPlugin.getResourceString(WZ_DESC));
	}

	public NewManagedProjectWizard(String title, String description) {
		super(title, description);
	}

	public void addPages() {
		// Add the default page for all new projects 
		super.addPages();
		
		// Add the configuration selection page
		targetConfigurationPage = new CProjectPlatformPage(PREFIX);
		targetConfigurationPage.setTitle(ManagedBuilderUIPlugin.getResourceString(CONF_TITLE));
		targetConfigurationPage.setDescription(ManagedBuilderUIPlugin.getResourceString(CONF_DESC));
		addPage(targetConfigurationPage);
		
		// Add the options (tabbed) page
		optionPage = new NewManagedProjectOptionPage(PREFIX);
		optionPage.setTitle(ManagedBuilderUIPlugin.getResourceString(OPTIONS_TITLE));
		optionPage.setDescription(ManagedBuilderUIPlugin.getResourceString(OPTIONS_DESC));
		addPage(optionPage);
	}

	protected void doRun(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		// super.doRun() just creates the project and does not assign a builder to it.
		super.doRun(new SubProgressMonitor(monitor, 5));

		// Add the managed build nature
		try {
			monitor.subTask(ManagedBuilderUIPlugin.getResourceString(MSG_ADD_NATURE));
			ManagedCProjectNature.addManagedNature(newProject, new SubProgressMonitor(monitor, 1));
		} catch (CoreException e) {
			// Bail out of the project creation
		}
		// Add the builder
		try {
			monitor.subTask(ManagedBuilderUIPlugin.getResourceString(MSG_ADD_BUILDER));
			ManagedCProjectNature.addManagedBuilder(newProject, new SubProgressMonitor(monitor, 1));
		} catch (CoreException e) {
			// Bail out of the project creation
		}
		        
		// Modify the project settings
		if (newProject != null) {
			optionPage.performApply(new SubProgressMonitor(monitor, 2));
		}

		// Add the target to the project
		ITarget newTarget = null;
		try {
			ITarget parent = targetConfigurationPage.getSelectedTarget();
			newTarget = ManagedBuildManager.createTarget(newProject, parent);
			if (newTarget != null) {
				// TODO add name entry field to project
				String artifactName = newProject.getName();
				artifactName +=  parent.getDefaultExtension().length() == 0 ? "" : "." + parent.getDefaultExtension();
				newTarget.setBuildArtifact(artifactName);
				IConfiguration [] selectedConfigs = targetConfigurationPage.getSelectedConfigurations();
				for (int i = 0; i < selectedConfigs.length; i++) {
					IConfiguration config = selectedConfigs[i];
					newTarget.createConfiguration(config, config.getId() + "." + i);
				}
				// Now add the first config in the list as the default
				IConfiguration[] newConfigs = newTarget.getConfigurations();
				if (newConfigs.length > 0) {
					ManagedBuildManager.setDefaultConfiguration(newProject, newConfigs[0]);
				}
			}
		} catch (BuildException e) {
			// TODO Flag the error to the user
		}

		// Associate the project with the managed builder so the clients can get proper information
		try {
			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(newProject);
			desc.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
			desc.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, ManagedBuildManager.INTERFACE_IDENTITY);
			
			desc.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
			// org.eclipse.cdt.core.ELF or "org.eclipse.cdt.core.PE"
			desc.create(CCorePlugin.BINARY_PARSER_UNIQ_ID, newTarget.getBinaryParserId());
		} catch (CoreException e) {
			// TODO Flag the error to the user
		}
		
		// Save the build options
		monitor.subTask(ManagedBuilderUIPlugin.getResourceString(MSG_SAVE));
		ManagedBuildManager.saveBuildInfo(newProject);
		monitor.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.NewCProjectWizard#doRunPrologue(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doRunPrologue(IProgressMonitor monitor) {
		// Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.NewCProjectWizard#doRunEpilogue(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doRunEpilogue(IProgressMonitor monitor) {
		// Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.NewCProjectWizard#getProjectID()
	 */
	public String getProjectID() {
		return "org.eclipse.cdt.make.core.make";
//		return ManagedBuilderCorePlugin.getUniqueIdentifier() + ".make"; //$NON-NLS-1$
	}

}
