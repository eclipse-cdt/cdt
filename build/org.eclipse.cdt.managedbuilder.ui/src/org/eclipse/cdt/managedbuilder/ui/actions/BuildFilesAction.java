/*******************************************************************************
 * Copyright (c) 2006 Texas Instruments Incorporated and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Texas Instruments - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.actions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.GeneratedMakefileBuilder;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderMakefileGenerator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * @author crecoskie
 *
 */
public class BuildFilesAction extends ActionDelegate implements
		IWorkbenchWindowActionDelegate {

	/**
	 * The workbench window; or <code>null</code> if this action has been
	 * <code>dispose</code>d.
	 */
	private IWorkbenchWindow workbenchWindow = null;

	private IAction action = null;

	/**
	 * 
	 */
	public BuildFilesAction() {
		this(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
	}

	/**
	 * Creates an instance of this action, for use in the given window.
	 */
	public BuildFilesAction(IWorkbenchWindow window) {
		if (window == null) {
			throw new IllegalArgumentException();
		}
		this.workbenchWindow = window;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		this.action = action;
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		workbenchWindow = window;

	}

	/**
	 * Helper method that converts an object to the <code>IFile</code>
	 * interface. The conversion is a bare cast operation (if the object is
	 * instance of <code>IFile</code>, or an adaptation (if the object is
	 * instance of <code>IAdaptable</code>).
	 * 
	 * @param object
	 *            the object to be cast to <code>IFile</code>
	 * @return a reference to an IFile corresponding to the object provided, or
	 *         null if it is not possible to convert the provided object to
	 *         <code>IFile</code>.
	 */
	private IFile convertToIFile(Object object) {

		if (object instanceof IFile) {
			return (IFile) object;
		}

		if (object instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) object;
			IFile file = (IFile) adaptable.getAdapter(IFile.class);

			if (file != null) {
				return file;
			}
		}

		// this *should* be redundant now that I've made CElement adaptable to IFile but we'll leave
		// it just to be safe
		if (object instanceof ITranslationUnit) {
			IResource resource = ((ITranslationUnit) object).getResource();

			// should be safe to cast to IFile (can't really have a non-file
			// translation unit), but check anyway
			if (resource instanceof IFile) {
				return (IFile) resource;
			}

		}

		return null;
	}

	/**
	 * Returns a list of resources currently selected.
	 * "Buildable" means buildable by MBS.
	 * 
	 * @return a list of resources
	 */
	private List getSelectedBuildableFiles() {

		List files = new LinkedList();

		ISelectionService selectionService = workbenchWindow
				.getSelectionService();
		ISelection selection = selectionService.getSelection();

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			for (Iterator elements = structuredSelection.iterator(); elements
					.hasNext();) {
				IFile file = convertToIFile(elements.next());
				if (file != null) {
					// we only add files that we can actually build
					IManagedBuildInfo buildInfo = ManagedBuildManager
							.getBuildInfo(file.getProject());

					if ((buildInfo != null) && buildInfo.isValid()
							&& buildInfo
									.buildsFileType(file.getFileExtension())) {
						files.add(file);
					}
				}
			}

			// since we don't allow building folders, there can be no
			// redundancies
			// eliminateRedundancies(resources);
		}

		return files;
	}

	private static final class BuildFilesJob extends Job {
		private final List files;

		BuildFilesJob(List filesToBuild) {
			super(
					ManagedMakeMessages
							.getResourceString("BuildFilesAction.buildingSelectedFiles")); //$NON-NLS-1$

			files = filesToBuild;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {

			Iterator iterator = files.iterator();

			GeneratedMakefileBuilder builder = new GeneratedMakefileBuilder();

			monitor
					.beginTask(
							ManagedMakeMessages
									.getResourceString("BuildFilesAction.building"), files.size()); //$NON-NLS-1$

			boolean isFirstFile = true;

			while (iterator.hasNext()) {
				IFile file = (IFile) iterator.next();

				IManagedBuildInfo buildInfo = ManagedBuildManager
						.getBuildInfo(file.getProject());

				IResource[] resources = { file };

				// invoke the internal builder to do the build
				builder.invokeInternalBuilder(resources, buildInfo
						.getDefaultConfiguration(), false, false, isFirstFile,
						!iterator.hasNext(), monitor);

				if (isFirstFile) {
					isFirstFile = false;
				}

				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}

			}

			monitor.done();
			return Status.OK_STATUS;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
		 */
		public boolean belongsTo(Object family) {
			return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {

		List selectedFiles = getSelectedBuildableFiles();

		Job buildFilesJob = new BuildFilesJob(selectedFiles);

		buildFilesJob.schedule();

	}

	private boolean shouldBeEnabled() {
		
		// fix for Bugzilla 139663
		// if build automatically is turned on, then this menu should be turned off as
		// it will trigger the auto build
		Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
		
		if(preferences.getBoolean(ResourcesPlugin.PREF_AUTO_BUILDING))
		{
			// auto building is on... do not enable the menu
			return false;
		}
		
		ISelectionService selectionService = workbenchWindow
				.getSelectionService();
		ISelection selection = selectionService.getSelection();

		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;

			if (structuredSelection.size() <= 0) {
				return false;
			}

			for (Iterator elements = structuredSelection.iterator(); elements
					.hasNext();) {
				IFile file = convertToIFile(elements.next());
				if (file != null) {
					// we only add files that we can actually build
					if (!ManagedBuildManager.manages(file.getProject())) {
						return false;
					}

					IManagedBuildInfo buildInfo = ManagedBuildManager
							.getBuildInfo(file.getProject());

					if (buildInfo == null || !buildInfo.isValid()) {
						return false;
					}

					IManagedBuilderMakefileGenerator buildfileGenerator = ManagedBuildManager
							.getBuildfileGenerator(buildInfo
									.getDefaultConfiguration());

					if (buildfileGenerator == null) {
						return false;
					}

					// make sure build file generator is initialized
					buildfileGenerator.initialize(file.getProject(), buildInfo,
							new NullProgressMonitor());

					// if we have no build info or we can't build the file, then
					// disable the action
					if (!buildInfo.buildsFileType(file.getFileExtension())
							|| buildfileGenerator.isGeneratedResource(file)) {

						return false;

					}
				}

				else {
					return false;
				}
			}
			return true;
		}

		return false;

	}

	/*
	 * Updates the enablement state for the action based upon the selection. If
	 * the selection corresponds to files buildable by MBS, then the action will
	 * be enabled.
	 */
	private void update() {
		if (action != null) {
			action.setEnabled(shouldBeEnabled());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// update state
		update();
	}

}
