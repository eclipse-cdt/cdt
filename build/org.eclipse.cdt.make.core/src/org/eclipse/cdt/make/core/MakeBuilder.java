/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.StreamMonitor;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoConsoleParserFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.osgi.service.environment.Constants;

public class MakeBuilder extends ACBuilder {

	public final static String BUILDER_ID = MakeCorePlugin.getUniqueIdentifier() + ".makeBuilder"; //$NON-NLS-1$

	public MakeBuilder() {
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
			IResourceDelta delta = getDelta(getProject());
			if (delta != null) {
				IResource res = delta.getResource();
				if (res != null) {
					bPerformBuild = res.getProject().equals(getProject());
				}
			} else {
				bPerformBuild = false;
			}
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

	
	protected void clean(IProgressMonitor monitor) throws CoreException {
		final IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(getProject(), BUILDER_ID);
		if (shouldBuild(CLEAN_BUILD, info)) {
			Job backgroundJob = new Job("Standard Make Builder"){  //$NON-NLS-1$
				/* (non-Javadoc)
				 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				protected IStatus run(IProgressMonitor monitor) {
					invokeMake(CLEAN_BUILD, info, monitor);
					IStatus returnStatus = Status.OK_STATUS;
					return returnStatus;
				}
				
				
			};
			
			backgroundJob.setRule(getProject());
			backgroundJob.schedule();
		}
	}
	
	protected boolean invokeMake(int kind, IMakeBuilderInfo info, IProgressMonitor monitor) {
		boolean isClean = false;
		IProject currProject = getProject();

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(MakeMessages.getString("MakeBuilder.Invoking_Make_Builder") + currProject.getName(), 100); //$NON-NLS-1$

		try {
			IPath buildCommand = info.getBuildCommand();
			if (buildCommand != null) {
				IConsole console = CCorePlugin.getDefault().getConsole();
				console.start(currProject);

				OutputStream cos = console.getOutputStream();

				// remove all markers for this project
				removeAllMarkers(currProject);

				IPath workingDirectory = null;
				if (!info.getBuildLocation().isEmpty()) {
					IResource res = currProject.getParent().findMember(info.getBuildLocation());
					if (res instanceof IContainer && res.exists()) {
						workingDirectory = res.getLocation();
					}
				}
				if (workingDirectory == null) {
					workingDirectory = currProject.getLocation();
				}
				String[] targets = getTargets(kind, info);
				if (targets.length != 0 && targets[targets.length - 1].equals(info.getCleanBuildTarget())) //$NON-NLS-1$
					isClean = true;

				String errMsg = null;
				CommandLauncher launcher = new CommandLauncher();
				// Print the command for visual interaction.
				launcher.showCommand(true);

				// Set the environmennt, some scripts may need the CWD var to be set.
				HashMap envMap = new HashMap();
				if (info.appendEnvironment()) {
					envMap.putAll(launcher.getEnvironment());
				}
				envMap.put("CWD", workingDirectory.toOSString()); //$NON-NLS-1$
				envMap.put("PWD", workingDirectory.toOSString()); //$NON-NLS-1$
				// Add variables from build info
				Map userEnv = info.getEnvironment();
				Iterator iter= userEnv.entrySet().iterator();
				boolean win32= Platform.getOS().equals(Constants.OS_WIN32);
				while (iter.hasNext()) {
					Map.Entry entry= (Map.Entry) iter.next();
					String key= (String) entry.getKey();
					if (win32) {
						// Win32 vars are case insensitive. Uppercase everything so
						// that (for example) "pAtH" will correctly replace "PATH"
						key= key.toUpperCase();
					}
					String value = (String) entry.getValue();
					// translate any string substitution variables
					String translated = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(value);
					envMap.put(key, translated);
				}		
				
				iter= envMap.entrySet().iterator();
				List strings= new ArrayList(envMap.size());
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					StringBuffer buffer= new StringBuffer((String) entry.getKey());
					buffer.append('=').append((String) entry.getValue());
					strings.add(buffer.toString());
				}
				String[] env = (String[]) strings.toArray(new String[strings.size()]);
				String[] buildArguments = targets;
				if (info.isDefaultBuildCmd()) {
					if (!info.isStopOnError()) {
						buildArguments = new String[targets.length + 1];
						buildArguments[0] = "-k"; //$NON-NLS-1$
						System.arraycopy(targets, 0, buildArguments, 1, targets.length);
					}
				} else {
					String args = info.getBuildArguments();
					if (args != null && !args.equals("")) { //$NON-NLS-1$
						String translated = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(args);
						String[] newArgs = makeArray(translated);
						buildArguments = new String[targets.length + newArgs.length];
						System.arraycopy(newArgs, 0, buildArguments, 0, newArgs.length);
						System.arraycopy(targets, 0, buildArguments, newArgs.length, targets.length);
					}
				}
//					MakeRecon recon = new MakeRecon(buildCommand, buildArguments, env, workingDirectory, makeMonitor, cos);
//					recon.invokeMakeRecon();
//					cos = recon;
				QualifiedName qName = new QualifiedName(MakeCorePlugin.getUniqueIdentifier(), "progressMonitor"); //$NON-NLS-1$
				Integer last = (Integer)getProject().getSessionProperty(qName);
				if (last == null) {
					last = new Integer(100);
				}
				StreamMonitor streamMon = new StreamMonitor(new SubProgressMonitor(monitor, 100), cos, last.intValue());
				ErrorParserManager epm = new ErrorParserManager(getProject(), workingDirectory, this, info.getErrorParsers());
				epm.setOutputStream(streamMon);
				OutputStream stdout = epm.getOutputStream();
				OutputStream stderr = epm.getOutputStream();
				// Sniff console output for scanner info
				ConsoleOutputSniffer sniffer = ScannerInfoConsoleParserFactory.getMakeBuilderOutputSniffer(
						stdout, stderr, getProject(), workingDirectory, null, this, null);
				OutputStream consoleOut = (sniffer == null ? stdout : sniffer.getOutputStream());
				OutputStream consoleErr = (sniffer == null ? stderr : sniffer.getErrorStream());
				Process p = launcher.execute(buildCommand, buildArguments, env, workingDirectory);
				if (p != null) {
					try {
						// Close the input of the Process explicitly.
						// We will never write to it.
						p.getOutputStream().close();
					} catch (IOException e) {
					}
					// Before launching give visual cues via the monitor
					monitor.subTask(MakeMessages.getString("MakeBuilder.Invoking_Command") + launcher.getCommandLine()); //$NON-NLS-1$
					if (launcher.waitAndRead(consoleOut, consoleErr, new SubProgressMonitor(monitor, 0))
						!= CommandLauncher.OK)
						errMsg = launcher.getErrorMessage();
					monitor.subTask(MakeMessages.getString("MakeBuilder.Updating_project")); //$NON-NLS-1$

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
				getProject().setSessionProperty(qName, !monitor.isCanceled() && !isClean ? new Integer(streamMon.getWorkDone()) : null);

				if (errMsg != null) {
					StringBuffer buf = new StringBuffer(buildCommand.toString() + " "); //$NON-NLS-1$
					for (int i = 0; i < buildArguments.length; i++) {
						buf.append(buildArguments[i]);
						buf.append(' ');
					}

					String errorDesc = MakeMessages.getFormattedString("MakeBuilder.buildError", buf.toString()); //$NON-NLS-1$
					buf = new StringBuffer(errorDesc);
					buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append("(").append(errMsg).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
					cos.write(buf.toString().getBytes());
					cos.flush();
				}

				stdout.close();
				stderr.close();

				monitor.subTask(MakeMessages.getString("MakeBuilder.Creating_Markers")); //$NON-NLS-1$
				consoleOut.close();
				consoleErr.close();
				epm.reportProblems();
				cos.close();
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
			case IncrementalProjectBuilder.CLEAN_BUILD :
				return info.isCleanBuildEnabled();
		}
		return true;
	}

	protected String[] getTargets(int kind, IMakeBuilderInfo info) throws CoreException {
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
			case IncrementalProjectBuilder.CLEAN_BUILD :
				targets = info.getCleanBuildTarget();
				break;
		}
		String translated = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(targets);
		return makeArray(translated);
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
