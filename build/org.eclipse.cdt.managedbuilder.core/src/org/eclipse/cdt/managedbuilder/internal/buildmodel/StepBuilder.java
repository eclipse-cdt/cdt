/*******************************************************************************
 * Copyright (c) 2006 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.io.OutputStream;

import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildCommand;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * 
 * This class implements the IBuildStep building
 * To build the step, create an instance of this class
 * and invoke the build method
 *
 * NOTE: This class is subject to change and discuss, 
 * and is currently available in experimental mode only
 *  
 */
public class StepBuilder implements IBuildModelBuilder {
	private IBuildStep fStep;
	private IPath fCWD;
	private GenDirInfo fDirs;
	private boolean fResumeOnErrs;
	private int fNumCommands = -1;
	private CommandBuilder fCommandBuilders[];

	public StepBuilder(IBuildStep step){
		this(step, null);
	}

	public StepBuilder(IBuildStep step, IPath cwd){
		this(step, cwd, true, null);
	}

	public StepBuilder(IBuildStep step, IPath cwd, boolean resumeOnErrs, GenDirInfo dirs){
		fStep = step;
		fCWD = cwd;
		fDirs = dirs;
		fResumeOnErrs = resumeOnErrs;
		
		if(fDirs == null)
			fDirs = new GenDirInfo(fStep.getBuildDescription().getConfiguration());
		
		if(fCWD == null)
			fCWD = fStep.getBuildDescription().getDefaultBuildDirLocation();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.builddescription.IBuildDescriptionBuilder#build(java.io.OutputStream, java.io.OutputStream, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int build(OutputStream out, OutputStream err,
			IProgressMonitor monitor){

		monitor.beginTask("", getNumCommands());	//$NON-NLS-1$
		monitor.subTask("");	//$NON-NLS-1$

		int status = STATUS_OK;
		CommandBuilder bs[] = getCommandBuilders();
		if(bs.length > 0){
			//TODO: monitor
			createOutDirs(new NullProgressMonitor());
			
			for(int i = 0; 
					i < bs.length 
						&& status != STATUS_CANCELLED
						&& (fResumeOnErrs || status == STATUS_OK);
					i++){
				CommandBuilder builder = bs[i];
				switch(builder.build(out, err, new SubProgressMonitor(monitor, builder.getNumCommands()))){
				case STATUS_OK:
					break;
				case STATUS_CANCELLED:
					status = STATUS_CANCELLED;
				case STATUS_ERROR_BUILD:
					if(status != STATUS_ERROR_LAUNCH)
						status = STATUS_ERROR_BUILD;
					break;
				case STATUS_ERROR_LAUNCH:
				default:
					status = STATUS_ERROR_LAUNCH;
					break;
				}
			}
			//TODO: monitor
			status = postProcess(status, new NullProgressMonitor());
		}
		monitor.done();
		return status;
	}
	
	protected int postProcess(int status, IProgressMonitor monitor){
		if(status != STATUS_ERROR_LAUNCH){
			refreshOutputs(monitor);
		}
		switch(status){
		case STATUS_OK:
			break;
		case STATUS_CANCELLED:
		case STATUS_ERROR_BUILD:
		case STATUS_ERROR_LAUNCH:
		default:
			cleanOutputs(monitor);
			break;
		}
		return status;
	}
	
	protected void refreshOutputs(IProgressMonitor monitor){
		if(fStep == fStep.getBuildDescription().getInputStep())
			return;
		
		IBuildResource rcs[] = fStep.getOutputResources();
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for(int i = 0; i < rcs.length; i++){
			IPath path = rcs[i].getFullPath();
			if(path != null){
				IFile file = root.getFile(path);
				try {
					file.refreshLocal(IResource.DEPTH_ZERO, monitor);
				} catch (CoreException e) {
					if(DbgUtil.DEBUG){
						DbgUtil.trace("failed to refresh resource " 	//$NON-NLS-1$
								+ file.getFullPath() 
								+ ", error: " + e.getLocalizedMessage());	//$NON-NLS-1$
					}
				}
			}
		}
	}
	
	protected void cleanOutputs(IProgressMonitor monitor){
		if(fStep == fStep.getBuildDescription().getInputStep())
			return;
		
		IBuildResource bRcs[] = fStep.getOutputResources();
		for(int i = 0; i < bRcs.length; i++){
			IResource rc = BuildDescriptionManager.findResourceForBuildResource(bRcs[i]);
			if(rc != null){
				try {
					rc.delete(true, monitor);
				} catch (CoreException e) {
					if(DbgUtil.DEBUG){
						DbgUtil.trace("failed to delete resource " 	//$NON-NLS-1$
								+ rc.getFullPath() 
								+ ", error: " + e.getLocalizedMessage());	//$NON-NLS-1$
					}
				}
			}
		}
	}
	
	protected void createOutDirs(IProgressMonitor monitor){
		IBuildResource rcs[] = fStep.getOutputResources();
		
		for(int i = 0; i < rcs.length; i++){
			fDirs.createDir(rcs[i], monitor);
		}
	}

	public int getNumCommands() {
		if(fNumCommands == -1){
			CommandBuilder bs[] = getCommandBuilders();
			fNumCommands = 0;
			for(int i = 0; i < bs.length; i++){
				fNumCommands += bs[i].getNumCommands();
			}
		}
		return fNumCommands;
	}
	
	protected CommandBuilder[] getCommandBuilders(){
		if(fCommandBuilders == null){
			IBuildCommand cmds[] = fStep.getCommands(fCWD, null, null, true);
			if(cmds == null)
				fCommandBuilders = new CommandBuilder[0];
			else {
				fCommandBuilders = new CommandBuilder[cmds.length];
				for(int i = 0; i < cmds.length; i++){
					fCommandBuilders[i] = new CommandBuilder(cmds[i]);
				}
			}
		}
		return fCommandBuilders;
	}
}
