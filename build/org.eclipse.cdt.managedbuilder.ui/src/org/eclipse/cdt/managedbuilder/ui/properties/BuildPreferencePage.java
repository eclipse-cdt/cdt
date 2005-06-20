/*******************************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuildOptionBlock;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;

public class BuildPreferencePage extends PreferencePage 
				implements IWorkbenchPreferencePage, ICOptionContainer{

	/*
	 * String constants
	 */
	private static final String PREFIX = "BuildPreferencePage";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String SETTINGS_LABEL = LABEL + ".Settings";	//$NON-NLS-1$
	private static final String REBUILD_JOB_NAME = PREFIX + ".job.rebuild";	//$NON-NLS-1$
	private static final String APPLY_INTARNAL_ERROR = PREFIX + ".apply.internal.error";	//$NON-NLS-1$

	
	/*
	 * Bookeeping variables
	 */
	protected ManagedBuildOptionBlock fOptionBlock;
	private boolean fRebuildNeeded = false; 

	public BuildPreferencePage(){
		fOptionBlock = new ManagedBuildOptionBlock(this);
	}
	
	/* 
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init()
	 */
	public void init(IWorkbench workbench){
		
	}
	
	/*
	 * specifies whether the rebuild of all managed projects is needed
	 * @see setRebuildState()
	 */
	protected boolean rebuildNeeded(){
		return fRebuildNeeded;
	}
	
	/*
	 * sets the rebuild state
	 * When the build settings apply operation is performed
	 * the rebuild will not be initiated, but the rebuild state will be set to true.
	 * The rebuild will be initiated only when closing the preference page. 
	 * In case the rebuild state is "true", the rebuild of all managed projects
	 * will be initiated both for OK or Cancel operations
	 */
	protected void setRebuildState(boolean rebuild){
		fRebuildNeeded = rebuild;
	}

	/* 
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents()
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		fOptionBlock.createContents(composite,ResourcesPlugin.getWorkspace());
		
		return composite;
	}

	/* 
	 *  (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#updateContainer()
	 */
	public void updateContainer(){
		fOptionBlock.update();
		setValid(fOptionBlock.isValid());
		setErrorMessage(fOptionBlock.getErrorMessage());
	}

	public IProject getProject(){
		return null;
	}

	public Preferences getPreferences(){
		return null;
	}

	/* 
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	protected void performApply() {
		applySettings();
    }
	
    protected void performDefaults() {
		fOptionBlock.performDefaults();
        super.performDefaults();
    }

	public ManagedBuildOptionBlock getOptionBlock(){
		return fOptionBlock;
	}

	
	/*
	 * apply settings
	 * when the Apply operation is performed, the user-modified settings are saved,
	 * but the rebuild of all managed projects is not initiated.
	 * The rebuild state is set to true instead.
	 * Rebuild will initiated when closing the preference page
	 */
	protected boolean applySettings(){
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				if(fOptionBlock.isDirty()){
					fOptionBlock.performApply(monitor);
					fOptionBlock.setDirty(false);
					// specify that the managed projects rebuild is needed
					// the rebuild will be initiated when closing the preference page
					// both in the case of OK and Cancel
					setRebuildState(true);
				}
			}
		};

		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);

		try {
			new ProgressMonitorDialog(getShell()).run(false, true, op);
		} catch (InvocationTargetException e) {
			Throwable e1 = e.getTargetException();
			ManagedBuilderUIPlugin.errorDialog(getShell(), ManagedBuilderUIMessages.getResourceString(APPLY_INTARNAL_ERROR),e1.toString(), e1); //$NON-NLS-1$
			return false;
		} catch (InterruptedException e) {
			// cancelled
			return false;
		}
		return true;
	}
	
	/* 
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performCancel()
	 */
    public boolean performCancel() {
		//the rebuild is needed in case the apply button was previousely pressed 
		if(rebuildNeeded()){
			initiateRebuild();
		}
        return true;
    }

	/* 
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {

		if(!applySettings())
			return false;

		if(rebuildNeeded())
			initiateRebuild();

		return true;
    }
	
	/*
	 * initiate the rebuild for all managed projects
	 */
	private void initiateRebuild(){
		setRebuildState(false);
		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		IProject projects[] = wsp.getRoot().getProjects();
		List managedProjectList = new ArrayList();
		for(int i = 0; i < projects.length; i++){
			if(ManagedBuildManager.manages(projects[i])){
				managedProjectList.add(projects[i]);
			}
		}
		projects = (IProject[])managedProjectList.toArray(new IProject[managedProjectList.size()]);
		final IProject projectsToBuild[] = wsp.computeProjectOrder(projects).projects;

		WorkspaceJob rebuildJob = new WorkspaceJob(ManagedBuilderUIMessages.getResourceString(REBUILD_JOB_NAME)) {
    		public boolean belongsTo(Object family) {
    			return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
    		}
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				
                for(int i = 0; i < projectsToBuild.length; i++){
                    if(ManagedBuildManager.manages(projectsToBuild [i])){
                          IManagedBuildInfo bi = ManagedBuildManager.getBuildInfo(projectsToBuild [i]);
                          if (bi != null & bi instanceof ManagedBuildInfo) {
                                ((ManagedBuildInfo)bi).initializePathEntries();
                          }
                    }
				}
				
				for(int i = 0; i < projectsToBuild.length; i++){
					try{
						projectsToBuild[i].build(IncrementalProjectBuilder.FULL_BUILD,monitor);
					}catch(CoreException e){
						//TODO:
					}catch(OperationCanceledException e){
						throw e;
					}
				}
                return Status.OK_STATUS;
    		}
    	};
		rebuildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory()
                .buildRule());
		rebuildJob.setUser(true);
		rebuildJob.schedule();
		
	}
	
	/**
	 * @see DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		fOptionBlock.setVisible(visible);
	}

}
