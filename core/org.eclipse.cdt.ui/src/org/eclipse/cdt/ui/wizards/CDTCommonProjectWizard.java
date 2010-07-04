/*******************************************************************************
 * Copyright (c) 2002, 2009 Rational Software Corporation and others.
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


import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;

public abstract class CDTCommonProjectWizard extends BasicNewResourceWizard 
implements IExecutableExtension, IWizardWithMemory, ICDTCommonProjectWizard
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
	private URI lastProjectLocation = null;
	private CWizardHandler savedHandler = null;

	public CDTCommonProjectWizard() {
		this(Messages.NewModelProjectWizard_0,Messages.NewModelProjectWizard_1); 
	}

	public CDTCommonProjectWizard(String title, String desc) {
		super();
		setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		setWindowTitle(title);
		wz_title = title;
		wz_desc = desc;
	}
	
	@Override
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
			
		URI projectLocation = fMainPage.getProjectLocation();
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
		  	try {
		  		IFileStore fs;
				URI p = fMainPage.getProjectLocation();
			  	if (p == null) { 
			  		fs = EFS.getStore(ResourcesPlugin.getWorkspace().getRoot().getLocationURI());
				    fs = fs.getChild(fMainPage.getProjectName());
			  	} else
			  		fs = EFS.getStore(p);
		  		IFileInfo f = fs.fetchInfo();
		  		if (f.exists() && f.isDirectory()) {
		  			if (fs.getChild(".project").fetchInfo().exists()) { //$NON-NLS-1$
						if (!MessageDialog.openConfirm(getShell(), Messages.CDTCommonProjectWizard_0, Messages.CDTCommonProjectWizard_1))
	                		return null;
	                }
	                existingPath = true;
		  		}
        	} catch (CoreException e) {
        		CUIPlugin.log(e.getStatus());
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
			CUIPlugin.errorDialog(getShell(), title, message, e.getTargetException(), true);
			clearProject();
			return false;
		} catch  (InterruptedException e) {
			clearProject();
			return false;
		}
		return true;
	}

	@Override
	public boolean performFinish() {
		boolean needsPost = (newProject != null && !isChanged());
		// create project if it is not created yet
		if (getProject(fMainPage.isCurrent(), true) == null) 
			return false;
		fMainPage.h_selected.postProcess(newProject, needsPost);
		try {
			setCreated();
		} catch (CoreException e) {
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
		
		if(des == null ) {
			return false;
		}
		
		if(des.isCdtProjectCreating()){
			des = mngr.getProjectDescription(newProject, true);
			des.setCdtProjectCreated();
			mngr.setProjectDescription(newProject, des, false, null);
			return true;
		}
		return false;
	}
	
    @Override
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
				final Exception except[] = new Exception[1];
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
								fMonitor.beginTask(CUIPlugin.getResourceString("CProjectWizard.op_description"), 100); //$NON-NLS-1$
								fMonitor.worked(10);
								try {							
									newProject = createIProject(lastProjectName, lastProjectLocation, new SubProgressMonitor(fMonitor, 40));
									if (newProject != null) 
										fMainPage.h_selected.createProject(newProject, defaults, onFinish, new SubProgressMonitor(fMonitor, 40));
									fMonitor.worked(10);
								} catch (CoreException e) {	CUIPlugin.log(e); }
								finally {
									fMonitor.done();
								}
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
		};
	}
	
	public IProject createIProject(final String name, final URI location) throws CoreException{
		return createIProject(name, location, new NullProgressMonitor());
	}
	
	/**
	 * @since 5.1
	 */
	protected IProgressMonitor continueCreationMonitor;

	/**
	 * @param monitor 
	 * @since 5.1
	 * 
	 */	
	public IProject createIProject(final String name, final URI location, IProgressMonitor monitor) throws CoreException{
		
		monitor.beginTask(Messages.CDTCommonProjectWizard_creatingProject, 100); 
		
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
				description.setLocationURI(location);
			newProject = CCorePlugin.getDefault().createCDTProject(description, newProjectHandle, new SubProgressMonitor(monitor,25));
		} else {
			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					newProjectHandle.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				}
			};
			workspace.run(runnable, root, IWorkspace.AVOID_UPDATE, new SubProgressMonitor(monitor,25));
			newProject = newProjectHandle;
		}
        
		// Open the project if we have to
		if (!newProject.isOpen()) {
			newProject.open(new SubProgressMonitor(monitor,25));
		}
		
		continueCreationMonitor = new SubProgressMonitor(monitor,25);
		IProject proj = continueCreation(newProject);
		
		monitor.done();
		
		return proj;	
	}
	
	protected abstract IProject continueCreation(IProject prj); 
	public abstract String[] getNatures();
	
	@Override
	public void dispose() {
		fMainPage.dispose();
	}
	
    @Override
	public boolean canFinish() {
    	if (fMainPage.h_selected != null) {
    		if(!fMainPage.h_selected.canFinish())
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

	public URI getLastProjectLocation() {
		return lastProjectLocation;
	}

	public IProject getLastProject() {
		return newProject;
	}

	// Methods below should provide data for language check
	public String[] getLanguageIDs (){
		String[] contentTypeIds = getContentTypeIDs();
		if(contentTypeIds.length > 0) {
			IContentTypeManager manager = Platform.getContentTypeManager();
			List<String> languageIDs = new ArrayList<String>();
			for(int i = 0; i < contentTypeIds.length; ++i) {
				IContentType contentType = manager.getContentType(contentTypeIds[i]);
				if(null != contentType) {
					ILanguage language = LanguageManager.getInstance().getLanguage(contentType);
					if(!languageIDs.contains(language.getId())) {
						languageIDs.add(language.getId());
					}
				}
			}
			return languageIDs.toArray(new String[languageIDs.size()]);
		}
		return EMPTY_ARR;
	}
	public String[] getContentTypeIDs (){
		return EMPTY_ARR;
	}
	public String[] getExtensions (){
		String[] contentTypeIds = getContentTypeIDs();
		if(contentTypeIds.length > 0) {
			IContentTypeManager manager = Platform.getContentTypeManager();
			List<String> extensions = new ArrayList<String>();
			for(int i = 0; i < contentTypeIds.length; ++i) {
				IContentType contentType = manager.getContentType(contentTypeIds[i]);
				if(null != contentType) {
					String[] thisTypeExtensions = contentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
					extensions.addAll(Arrays.asList(thisTypeExtensions));
				}
			}
			return extensions.toArray(new String[extensions.size()]);
		}
		return EMPTY_ARR;
	}
	
}
