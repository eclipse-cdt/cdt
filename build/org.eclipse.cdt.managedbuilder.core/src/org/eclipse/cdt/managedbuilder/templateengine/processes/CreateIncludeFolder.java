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

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.processes.CreateSourceFolder;
import org.eclipse.cdt.core.templateengine.process.processes.Messages;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * Creates a include Folder to the project.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CreateIncludeFolder extends CreateSourceFolder {
	
	/**
	 * This method Creates a include Folder to the project.
	 * 
	 */
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {
		String projectName = args[0].getSimpleValue();
		String targetPath = args[1].getSimpleValue();
		createSourceFolder(projectName, targetPath, monitor);
		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(projectHandle);
		try {
			IConfiguration[] configs = info.getManagedProject().getConfigurations();
			for(int i=0; i<configs.length; i++) {
				String path = projectHandle.getFolder(targetPath).getLocation().toOSString();
				IToolChain toolChain = configs[i].getToolChain();
				setIncludePathOptionForConfig(path, configs[i], toolChain.getOptions(), toolChain);
				
				ITool[] tools = configs[i].getTools();
				for(int j=0; j<tools.length; j++) {
					setIncludePathOptionForConfig(path, configs[i], tools[j].getOptions(), tools[j]);
				}
			}
		} catch (BuildException e) {
			throw new ProcessFailureException(Messages.getString("CreateIncludeFolder.3") + e.getMessage(), e); //$NON-NLS-1$
		}
		ManagedBuildManager.saveBuildInfo(projectHandle, true);
	}

	private void setIncludePathOptionForConfig(String path, IConfiguration config, IOption[] options, IHoldsOptions optionHolder) throws BuildException {
		for (int i = 0; i < options.length; i++) {
			IOption option = options[i];
			if (option.getValueType() == IOption.INCLUDE_PATH) {
				String[] includePaths = option.getIncludePaths();
				String[] newPaths = new String[includePaths.length + 1];
				System.arraycopy(includePaths, 0, newPaths, 0, includePaths.length);
				newPaths[includePaths.length] = path;
				ManagedBuildManager.setOption(config, optionHolder, option, newPaths);
			}
		}
	}
	
}
