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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.GeneratedMakefileBuilder;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;

/**
 * @author crecoskie
 *
 */
public class BuildFilesAction extends Action implements IWorkbenchWindowActionDelegate {

	/**
	 * The workbench window; or <code>null</code> if this action has been
	 * <code>dispose</code>d.
	 */
	private IWorkbenchWindow workbenchWindow;

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
		super(ManagedMakeMessages.getResourceString("BuildFilesAction.buildFiles"));  //$NON-NLS-1$
		if (window == null) {
			throw new IllegalArgumentException();
		}
		this.workbenchWindow = window;
		setToolTipText(ManagedMakeMessages.getResourceString("BuildFilesAction.buildSelectedFile")); //$NON-NLS-1$
		setActionDefinitionId("org.eclipse.cdt.managedbuilder.ui.BuildFilesAction"); //$NON-NLS-1$
	}

	/**
	 * @param text
	 */
	public BuildFilesAction(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param text
	 * @param image
	 */
	public BuildFilesAction(String text, ImageDescriptor image) {
		super(text, image);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param text
	 * @param style
	 */
	public BuildFilesAction(String text, int style) {
		super(text, style);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

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

					if (buildInfo.buildsFileType(file.getFileExtension())) {
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

		BuildFilesJob(List filesToBuild)
		{
			super(ManagedMakeMessages.getResourceString("BuildFilesAction.buildingSelectedFiles")); //$NON-NLS-1$
			
			files = filesToBuild;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {

			Iterator iterator = files.iterator();
			
			GeneratedMakefileBuilder builder = new GeneratedMakefileBuilder();
			
			monitor.beginTask(ManagedMakeMessages.getResourceString("BuildFilesAction.building"), files.size()); //$NON-NLS-1$
			
			boolean isFirstFile = true;
			
			while(iterator.hasNext())
			{
				IFile file = (IFile) iterator.next();
				
				IManagedBuildInfo buildInfo = ManagedBuildManager
				.getBuildInfo(file.getProject());
							
				IResource[] resources = {file};
				
				// invoke the internal builder to do the build
				builder.invokeInternalBuilder(resources, buildInfo
						.getDefaultConfiguration(), false, false, isFirstFile,
						!iterator.hasNext(), monitor);
				
				if(isFirstFile) {
					isFirstFile = false;
				}
				
				if(monitor.isCanceled())
				{
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}



}
