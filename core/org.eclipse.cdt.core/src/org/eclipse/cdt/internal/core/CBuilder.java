package org.eclipse.cdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.resources.IPropertyStore;
import org.eclipse.cdt.internal.CCorePlugin;
import org.eclipse.cdt.internal.errorparsers.ErrorParserManager;
import org.eclipse.cdt.core.*;
import org.eclipse.cdt.core.resources.*;



public class CBuilder extends ACBuilder {
	
	private static final String BUILD_ERROR= "CBuilder.build_error";
	
	private IPropertyStore fPreferenceStore;
	private ErrorParserManager fErrorParserManager;
		
	public CBuilder() {
		fPreferenceStore= CCorePlugin.getDefault().getPropertyStore();
		fErrorParserManager= new ErrorParserManager(this);
	}

    public IPath getWorkingDirectory() {
		IProject currProject= getProject();
		IPath workingDirectory = new Path(MakeUtil.getSessionBuildDir((IResource)currProject));
		if (workingDirectory.isEmpty())
			workingDirectory = currProject.getLocation();
		return workingDirectory;
    }

	/**
	 * @see IncrementalProjectBuilder#build
	 */	
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {	
		IResourceDelta delta= getDelta(getProject());
		boolean isFullBuild= (kind == IncrementalProjectBuilder.FULL_BUILD) || (delta == null);
		if (isFullBuild) {
			invokeMake(true, monitor);
		} else {
			IResource res= delta.getResource();
			//if (res != null && delta.getKind() != 0) {
			if (res != null) {
				IProject currProject= getProject();
				if (currProject.equals(res.getProject())) {
					invokeMake(false, monitor);
				}
			}
		}
		return null;
	}
	
	
	private void invokeMake(boolean fullBuild, IProgressMonitor monitor) {
		IProject currProject= getProject();
		SubProgressMonitor subMonitor = null;

		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		monitor.beginTask("Invoking the C Builder: " + currProject.getName(), IProgressMonitor.UNKNOWN);

		try {
			CProjectNature nature= (CProjectNature)currProject.getNature(CProjectNature.C_NATURE_ID);
			IPath makepath= nature.getBuildCommand();
			if (!makepath.isEmpty()) {
				// clear console if requested
				IConsole console = CCorePlugin.getDefault().getConsole();
				if (BuildInfoFactory.create().isClearBuildConsole()
					&& MakeUtil.getSessionConsoleMode(currProject)) {
					console.clear();
				}
				
				ConsoleOutputStream cos = console.getOutputStream();
				
				// remove all markers for this project
				removeAllMarkers(currProject);

				IPath workingDirectory= getWorkingDirectory();
				String[] userArgs= parseArguments(fullBuild, nature.getIncrBuildArguments());				

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
				String[] env= null;
				ArrayList envList = new ArrayList();
				Enumeration names = props.propertyNames();
				if (names != null) {
					while (names.hasMoreElements()) {
						String key = (String)names.nextElement();
						envList.add(key +"=" +props.getProperty(key));
					}
					env = (String []) envList.toArray(new String [envList.size()]);
				}

				launcher.execute(makepath, userArgs, env, workingDirectory);
				if (launcher.waitAndRead(cos, cos, subMonitor) != CommandLauncher.OK)
					errMsg = launcher.getErrorMessage();
				
				monitor.setCanceled(false);

				subMonitor = new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN);
				subMonitor.subTask("Refresh From Local");

				try {
					currProject.refreshLocal(IResource.DEPTH_INFINITE, subMonitor);
				} catch (CoreException e) {
				}

				subMonitor = new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN);
				subMonitor.subTask("Parsing");
				cos.flush();
				fErrorParserManager.parse(cos.getContent());

				if (errMsg != null) {
					String errorDesc= CCorePlugin.getFormattedString(BUILD_ERROR, makepath.toString());
					StringBuffer buf= new StringBuffer(errorDesc);
					buf.append(System.getProperty("line.separator", "\n"));
					buf.append("(");
					buf.append(errMsg);
					buf.append(")");
					cos.write(buf.toString().getBytes());
					cos.flush();
				}
				subMonitor.done();
			}
		} catch (Exception e) {
			CCorePlugin.log(e);
		}
		monitor.done();
	}
	
	private String[] parseArguments(boolean fullBuild, String override_args) {
		ArrayList list= new ArrayList();
		IProject currProject = getProject();
		try {
			CProjectNature nature= (CProjectNature)currProject.getNature(CProjectNature.C_NATURE_ID);			
			if (nature.isDefaultBuildCmd()) {
				if (!nature.isStopOnError()) {
					list.add("-k");
				}
			} else {
				String[] ovrd_args = makeArray(nature.getFullBuildArguments());
				list.addAll(Arrays.asList(ovrd_args));
			}
		} catch (CoreException e) {
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
		char []array = string.toCharArray();
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
		return (String[])aList.toArray(new String[aList.size()]);
	}
	
	//private void clearConsole(final IDocument doc) {
	//	Display.getDefault().syncExec(new Runnable() {
	//		public void run() {
	//			doc.set("");
	//		}
	//	});	
	//}
	
	private void removeAllMarkers(IProject currProject) throws CoreException {
		IWorkspace workspace= currProject.getWorkspace();
		
		// remove all markers
		IMarker[] markers= currProject.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		if (markers != null) {
			workspace.deleteMarkers(markers);
		}
	}
}
