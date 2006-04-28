/*******************************************************************************
 * Copyright (c) 2006 Nokia and others.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.internal.core.model.ExternalTranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
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

	private ImportExecutablePageOne pageOne;

	private ImportExecutablePageTwo pageTwo;
	
	/**
	 * Override this method to add the correct binary parsers to the project.
	 * @param newProject - the project created by the wizard
	 * @throws CoreException
	 */
	public abstract void addBinaryParsers(IProject newProject) throws CoreException;
	
	/**
	 * Adds the executables to a new or existing project. The executables are
	 * added as external links.
	 * 
	 * @param project -
	 *            project receiving the executables
	 * @throws CoreException
	 */
	private void addExecutables(IProject project) throws CoreException {

		String[] executables = pageOne.getSelectedExecutables();

		for (int i = 0; i < executables.length; i++) {
			IPath location = Path.fromOSString(executables[i]);
			String executableName = location.toFile().getName();
			IFile exeFile = project.getFile(executableName);
			if (!exeFile.exists())
				exeFile.createLink(location, 0, null);
		}

	}

	public void addPages() {
		super.addPages();
		pageOne = new ImportExecutablePageOne(this);
		addPage(pageOne);
		pageTwo = new ImportExecutablePageTwo(this);
		addPage(pageTwo);
	}

	private void addSourceLocation(ISourceLocator locator, AbstractSourceLookupDirector director, IPath unitLocation)
	{
		if (unitLocation.toFile().exists()) {
			boolean found = false;
			String unitLocationPathString = unitLocation.toOSString();
			if (locator instanceof ICSourceLocator)
				found = (((ICSourceLocator) locator).findSourceElement(unitLocationPathString) != null);
			else if (locator instanceof CSourceLookupDirector)
				found = ((CSourceLookupDirector) locator).contains(unitLocationPathString);

			if (!found) {

				DirectorySourceContainer directoryContainer = new DirectorySourceContainer(
						unitLocation.removeLastSegments(1), false);
				ArrayList containerList = new ArrayList(Arrays.asList(director
						.getSourceContainers()));
				containerList.add(directoryContainer);
				director.setSourceContainers((ISourceContainer[]) containerList
						.toArray(new ISourceContainer[containerList.size()]));
			}
		}
	}

	protected void addSourceLocations(IBinary[] binaries, ILaunchConfigurationWorkingCopy configuration) {

		String memento = null;
		String type = null;
		try {
			memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String) null);
			type = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String) null);
			if (type == null) {
				type = configuration.getType().getSourceLocatorId();
			}

			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			ISourceLocator locator = launchManager.newSourceLocator(type);
			if (locator instanceof AbstractSourceLookupDirector) {
				AbstractSourceLookupDirector director = (AbstractSourceLookupDirector) locator;
				if (memento == null) {
					director.initializeDefaults(configuration);
				} else {
					director.initializeFromMemento(memento, configuration);
				}

				for (int i = 0; i < binaries.length; i++) {
					IBinary binary = binaries[i];
					if (!binary.getPath().lastSegment().startsWith(".")) {
						addSourceLocation(locator, director, binary.getUnderlyingResource().getLocation());
						List sourceFiles;
						sourceFiles = binary.getChildrenOfType(ICElement.C_UNIT);
						if (sourceFiles.size() == 0)
						{
							sourceFiles = binary.getChildrenOfType(ICElement.C_UNIT);							
						}
						for (Iterator iter = sourceFiles.iterator(); iter.hasNext();) {
							Object element = (Object) iter.next();
							if (element instanceof ExternalTranslationUnit) {
								ExternalTranslationUnit unit = (ExternalTranslationUnit) element;
								IPath unitLocation = unit.getLocation();
								addSourceLocation(locator, director, unitLocation);
							}

						}

					}
				}
				configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, director.getMemento());
				configuration.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, director.getId());

			}
		} catch (CoreException e) {
			return;
		}

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

		wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, targetProject.getProject().getName());
		wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, new File(getImportExecutablePage()
				.getSelectedExecutables()[0]).getName());

		addSourceLocations(targetProject.getBinaryContainer().getBinaries(), wc);
		setConfigurationDefaults(wc);

		final IStructuredSelection selection = new StructuredSelection(wc.doSave());
		final String identifier = new String("org.eclipse.debug.ui.launchGroup.debug");

		UIJob openLaunchConfigJob = new UIJob(Messages.AbstractImportExecutableWizard_CreateLaunchConfiguration) {

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

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(getDefaultWindowTitle());
		setNeedsProgressMonitor(true);
	}

	public abstract boolean isExecutableFile(File file);
	
	public boolean performFinish() {

		ICProject targetProject = null;
		try {
			if (pageTwo.isCreateNewProjectSelected()) {
				// Create a new project and add the executables and binary
				// parsers.
				IProject newProject = createCProjectForExecutable(pageTwo
						.getNewProjectName());
				setupProject(newProject);
				addExecutables(newProject);
				addBinaryParsers(newProject);
				targetProject = CCorePlugin.getDefault().getCoreModel().create(
						newProject);
			} else {
				// Assume the existing project already has binary parsers setup,
				// just add the executables.
				ICProject existingProject = pageTwo.getExistingCProject();
				addExecutables(existingProject.getProject());
				targetProject = existingProject;
			}
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
	 * created by the wizard. The default implementation does nothing.
	 * @param config the launch configuration created by the wizard
	 */
	public void setConfigurationDefaults(ILaunchConfigurationWorkingCopy config) {

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

}
