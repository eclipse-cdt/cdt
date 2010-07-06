/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Doug Schaefer (WRS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Wizard to create a new CDT project that wraps existing code.
 * @since 7.0
 */
public class NewMakeProjFromExisting extends Wizard implements IImportWizard, INewWizard {

	NewMakeProjFromExistingPage page;
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Messages.NewMakeProjFromExisting_0);
	}

	@Override
	public void addPages() {
		page = new NewMakeProjFromExistingPage();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		final String projectName = page.getProjectName();
		final String location = page.getLocation();
		final boolean isCPP = page.isCPP();
		final IToolChain toolChain = page.getToolChain();
		
		IRunnableWithProgress op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
					InterruptedException {
				monitor.beginTask(Messages.NewMakeProjFromExisting_1, 10);
				
				// Create Project
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IProject project = workspace.getRoot().getProject(projectName);
				
				// TODO handle the case where a .project file was already there
				IProjectDescription description = workspace.newProjectDescription(project.getName());
				description.setLocation(new Path(location));
				
				CCorePlugin.getDefault().createCDTProject(description, project, monitor);
				
				// Optionally C++ natures
				if (isCPP)
					CCProjectNature.addCCNature(project, new SubProgressMonitor(monitor, 1));
				
				// Set up build information
				ICProjectDescriptionManager pdMgr = CoreModel.getDefault().getProjectDescriptionManager();
				ICProjectDescription projDesc = pdMgr.createProjectDescription(project, false);
				ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
				ManagedProject mProj = new ManagedProject(projDesc);
				info.setManagedProject(mProj);
				monitor.worked(1);
				
				CfgHolder cfgHolder = new CfgHolder(toolChain, null);
				String s = toolChain == null ? "0" : ((ToolChain)toolChain).getId(); //$NON-NLS-1$
				Configuration config = new Configuration(mProj, (ToolChain)toolChain, ManagedBuildManager.calculateChildId(s, null), cfgHolder.getName());
				IBuilder builder = config.getEditableBuilder();
				builder.setManagedBuildOn(false);
				CConfigurationData data = config.getConfigurationData();
				projDesc.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
				monitor.worked(1);
				
				pdMgr.setProjectDescription(project, projDesc);
				
				monitor.done();
			}
		};
		
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
