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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

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
		int status = STATUS_OK;
		IBuildCommand cmds[] = fStep.getCommands(fCWD, null, null, true);
		if(cmds != null){
			createOutDirs(monitor);
			
			for(int i = 0; 
					i < cmds.length 
						&& status != STATUS_CANCELLED
						&& (fResumeOnErrs || status == STATUS_OK);
					i++){
				CommandBuilder builder = new CommandBuilder(cmds[i]);
				switch(builder.build(out, err, monitor)){
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
		}
		return postProcess(status);
	}
	
	protected int postProcess(int status){
		switch(status){
		case STATUS_OK:
			break;
		case STATUS_CANCELLED:
		case STATUS_ERROR_BUILD:
		case STATUS_ERROR_LAUNCH:
		default:
			cleanOutputs();
			break;
		}
		return status;
	}
	
	protected void cleanOutputs(){
		IBuildResource bRcs[] = fStep.getOutputResources();
		for(int i = 0; i < bRcs.length; i++){
			IResource rc = BuildDescriptionManager.findResourceForBuildResource(bRcs[i]);
			if(rc != null){
				try {
					rc.delete(true, null);
				} catch (CoreException e) {
					if(DbgUtil.DEBUG){
						DbgUtil.traceln("failed to delete resource " 
								+ rc.getFullPath() 
								+ ", error: " + e.getLocalizedMessage());
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
}
