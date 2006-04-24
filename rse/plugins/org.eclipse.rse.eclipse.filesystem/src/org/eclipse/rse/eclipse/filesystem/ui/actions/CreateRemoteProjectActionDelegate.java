package org.eclipse.rse.eclipse.filesystem.ui.actions;

import java.net.URI;
import java.util.Iterator;

import org.eclipse.core.internal.resources.ProjectDescription;
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
import org.eclipse.rse.core.SystemResourceManager;
import org.eclipse.rse.eclipse.filesystem.RSEFileSystem;
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

		if (editProject == null)
		{
			// log error and throw an exception
		}

		try
		{

			
			
			IProjectDescription description = root.getWorkspace().newProjectDescription(directory.getName());
			URI location = RSEFileSystem.getInstance().getURIFor(directory);
			description.setLocationURI(location);

			
		
			//description.setReferencedProjects(new IProject[]{SystemResourceManager.getRemoteSystemsTempFilesProject()});
			editProject.create(description, monitor);
			

			editProject.open(monitor);
			
		    editProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);

		    IProject tempFilesProject = SystemResourceManager.getRemoteSystemsTempFilesProject();
			IProjectDescription tempFilesDescription = tempFilesProject.getDescription();
			tempFilesDescription.setReferencedProjects(new IProject[]{editProject});
			tempFilesProject.setDescription(tempFilesDescription, monitor);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
			SystemBasePlugin.logError("Error creating temp project", e);
		}
		catch (Exception e)
		{
			e.printStackTrace();
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