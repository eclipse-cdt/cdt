package org.eclipse.cdt.internal.core;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import java.io.ByteArrayInputStream;
import java.util.Map;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.build.managed.ManagedBuildManager;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.MakeUtil;
import org.eclipse.cdt.internal.core.build.managed.ResourceBuildInfo;
import org.eclipse.cdt.internal.core.model.Util;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;

public class GeneratedMakefileBuilder extends ACBuilder {
	// String constants
	private static final String MESSAGE = "MakeBuilder.message";	//$NON-NLS-1$
	private static final String REBUILD = MESSAGE + ".rebuild";	//$NON-NLS-1$
	private static final String INCREMENTAL = MESSAGE + ".incremental";	//$NON-NLS-1$
	private static final String FILENAME = "makefile";	//$NON-NLS-1$
	private static final String NEWLINE = System.getProperty("line.separator", "\n");	//$NON-NLS-1$
	private static final String TAB = "\t";	//$NON-NLS-1$
	
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
	 * 
	 */
	public GeneratedMakefileBuilder() {
		super();
	}

	/**
	 * @param buffer
	 */
	private void addMacros(StringBuffer buffer, ResourceBuildInfo info) {
		// TODO this should come from the build model
		buffer.append("CC = " + NEWLINE);
		buffer.append("CFLAGS = " + NEWLINE);
		buffer.append("LD = " + NEWLINE);
		buffer.append("LDFLAGS = " + NEWLINE);
		buffer.append("RM = rm -f" + NEWLINE);
		buffer.append("MAKE = make" + NEWLINE);
		
		buffer.append(NEWLINE);
	}

	/**
	 * @param buffer
	 */
	private void addTargets(StringBuffer buffer) {
		// TODO Targets should come from build model

		// TODO Generate 'all' for now
		buffer.append("all:" + NEWLINE);
		buffer.append(NEWLINE);
		
		
		// Always add a clean target
		buffer.append("clean:" + NEWLINE);
		buffer.append(TAB + "$(RM) *.o" + NEWLINE);
		buffer.append(NEWLINE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (kind == IncrementalProjectBuilder.FULL_BUILD) {
			fullBuild(monitor);
		}
		else {
			// Create a delta visitor to make sure we should be rebuilding
			MyResourceDeltaVisitor visitor = new MyResourceDeltaVisitor();
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			}
			else {
				delta.accept(visitor);
				if (visitor.shouldBuild()) {
					incrementalBuild(delta, monitor);
				}
			}
		}
		// Checking to see if the user cancelled the build
		checkCancel(monitor);
		// Build referenced projects
		return getProject().getReferencedProjects();
	}

	/**
	 * Check whether the build has been canceled.
	 */
	public void checkCancel(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled())
			throw new OperationCanceledException();
	}

	/**
	 * @param monitor
	 */
	private void fullBuild(IProgressMonitor monitor) throws CoreException {
		// Rebuild the entire project
		IProject currentProject = getProject();
		String statusMsg = null;
		
		// Need to report status to the user
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		statusMsg = CCorePlugin.getFormattedString(REBUILD, currentProject.getName());
		monitor.subTask(statusMsg);

		// Get a filehandle for the makefile
		IPath filePath = getWorkingDirectory().append(IPath.SEPARATOR + FILENAME);
		String temp = filePath.toString();
		IFile fileHandle = getMakefile(filePath, monitor);

		// Add the items to the makefile
		populateMakefile(fileHandle, monitor);		
		
		monitor.worked(1);		
	}

	/**
	 * Gets the makefile for the project. It may be empty. 
	 * 
	 * @return The <code>IFile</code> to generate the makefile into.
	 */
	public IFile getMakefile(IPath filePath, IProgressMonitor monitor) throws CoreException {
		// Create or get the handle for the makefile
		IWorkspaceRoot root= CCorePlugin.getWorkspace().getRoot();
		IFile newFile = root.getFileForLocation(filePath);
		if (newFile == null) {
			newFile = root.getFile(filePath);
		}
		// Create the file if it does not exist
		ByteArrayInputStream contents = new ByteArrayInputStream(new byte[0]);
		try {
			newFile.create(contents, false, monitor);
		}
		catch (CoreException e) {
			// If the file already existed locally, just refresh to get contents
			if (e.getStatus().getCode() == IResourceStatus.PATH_OCCUPIED)
				newFile.refreshLocal(IResource.DEPTH_ZERO, null);
			else
				throw e;
		}
		// TODO handle long running file operation
		return newFile;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.ACBuilder#getWorkingDirectory()
	 */
	public IPath getWorkingDirectory() {
		IProject currProject = getProject();
		IPath workingDirectory = new Path(MakeUtil.getSessionBuildDir((IResource) currProject));
		if (workingDirectory.isEmpty())
			workingDirectory = currProject.getLocation();
		return workingDirectory;
	}

	/**
	 * @param delta
	 * @param monitor
	 */
	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// Rebuild the resource tree in the delta
		IProject currentProject = getProject();
		String statusMsg = null;
		
		// Need to report status to the user
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		statusMsg = CCorePlugin.getFormattedString(INCREMENTAL, currentProject.getName());
		monitor.subTask(statusMsg);

		// Get a filehandle for the makefile
		IPath filePath = getWorkingDirectory();
		filePath.addTrailingSeparator();
		filePath.append(FILENAME);
		IFile fileHandle = getMakefile(filePath, monitor);

		// Now populate it
		populateMakefile(fileHandle, monitor);
		
		monitor.worked(1);		
	}

	/**
	 * Recreate the entire contents of the makefile.
	 * 
	 * @param fileHandle The file to place the contents in.
	 */
	private void populateMakefile(IFile fileHandle, IProgressMonitor monitor) throws CoreException {
		// Write out the contents of the build model
		StringBuffer buffer = new StringBuffer();
		ResourceBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		
		// Add the macro definitions
		addMacros(buffer, info);

		// Add targets
		addTargets(buffer);

		// Save the file
		Util.save(buffer, fileHandle);
	}


}
