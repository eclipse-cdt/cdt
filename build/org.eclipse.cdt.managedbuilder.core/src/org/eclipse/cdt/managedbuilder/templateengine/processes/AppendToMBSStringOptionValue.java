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

import org.eclipse.cdt.core.templateengine.process.processes.Messages;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * This class Appends contents to Managed Build System String Option Value.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class AppendToMBSStringOptionValue extends ProcessRunner {
	
	/**
	 * This method Appends contents to Managed Build System StringList Option Values.
	 */
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {
		String projectName = args[0].getSimpleValue();
		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription workspaceDesc = workspace.getDescription();
		boolean autoBuilding = workspaceDesc.isAutoBuilding();
		workspaceDesc.setAutoBuilding(false);
		try {
			workspace.setDescription(workspaceDesc);
		} catch (CoreException e) {//ignore
		}
		
		ProcessArgument[][] resourcePathObjects = args[1].getComplexArrayValue();
		boolean modified = false;
		for(int i=0; i<resourcePathObjects.length; i++) {
			ProcessArgument[] resourcePathObject = resourcePathObjects[i];
			String id = resourcePathObject[0].getSimpleValue();
			String value = resourcePathObject[1].getSimpleValue();
			String path = resourcePathObject[2].getSimpleValue();
			try {
				modified |= setOptionValue(projectHandle, id, value, path);
			} catch (BuildException e) {
				throw new ProcessFailureException(Messages.getString("AppendToMBSStringOptionValue.0") + e.getMessage(), e); //$NON-NLS-1$
			}
		}
		if (modified) {
			ManagedBuildManager.saveBuildInfo(projectHandle, true);
		}

		workspaceDesc.setAutoBuilding(autoBuilding);
		try {
			workspace.setDescription(workspaceDesc);
		} catch (CoreException e) {//ignore
		}
	}
	
	private boolean setOptionValue(IProject projectHandle, String id, String value, String path) throws BuildException, ProcessFailureException {
		IConfiguration[] projectConfigs = ManagedBuildManager.getBuildInfo(projectHandle).getManagedProject().getConfigurations();
		
		boolean resource = !(path == null || path.equals("") || path.equals("/")); //$NON-NLS-1$ //$NON-NLS-2$
		boolean modified = false;
		
		for(int i=0; i<projectConfigs.length; i++) {
			IConfiguration config = projectConfigs[i];
			IResourceConfiguration resourceConfig = null;
			if (resource) {
				resourceConfig = config.getResourceConfiguration(path);
				if (resourceConfig == null) {
					IFile file = projectHandle.getFile(path);
					if (file == null) {
						throw new ProcessFailureException(Messages.getString("AppendToMBSStringOptionValue.3") + path); //$NON-NLS-1$
					}
					resourceConfig = config.createResourceConfiguration(file);
				}
				ITool[] tools = resourceConfig.getTools();
				for(int j=0; j<tools.length; j++) {
					modified |= setOptionForResourceConfig(id, value, resourceConfig, tools[j].getOptions(), tools[j]);
				}
			} else {
				IToolChain toolChain = config.getToolChain();
				modified |= setOptionForConfig(id, value, config, toolChain.getOptions(), toolChain);
				
				ITool[] tools = config.getTools();
				for(int j=0; j<tools.length; j++) {
					modified |= setOptionForConfig(id, value, config, tools[j].getOptions(), tools[j]);
				}
			}
		}
		
		return modified;
	}

	private boolean setOptionForResourceConfig(String id, String value, IResourceConfiguration resourceConfig, IOption[] options, IHoldsOptions optionHolder) throws BuildException {
		boolean modified = false;
		String lowerId = id.toLowerCase();
		for (int i = 0; i < options.length; i++) {
			if (options[i].getId().toLowerCase().matches(lowerId)) {
				if (options[i].getValueType() == IOption.STRING) {
					String oldValue = options[i].getStringValue();
					String newValue = oldValue + value;
					IOption setOption = ManagedBuildManager.setOption(resourceConfig, optionHolder, options[i], newValue);
					if (setOption == null) {
						setOption = options[i];
					}
					modified = true;
				}
			}
		}
		return modified;
	}

	private boolean setOptionForConfig(String id, String value, IConfiguration config, IOption[] options, IHoldsOptions optionHolder) throws BuildException {
		boolean modified = false;
		String lowerId = id.toLowerCase();
		for (int i = 0; i < options.length; i++) {
			if (options[i].getId().toLowerCase().matches(lowerId)) {
				if (options[i].getValueType() == IOption.STRING) {
					String oldValue = options[i].getStringValue();
					String newValue = oldValue + value;
					IOption setOption = ManagedBuildManager.setOption(config, optionHolder, options[i], newValue);
					if (setOption == null) {
						setOption = options[i];
					}
					modified = true;
				}
			}
		}
		return modified;
	}
}
