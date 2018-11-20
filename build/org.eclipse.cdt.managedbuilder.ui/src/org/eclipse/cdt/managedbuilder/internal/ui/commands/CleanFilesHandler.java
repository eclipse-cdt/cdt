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
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.core.AdapterUtil;
import org.eclipse.cdt.internal.ui.util.AbstractResourceActionHandler;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.GeneratedMakefileBuilder;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Action to clean build output for selected files
 */
public class CleanFilesHandler extends AbstractResourceActionHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<IFile> selectedFiles = getSelectedBuildableFiles(event);
		if (selectedFiles.isEmpty())
			return null;

		CleanFilesJob job = new CleanFilesJob(selectedFiles);
		job.schedule();
		return null;
	}

	/**
	 * Returns a list of buildable resources currently selected.
	 * "Buildable" means buildable by MBS.
	 */
	private List<IFile> getSelectedBuildableFiles(ExecutionEvent event) throws ExecutionException {
		Collection<IResource> selectedResources = getSelectedResources(event);
		List<IFile> files = new ArrayList<>(selectedResources.size());
		for (IResource resource : selectedResources) {
			// We only add files that we can actually build.
			if (resource instanceof IFile && isBuildable(resource)) {
				files.add((IFile) resource);
			}
		}
		return files;
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
			if (!isBuildable(file))
				return false;
		}

		return true;
	}

	private static boolean isBuildable(IResource resource) {
		IProject project = resource.getProject();
		if (!ManagedBuildManager.manages(project))
			return false;
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		return buildInfo != null && buildInfo.isValid() && buildInfo.buildsFileType(resource.getFileExtension());
	}

	private static class CleanFilesJob extends Job {
		private final List<IFile> files;

		private CleanFilesJob(List<IFile> filesToBuild) {
			super(Messages.CleanFilesHandler_cleaningFiles);
			files = filesToBuild;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			synchronized (getClass()) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				Job[] buildJobs = Job.getJobManager().find(ResourcesPlugin.FAMILY_MANUAL_BUILD);
				for (Job job : buildJobs) {
					if ((job != this) && (job instanceof CleanFilesJob)) {
						job.cancel(); // Cancel all other build jobs of our kind.
					}
				}
			}

			GeneratedMakefileBuilder builder = new GeneratedMakefileBuilder();
			return builder.cleanFiles(files, monitor);
		}

		@Override
		public boolean belongsTo(Object family) {
			return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
		}
	}
}
