/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.templateengine.processes;

import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.core.templateengine.process.processes.Messages;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.templateengine.ProjectCreatedActions;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;


/**
 * Creates a new Project in the workspace.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class NewManagedProject extends ProcessRunner {
	protected boolean savedAutoBuildingValue;
	protected ProjectCreatedActions pca;
	protected IManagedBuildInfo info;
	
	public NewManagedProject() {
		pca = new ProjectCreatedActions();
	}
	
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {
		String projectName = args[0].getSimpleValue();
		String location = args[1].getSimpleValue();
		String artifactExtension = args[2].getSimpleValue();
		String isCProjectValue = args[3].getSimpleValue();
		boolean isCProject = Boolean.valueOf(isCProjectValue).booleanValue();
				
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		if (!project.exists()) {
			try {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				turnOffAutoBuild(workspace);

				IPath locationPath = null;
				if (location != null && !location.trim().equals("")) { //$NON-NLS-1$
					locationPath = Path.fromPortableString(location);
				}
				
				@SuppressWarnings("unchecked")
				List<IConfiguration> configs = (List<IConfiguration>) template.getTemplateInfo().getConfigurations();
				if (configs == null || configs.size() == 0) {
					throw new ProcessFailureException(Messages.getString("NewManagedProject.4") + projectName); //$NON-NLS-1$
				}
				
				pca.setProject(project);
				pca.setProjectLocation(locationPath);
				pca.setConfigs(configs.toArray(new IConfiguration[configs.size()]));
				pca.setArtifactExtension(artifactExtension);
				info = pca.createProject(monitor, CCorePlugin.DEFAULT_INDEXER, isCProject);

				info.setValid(true);
				ManagedBuildManager.saveBuildInfo(project, true);

				restoreAutoBuild(workspace);
				
			} catch (CoreException e) {
				throw new ProcessFailureException(Messages.getString("NewManagedProject.3") + e.getMessage(), e); //$NON-NLS-1$
			} catch (BuildException e) {
				throw new ProcessFailureException(Messages.getString("NewManagedProject.3") + e.getMessage(), e); //$NON-NLS-1$
			}
		} else {
//			throw new ProcessFailureException(Messages.getString("NewManagedProject.5") + projectName); //$NON-NLS-1$
		}
	}

	protected final void turnOffAutoBuild(IWorkspace workspace) throws CoreException {
		IWorkspaceDescription workspaceDesc = workspace.getDescription();
		savedAutoBuildingValue = workspaceDesc.isAutoBuilding();
		workspaceDesc.setAutoBuilding(false);
		workspace.setDescription(workspaceDesc);
	}
	
	protected final void restoreAutoBuild(IWorkspace workspace) throws CoreException {
		IWorkspaceDescription workspaceDesc = workspace.getDescription();
		workspaceDesc.setAutoBuilding(savedAutoBuildingValue);
		workspace.setDescription(workspaceDesc);
	}
	
	/**
	 * setOptionValue
	 */
	protected void setOptionValue(IConfiguration config, IOption option, String val) throws BuildException {
		if (val != null) {
			if (!option.isExtensionElement()) {
				option.setValue(val);
			} else {
				IOption newOption = config.getToolChain().createOption(option, option.getId() + "." + ManagedBuildManager.getRandomNumber(), option.getName(), false); //$NON-NLS-1$
				newOption.setValue(val);
			}
		}
	}

}
