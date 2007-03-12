/*******************************************************************************
 * Copyright (c) 2002, 2005 Rational Software Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.newui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.NewUIMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public abstract class NewModelProjectWizard extends BasicNewResourceWizard implements IExecutableExtension  
//extends NewManagedProjectWizard 
{
	private static final String MSG_CREATE = "MngCCWizard.message.creating";	//$NON-NLS-1$
	private static final String PREFIX= "CProjectWizard"; //$NON-NLS-1$
	private static final String OP_ERROR= "CProjectWizard.op_error"; //$NON-NLS-1$
	private static final String OP_DESC= "CProjectWizard.op_description"; //$NON-NLS-1$
	
	protected IConfigurationElement fConfigElement;
	protected CMainWizardPage fMainPage;
	protected CConfigWizardPage fConfigPage;
	
	protected IProject newProject;
	private String wz_title;
	private String wz_desc;
	private String propertyId;

	protected List localPages = new ArrayList(); // replacing Wizard.pages since we have to delete them
	
	public NewModelProjectWizard() {
		this(IDEWorkbenchMessages.getString("NewModelProjectWizard.0"),IDEWorkbenchMessages.getString("NewModelProjectWizard.1")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public NewModelProjectWizard(String title, String desc) {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
		wz_title = title;
		wz_desc = desc;
	}
	
	public void addPages() {
		fConfigPage = new CConfigWizardPage();
		fMainPage= new CMainWizardPage(CUIPlugin.getResourceString(PREFIX), fConfigPage);
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
		addPage(fConfigPage);
		
		// support for custom wizard pages
		MBSCustomPageManager.init();
		MBSCustomPageManager.addStockPage(fMainPage, CMainWizardPage.PAGE_ID);
		MBSCustomPageManager.addStockPage(fConfigPage, CConfigWizardPage.PAGE_ID);
		
		// Set up custom page manager to current natures settings
		String[] natures = getNatures();
		if (natures == null || natures.length == 0)
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.NATURE, null);
		else if (natures.length == 1)
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.NATURE, natures[0]);
		else {
			Set x = new TreeSet();
			for (int i=0; i<natures.length; i++) x.add(natures[i]);
			MBSCustomPageManager.addPageProperty(MBSCustomPageManager.PAGE_ID, MBSCustomPageManager.NATURE, x);
		}
		
		// load all custom pages specified via extensions
		try	{
			MBSCustomPageManager.loadExtensions();
		} catch (BuildException e) { e.printStackTrace(); }
		
		IWizardPage[] customPages = MBSCustomPageManager.getCustomPages();
		if (customPages != null) {
			for (int k = 0; k < customPages.length; k++) {
				addPage(customPages[k]);
			}
		}
	}

	protected void doRun(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(NewUIMessages.getResourceString(MSG_CREATE), 8);
		
		newProject = createIProject(fMainPage.getProjectName(), fMainPage.getProjectLocation());
		if (newProject != null) {
			try {
				if (fMainPage.h_selected.needsConfig()) {
					boolean def = fMainPage.isCurrent();
					fMainPage.h_selected.createProject(newProject, fConfigPage.getConfigurations(def), fConfigPage.getNames(def));
				} else 
					fMainPage.h_selected.createProject(newProject, null, null);
			} catch (CoreException e) {
				ManagedBuilderUIPlugin.log(e);
				throw e;
			}
		}
		monitor.done();
	}

	protected IProject getProjectHandle() throws UnsupportedOperationException {
		if (null == fMainPage)
			throw new UnsupportedOperationException();
		return fMainPage.getProjectHandle();
	}

	protected boolean invokeRunnable(IRunnableWithProgress runnable) {
		IRunnableWithProgress op= new WorkspaceModifyDelegatingOperation(runnable);
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException e) {
			Shell shell= getShell();
			String title= CUIPlugin.getResourceString(OP_ERROR + ".title"); //$NON-NLS-1$
			String message= CUIPlugin.getResourceString(OP_ERROR + ".message"); //$NON-NLS-1$
                       
			Throwable th= e.getTargetException();
			CUIPlugin.errorDialog(shell, title, message, th, false);
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

	public boolean performFinish() {
		if (!invokeRunnable(getRunnable())) {
			return false;
		}
			
		BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
		IResource resource = newProject;
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
		// run properties dialog if required
		if (fMainPage.h_selected.showProperties())
			PreferencesUtil.createPropertyDialogOn(getShell(), newProject, propertyId, null, null).open();
		return true;
	}

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		fConfigElement= config;
	}
	
	public IRunnableWithProgress getRunnable() {
		return new WorkspaceModifyDelegatingOperation(new IRunnableWithProgress() {
			public void run(IProgressMonitor imonitor) throws InvocationTargetException, InterruptedException {
				final Exception except[] = new Exception[1];
				// ugly, need to make the wizard page run in a non ui thread so that this can go away!!!
				getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						IRunnableWithProgress op= new WorkspaceModifyDelegatingOperation(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				final IProgressMonitor fMonitor;
				if (monitor == null) {
					fMonitor= new NullProgressMonitor();
				} else {
					fMonitor = monitor;
				}
				fMonitor.beginTask(CUIPlugin.getResourceString(OP_DESC), 3);
						doRunPrologue(new SubProgressMonitor(fMonitor, 1));
						try {
							doRun(new SubProgressMonitor(fMonitor, 1));
						}
						catch (CoreException e) {
							except[0] = e;
						}
						doRunEpilogue(new SubProgressMonitor(fMonitor, 1));
								fMonitor.done();
					}
				});
						try {
							getContainer().run(false, true, op);
						} catch (InvocationTargetException e) {
							except[0] = e;
						} catch (InterruptedException e) {
							except[0] = e;
						}
					}
				});
				if (except[0] != null) {
					if (except[0] instanceof InvocationTargetException) {
						throw (InvocationTargetException)except[0];
					}
					if (except[0] instanceof InterruptedException) {
						throw (InterruptedException)except[0];
					}
					throw new InvocationTargetException(except[0]);
				}
	}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.NewCProjectWizard#doRunPrologue(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doRunPrologue(IProgressMonitor monitor) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.NewCProjectWizard#doRunEpilogue(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doRunEpilogue(IProgressMonitor monitor) {
		if(newProject == null) return;

		IStatus initResult = ManagedBuildManager.initBuildInfoContainer(newProject);
		if (initResult.getCode() != IStatus.OK) {
			ManagedBuilderUIPlugin.log(initResult);
		}
		
		// execute any operations specified by custom pages
		Runnable operations[] = MBSCustomPageManager.getOperations();
		if(operations != null)
			for(int k = 0; k < operations.length; k++)
				operations[k].run();
	}
	
	/**
	 * 
	 */	
	public IProject createIProject(final String name, final IPath location) throws CoreException{
		if (newProject != null)
			return newProject;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		final IProject newProjectHandle = root.getProject(name);
		
		if (!newProjectHandle.exists()) {
			IWorkspaceDescription workspaceDesc = workspace.getDescription();
			workspaceDesc.setAutoBuilding(false);
			workspace.setDescription(workspaceDesc);
			IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
			if(location != null)
				description.setLocation(location);
			newProject = CCorePlugin.getDefault().createCDTProject(description, newProjectHandle, new NullProgressMonitor());
		} else {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					newProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			};
			NullProgressMonitor monitor = new NullProgressMonitor();
			workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, monitor);
			newProject = newProjectHandle;
		}
        
		// Open the project if we have to
		if (!newProject.isOpen()) {
			newProject.open(new NullProgressMonitor());
		}
		return continueCreation(newProject);	
	}
	
	protected abstract IProject continueCreation(IProject prj); 
	protected abstract String[] getNatures(); 	
}
