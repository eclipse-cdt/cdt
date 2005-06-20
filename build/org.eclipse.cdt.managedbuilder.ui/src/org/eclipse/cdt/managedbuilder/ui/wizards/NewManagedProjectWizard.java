/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ITargetPlatform;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderHelpContextIds;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.cdt.ui.wizards.NewCProjectWizardPage;
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
	protected CProjectPlatformPage projectConfigurationPage;
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
		projectConfigurationPage = new CProjectPlatformPage(PREFIX, this);
		projectConfigurationPage.setTitle(ManagedBuilderUIMessages.getResourceString(CONF_TITLE));
		projectConfigurationPage.setDescription(ManagedBuilderUIMessages.getResourceString(CONF_DESC));
		addPage(projectConfigurationPage);
		
		// Add the options (tabbed) page
		optionPage = new NewManagedProjectOptionPage(PREFIX, this);
		optionPage.setTitle(ManagedBuilderUIMessages.getResourceString(OPTIONS_TITLE));
		optionPage.setDescription(ManagedBuilderUIMessages.getResourceString(OPTIONS_DESC));
		addPage(optionPage);
		
		// add custom pages
		MBSCustomPageManager.init();
		
		// add stock pages
		MBSCustomPageManager.addStockPage(fMainPage, NewCProjectWizardPage.PAGE_ID);
		MBSCustomPageManager.addStockPage(projectConfigurationPage, CProjectPlatformPage.PAGE_ID);
		MBSCustomPageManager.addStockPage(optionPage, NewManagedProjectOptionPage.PAGE_ID);
		
	}
	
	public void createPageControls(Composite pageContainer) {
		super.createPageControls( pageContainer );
		
		IWizardPage [] pages = getPages();
		
		if (pages != null) {
			for (int i = 0; i < pages.length; i++) {
				IWizardPage page = pages[i];
				if (page instanceof NewCProjectWizardPage) {
					WorkbenchHelp.setHelp(page.getControl(), ManagedBuilderHelpContextIds.MAN_PROJ_WIZ_NAME_PAGE);
				}
				else if (page instanceof NewManagedProjectOptionPage) {
					NewManagedProjectOptionPage optionPage = (NewManagedProjectOptionPage) page;
					optionPage.setupHelpContextIds();
				}
				//  The other built-in page is the CProjectPlatformPage which already has a help id.
			} 
		}
	}
	
	public void updateProjectTypeProperties() {
		//  Update the error parser list
		optionPage.updateProjectTypeProperties();
	}

	protected void doRun(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		// super.doRun() just creates the project and does not assign a builder to it.
		super.doRun(new SubProgressMonitor(monitor, 5));

		// Add the managed build nature and builder
		try {
			monitor.subTask(ManagedBuilderUIMessages.getResourceString(MSG_ADD_NATURE));
			ManagedCProjectNature.addManagedNature(newProject, new SubProgressMonitor(monitor, 1));
			monitor.subTask(ManagedBuilderUIMessages.getResourceString(MSG_ADD_BUILDER));
			ManagedCProjectNature.addManagedBuilder(newProject, new SubProgressMonitor(monitor, 1));
		} catch (CoreException e) {
			ManagedBuilderUIPlugin.log(e);
		}
		
		// Add the ManagedProject to the project
		IManagedProject newManagedProject = null;
		IManagedBuildInfo info = null;
		try {
			info = ManagedBuildManager.createBuildInfo(newProject);
			IProjectType parent = projectConfigurationPage.getSelectedProjectType();
			newManagedProject = ManagedBuildManager.createManagedProject(newProject, parent);
			if (newManagedProject != null) {
				IConfiguration [] selectedConfigs = projectConfigurationPage.getSelectedConfigurations();
				for (int i = 0; i < selectedConfigs.length; i++) {
					IConfiguration config = selectedConfigs[i];
					int id = ManagedBuildManager.getRandomNumber();
					IConfiguration newConfig = newManagedProject.createConfiguration(config, config.getId() + "." + id); //$NON-NLS-1$
					newConfig.setArtifactName(newManagedProject.getDefaultArtifactName());
				}
				// Now add the first supported config in the list as the default
				IConfiguration defaultCfg = null;
				IConfiguration[] newConfigs = newManagedProject.getConfigurations();
				for(int i = 0; i < newConfigs.length; i++) {
					if(newConfigs[i].isSupported()){
						defaultCfg = newConfigs[i];
						break;
					}
				}
				
				if(defaultCfg == null && newConfigs.length > 0)
					defaultCfg = newConfigs[0];
				
				if(defaultCfg != null) {
					ManagedBuildManager.setDefaultConfiguration(newProject, defaultCfg);
					ManagedBuildManager.setSelectedConfiguration(newProject, defaultCfg);
				}
				ManagedBuildManager.setNewProjectVersion(newProject);
				ICDescriptor desc = null;
				try {
					desc = CCorePlugin.getDefault().getCProjectDescription(newProject, true);
					desc.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, ManagedBuildManager.INTERFACE_IDENTITY);
					//  TODO:  The binary parser setting is currently per-project in the rest of CDT.
					//         In the MBS, it is per-coonfiguration.  For now, select the binary parsers of the
					//         first configuration.
					if (newConfigs.length > 0) {
						IToolChain tc = newConfigs[0].getToolChain();
						ITargetPlatform targetPlatform = tc.getTargetPlatform();
						if (targetPlatform != null) {
							// Create entries for all binary parsers
							String[] binaryParsers = targetPlatform.getBinaryParserList();
							for (int i=0; i<binaryParsers.length; i++) {
							    desc.create(CCorePlugin.BINARY_PARSER_UNIQ_ID, binaryParsers[i]);
							}
						}
					}
				} catch (CoreException e) {
					ManagedBuilderUIPlugin.log(e);
				}
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
		if (info != null) {
			info.setValid(true);
			ManagedBuildManager.saveBuildInfo(newProject, true);
		}
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
		// Get my initializer to run
		if(newProject == null)
			return;

		IStatus initResult = ManagedBuildManager.initBuildInfoContainer(newProject);
		if (initResult.getCode() != IStatus.OK) {
			// At this point, I can live with a failure
			ManagedBuilderUIPlugin.log(initResult);
		}
		
		// execute any operations specified by custom pages
		Runnable operations[] = MBSCustomPageManager.getOperations();
		
		if(operations != null)
		{
			for(int k = 0; k < operations.length; k++)
			{
				operations[k].run();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.NewCProjectWizard#getProjectID()
	 */
	public String getProjectID() {
//		return "org.eclipse.cdt.make.core.make"; //$NON-NLS-1$
		return ManagedBuilderCorePlugin.MANAGED_MAKE_PROJECT_ID;
	}
	
	public IProjectType getSelectedProjectType() {
		return projectConfigurationPage.getSelectedProjectType();
	}

}
