/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.templateengine;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.templateengine.TemplateEngineMessages;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;

/**
 * This class is a helper for creating general CDT projects
 */
public class ProjectCreatedActions {
	IProject project;
	IPath projectLocation;
	IConfiguration[] configs;
	String artifactExtension;
	private static final String PROPERTY = "org.eclipse.cdt.build.core.buildType"; //$NON-NLS-1$
	private static final String PROP_VAL = PROPERTY + ".debug"; //$NON-NLS-1$
	
	/*
	 *  create a project and do things common to project creationr from the new project
	 *  wizard.
	 */
	public ProjectCreatedActions() {}

	Map/*<IConfiguration, IConfiguration>*/ original2newConfigs;
	
	/**
	 * Utility method that
	 * <ul>
	 * <li>Creates a CDT MBS project from an IProject
	 * <li>Autoexpands the project in the C/C++ Projects view
	 * </ul>
	 * <p>
	 * 
	 * @param project the IProject to turn into a CDT MBS project
	 * @param projType
	 * @param configs
	 * @param artifactExtn
	 * @return an IManagedBuildInfo instance (from which the IManagedProject can be retrieved)
	 * @throws CoreException
	 * @throws BuildException
	 */
	public IManagedBuildInfo createProject(IProgressMonitor monitor, String indexerId, boolean isCProject) throws CoreException, BuildException {
		if(!areFieldsValid()) {
			throw new IllegalArgumentException(TemplateEngineMessages.getString("ProjectCreatedActions.InsufficientInformation")); //$NON-NLS-1$
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = workspace.newProjectDescription(project.getName());

		if ((projectLocation != null) && (!projectLocation.equals(Platform.getLocation()))) {
			description.setLocation(projectLocation);
		}

		Preferences corePrefs = CCorePlugin.getDefault().getPluginPreferences();
		corePrefs.setValue(CCorePlugin.PREF_INDEXER, indexerId);

		CCorePlugin.getDefault().createCDTProject(description, project, monitor);
		// Open the project if we have to
		if (!project.isOpen()) {
			project.open(monitor);
		}
		
		if (isCProject) {
			CProjectNature.addCNature(project, monitor);
		} else {
			CProjectNature.addCNature(project, monitor);
			CCProjectNature.addCCNature(project, monitor);
		}

		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription des = coreModel.createProjectDescription(project, false);
		ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
		ManagedProject newManagedProject = new ManagedProject(project, configs[0].getProjectType());
		info.setManagedProject(newManagedProject);

		original2newConfigs = new HashMap/*<IConfiguration,IConfiguration>*/();
		ICConfigurationDescription active = null;
		for(int i=0; i < configs.length; i++) {
			IConfiguration config = configs[i];
			if (config != null) {
				String id = ManagedBuildManager.calculateChildId(config.getId(), null);
				Configuration configuration = new Configuration(newManagedProject, (Configuration)config, id, false, true);
				CConfigurationData data = configuration.getConfigurationData();
				ICConfigurationDescription cfgDes = des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
				configuration.setConfigurationDescription(cfgDes);
				configuration.exportArtifactInfo();
				configuration.setArtifactExtension(artifactExtension);
				original2newConfigs.put(config, (IConfiguration)configuration);

				IBuilder builder = configuration.getEditableBuilder();
				if (builder != null) {
					builder.setManagedBuildOn(builder.isManagedBuildOn()); 
				}

				configuration.setName(config.getName());
				configuration.setArtifactName(newManagedProject.getDefaultArtifactName());

				IBuildProperty buildProperty = configuration.getBuildProperties().getProperty(PROPERTY);
				if (buildProperty != null && buildProperty.getValue() != null && PROP_VAL.equals(buildProperty.getValue().getId())) {
					active = cfgDes;
				} else if (active == null) {// select at least first configuration 
					active = cfgDes;
				}
			}
		}
		
		if (active != null) {
			active.setActive();
		}
		coreModel.setProjectDescription(project, des);

		info.setValid(true);
		ManagedBuildManager.saveBuildInfo(project, true);

		IStatus status = ManagedBuildManager.initBuildInfoContainer(project);
		if (status.getCode() != IStatus.OK) {
			ResourcesPlugin.getPlugin().getLog().log(status);
		}

		// Force the binary parser list to be updated for the project.
		// An attempt is made to do this earlier when the project is being
		// created, but the build info may not be ready then.
		CCorePlugin.getDefault().getCProjectDescription(project, false).get(CCorePlugin.BINARY_PARSER_UNIQ_ID, true);
		
		return info;
	}
	
	protected boolean areFieldsValid() {
		return project!=null && configs!=null && artifactExtension!=null;
	}
	
	public IConfiguration getNewConfiguration(IConfiguration original) {
		return (IConfiguration) original2newConfigs.get(original);
	}
	
	public Set/*<IConfiguration>*/ getNewConfigurations(Collection/*<IConfiguration>*/ originalConfigs) {
		Set/*<IConfiguration>*/ result = new HashSet/*<IConfiguration>*/();
		for(Iterator i = originalConfigs.iterator(); i.hasNext(); ) {
			result.add(getNewConfiguration((IConfiguration)i.next()));
		}
		return result;
	}
	
	/**
	 * Chooses the IConfiguration which should be considered the project default.
	 * @param configs an array containing each of the newly created IConfigurations
	 * @return an element of the input array
	 */
	protected IConfiguration chooseDefaultConfiguration(IConfiguration[] configs) {
		return configs[0];
	}

	/**
	 * @param artifactExtension  the artifactExtension to set
	 */
	public void setArtifactExtension(String artifactExtension) {
		this.artifactExtension = artifactExtension;
	}


	/**
	 * @return  the configs
	 */
	public IConfiguration[] getConfigs() {
		return configs;
	}


	/**
	 * @param configs  the configs to set
	 */
	public void setConfigs(IConfiguration[] configs) {
		this.configs = configs;
	}


	/**
	 * @return  the project
	 */
	public IProject getProject() {
		return project;
	}


	/**
	 * @param project  the project to set
	 */
	public void setProject(IProject project) {
		this.project = project;
	}


	/**
	 * @return  the projectLocation
	 */
	public IPath getProjectLocation() {
		return projectLocation;
	}


	/**
	 * @param projectLocation  the projectLocation to set
	 */
	public void setProjectLocation(IPath projectLocation) {
		this.projectLocation = projectLocation;
	}


	protected IConfiguration createNewConfiguration(IManagedProject managedProject, IConfiguration config) {
		return managedProject.createConfiguration(config, config.getId() + "." + ManagedBuildManager.getRandomNumber()); //$NON-NLS-1$
	}
}
