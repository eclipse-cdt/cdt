/*******************************************************************************
 * Copyright (c) 2006, 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildStep;
import org.eclipse.cdt.managedbuilder.buildmodel.IStepVisitor;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
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
	private static final String BUILDER_MSG_HEADER = "InternalBuilder.msg.header"; //$NON-NLS-1$
	private static final String BUILDER_NOTHING_TODO = "InternalBuilder.nothing.todo"; //$NON-NLS-1$
	private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$


	private IBuildDescription fDes;
	private IPath fCWD;
	private boolean fBuildIncrementaly;
	private boolean fResumeOnErrs;
	private Map<IBuildStep, StepBuilder> fStepToStepBuilderMap = new HashMap<IBuildStep, StepBuilder>();
	private int fNumCommands = -1;
	private GenDirInfo fDir;
	private IResourceRebuildStateContainer fRebuildStateContainer;

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
		@Override
		public int visit(IBuildStep action) throws CoreException {
			if(fMonitor.isCanceled())
				return VISIT_STOP;

			if(DbgUtil.DEBUG)
				DbgUtil.trace("visiting step " + DbgUtil.stepName(action)); //$NON-NLS-1$
			if(!action.isRemoved()
					&& (!fBuildIncrementaly || action.needsRebuild())){
				if(DbgUtil.DEBUG)
					DbgUtil.trace("step " + DbgUtil.stepName(action) + " needs rebuild" ); //$NON-NLS-1$ //$NON-NLS-2$
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

	public DescriptionBuilder(IBuildDescription des, IResourceRebuildStateContainer rs){
		this(des, true, rs);
	}

	public DescriptionBuilder(IBuildDescription des, boolean buildIncrementaly, IResourceRebuildStateContainer rs){
		this(des, buildIncrementaly, true, rs);
	}

	public DescriptionBuilder(IBuildDescription des, boolean buildIncrementaly, boolean resumeOnError, IResourceRebuildStateContainer rs){
		this(des, buildIncrementaly, resumeOnError, null, rs);
	}

	public DescriptionBuilder(IBuildDescription des, boolean buildIncrementaly, boolean resumeOnErrs, IPath cwd, IResourceRebuildStateContainer rs){
		fDes = des;
		fCWD = cwd;
		fBuildIncrementaly = buildIncrementaly;
		fResumeOnErrs = resumeOnErrs;
		fDir = new GenDirInfo(fDes.getConfiguration());

		if(fCWD == null)
			fCWD = fDes.getDefaultBuildDirLocation();

		fRebuildStateContainer = rs;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.internal.builddescription.IBuildDescriptionBuilder#build(java.io.OutputStream, java.io.OutputStream, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public int build(OutputStream out, OutputStream err,
			IProgressMonitor monitor){

		initRebuildStates();
		int num = getNumCommands();
		int status = STATUS_OK;

		//TODO: should we specify some task name here?
		monitor.beginTask("", num > 0 ? num : 1);	//$NON-NLS-1$
		monitor.subTask("");	//$NON-NLS-1$

		if(num > 0){
			BuildStepVisitor visitor = new BuildStepVisitor(out, err, monitor);
			try {
				BuildDescriptionManager.accept(visitor,
						fDes, true);
			} catch (CoreException e) {
				status = STATUS_ERROR_LAUNCH;
			}

			if(status == STATUS_OK)
				status = visitor.fStatus;
		} else {
			printMessage(
					ManagedMakeMessages.getFormattedString(BUILDER_NOTHING_TODO,
							fDes.getConfiguration().getOwner().getName()),
					out);
		}

		monitor.done();

		if(status == STATUS_OK)
			clearRebuildStates();

		return status;
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
		StepBuilder b = fStepToStepBuilderMap.get(step);
		if(b == null){
			b = new StepBuilder(step, fCWD, fResumeOnErrs, fDir, fRebuildStateContainer);
			fStepToStepBuilderMap.put(step, b);
		}
		return b;
	}

	protected void printMessage(String msg, OutputStream os){
		if (os != null) {
			msg = ManagedMakeMessages.getFormattedString(BUILDER_MSG_HEADER, msg) + LINE_SEPARATOR;
			try {
				os.write(msg.getBytes());
				os.flush();
			} catch (IOException e) {
				// ignore;
			}
		}
	}

	private void initRebuildStates(){
		if(fRebuildStateContainer == null)
			return;

		fRebuildStateContainer.setState(0);

		IBuildResource[] rcs = fDes.getResources();
		putAll(fRebuildStateContainer, rcs, IRebuildState.NEED_REBUILD, true);
	}

	private void clearRebuildStates(){
		if(fRebuildStateContainer == null)
			return;

		fRebuildStateContainer.setState(0);

//		IBuildResource[] rcs = fDes.getResources();
//		putAll(fRebuildStateContainer, rcs, IRebuildState.NEED_REBUILD, true);
	}

	static void putAll(IResourceRebuildStateContainer cbs, IBuildResource[] rcs, int state, boolean rebuildRcOnly){
		for(int i = 0; i < rcs.length; i++){
			IBuildResource rc = rcs[i];
			if(rebuildRcOnly && !rc.needsRebuild())
				continue;

			if(!rc.isProjectResource())
				continue;
			IPath fullPath = rc.getFullPath();
			if(fullPath == null)
				continue;

			cbs.setStateForFullPath(fullPath, state);
		}
	}

}
