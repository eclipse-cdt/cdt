/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Implements error reporting mechanism and file/path translation mechanism
 * Taken from ErrorParserManager and modified.
 * 
 * @author vhirsl
 */
public class ScannerInfoConsoleParserUtility extends AbstractGCCBOPConsoleParserUtility {
	/*
	 * For tracking the location of files being compiled
	 */
	private Map fFilesInProject;
	private List fCollectedFiles;
	private List fNameConflicts;
    
	public ScannerInfoConsoleParserUtility(IProject project, IPath workingDirectory, IMarkerGenerator markerGenerator) {
	    super(project, workingDirectory, markerGenerator);
        
		fFilesInProject = new HashMap();
		fCollectedFiles = new ArrayList();
		fNameConflicts = new ArrayList();

		collectFiles(getProject(), fCollectedFiles);

		for (int i = 0; i < fCollectedFiles.size(); i++) {
			IFile curr = (IFile) fCollectedFiles.get(i);
			Object existing = fFilesInProject.put(curr.getName(), curr);
			if (existing != null) {
				fNameConflicts.add(curr.getName());
			}
		}
	}
	
	/**
	 * Called by the console line parsers to find a file with a given name.
	 * @param fileName
	 * @return IFile or null
	 */
	public IFile findFile(String fileName) {
		IFile file = findFilePath(fileName);
		if (file == null) {
			// Try the project's map.
			file = findFileName(fileName);
			if (file != null) {
				// If there is a conflict then try all files in the project.
				if (isConflictingName(fileName)) {
					file = null;
					
					// Create a problem marker
					final String error = MakeMessages.getString("ConsoleParser.Ambiguous_Filepath_Error_Message"); //$NON-NLS-1$
					TraceUtil.outputError(error, fileName);
					generateMarker(getProject(), -1, error+fileName, IMarkerGenerator.SEVERITY_WARNING, null);
				}
			}
		}
		return file;
	}
	
	/**
	 * @param filePath
	 * @return
	 */
	protected IFile findFilePath(String filePath) {
		IPath path = null;
		IPath fp = new Path(filePath);
		if (fp.isAbsolute()) {
			if (getBaseDirectory().isPrefixOf(fp)) {
				int segments = getBaseDirectory().matchingFirstSegments(fp);
				path = fp.removeFirstSegments(segments);
			} else {
				path = fp;
			}
		} else {
			path = getWorkingDirectory().append(filePath);
		}

		IFile file = null;
		// The workspace may throw an IllegalArgumentException
		// Catch it and the parser should fallback to scan the entire project.
		try {
			file = findFileInWorkspace(path);
		} catch (Exception e) {
		}

		// We have to do another try, on Windows for cases like "TEST.C" vs "test.c"
		// We use the java.io.File canonical path.
		if (file == null || !file.exists()) {
			File f = path.toFile();
			try {
				String canon = f.getCanonicalPath();
				path = new Path(canon);
				file = findFileInWorkspace(path);
			} catch (IOException e1) {
			}
		}
		return (file != null && file.exists()) ? file : null;
	}

	/**
	 * @param fileName
	 * @return
	 */
	protected IFile findFileName(String fileName) {
		IPath path = new Path(fileName);
		return (IFile) fFilesInProject.get(path.lastSegment());
	}

	protected IFile findFileInWorkspace(IPath path) {
		IFile file = null;
		if (path.isAbsolute()) {
			IWorkspaceRoot root = getProject().getWorkspace().getRoot();
			file =  root.getFileForLocation(path);
			// It may be a link resource so we must check it also.
			if (file == null) {
				IFile[] files = root.findFilesForLocation(path);
				for (int i = 0; i < files.length; i++) {
					if (files[i].getProject().equals(getProject())) {
						file = files[i];
						break;
					}
				}
			}

		} else {
			file = getProject().getFile(path);
		}
		return file;
	}

	protected void collectFiles(IContainer parent, List result) {
		try {
			IResource[] resources = parent.members();
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				if (resource instanceof IFile) {
					result.add(resource);
				} else if (resource instanceof IContainer) {
					collectFiles((IContainer) resource, result);
				}
			}
		} catch (CoreException e) {
			MakeCorePlugin.log(e.getStatus());
		}
	}

	protected boolean isConflictingName(String fileName) {
		IPath path = new Path(fileName);
		return fNameConflicts.contains(path.lastSegment());
	}

	public List translateRelativePaths(IFile file, String fileName, List includes) {
		List translatedIncludes = new ArrayList(includes.size());
		for (Iterator i = includes.iterator(); i.hasNext(); ) {
			String include = (String) i.next();
			IPath includePath = new Path(include);
			if (!includePath.isAbsolute() && !includePath.isUNC()) {	// do not translate UNC paths
				// First try the current working directory
				IPath cwd = getWorkingDirectory();
				if (!cwd.isAbsolute()) {
					cwd = getBaseDirectory().append(cwd);
				}
				
				IPath filePath = new Path(fileName);
				if (!filePath.isAbsolute()) {
					// check if the cwd is the right one
					// appending fileName to cwd should yield file path
					filePath = cwd.append(fileName);
				}
				if (!filePath.toString().equalsIgnoreCase(file.getLocation().toString())) {
					// must be the cwd is wrong
					// check if file name starts with ".."
					if (fileName.startsWith("..")) {	//$NON-NLS-1$
						// probably multiple choices for cwd, hopeless
						final String error = MakeMessages.getString("ConsoleParser.Working_Directory_Error_Message"); //$NON-NLS-1$
						TraceUtil.outputError(error, fileName); //$NON-NLS-1$
						generateMarker(file, -1, error,	 IMarkerGenerator.SEVERITY_WARNING, fileName);				
						break;
					}
					else {
						// remove common segments at the end 
						IPath tPath = new Path(fileName);
						if (fileName.startsWith(".")) {	//$NON-NLS-1$
							tPath = tPath.removeFirstSegments(1);
						}
						// get the file path from the file
						filePath = file.getLocation();
						IPath lastFileSegment = filePath.removeFirstSegments(filePath.segmentCount() - tPath.segmentCount());
						if (lastFileSegment.matchingFirstSegments(tPath) == tPath.segmentCount()) {
							cwd = filePath.removeLastSegments(tPath.segmentCount());
						}
					}
				}
				
				IPath candidatePath = cwd.append(includePath);
				File dir = candidatePath.toFile();
				include = candidatePath.toString();
				if (!dir.exists()) {
					final String error = MakeMessages.getString("ConsoleParser.Nonexistent_Include_Path_Error_Message"); //$NON-NLS-1$
					TraceUtil.outputError(error, include);
//					generateMarker(file, -1, error+include, IMarkerGenerator.SEVERITY_WARNING, fileName);				
				}
			}
			// TODO VMIR for now add unresolved paths as well
			translatedIncludes.add(include);
		}
		return translatedIncludes;
	}

	public String normalizePath(String path) {
		int column = path.indexOf(':');
		if (column > 0) {
			char driveLetter = path.charAt(column - 1);
			if (Character.isLowerCase(driveLetter)) {
				StringBuffer sb = new StringBuffer();
				if (column - 1 > 0) {
					sb.append(path.substring(0, column-1));
				}
				sb.append(Character.toUpperCase(driveLetter));
				sb.append(path.substring(column));
				path = sb.toString();
			}
		}
		if (path.indexOf('.') == -1 || path.equals(".")) {	//$NON-NLS-1$
			return (new Path(path)).toString();	// convert separators to '/'
		}
		// lose "./" segments since they confuse the Path normalization
		StringBuffer buf = new StringBuffer(path);
		int len = buf.length();
		StringBuffer newBuf = new StringBuffer(buf.length());
		int scp = 0; // starting copy point
		int ssp = 0;	// starting search point
		int sdot;
		boolean validPrefix;
		while (ssp < len && (sdot = buf.indexOf(".", ssp)) != -1) {	//$NON-NLS-1$
			validPrefix = false;
			int ddot = buf.indexOf("..", ssp);//$NON-NLS-1$
			if (sdot < ddot || ddot == -1) {
				newBuf.append(buf.substring(scp, sdot));
				scp = sdot;
				ssp = sdot + 1;
				if (ssp < len) {
					if (sdot == 0 || buf.charAt(sdot - 1) == '/' || buf.charAt(sdot - 1) == '\\') {
						validPrefix = true;
					}
					char nextChar = buf.charAt(ssp);
					if (validPrefix && nextChar == '/') {
						++ssp;
						scp = ssp;
					}
					else if (validPrefix && nextChar == '\\') {
						++ssp;
						if (ssp < len - 1 && buf.charAt(ssp) == '\\') {
							++ssp;
						}
						scp = ssp;
					}
					else {
						// no path delimiter, must be '.' inside the path
						scp = ssp - 1;
					}
				}
			}
			else if (sdot == ddot) {
				ssp = sdot + 2;
			}
		}
		newBuf.append(buf.substring(scp, len));
					 
		IPath orgPath = new Path(newBuf.toString());
		return orgPath.toString();
	}

}
