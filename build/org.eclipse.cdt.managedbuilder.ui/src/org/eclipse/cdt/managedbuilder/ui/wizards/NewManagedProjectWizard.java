package org.eclipse.cdt.managedbuilder.ui.wizards;

/**********************************************************************
 * Copyright (c) 2002,2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import java.util.Random;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderHelpContextIds;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;


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
		this(ManagedBuilderUIMessages.getResourceString(WZ_TITLE), ManagedBuilderUIMessages.getResourceString(WZ_DESC));
	}

	public NewManagedProjectWizard(String title, String description) {
		super(title, description);
	}

	public void addPages() {
		// Add the default page for all new projects 
		super.addPages();
		
		// Add the configuration selection page
		targetConfigurationPage = new CProjectPlatformPage(PREFIX, this);
		targetConfigurationPage.setTitle(ManagedBuilderUIMessages.getResourceString(CONF_TITLE));
		targetConfigurationPage.setDescription(ManagedBuilderUIMessages.getResourceString(CONF_DESC));
		addPage(targetConfigurationPage);
		
		// Add the options (tabbed) page
		optionPage = new NewManagedProjectOptionPage(PREFIX, this);
		optionPage.setTitle(ManagedBuilderUIMessages.getResourceString(OPTIONS_TITLE));
		optionPage.setDescription(ManagedBuilderUIMessages.getResourceString(OPTIONS_DESC));
		addPage(optionPage);
	}
	
	public void createPageControls(Composite pageContainer) {
		super.createPageControls( pageContainer );
		
		IWizardPage [] pages = getPages();
		
		if( pages != null && pages.length == 3 ){
			WorkbenchHelp.setHelp(pages[0].getControl(), ManagedBuilderHelpContextIds.MAN_PROJ_WIZ_NAME_PAGE);
	
			//pages[1] is the CProjectPlatformPage which already has a help id.
			
			NewManagedProjectOptionPage optionPage = (NewManagedProjectOptionPage) pages[2];
			optionPage.setupHelpContextIds();
		}
	}
	public void updateTargetProperties() {
		//  Update the error parser list
		optionPage.updateTargetProperties();
	}

	protected void doRun(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		// super.doRun() just creates the project and does not assign a builder to it.
		super.doRun(new SubProgressMonitor(monitor, 5));

		// Add the managed build nature and builder
		ICDescriptor desc = null;
		try {
			monitor.subTask(ManagedBuilderUIMessages.getResourceString(MSG_ADD_NATURE));
			ManagedCProjectNature.addManagedNature(newProject, new SubProgressMonitor(monitor, 1));
			monitor.subTask(ManagedBuilderUIMessages.getResourceString(MSG_ADD_BUILDER));
			ManagedCProjectNature.addManagedBuilder(newProject, new SubProgressMonitor(monitor, 1));
			desc = CCorePlugin.getDefault().getCProjectDescription(newProject, true);
			desc.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
			desc.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, ManagedBuildManager.INTERFACE_IDENTITY);
			desc.remove(CCorePlugin.BINARY_PARSER_UNIQ_ID);
		} catch (CoreException e) {
			ManagedBuilderUIPlugin.log(e);
		}
		
		// Add the target to the project
		ITarget newTarget = null;
		try {
			ManagedBuildManager.createBuildInfo(newProject);
			ITarget parent = targetConfigurationPage.getSelectedTarget();
			newTarget = ManagedBuildManager.createTarget(newProject, parent);
			if (newTarget != null) {
				try {
					// org.eclipse.cdt.core.ELF or org.eclipse.cdt.core.PE
					desc.create(CCorePlugin.BINARY_PARSER_UNIQ_ID, newTarget.getBinaryParserId());
				} catch (CoreException e) {
					ManagedBuilderUIPlugin.log(e);
				}
				newTarget.setArtifactName(getBuildGoalName());
				IConfiguration [] selectedConfigs = targetConfigurationPage.getSelectedConfigurations();
				Random r = new Random();
				r.setSeed(System.currentTimeMillis());
				for (int i = 0; i < selectedConfigs.length; i++) {
					IConfiguration config = selectedConfigs[i];
					int id = r.nextInt();
					if (id < 0) {
						id *= -1;
					}
					newTarget.createConfiguration(config, config.getId() + "." + id); //$NON-NLS-1$
				}
				// Now add the first config in the list as the default
				IConfiguration[] newConfigs = newTarget.getConfigurations();
				if (newConfigs.length > 0) {
					ManagedBuildManager.setDefaultConfiguration(newProject, newConfigs[0]);
				}
				ManagedBuildManager.setSelectedTarget(newProject, newTarget);
				ManagedBuildManager.setNewProjectVersion(newProject);
			}
		} catch (BuildException e) {
			ManagedBuilderUIPlugin.log(e);
		}

		// Modify the project settings
		if (newProject != null) {
			optionPage.performApply(new SubProgressMonitor(monitor, 2));
		}

		// Save the build options
		monitor.subTask(ManagedBuilderUIMessages.getResourceString(MSG_SAVE));
		ManagedBuildManager.saveBuildInfo(newProject, true);
		monitor.done();
	}

	/**
	 * @return
	 */
	private String getBuildGoalName() {
		String name = new String();
		// Check for spaces
		String[] tokens = newProject.getName().split("\\s");	//$NON-NLS-1$
		for (int index = 0; index < tokens.length; ++index) {
			name += tokens[index];
		}
		return name;
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
		// Get my initializer to run
		IStatus initResult = ManagedBuildManager.initBuildInfoContainer(newProject);
		if (initResult.getCode() != IStatus.OK) {
			// At this point, I can live with a failure
			ManagedBuilderUIPlugin.log(initResult);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.NewCProjectWizard#getProjectID()
	 */
	public String getProjectID() {
		return "org.eclipse.cdt.make.core.make"; //$NON-NLS-1$
//		return ManagedBuilderCorePlugin.getUniqueIdentifier() + ".make"; //$NON-NLS-1$
	}
	
	public ITarget getSelectedTarget() {
		return targetConfigurationPage.getSelectedTarget();
	}

}
