/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

public class MakeBuilder extends ACBuilder {

	private static final String BUILD_ERROR = "MakeBuilder.buildError"; //$NON-NLS-1$

	public final static String BUILDER_ID = MakeCorePlugin.getUniqueIdentifier() + ".makeBuilder"; //$NON-NLS-1$

	public MakeBuilder() {
	}

	public class MyResourceDeltaVisitor implements IResourceDeltaVisitor {
		boolean bContinue;

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (resource != null && resource.getProject() == getProject()) {
				bContinue = true;
				return false;
			}
			return true;
		}
		public boolean shouldBuild() {
			return bContinue;
		}
	}
	/**
	 * @see IncrementalProjectBuilder#build
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		boolean bPerformBuild = true;
		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(args, MakeBuilder.BUILDER_ID);
		if (!shouldBuild(kind, info)) {
			return new IProject[0];
		}
		if (kind == IncrementalProjectBuilder.AUTO_BUILD) {
			MyResourceDeltaVisitor vis = new MyResourceDeltaVisitor();
			IResourceDelta delta = getDelta(getProject());
			if (delta != null) {
				delta.accept(vis);
				bPerformBuild = vis.shouldBuild();
			} else
				bPerformBuild = false;
		}
		if (bPerformBuild) {
			boolean isClean = invokeMake(kind, info, monitor);
			if (isClean) {
				forgetLastBuiltState();
			}
		}
		checkCancel(monitor);
		return getProject().getReferencedProjects();
	}

	private boolean invokeMake(int kind, IMakeBuilderInfo info, IProgressMonitor monitor) {
		boolean isClean = false;
		IProject currProject = getProject();

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(MakeCorePlugin.getResourceString("MakeBuilder.Invoking_Make_Builder") + currProject.getName(), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

		try {
			IPath buildCommand = info.getBuildCommand();
			if (buildCommand != null) {
				IConsole console = CCorePlugin.getDefault().getConsole();
				console.start(currProject);

				ConsoleOutputStream cos = console.getOutputStream();

				// remove all markers for this project
				removeAllMarkers(currProject);

				IPath workingDirectory = info.getBuildLocation();
				if (workingDirectory.isEmpty()) {
					workingDirectory = currProject.getLocation();
				}
				String[] targets = getTargets(kind, info);
				if (targets.length != 0 && targets[targets.length - 1].equals("clean")) //$NON-NLS-1$
					isClean = true;

				// Before launching give visual cues via the monitor
				monitor.subTask(MakeCorePlugin.getResourceString("MakeBuilder.Invoking_Command") + buildCommand.toString()); //$NON-NLS-1$

				String errMsg = null;
				CommandLauncher launcher = new CommandLauncher();
				// Print the command for visual interaction.
				launcher.showCommand(true);

				// Set the environmennt, some scripts may need the CWD var to be set.
				Properties props = launcher.getEnvironment();
				props.put("CWD", workingDirectory.toOSString()); //$NON-NLS-1$
				props.put("PWD", workingDirectory.toOSString()); //$NON-NLS-1$
				String[] env = null;
				ArrayList envList = new ArrayList();
				Enumeration names = props.propertyNames();
				if (names != null) {
					while (names.hasMoreElements()) {
						String key = (String) names.nextElement();
						envList.add(key + "=" + props.getProperty(key)); //$NON-NLS-1$
					}
					env = (String[]) envList.toArray(new String[envList.size()]);
				}
				ErrorParserManager epm = new ErrorParserManager(getProject(), this, info.getErrorParsers());
				epm.setOutputStream(cos);
				OutputStream stdout = epm.getOutputStream();
				OutputStream stderr = epm.getOutputStream();

				String[] buildArguments = targets;
				if (info.isDefaultBuildCmd()) {
					if ( !info.isStopOnError()) {
						buildArguments = new String[targets.length + 1];
						buildArguments[0] = "-k"; //$NON-NLS-1$
						System.arraycopy(targets, 0, buildArguments, 1, targets.length);
					}
				} else {
					String args = info.getBuildArguments();
					if ( args != null && !args.equals("")) { //$NON-NLS-1$
						String[] newArgs = makeArray(args);						
						buildArguments = new String[targets.length + newArgs.length];
						System.arraycopy(newArgs, 0, buildArguments, 0, newArgs.length);
						System.arraycopy(targets, 0, buildArguments, newArgs.length, targets.length);
					}
				}
				Process p = launcher.execute(buildCommand, buildArguments, env, workingDirectory);
				if (p != null) {
					try {
						// Close the input of the Process explicitely.
						// We will never write to it.
						p.getOutputStream().close();
					} catch (IOException e) {
					}
					if (launcher.waitAndRead(stdout, stderr, new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN)) != CommandLauncher.OK)
						errMsg = launcher.getErrorMessage();

					monitor.subTask(MakeCorePlugin.getResourceString("MakeBuilder.Updating_project")); //$NON-NLS-1$

					try {
						// Do not allow the cancel of the refresh, since the builder is external
						// to Eclipse, files may have been created/modified and we will be out-of-sync.
						// The caveat is for hugue projects, it may take sometimes at every build.
						currProject.refreshLocal(IResource.DEPTH_INFINITE, null);
					} catch (CoreException e) {
					}
				} else {
					errMsg = launcher.getErrorMessage();
				}

				if (errMsg != null) {
					StringBuffer buf = new StringBuffer(buildCommand.toString() + " "); //$NON-NLS-1$
					for (int i = 0; i < buildArguments.length; i++) {
						buf.append(buildArguments[i]);
						buf.append(' ');
					}

					String errorDesc = MakeCorePlugin.getFormattedString(BUILD_ERROR, buf.toString());
					buf = new StringBuffer(errorDesc);
					buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append("(").append(errMsg).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
					cos.write(buf.toString().getBytes());
					cos.flush();
				}

				stdout.close();
				stderr.close();

				monitor.subTask(MakeCorePlugin.getResourceString("MakeBuilder.Creating_Markers")); //$NON-NLS-1$
				epm.reportProblems();
			}
		} catch (Exception e) {
			CCorePlugin.log(e);
		} finally {
			monitor.done();
		}
		return (isClean);
	}

	/**
	 * Check whether the build has been canceled.
	 */
	public void checkCancel(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled())
			throw new OperationCanceledException();
	}

	protected boolean shouldBuild(int kind, IMakeBuilderInfo info) {
		switch (kind) {
			case IncrementalProjectBuilder.AUTO_BUILD :
				return info.isAutoBuildEnable();
			case IncrementalProjectBuilder.INCREMENTAL_BUILD :
				return info.isIncrementalBuildEnabled();
			case IncrementalProjectBuilder.FULL_BUILD :
				return info.isFullBuildEnabled();
		}
		return true;
	}

	protected String[] getTargets(int kind, IMakeBuilderInfo info) {
		String targets = ""; //$NON-NLS-1$
		switch (kind) {
			case IncrementalProjectBuilder.AUTO_BUILD :
				targets = info.getAutoBuildTarget();
				break;
			case IncrementalProjectBuilder.INCREMENTAL_BUILD :
				targets = info.getIncrementalBuildTarget();
				break;
			case IncrementalProjectBuilder.FULL_BUILD :
				targets = info.getFullBuildTarget();
				break;
		}
		return makeArray(targets);
	}

	// Turn the string into an array.
	String[] makeArray(String string) {
		string.trim();
		char[] array = string.toCharArray();
		ArrayList aList = new ArrayList();
		StringBuffer buffer = new StringBuffer();
		boolean inComment = false;
		for (int i = 0; i < array.length; i++) {
			char c = array[i];
			if (array[i] == '"' || array[i] == '\'') {
				if (i > 0 && array[i - 1] == '\\') {
					inComment = false;
				} else {
					inComment = !inComment;
				}
			}
			if (c == ' ' && !inComment) {
				aList.add(buffer.toString());
				buffer = new StringBuffer();
			} else {
				buffer.append(c);
			}
		}
		if (buffer.length() > 0)
			aList.add(buffer.toString());
		return (String[]) aList.toArray(new String[aList.size()]);
	}

	private void removeAllMarkers(IProject currProject) throws CoreException {
		IWorkspace workspace = currProject.getWorkspace();

		// remove all markers
		IMarker[] markers = currProject.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		if (markers != null) {
			workspace.deleteMarkers(markers);
		}
	}
}
