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
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.buildmodel.IStepVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * 
 * This class implements the IBuildDescription building,
 * that is the build of the entire configuration/project 
 * To perform a build, create an instance of this class
 * and invoke the build method
 *
 * NOTE: This class is subject to change and discuss, 
 * and is currently available in experimental mode only
 *  
 */
public class DescriptionBuilder implements IBuildModelBuilder {
	private IBuildDescription fDes;
	private IPath fCWD;
	private boolean fBuildIncrementaly;
	private boolean fResumeOnErrs;
	
	private class BuildStepVisitor implements IStepVisitor{
		private OutputStream fOut;
		private OutputStream fErr;
		private IProgressMonitor fMonitor;
		private GenDirInfo fDir = new GenDirInfo(fDes.getConfiguration());
		private int fStatus;

		public BuildStepVisitor(OutputStream out, OutputStream err, IProgressMonitor monitor){
			fOut = out;
			fErr = err;
			fMonitor = monitor;
			fStatus = STATUS_OK;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.builddescription.IStepVisitor#visit(org.eclipse.cdt.managedbuilder.builddescription.IBuildStep)
		 */
		public int visit(IBuildStep action) throws CoreException {
			if(fMonitor.isCanceled())
				return VISIT_STOP;
			
			if(DbgUtil.DEBUG)
				DbgUtil.traceln("visiting step " + DbgUtil.stepName(action));
			if(!action.isRemoved()
					&& (!fBuildIncrementaly || action.needsRebuild())){
				if(DbgUtil.DEBUG)
					DbgUtil.traceln("step " + DbgUtil.stepName(action) + " needs rebuild" );
				StepBuilder builder = new StepBuilder(action, fCWD, fResumeOnErrs, fDir);
				
				switch(builder.build(fOut, fErr, fMonitor)){
				case STATUS_OK:
					break;
				case STATUS_CANCELLED:
					fStatus = STATUS_CANCELLED;
					break;
				case STATUS_ERROR_BUILD:
				case STATUS_ERROR_LAUNCH:
				default:
					fStatus = STATUS_ERROR_BUILD; 
				break;
				}
			}
			
			if(fStatus != STATUS_CANCELLED 
				&& (fResumeOnErrs || fStatus == STATUS_OK))
				return VISIT_CONTINUE;
			return VISIT_STOP;
		}
		
	}

	public DescriptionBuilder(IBuildDescription des){
		this(des, true);
	}

	public DescriptionBuilder(IBuildDescription des, boolean buildIncrementaly){
		this(des, buildIncrementaly, true);
	}

	public DescriptionBuilder(IBuildDescription des, boolean buildIncrementaly, boolean resumeOnError){
		this(des, buildIncrementaly, resumeOnError, null);
	}

	public DescriptionBuilder(IBuildDescription des, boolean buildIncrementaly, boolean resumeOnErrs, IPath cwd){
		fDes = des;
		fCWD = cwd;
		fBuildIncrementaly = buildIncrementaly;
		fResumeOnErrs = resumeOnErrs;
		
		if(fCWD == null)
			fCWD = fDes.getDefaultBuildDirLocation();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.builddescription.IBuildDescriptionBuilder#build(java.io.OutputStream, java.io.OutputStream, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int build(OutputStream out, OutputStream err,
			IProgressMonitor monitor){
		
		BuildStepVisitor visitor = new BuildStepVisitor(out, err, monitor);
		try {
			BuildDescriptionManager.accept(visitor,
					fDes, true);
		} catch (CoreException e) {
			return STATUS_ERROR_LAUNCH;
		}
		return visitor.fStatus;
	}
	
}
