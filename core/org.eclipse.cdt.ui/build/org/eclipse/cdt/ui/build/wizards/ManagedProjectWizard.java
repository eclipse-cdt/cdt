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
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ManagedCProjectNature;
import org.eclipse.cdt.core.build.managed.BuildException;
import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ManagedBuildManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.BinaryParserBlock;
import org.eclipse.cdt.ui.wizards.CProjectWizard;
import org.eclipse.cdt.ui.wizards.CProjectWizardPage;
import org.eclipse.cdt.ui.wizards.ReferenceBlock;
import org.eclipse.cdt.ui.wizards.TabFolderPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public abstract class ManagedProjectWizard extends CProjectWizard {

	/* (non-Javadoc)
	 * String constants
	 */
	protected static final String PREFIX = "MngMakeProjectWizard";	//$NON-NLS-1$
	protected static final String OP_ERROR= PREFIX + ".op_error";	//$NON-NLS-1$
	protected static final String WZ_TITLE= PREFIX + ".title";	//$NON-NLS-1$
	protected static final String WZ_DESC= PREFIX + ".description";	//$NON-NLS-1$
	protected static final String SETTINGS_TITLE= "MngMakeWizardSettings.title"; //$NON-NLS-1$
	protected static final String SETTINGS_DESC= "MngMakeWizardSettings.description";	//$NON-NLS-1$
	
	/* (non-Javadoc)
	 * Wizard has a page inherited from super class for setting project
	 * location, one for choosing the platform and a tabbed page to set
	 * configuration options
	 */
	protected CProjectPlatformPage targetConfigurationPage; 
	protected ConfigurationBlock configBlock;
	protected ReferenceBlock referenceBlock;
	protected BinaryParserBlock binaryParserBlock;

	/**
	 * Default Constructor
	 */
	public ManagedProjectWizard() {
		this(CUIPlugin.getResourceString(WZ_TITLE), CUIPlugin.getResourceString(WZ_DESC));
	}
	
	/**
	 * @see org.eclipse.cdt.ui.wizards.CProjectWizard#CProjectWizard(java.lang.String, java.lang.String)
	 */
	public ManagedProjectWizard(String title, String desc) {
		super(title,desc);
	}

	public void addManagedBuildNature (IProject project, IProgressMonitor monitor) {
		// Add the managed build nature
		try {
			monitor.subTask("Adding Managed Nature");
			ManagedCProjectNature.addManagedNature(project, monitor);
			monitor.worked(1);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		// Add the builder
		try {
			monitor.subTask("Adding Makefile Generator");
			ManagedCProjectNature.addManagedBuilder(project, monitor);
			monitor.worked(1);
		} catch (CoreException e) {
			// TODO: handle exception
		}
		
		// Add the target to the project
		try {
			ITarget parent = targetConfigurationPage.getSelectedTarget();
			ITarget newTarget = ManagedBuildManager.createTarget(project, parent);
			if (newTarget != null) {
				// TODO add name entry field to project
				String artifactName = project.getName();
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
					ManagedBuildManager.setDefaultConfiguration(project, newConfigs[0]);
				}
			}
		} catch (BuildException e) {
			e.printStackTrace();
		}
		
		// Associate the project with the managed builder so the clients can get proper information
		try {
			ICDescriptor desc = CCorePlugin.getDefault().getCProjectDescription(project);
			desc.remove(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID);
			desc.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, ManagedBuildManager.INTERFACE_IDENTITY);
		} catch (CoreException e) {
			// TODO Flag the error to the user
		}
		
		// Save the build options
		monitor.subTask("Saving new build options.");
		ManagedBuildManager.saveBuildInfo(project);
		monitor.worked(1);
	}

	/** 
	 * @see Wizard#createPages
	 */		
	public void addPages() {
		// Add the page to name the project and set the location
		fMainPage= new CProjectWizardPage(this, new String());
		fMainPage.setTitle(CUIPlugin.getResourceString(WZ_TITLE));
		fMainPage.setDescription(CUIPlugin.getResourceString(WZ_DESC));
		addPage(fMainPage);
		
		// Add a page to chose the build platform
		targetConfigurationPage = new CProjectPlatformPage(this, new String());
		targetConfigurationPage.setTitle(CUIPlugin.getResourceString(WZ_TITLE));
		targetConfigurationPage.setDescription(CUIPlugin.getResourceString(WZ_DESC));
		addPage(targetConfigurationPage);
		
		// Add the tab container
		fTabFolderPage = new TabFolderPage(this);
		addPage(fTabFolderPage);
	}

	/**
	 * @see org.eclipse.cdt.ui.wizards.CProjectWizard#addTabItems(org.eclipse.swt.widgets.TabFolder)
	 */
	public void addTabItems(TabFolder folder) {
		fTabFolderPage.setTitle(CUIPlugin.getResourceString(SETTINGS_TITLE));
		fTabFolderPage.setDescription(CUIPlugin.getResourceString(SETTINGS_DESC));

		// Add the tab to set the project dependencies
		referenceBlock = new ReferenceBlock(getValidation());
		TabItem item2 = new TabItem(folder, SWT.NONE);
		item2.setText(referenceBlock.getLabel());
		Image img2 = referenceBlock.getImage();
		if (img2 != null)
			item2.setImage(img2);
		item2.setData(referenceBlock);
		item2.setControl(referenceBlock.getControl(folder));
		addTabItem(referenceBlock);

		// add the tab to select which parser to use for binaries
		binaryParserBlock = new BinaryParserBlock(getValidation());
		TabItem item3 = new TabItem(folder, SWT.NONE);
		item3.setText(binaryParserBlock.getLabel());
		Image img3 = binaryParserBlock.getImage();
		if (img3 != null)
			item3.setImage(img3);
		item3.setData(binaryParserBlock);
		item3.setControl(binaryParserBlock.getControl(folder));
		addTabItem(binaryParserBlock);
	}

	protected void doRunPrologue(IProgressMonitor monitor) {
	}

	protected void doRunEpilogue(IProgressMonitor monitor) {
	}

	protected void doRun(IProgressMonitor monitor) throws CoreException {
		// super.doRun() just creates the project and does not assign a builder to it.
		super.doRun(monitor);
        
		// Modify the project based on what the user has selected
		if (newProject != null) {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			// Update the referenced project if provided.
			monitor.subTask("Adding project references");
			if (referenceBlock != null) {
				referenceBlock.doRun(newProject, new SubProgressMonitor(monitor, 1));
			}
			monitor.worked(1);
			// Update the binary parser
			monitor.subTask("Setting binary parser");
			if (binaryParserBlock != null) {
				binaryParserBlock.doRun(newProject, new SubProgressMonitor(monitor, 1));
			}
			monitor.worked(1);
		}
	}
	
	public String getProjectID() {
		return CCorePlugin.PLUGIN_ID + ".make";
	}
}
