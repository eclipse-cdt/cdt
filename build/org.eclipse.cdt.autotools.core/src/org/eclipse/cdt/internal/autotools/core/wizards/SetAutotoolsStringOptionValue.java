/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 * Jon Beniston - Add support for Autotools
 *******************************************************************************/

package org.eclipse.cdt.internal.autotools.core.wizards;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfigurationManager;
import org.eclipse.cdt.internal.autotools.core.configure.IAConfiguration;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class SetAutotoolsStringOptionValue extends ProcessRunner {

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
		for(int i=0; i<resourcePathObjects.length; i++) {
			ProcessArgument[] resourcePathObject = resourcePathObjects[i];
			String id = resourcePathObject[0].getSimpleValue();
			String value = resourcePathObject[1].getSimpleValue();
			try {
				setOptionValue(projectHandle, id, value);
			} catch (BuildException e) {
				throw new ProcessFailureException(AutotoolsPlugin.getFormattedString("SetAutotoolsStringOptionValue.error", new String[]{e.getMessage()}), e); //$NON-NLS-1$
			}
		}

		workspaceDesc.setAutoBuilding(autoBuilding);
		try {
			workspace.setDescription(workspaceDesc);
		} catch (CoreException e) {//ignore
		}
	}
	
	private void setOptionValue(IProject projectHandle, String id, String
			value) throws BuildException, ProcessFailureException {

		AutotoolsConfigurationManager.getInstance().syncConfigurations(projectHandle);
		ICConfigurationDescription[] cfgds =
			CoreModel.getDefault().getProjectDescription(projectHandle).getConfigurations();
		if (cfgds != null && cfgds.length >= 1) {
			IAConfiguration iaConfig =
				AutotoolsConfigurationManager.getInstance().getConfiguration(projectHandle,
						cfgds[0].getId());
			iaConfig.setOption(id, value);
		}
	}

}
