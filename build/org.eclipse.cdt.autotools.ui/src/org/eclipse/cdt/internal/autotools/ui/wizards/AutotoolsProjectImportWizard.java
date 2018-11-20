/*******************************************************************************
 * Copyright (c) 2015 Mentor Graphics Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mohamed Azab (Mentor Graphics) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.core.ToolChain;
import org.eclipse.cdt.managedbuilder.ui.wizards.CfgHolder;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.NewMakeProjFromExisting;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

@SuppressWarnings("restriction")
public class AutotoolsProjectImportWizard extends NewMakeProjFromExisting {
	private final String TITLE = "Import Existing code as Autotools project";
	private static final String PREFIX = "WizardAutotoolsConversion"; //$NON-NLS-1$
	protected static final String CONF_TITLE = PREFIX + ".config.title"; //$NON-NLS-1$
	protected static final String CONF_DESC = PREFIX + ".config.desc"; //$NON-NLS-1$

	private AutotoolsProjectImportWizardPage page;
	protected CProjectPlatformPage projectConfigurationPage;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(TITLE);
	}

	@Override
	public void addPages() {
		page = new AutotoolsProjectImportWizardPage();
		addPage(page);

		// Add the configuration selection page
		projectConfigurationPage = new CProjectPlatformPage(PREFIX);
		projectConfigurationPage.setTitle(AutotoolsUIPlugin.getResourceString(CONF_TITLE));
		projectConfigurationPage.setDescription(AutotoolsUIPlugin.getResourceString(CONF_DESC));
		addPage(projectConfigurationPage);

		// add custom pages
		MBSCustomPageManager.init();

		// add stock pages
		MBSCustomPageManager.addStockPage(projectConfigurationPage, CProjectPlatformPage.PAGE_ID);

	}

	public IProjectType getProjectType() {
		return projectConfigurationPage.getProjectType();
	}

	public IConfiguration[] getSelectedConfigurations() {
		return projectConfigurationPage.getSelectedConfigurations();
	}

	@Override
	public boolean performFinish() {
		final String projectName = page.getProjectName();
		final String locationStr = page.getLocation();
		final boolean isCPP = page.isCPP();
		final IToolChain toolChain = page.getToolChain();

		IRunnableWithProgress op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) {
				monitor.beginTask("Creating Autotools project", 10);

				// Create Project
				try {
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IProject project = workspace.getRoot().getProject(projectName);

					IProjectDescription description = workspace.newProjectDescription(projectName);
					IPath defaultLocation = workspace.getRoot().getLocation().append(projectName);
					Path location = new Path(locationStr);
					if (!location.isEmpty() && !location.equals(defaultLocation)) {
						description.setLocation(location);
					}

					CCorePlugin.getDefault().createCDTProject(description, project, monitor);

					// C++ natures
					if (isCPP) {
						CCProjectNature.addCCNature(project, SubMonitor.convert(monitor, 1));
					}

					// Set up build information
					ICProjectDescriptionManager pdMgr = CoreModel.getDefault().getProjectDescriptionManager();
					ICProjectDescription projDesc = pdMgr.createProjectDescription(project, false);
					ManagedBuildInfo info = ManagedBuildManager.createBuildInfo(project);
					ManagedProject mProj = new ManagedProject(projDesc);
					info.setManagedProject(mProj);
					monitor.worked(1);

					CfgHolder cfgHolder = new CfgHolder(toolChain, null);
					String s = toolChain == null ? "0" : ((ToolChain) toolChain).getId(); //$NON-NLS-1$
					Configuration config = new Configuration(mProj, (ToolChain) toolChain,
							ManagedBuildManager.calculateChildId(s, null), cfgHolder.getName());
					IBuilder builder = config.getEditableBuilder();
					builder.setManagedBuildOn(false);
					CConfigurationData data = config.getConfigurationData();
					projDesc.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
					monitor.worked(1);

					pdMgr.setProjectDescription(project, projDesc);

					// Convert the created project to an Autotools project
					page.convertProject(project, monitor, projectName);
				} catch (Throwable e) {
				}
				monitor.done();
			}
		};

		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException | InterruptedException e) {
			return false;
		}
		return true;
	}

}
