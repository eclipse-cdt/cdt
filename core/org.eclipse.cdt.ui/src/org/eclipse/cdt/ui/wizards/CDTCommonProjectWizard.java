/*******************************************************************************
 * Copyright (c) 2002, 2007 Rational Software Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Intel corp - rework for New Project Model
 *******************************************************************************/
package org.eclipse.cdt.ui.wizards;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.newui.UIMessages;

public abstract class CDTCommonProjectWizard extends BasicNewResourceWizard 
implements IExecutableExtension, IWizardWithMemory  
{
	private static final String PREFIX= "CProjectWizard"; //$NON-NLS-1$
	private static final String OP_ERROR= "CProjectWizard.op_error"; //$NON-NLS-1$
	private static final String title= CUIPlugin.getResourceString(OP_ERROR + ".title"); //$NON-NLS-1$
	private static final String message= CUIPlugin.getResourceString(OP_ERROR + ".message"); //$NON-NLS-1$
	private static final String[] EMPTY_ARR = new String[0]; 
	
	protected IConfigurationElement fConfigElement;
	protected CDTMainWizardPage fMainPage;
	
	protected IProject newProject;
	private String wz_title;
	private String wz_desc;
	
	private boolean existingPath = false;
	private String lastProjectName = null;
	private IPath lastProjectLocation = null;
	private CWizardHandler savedHandler = null;

	protected List localPages = new ArrayList(); // replacing Wizard.pages since we have to delete them
	
	public CDTCommonProjectWizard() {
		this(UIMessages.getString("NewModelProjectWizard.0"),UIMessages.getString("NewModelProjectWizard.1")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public CDTCommonProjectWizard(String title, String desc) {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		wz_title = title;
		wz_desc = desc;
	}
	
	public void addPages() {
		fMainPage= new CDTMainWizardPage(CUIPlugin.getResourceString(PREFIX));
		fMainPage.setTitle(wz_title);
		fMainPage.setDescription(wz_desc);
		addPage(fMainPage);
	}

	/**
	 * @return true if user has changed settings since project creation
	 */
	private boolean isChanged() {
		if (savedHandler != fMainPage.h_selected)
			return true;

		if (!fMainPage.getProjectName().equals(lastProjectName))
			return true;
			
		IPath projectLocation = fMainPage.getProjectLocation();
		if (projectLocation == null) {
			if (lastProjectLocation != null)
				return true;
		} else if (!projectLocation.equals(lastProjectLocation))
			return true;
		
		return savedHandler.isChanged(); 
	}

	public IProject getProject(boolean defaults) {
		return getProject(defaults, true);
	}

	public IProject getProject(boolean defaults, boolean onFinish) {
		if (newProject != null && isChanged()) 
			clearProject(); 
		if (newProject == null)	{
            existingPath = false;
			IPath p = fMainPage.getProjectLocation();
		  	if (p == null) { 
		  		p = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			    p = p.append(fMainPage.getProjectName());
		  	}
        	File f = p.toFile();
        	if (f.exists() && f.isDirectory()) {
                if (p.append(".project").toFile().exists()) { //$NON-NLS-1$
                	if (!
                		MessageDialog.openConfirm(getShell(), 
        				UIMessages.getString("CDTCommonProjectWizard.0"),  //$NON-NLS-1$
						UIMessages.getString("CDTCommonProjectWizard.1")) //$NON-NLS-1$
						)
                		return null;
                }
                existingPath = true;
        	} 
			savedHandler = fMainPage.h_selected;
			savedHandler.saveState();
			lastProjectName = fMainPage.getProjectName();
			lastProjectLocation = fMainPage.getProjectLocation();
			// start creation process
			invokeRunnable(getRunnable(defaults, onFinish)); 
		} 
		return newProject;
	}

	/**
	 * Remove created project either after error
	 * or if user returned back from config page. 
	 */
	private void clearProject() {
		if (lastProjectName == null) return;
		try {
			ResourcesPlugin.getWorkspace().getRoot().getProject(lastProjectName).delete(!existingPath, true, null);
		} catch (CoreException ignore) {}
		newProject = null;
		lastProjectName = null;
		lastProjectLocation = null;
	}
	
	private boolean invokeRunnable(IRunnableWithProgress runnable) {
		IRunnableWithProgress op= new WorkspaceModifyDelegatingOperation(runnable);
		try {
			getContainer().run(true, true, op);
		} catch (InvocationTargetException e) {
			CUIPlugin.errorDialog(getShell(), title, message, e.getTargetException(), false);
			clearProject();
			return false;
		} catch  (InterruptedException e) {
			clearProject();
			return false;
		}
		return true;
	}

	public boolean performFinish() {
		// create project if it is not created yet
		if (getProject(fMainPage.isCurrent(), true) == null) return false;
		fMainPage.h_selected.postProcess(newProject);
		try {
			setCreated();
		} catch (CoreException e) {
			// TODO log or display a message
			e.printStackTrace();
			return false;
		}
		BasicNewProjectResourceWizard.updatePerspective(fConfigElement);
		selectAndReveal(newProject);
		return true;
	}
	
	protected boolean setCreated() throws CoreException {
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		ICProjectDescription des = mngr.getProjectDescription(newProject, false);
		if(des.isCdtProjectCreating()){
			des = mngr.getProjectDescription(newProject, true);
			des.setCdtProjectCreated();
			mngr.setProjectDescription(newProject, des, false, null);
			return true;
		}
		return false;
	}
	
    public boolean performCancel() {
    	clearProject();
        return true;
    }

	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		fConfigElement= config;
	}

	private IRunnableWithProgress getRunnable(boolean _defaults, final boolean onFinish) {
		final boolean defaults = _defaults;
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor imonitor) throws InvocationTargetException, InterruptedException {
				getShell().getDisplay().syncExec(new Runnable() {
					public void run() { 
						try {
							newProject = createIProject(lastProjectName, lastProjectLocation);
							if (newProject != null) 
								fMainPage.h_selected.createProject(newProject, defaults, onFinish);
						} catch (CoreException e) {	CUIPlugin.getDefault().log(e); }
					}
				});
			}
		};
	}

	/**
	 * 
	 */	
	public IProject createIProject(final String name, final IPath location) throws CoreException{
		if (newProject != null)	return newProject;
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		final IProject newProjectHandle = root.getProject(name);
		
		if (!newProjectHandle.exists()) {
//			IWorkspaceDescription workspaceDesc = workspace.getDescription();
//			workspaceDesc.setAutoBuilding(false);
//			workspace.setDescription(workspaceDesc);
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
	public abstract String[] getNatures();
	
	public void dispose() {
		fMainPage.dispose();
	}
	
    public boolean canFinish() {
    	if (fMainPage.h_selected != null) {
    		if(!fMainPage.h_selected.canFinich())
    			return false;
    		String s = fMainPage.h_selected.getErrorMessage();
    		if (s != null) return false;
    	}
    	return super.canFinish();
    }
    /**
     * Returns last project name used for creation
     */
	public String getLastProjectName() {
		return lastProjectName;
	}

	public IPath getLastProjectLocation() {
		return lastProjectLocation;
	}
	
	// Methods below should provide data for language check
	public String[] getLanguageIDs (){
		return EMPTY_ARR;
	}
	public String[] getContentTypeIDs (){
		return EMPTY_ARR;
	}
	public String[] getExtensions (){
		return EMPTY_ARR;
	}
	
}
