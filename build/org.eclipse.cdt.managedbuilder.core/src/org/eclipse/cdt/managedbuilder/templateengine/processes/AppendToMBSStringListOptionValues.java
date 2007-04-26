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
 * This class Appends contents to Managed Build System StringList Option Values.
 */
public class AppendToMBSStringListOptionValues extends ProcessRunner {
	
	/**
	 * This method Appends contents to Managed Build System StringList Option Values.
	 */
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
			String[] values = resourcePathObject[1].getSimpleArrayValue();
			String path = resourcePathObject[2].getSimpleValue();
			try {
				modified |= setOptionValue(projectHandle, id, values, path);
			} catch (BuildException e) {
				throw new ProcessFailureException(Messages.getString("AppendToMBSStringListOptionValues.0") + e.getMessage(), e); //$NON-NLS-1$
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
	
	private boolean setOptionValue(IProject projectHandle, String id, String[] value, String path) throws BuildException, ProcessFailureException {
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
						throw new ProcessFailureException(Messages.getString("AppendToMBSStringListOptionValues.3") + path); //$NON-NLS-1$
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

	private boolean setOptionForResourceConfig(String id, String[] value, IResourceConfiguration resourceConfig, IOption[] options, IHoldsOptions optionHolder) throws BuildException {
		boolean modified = false;
		String lowerId = id.toLowerCase();
		for (int i = 0; i < options.length; i++) {
			IOption option = options[i];
			if (option.getId().toLowerCase().matches(lowerId)) {
				String[] oldValue;
				switch (option.getValueType()) {
				case IOption.STRING_LIST:
					oldValue = option.getStringListValue();
					break;
				case IOption.INCLUDE_PATH:
					oldValue = option.getIncludePaths();
					break;
				case IOption.PREPROCESSOR_SYMBOLS:
					oldValue = option.getDefinedSymbols();
					break;
				case IOption.LIBRARIES:
					oldValue = option.getLibraries();
					break;
				case IOption.OBJECTS:
					oldValue = option.getUserObjects();
					break;
				default:
					continue;
				}
				String[] newValue = new String[oldValue.length + value.length];
				System.arraycopy(oldValue, 0, newValue, 0, oldValue.length);
				System.arraycopy(value, 0, newValue, oldValue.length, value.length);
				IOption setOption = ManagedBuildManager.setOption(resourceConfig, optionHolder, option, newValue);
				if (setOption == null) {
					setOption = option;
				}
				modified = true;
			}
		}
		return modified;
	}

	private boolean setOptionForConfig(String id, String[] value, IConfiguration config, IOption[] options, IHoldsOptions optionHolder) throws BuildException {
		boolean modified = false;
		String lowerId = id.toLowerCase();
		for (int i = 0; i < options.length; i++) {
			IOption option = options[i];
			if (option.getId().toLowerCase().matches(lowerId)) {
				String[] oldValue;
				switch (option.getValueType()) {
				case IOption.STRING_LIST:
					oldValue = option.getStringListValue();
					break;
				case IOption.INCLUDE_PATH:
					oldValue = option.getIncludePaths();
					break;
				case IOption.PREPROCESSOR_SYMBOLS:
					oldValue = option.getDefinedSymbols();
					break;
				case IOption.LIBRARIES:
					oldValue = option.getLibraries();
					break;
				case IOption.OBJECTS:
					oldValue = option.getUserObjects();
					break;
				default:
					continue;
				}
				String[] newValue = new String[oldValue.length + value.length];
				System.arraycopy(oldValue, 0, newValue, 0, oldValue.length);
				System.arraycopy(value, 0, newValue, oldValue.length, value.length);
				IOption setOption = ManagedBuildManager.setOption(config, optionHolder, option, newValue);
				if (setOption == null) {
					setOption = option;
				}
				modified = true;
			}
		}
		return modified;
	}
}
