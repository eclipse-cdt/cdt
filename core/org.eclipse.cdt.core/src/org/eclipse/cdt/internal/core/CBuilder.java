package org.eclipse.cdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.MakeUtil;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

public class CBuilder extends ACBuilder {

	private static final String BUILD_ERROR = "CBuilder.build_error";

	public CBuilder() {
	}

	public IPath getWorkingDirectory() {
		IProject currProject = getProject();
		IPath workingDirectory = new Path(MakeUtil.getSessionBuildDir((IResource) currProject));
		if (workingDirectory.isEmpty())
			workingDirectory = currProject.getLocation();
		return workingDirectory;
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
		if (kind == IncrementalProjectBuilder.AUTO_BUILD) {
			MyResourceDeltaVisitor vis = new MyResourceDeltaVisitor();
			IResourceDelta delta = getDelta(getProject());
			if  (delta != null )  {
				delta.accept(vis);
				bPerformBuild = vis.shouldBuild();
			} else
				bPerformBuild = false;
		}
		if ( bPerformBuild ) {
			boolean isClean = invokeMake((kind == IncrementalProjectBuilder.FULL_BUILD), monitor);
			if (isClean) {
				forgetLastBuiltState();
			}
		}
		checkCancel(monitor);
		return getProject().getReferencedProjects();
	}

	private boolean invokeMake(boolean fullBuild, IProgressMonitor monitor) {
		boolean isClean = false;
		boolean isCanceled = false;
		IProject currProject = getProject();
		SubProgressMonitor subMonitor = null;

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("Invoking the C Builder: " + currProject.getName(), IProgressMonitor.UNKNOWN);

		try {
			CProjectNature nature = (CProjectNature) currProject.getNature(CProjectNature.C_NATURE_ID);
			IPath makepath = nature.getBuildCommand();
			if (!makepath.isEmpty()) {
				IConsole console = CCorePlugin.getDefault().getConsole();
				console.start(currProject);

				ConsoleOutputStream cos = console.getOutputStream();

				// remove all markers for this project
				removeAllMarkers(currProject);

				IPath workingDirectory = getWorkingDirectory();
				String[] userArgs = parseArguments(fullBuild, nature.getIncrBuildArguments());
				if (userArgs.length != 0 && userArgs[userArgs.length - 1].equals("clean"))
					isClean = true;
				// Before launching give visual cues via the monitor
				subMonitor = new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN);
				subMonitor.subTask("Invoking Command: " + makepath.toString());

				String errMsg = null;
				CommandLauncher launcher = new CommandLauncher();
				// Print the command for visual interaction.
				launcher.showCommand(true);

				// Set the environmennt, some scripts may need the CWD var to be set.
				Properties props = launcher.getEnvironment();
				props.put("CWD", workingDirectory.toOSString());
				props.put("PWD", workingDirectory.toOSString());
				String[] env = null;
				ArrayList envList = new ArrayList();
				Enumeration names = props.propertyNames();
				if (names != null) {
					while (names.hasMoreElements()) {
						String key = (String) names.nextElement();
						envList.add(key + "=" + props.getProperty(key));
					}
					env = (String[]) envList.toArray(new String[envList.size()]);
				}
				ErrorParserManager epm = new ErrorParserManager(this);
				epm.setOutputStream(cos);
				OutputStream stdout = epm.getOutputStream();
				OutputStream stderr = epm.getOutputStream();

				Process p = launcher.execute(makepath, userArgs, env, workingDirectory);
				if (p != null) {
					try {
						// Close the input of the Process explicitely.
						// We will never write to it.
						p.getOutputStream().close();
					} catch (IOException e) {
					}
					if (launcher.waitAndRead(stdout, stderr, subMonitor) != CommandLauncher.OK)
						errMsg = launcher.getErrorMessage();

					isCanceled = monitor.isCanceled();
					monitor.setCanceled(false);
					subMonitor = new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN);
					subMonitor.subTask("Refresh From Local");

					try {
						currProject.refreshLocal(IResource.DEPTH_INFINITE, subMonitor);
					} catch (CoreException e) {
					}

					subMonitor = new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN);
					subMonitor.subTask("Parsing");
				} else {
					errMsg = launcher.getErrorMessage();
				}

				if (errMsg != null) {
					String errorDesc = CCorePlugin.getFormattedString(BUILD_ERROR, makepath.toString());
					StringBuffer buf = new StringBuffer(errorDesc);
					buf.append(System.getProperty("line.separator", "\n"));
					buf.append("(").append(errMsg).append(")");
					cos.write(buf.toString().getBytes());
					cos.flush();
				}

				stdout.close();
				stderr.close();				

				epm.reportProblems();

				subMonitor.done();
				monitor.setCanceled(isCanceled);
			}
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
		monitor.done();
		return (isClean);
	}

	/**
	 * Check whether the build has been canceled.
	 */
	public void checkCancel(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled())
			throw new OperationCanceledException();
	}

	private String[] parseArguments(boolean fullBuild, String override_args) {
		ArrayList list = new ArrayList();
		IProject currProject = getProject();
		try {
			CProjectNature nature = (CProjectNature) currProject.getNature(CProjectNature.C_NATURE_ID);
			if (nature.isDefaultBuildCmd()) {
				if (!nature.isStopOnError()) {
					list.add("-k");
				}
			}
			else {
				String[] ovrd_args = makeArray(nature.getFullBuildArguments());
				list.addAll(Arrays.asList(ovrd_args));
			}
		}
		catch (CoreException e) {
		}

		String sessionTarget = MakeUtil.getSessionTarget((IResource) currProject);
		String[] targets = makeArray(sessionTarget);
		for (int i = 0; i < targets.length; i++) {
			list.add(targets[i]);
		}

		// Lets try this: if FULL_BUILD; we run "clean all"
		if (fullBuild && targets.length == 0) {
			list.add("clean");
			list.add("all");
		}

		return (String[]) list.toArray(new String[list.size()]);
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
				}
				else {
					inComment = !inComment;
				}
			}
			if (c == ' ' && !inComment) {
				aList.add(buffer.toString());
				buffer = new StringBuffer();
			}
			else {
				buffer.append(c);
			}
		}
		if (buffer.length() > 0)
			aList.add(buffer.toString());
		return (String[]) aList.toArray(new String[aList.size()]);
	}

	//private void clearConsole(final IDocument doc) {
	//	Display.getDefault().syncExec(new Runnable() {
	//		public void run() {
	//			doc.set("");
	//		}
	//	});	
	//}

	private void removeAllMarkers(IProject currProject) throws CoreException {
		IWorkspace workspace = currProject.getWorkspace();

		// remove all markers
		IMarker[] markers = currProject.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		if (markers != null) {
			workspace.deleteMarkers(markers);
		}
	}
}
