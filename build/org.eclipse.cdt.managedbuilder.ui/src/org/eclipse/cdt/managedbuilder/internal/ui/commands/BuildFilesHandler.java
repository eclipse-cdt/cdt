/*******************************************************************************
 * Copyright (c) 2006, 2014 Texas Instruments Incorporated and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Texas Instruments - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.internal.core.AdapterUtil;
import org.eclipse.cdt.internal.ui.util.AbstractResourceActionHandler;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.GeneratedMakefileBuilder;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.ide.actions.BuildUtilities;

public class BuildFilesHandler extends AbstractResourceActionHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<IFile> selectedFiles = getSelectedFiles(event);
		if (selectedFiles.isEmpty())
			return null;

		Job buildFilesJob = new BuildFilesJob(selectedFiles);

		Collection<IProject> projects = getProjectsToBuild(selectedFiles);
		BuildUtilities.saveEditors(projects); // Save all resources prior to doing build.
		buildFilesJob.schedule();
		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		super.setEnabled(evaluationContext);
		setBaseEnabled(shouldBeEnabled());
	}

	private boolean shouldBeEnabled() {
		// Fix for bug 139663.
		// If build automatically is turned on, then this command should be turned off as
		// it will trigger the auto build.
		IPreferencesService preferences = Platform.getPreferencesService();

		if (preferences.getBoolean(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PREF_AUTO_BUILDING, false, null)) {
			// Auto building is on - do not enable the command.
			return false;
		}

		IStructuredSelection selection = getSelection();
		if (selection.isEmpty())
			return false;

		for (Iterator<?> elements = selection.iterator(); elements.hasNext();) {
			IFile file = AdapterUtil.adapt(elements.next(), IFile.class);
			if (file == null)
				return false;

			// We only add files that we can actually build.
			if (!ManagedBuildManager.manages(file.getProject()))
				return false;

			IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(file.getProject());

			if (buildInfo == null || !buildInfo.isValid() || buildInfo.getDefaultConfiguration() == null
					|| !buildInfo.getDefaultConfiguration().isManagedBuildOn()) {
				return false;
			}

			IManagedBuilderMakefileGenerator buildfileGenerator = ManagedBuildManager
					.getBuildfileGenerator(buildInfo.getDefaultConfiguration());

			if (buildfileGenerator == null)
				return false;

			// Make sure build file generator is initialized.
			buildfileGenerator.initialize(file.getProject(), buildInfo, new NullProgressMonitor());

			// If we have no build info or we can't build the file, then disable the command.
			if (!buildInfo.buildsFileType(file.getFileExtension()) || buildfileGenerator.isGeneratedResource(file)) {
				return false;
			}
		}
		return true;
	}

	private List<IFile> getSelectedFiles(ExecutionEvent event) throws ExecutionException {
		Collection<IResource> selectedResources = getSelectedResources(event);
		List<IFile> files = new ArrayList<>(selectedResources.size());
		for (IResource resource : selectedResources) {
			if (resource instanceof IFile)
				files.add((IFile) resource);
		}
		return files;
	}

	/*
	 * Returns the projects to build. These are the projects which have builders,
	 * across all selected resources.
	 */
	private Collection<IProject> getProjectsToBuild(List<IFile> selectedFiles) {
		Set<IProject> projectsToBuild = new HashSet<>();
		for (IFile file : selectedFiles) {
			IProject project = file.getProject();
			if (!projectsToBuild.contains(project)) {
				if (hasBuilder(project)) {
					projectsToBuild.add(project);
				}
			}
		}

		return projectsToBuild;
	}

	/*
	 * Checks whether there are builders configured on the given project.
	 *
	 * @return {@code true} if it has builders, {@code false} if not, or if this couldn't be
	 *     determined
	 */
	private boolean hasBuilder(IProject project) {
		if (!project.isAccessible())
			return false;

		try {
			ICommand[] commands = project.getDescription().getBuildSpec();
			if (commands.length > 0)
				return true;
		} catch (CoreException e) {
			// This method is called when selection changes, so just fall through if it
			// fails. This shouldn't happen anyway, since the list of selected resources
			// has already been checked for accessibility before this is called.
		}
		return false;
	}

	private static class BuildFilesJob extends Job {
		private final List<IFile> files;

		BuildFilesJob(List<IFile> filesToBuild) {
			super(Messages.BuildFilesHandler_buildingSelectedFiles);
			files = filesToBuild;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			GeneratedMakefileBuilder builder = new GeneratedMakefileBuilder();
			return builder.invokeInternalBuilder(files, monitor);
		}

		@Override
		public boolean belongsTo(Object family) {
			return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
		}
	}
}
