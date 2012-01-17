/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.importexecutable;

import java.io.File;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.progress.UIJob;

public abstract class AbstractImportExecutableWizard extends Wizard implements INewWizard {

	// The ImportExecutableWizard lets you select one or more executables and
	// import them into the workspace. You can bring the executables into an
	// existing project or have the wizard create a new project that
	// will contains the executables and allow you to debug them. The wizard can
	// also create a default launch configuration for you that's pre configured
	// to debug the executables.

	public static final String DEBUG_PROJECT_ID = "org.eclipse.cdt.debug"; //$NON-NLS-1$

	protected ImportExecutablePageOne pageOne;

	protected ImportExecutablePageTwo pageTwo;

	public void addBinaryParsers(IProject newProject) throws CoreException {
		ICProjectDescription pd = CCorePlugin.getDefault().getProjectDescription(newProject);
		String[] parserIDs = pageOne.getSupportedBinaryParserIds();
		for (int i = 0; i < parserIDs.length; i++) {
			pd.getDefaultSettingConfiguration().create(CCorePlugin.BINARY_PARSER_UNIQ_ID, parserIDs[i]);
		}
		CCorePlugin.getDefault().setProjectDescription(newProject, pd, true, new NullProgressMonitor());
	}
	
	/**
	 * Adds the executables to a new or existing project. The executables are
	 * added as external links.
	 * 
	 * @param project -
	 *            project receiving the executables
	 * @throws CoreException
	 */
	private void addExecutables(ICProject project) {

		String[] executables = pageOne.getSelectedExecutables();

		for (int i = 0; i < executables.length; i++) {
			IPath location = Path.fromOSString(executables[i]);
			String executableName = location.toFile().getName();
			IFile exeFile = project.getProject().getFile(executableName);
			if (!exeFile.exists())
			{
				try {
					exeFile.createLink(location, 0, null);
				} catch (Exception e) {
					this.getImportExecutablePage2().setErrorMessage("Error importing: " + executables[i]);
				}
			}
		}
	}

	@Override
	public void addPages() {
		super.addPages();
		pageOne = new ImportExecutablePageOne(this);
		addPage(pageOne);
		pageTwo = new ImportExecutablePageTwo(this);
		addPage(pageTwo);
	}

	public IProject createCProjectForExecutable(String projectName) throws OperationCanceledException, CoreException {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject newProjectHandle = workspace.getRoot().getProject(projectName);

		IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
		description.setLocation(null);

		IProject newProject = CCorePlugin.getDefault().createCProject(description, newProjectHandle, null,
				DEBUG_PROJECT_ID);

		return newProject;
	}

	public void createLaunchConfiguration(ICProject targetProject) throws CoreException {
		
		ILaunchConfigurationWorkingCopy wc = this.getSelectedLaunchConfigurationType().newInstance(null,
				this.getImportExecutablePage2().getNewConfigurationName());

		setConfigurationDefaults(wc, targetProject);

		final IStructuredSelection selection = new StructuredSelection(wc.doSave());
		final String identifier = "org.eclipse.debug.ui.launchGroup.debug"; //$NON-NLS-1$

		UIJob openLaunchConfigJob = new UIJob(Messages.AbstractImportExecutableWizard_CreateLaunchConfiguration) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				DebugUITools.openLaunchConfigurationDialogOnGroup(CUIPlugin.getActiveWorkbenchShell(), selection, identifier);
				return Status.OK_STATUS;
			}};
		openLaunchConfigJob.schedule();

	}

	public abstract String getExecutableListLabel();

	public ImportExecutablePageOne getImportExecutablePage() {
		return pageOne;
	}

	public ImportExecutablePageTwo getImportExecutablePage2() {
		return pageTwo;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == pageOne) {
			pageTwo.checkExecutableSettings();
		}
		return super.getNextPage(page);
	}

	public abstract String getPageOneDescription();

	public abstract String getPageOneTitle();

	public ILaunchConfigurationType getSelectedLaunchConfigurationType() {
		return pageTwo.getSelectedLaunchConfigurationType();
	}

	public String getDefaultWindowTitle() {
		return Messages.AbstractImportExecutableWizard_windowTitle;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(getDefaultWindowTitle());
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public boolean performFinish() {

		ICProject targetProject = null;
		try {
			if (pageTwo.isCreateNewProjectSelected()) {
				IProject newProject = createCProjectForExecutable(pageTwo
						.getNewProjectName());
				setupProject(newProject);
				targetProject = CCorePlugin.getDefault().getCoreModel().create(
						newProject);
			} else {
				targetProject = pageTwo.getExistingCProject();
			}
			addBinaryParsers(targetProject.getProject());
			addExecutables(targetProject);
			if (pageTwo.isCreateLaunchConfigurationSelected()) {
				createLaunchConfiguration(targetProject);
			}
		} catch (OperationCanceledException e) {
		} catch (CoreException e) {
		}
		return true;
	}
	
	/**
	 * Subclasses should override this method to modify the launch configuration
	 * created by the wizard. The default implementation sets up the project
	 * and program names.
	 * @param config the launch configuration created by the wizard
	 * @param targetProject 
	 */
	public void setConfigurationDefaults(ILaunchConfigurationWorkingCopy config, ICProject project) {

		config.setMappedResources(new IResource[] {project.getProject()});
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, project.getProject().getName());
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, new File(getImportExecutablePage()
				.getSelectedExecutables()[0]).getName());

	}

	public abstract void setupFileDialog(FileDialog dialog);

	public void setupProject(IProject newProject) throws CoreException {
	}

	/**
	 * The wizard will only display launch configuration types that you support.
	 * This method will be called for each available type.
	 * 
	 * @param type -
	 *            the type of launch configuration
	 * @return - if the wizard supports this launch configuration type
	 */
	public abstract boolean supportsConfigurationType(
			ILaunchConfigurationType type);

	/**
	 * Return true if you want the wizard to ask the user to select
	 * the binary parser. Otherwise it will only use the default one.
	 * A subclass can specify the default parser by overriding 
	 * getDefaultBinaryParserID.
	 * @return - If the binary parser selection combo should be displayed.
	 */
	public boolean userSelectsBinaryParser() {
		return true;
	}

	/** Get the default binary parser the wizard will use to determine if
	 * single file selections are valid and to filter the list for multi
	 * file selection.
	 * @return
	 */
	public String[] getDefaultBinaryParserIDs() {
		String defaultBinaryParserId = CCorePlugin.getDefault().getPluginPreferences().getDefaultString(CCorePlugin.PREF_BINARY_PARSER);
		if (defaultBinaryParserId == null || defaultBinaryParserId.length() == 0) {
			defaultBinaryParserId = CCorePlugin.DEFAULT_BINARY_PARSER_UNIQ_ID;
		}
		return new String[] { defaultBinaryParserId };
	}

	public String getDefaultProjectName() {
		String defaultName = ""; //$NON-NLS-1$
		String[] executables = getImportExecutablePage()
				.getSelectedExecutables();
		if (executables.length > 0) {
			String fileName = new File(executables[0]).getName();
			defaultName = Messages.ImportExecutablePageTwo_DefaultProjectPrefix + fileName;
		}
		return defaultName;
	}

}
