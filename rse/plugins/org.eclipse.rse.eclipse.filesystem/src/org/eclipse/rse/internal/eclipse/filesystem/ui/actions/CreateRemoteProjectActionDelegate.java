/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Kushal Munir (IBM) - moved to internal package
 ********************************************************************************/

package org.eclipse.rse.internal.eclipse.filesystem.ui.actions;

import java.net.URI;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.rse.internal.eclipse.filesystem.RSEFileSystem;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;



public class CreateRemoteProjectActionDelegate implements IActionDelegate {
	
	protected IStructuredSelection fSelection;

	
    /**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) 
	{
		IRemoteFile directory = (IRemoteFile)fSelection.getFirstElement();
        createRemoteProject(directory, new NullProgressMonitor());
  
	}
	

	private IProject createRemoteProject(IRemoteFile directory, IProgressMonitor monitor)
	{
		IWorkspaceRoot root = SystemBasePlugin.getWorkspaceRoot();

		IProject editProject = root.getProject(directory.getName());

		if ((editProject != null) && (editProject.exists()) && (editProject.isOpen()))
		{
			return editProject;
		}
		
		if (editProject == null) {
			return null;
		}

		try
		{
			IProjectDescription description = root.getWorkspace().newProjectDescription(directory.getName());
			URI location = RSEFileSystem.getInstance().getURIFor(directory);
			description.setLocationURI(location);

			editProject.create(description, monitor);
			
			editProject.open(monitor);
			
		    editProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		}
		catch (CoreException e)
		{
			SystemBasePlugin.logError("Error creating temp project", e); //$NON-NLS-1$
		}
		catch (Exception e)
		{
		}
		return editProject;
	}
	
	/**
	 * Sets the selection. The selection is only set if given a structured selection, otherwise it is set to an
	 * empty structured selection.
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		
		if (selection instanceof IStructuredSelection) {
			fSelection = (IStructuredSelection)selection;
		}
		else {
			fSelection = StructuredSelection.EMPTY;
		}
	}
	
	/**
	 * Returns the remote file in the selection.
	 * Use this method if this action allows only a single remote file selection.
	 * @return the single remote file.
	 */
	protected IRemoteFile getRemoteFile(IStructuredSelection selection) {
		return (IRemoteFile)selection.getFirstElement();
	}
	
	/**
	 * Returns the remote files in the selection.
	 * Use this method if this action allows multiple remote file selection.
	 * @return an array of remote files.
	 */
	protected IRemoteFile[] getRemoteFiles(IStructuredSelection selection) {

		IRemoteFile[] files = new IRemoteFile[selection.size()];
		Iterator iter = selection.iterator();
		
		int i = 0;
		
		while (iter.hasNext()) {
			files[i++] = (IRemoteFile)iter.next();
		}
		
		return files;
	}
	
	/**
	 * Returns the description file for the first description file in
	 * the selection. Use this method if this action allows only
	 * a single file selection.
	 * @return the single description file.
	 */
	protected IFile getDescriptionFile(IStructuredSelection selection) {
		return (IFile)selection.getFirstElement();
	}

	/**
	 * Returns a description file for each description file in
	 * the selection. Use this method if this action allows multiple
	 * selection.
	 * @return an array of description files.
	 */
	protected IFile[] getDescriptionFiles(IStructuredSelection selection) {
		IFile[] files = new IFile[selection.size()];
		Iterator iter = selection.iterator();
		
		int i = 0;
		
		while (iter.hasNext()) {
			files[i++] = (IFile)iter.next();
		}
		
		return files;
	}

	/**
	 * Returns the workbench.
	 * @return the workbench.
	 */
	protected IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}
	
	/**
	 * Returns the active shell.
	 * @return the active shell.
	 */
	protected Shell getShell() {
		return Display.getDefault().getActiveShell();
	}

	/**
	 * Returns the selection.
	 * @return the selection.
	 */
	protected IStructuredSelection getSelection() {
		return fSelection;
	}
}