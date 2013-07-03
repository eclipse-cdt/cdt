/*******************************************************************************
 * Copyright (c) 2010 CodeSourcery and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Dmitry Kozlov (CodeSourcery) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.buildconsole;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;

import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.CPluginImages;

/**
 * Run project build from Build Console action
 */
public class RunBuildAction extends Action {

	private static final String ID = "org.eclipse.cdt.ui.buildconsole.runbuild"; //$NON-NLS-1$
	
	private BuildConsolePage fConsolePage;
	private IProject fProject;
	private Job buildJob;

	public RunBuildAction(BuildConsolePage page) {
		super(ConsoleMessages.RunBuildAction_Tooltip); 
		fConsolePage = page;
		setToolTipText(ConsoleMessages.RunBuildAction_Tooltip); 
		CPluginImages.setImageDescriptors(this, CPluginImages.T_LCL, CPluginImages.IMG_RUN_BUILD);
	}

	/**
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		MakeCorePlugin makeCorePlugin = MakeCorePlugin.getDefault();
		if ( makeCorePlugin == null ) return;
		IMakeTargetManager targetManager = makeCorePlugin.getTargetManager();
		if ( targetManager == null ) return;
		if ( fProject == null ) return;
		String[] id = targetManager.getTargetBuilders(fProject);
		if (id.length == 0) {
			//throw new CoreException(new Status(IStatus.ERROR, MakeUIPlugin.getUniqueIdentifier(), -1,
			//		MakeUIPlugin.getResourceString("MakeTargetDialog.exception.noTargetBuilderOnProject"), null)); //$NON-NLS-1$
		}

		String targetBuildID = id[0];
		String targetBuilderID = targetManager.getBuilderID(targetBuildID);
		try {
			IMakeBuilderInfo buildInfo = MakeCorePlugin.createBuildInfo(fProject,targetBuilderID);
			IMakeTarget target = targetManager.createTarget(fProject, "all", targetBuildID);
			target.setUseDefaultBuildCmd(false);
			target.setBuildAttribute(IMakeTarget.BUILD_COMMAND, target.getBuildCommand().toString());
			String args = target.getBuildArguments();			
			args += fConsolePage.fBuildArgsCI.getArgs();
			target.setBuildAttribute(IMakeTarget.BUILD_ARGUMENTS, args);
			fConsolePage.fStopBuildAction.setEnabled(true);
			buildJob = TargetBuild.buildTargets(fConsolePage.getViewer().getControl().getShell(), new IMakeTarget[] { target });
			buildJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					synchronized (fConsolePage.fStopBuildAction) {
						fConsolePage.fStopBuildAction.setEnabled(false);
	                    event.getJob().removeJobChangeListener(this);
					}
				}});				
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}

	public void setProject(IProject project) {
		fProject = project;
		
	}

	public void cancelBuildJob() {
		try {
			if ( buildJob != null ) {
				buildJob.cancel();
			}
		} catch (Exception e) {
			CUIPlugin.log(e);
		}
	}
	
	@Override
	public String getId() {
		return ID;
	}

}
