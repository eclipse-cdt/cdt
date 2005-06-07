/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;


/**
 * C Project wizard that creates a new project resource in
 * a location of the user's choice.
 */
public abstract class NewCProjectWizard extends BasicNewResourceWizard implements IExecutableExtension {

	private static final String OP_ERROR= "CProjectWizard.op_error"; //$NON-NLS-1$
	private static final String OP_DESC= "CProjectWizard.op_description"; //$NON-NLS-1$

	private static final String PREFIX= "CProjectWizard"; //$NON-NLS-1$
	private static final String WZ_TITLE= "CProjectWizard.title"; //$NON-NLS-1$
	private static final String WZ_DESC= "CProjectWizard.description"; //$NON-NLS-1$

	private static final String WINDOW_TITLE = "CProjectWizard.windowTitle"; //$NON-NLS-1$
	

	private String wz_title;
	private String wz_desc;
	private String op_error;

	protected IConfigurationElement fConfigElement;
	protected NewCProjectWizardPage fMainPage; 
	protected IProject newProject;

	List tabItemsList = new ArrayList();

	public NewCProjectWizard() {
		this(CUIPlugin.getResourceString(WZ_TITLE), CUIPlugin.getResourceString(WZ_DESC), 
			CUIPlugin.getResourceString(OP_ERROR));
	}

	public NewCProjectWizard(String title, String description) {
		this(title, description, CUIPlugin.getResourceString(OP_ERROR));
	}

	public NewCProjectWizard(String title, String description, String error) {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
		wz_title = title;
		wz_desc = description;
		op_error = error;
	}

	/** 
	 * @see Wizard#createPages
	 */		
	public void addPages() {
		fMainPage= new NewCProjectWizardPage(CUIPlugin.getResourceString(PREFIX));
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
	}

	protected abstract void doRunPrologue(IProgressMonitor monitor);

	protected abstract void doRunEpilogue(IProgressMonitor monitor);

	protected IStatus isValidName(String name) {
		return new Status(IStatus.OK, CUIPlugin.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
	}

	/**
	 * Method isValidLocation.
	 * @param projectFieldContents
	 * @return IStatus
	 */
	protected IStatus isValidLocation(String projectFieldContents) {
		return new Status(IStatus.OK, CUIPlugin.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
	}

	/**
	 * Gets the project location path from the main page
	 * Overwrite this method if you do not have a main page
	 */
	protected IPath getLocationPath() throws UnsupportedOperationException {
		if (null == fMainPage)
			throw new UnsupportedOperationException();
		return fMainPage.getLocationPath();
	}

	/**
	 * Gets the project handle from the main page.
	 * Overwrite this method if you do not have a main page
	 */

	protected IProject getProjectHandle() throws UnsupportedOperationException {
		if (null == fMainPage)
			throw new UnsupportedOperationException();
		return fMainPage.getProjectHandle();
	}

	/**
	 * Returns the C project handle corresponding to the project defined in
	 * in the main page.
	 *
	 * @returns the C project
	 */    
	public IProject getNewProject() {
		return newProject;
	}

	protected IResource getSelectedResource() {
		return getNewProject();
	}

	/**
	 * @see Wizard#performFinish
	 */		
	public boolean performFinish() {
		if (!invokeRunnable(getRunnable())) {
			return false;
		}
		BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
		IResource resource = getSelectedResource();
		selectAndReveal(resource);
		if (resource != null && resource.getType() == IResource.FILE) {
			IFile file = (IFile)resource;
			// Open editor on new file.
			IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
			if (dw != null) {
				try {
					IWorkbenchPage page = dw.getActivePage();
					if (page != null)
						IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
					MessageDialog.openError(dw.getShell(),
						CUIPlugin.getResourceString(OP_ERROR), e.getMessage());
				}
			}
		}
		return true;
	}

	/**
	 * Stores the configuration element for the wizard.  The config element will be used
	 * in <code>performFinish</code> to set the result perspective.
	 *
	 * @see IExecutableExtension#setInitializationData
	 */
	public void setInitializationData(IConfigurationElement cfig, String propertyName, Object data) {
		fConfigElement= cfig;
	}
	
	/*
	 * Reimplemented method from superclass
	 */
	protected void initializeDefaultPageImageDescriptor() {
		setDefaultPageImageDescriptor(CPluginImages.DESC_WIZABAN_NEW_PROJ);
	}

	/* (non-Javadoc)
	 * Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setWindowTitle(CUIPlugin.getResourceString(WINDOW_TITLE));
	}

	public IRunnableWithProgress getRunnable() {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				final IProgressMonitor fMonitor;
				if (monitor == null) {
					fMonitor= new NullProgressMonitor();
				} else {
					fMonitor = monitor;
				}
				fMonitor.beginTask(CUIPlugin.getResourceString(OP_DESC), 3);
				final CoreException except[] = new CoreException[1];
				getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						doRunPrologue(new SubProgressMonitor(fMonitor, 1));
						try {
							doRun(new SubProgressMonitor(fMonitor, 1));
						}
						catch (CoreException e) {
							except[0] = e;
						}
						doRunEpilogue(new SubProgressMonitor(fMonitor, 1));
					}
				});
				if (except[0] != null) {
					throw new InvocationTargetException(except[0]);
				}
				fMonitor.done();
			}
		};
	}

	/**
	 * Utility method: call a runnable in a WorkbenchModifyDelegatingOperation
	 */
	protected boolean invokeRunnable(IRunnableWithProgress runnable) {
		IRunnableWithProgress op= new WorkspaceModifyDelegatingOperation(runnable);
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException e) {
			Shell shell= getShell();
			String title= CUIPlugin.getResourceString(OP_ERROR + ".title"); //$NON-NLS-1$
			String message= CUIPlugin.getResourceString(OP_ERROR + ".message"); //$NON-NLS-1$
                       
			Throwable th= e.getTargetException();
			CUIPlugin.errorDialog(shell, title, message, th);
			try {
				getProjectHandle().delete(false, false, null);
			} catch (CoreException ignore) {
			} catch (UnsupportedOperationException ignore) {
			}
			return false;
		} catch  (InterruptedException e) {
			return false;
		}
		return true;
	}

	protected void doRun(IProgressMonitor monitor) throws CoreException {
		createNewProject(monitor);
	}	

	/**
	 * Creates a new project resource with the selected name.
	 * <p>
	 * In normal usage, this method is invoked after the user has pressed Finish on
	 * the wizard; the enablement of the Finish button implies that all controls
	 * on the pages currently contain valid values.
	 * </p>
	 * <p>
	 * Note that this wizard caches the new project once it has been successfully
	 * created; subsequent invocations of this method will answer the same
	 * project resource without attempting to create it again.
	 * </p>
	 *
	 * @return the created project resource, or <code>null</code> if the project
	 *    was not created
	 */
	protected IProject createNewProject(IProgressMonitor monitor) throws CoreException {

		if (newProject != null)
			return newProject;

		// get a project handle
		IProject newProjectHandle = null;
		try {
			newProjectHandle = getProjectHandle();
		} catch (UnsupportedOperationException e) {
			throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, 0, e.getMessage(), null));
		}

		// get a project descriptor
		IPath defaultPath = Platform.getLocation();
		IPath newPath = getLocationPath();
		if (defaultPath.equals(newPath))
			newPath = null;
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
		description.setLocation(newPath);

		newProject = CCorePlugin.getDefault().createCProject(description, newProjectHandle, monitor, getProjectID());
		return newProject;
	}


	/**
	 * Method getID.
	 * @return String
	 */
	public abstract String getProjectID();

}
