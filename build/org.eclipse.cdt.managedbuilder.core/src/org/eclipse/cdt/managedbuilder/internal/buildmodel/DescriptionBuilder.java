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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.buildmodel.IStepVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

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
	private Map fStepToStepBuilderMap = new HashMap();
	private int fNumCommands = -1;
	private GenDirInfo fDir;
	
	private class BuildStepVisitor implements IStepVisitor{
		private OutputStream fOut;
		private OutputStream fErr;
		private IProgressMonitor fMonitor;
		private int fStatus;
		private boolean fBuild;

		public BuildStepVisitor(OutputStream out, OutputStream err, IProgressMonitor monitor){
			this(out, err, monitor, true);
		}

		public BuildStepVisitor(OutputStream out, OutputStream err, IProgressMonitor monitor, boolean build){
			fOut = out;
			fErr = err;
			fMonitor = monitor;
			fStatus = STATUS_OK;
			fBuild = build;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.managedbuilder.builddescription.IStepVisitor#visit(org.eclipse.cdt.managedbuilder.builddescription.IBuildStep)
		 */
		public int visit(IBuildStep action) throws CoreException {
			if(fMonitor.isCanceled())
				return VISIT_STOP;
			
			if(DbgUtil.DEBUG)
				DbgUtil.trace("visiting step " + DbgUtil.stepName(action));
			if(!action.isRemoved()
					&& (!fBuildIncrementaly || action.needsRebuild())){
				if(DbgUtil.DEBUG)
					DbgUtil.trace("step " + DbgUtil.stepName(action) + " needs rebuild" );
				StepBuilder builder = getStepBuilder(action);//new StepBuilder(action, fCWD, fResumeOnErrs, fDir);
				
				if(fBuild){
					switch(builder.build(fOut, fErr, new SubProgressMonitor(fMonitor, builder.getNumCommands()))){
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
				} else {
					fNumCommands += builder.getNumCommands();
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
		fDir = new GenDirInfo(fDes.getConfiguration());
		
		if(fCWD == null)
			fCWD = fDes.getDefaultBuildDirLocation();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.builddescription.IBuildDescriptionBuilder#build(java.io.OutputStream, java.io.OutputStream, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public int build(OutputStream out, OutputStream err,
			IProgressMonitor monitor){
		
		//TODO: should we specify some task name here?
		monitor.beginTask("", getNumCommands());	//$NON-NLS-1$
		monitor.subTask("");	//$NON-NLS-1$

		BuildStepVisitor visitor = new BuildStepVisitor(out, err, monitor);
		try {
			BuildDescriptionManager.accept(visitor,
					fDes, true);
		} catch (CoreException e) {
			return STATUS_ERROR_LAUNCH;
		}
		
		monitor.done();
		
		return visitor.fStatus;
	}

	public int getNumCommands() {
		if(fNumCommands == -1){
			fNumCommands = 0;
			BuildStepVisitor visitor = new BuildStepVisitor(null, null, new NullProgressMonitor(), false);
			try {
				BuildDescriptionManager.accept(visitor,
						fDes, true);
			} catch (CoreException e) {
				//TODO: report an error
			}
			if(DbgUtil.DEBUG)
				DbgUtil.trace("Description Builder: total work = " + fNumCommands);	//$NON-NLS-1$
		}
		return fNumCommands;
	}
	
	protected StepBuilder getStepBuilder(IBuildStep step){
		StepBuilder b = (StepBuilder)fStepToStepBuilderMap.get(step);
		if(b == null){
			b = new StepBuilder(step, fCWD, fResumeOnErrs, fDir);
			fStepToStepBuilderMap.put(step, b);
		}
		return b;
	}
	
}
