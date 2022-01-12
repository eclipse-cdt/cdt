/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.cdt.utils.EFSExtensionManager;
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
	private Map<String, IFile> fFilesInProject;
	private List<IResource> fCollectedFiles;
	private List<String> fNameConflicts;

	public ScannerInfoConsoleParserUtility(IProject project, IPath workingDirectory, IMarkerGenerator markerGenerator) {
		super(project, workingDirectory, markerGenerator);

		fFilesInProject = new HashMap<>();
		fCollectedFiles = new ArrayList<>();
		fNameConflicts = new ArrayList<>();

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
					generateMarker(getProject(), -1, error + fileName, IMarkerGenerator.SEVERITY_WARNING, null);
				}
			}
		}

		if (file != null) {
			IPath filePath = new Path(fileName);
			if (filePath.segment(0).compareTo("..") == 0) { //$NON-NLS-1$
				filePath = filePath.removeFirstSegments(1);
			}

			String foundLocation = file.getLocationURI().toString();
			if (!foundLocation.endsWith(filePath.toString())) {
				file = null;
			}
		}
		return file;
	}

	/**
	 * @return file in workspace as {@link IFile} or {@code null} if not found
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
	 * @return file in workspace as {@link IFile} or {@code null}
	 */
	protected IFile findFileName(String fileName) {
		IPath path = new Path(fileName);
		return fFilesInProject.get(path.lastSegment());
	}

	protected IFile findFileInWorkspace(IPath path) {
		IFile file = null;
		if (path.isAbsolute()) {
			IWorkspaceRoot root = getProject().getWorkspace().getRoot();
			file = root.getFileForLocation(path);
			// It may be a link resource so we must check it also.
			if (file == null) {
				file = ResourceLookup.selectFileForLocation(path, getProject());
			}
		} else {
			file = getProject().getFile(path);
		}
		return file;
	}

	protected void collectFiles(IContainer parent, List<IResource> result) {
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

	public List<String> translateRelativePaths(IFile file, String fileName, List<String> includes) {
		List<String> translatedIncludes = new ArrayList<>(includes.size());
		for (String include : includes) {
			IPath includePath = new Path(include);
			if (includePath.isUNC()) {
				// do not translate UNC paths
			} else if (includePath.isAbsolute()) {
				if (includePath.getDevice() == null) {
					String device = getWorkingDirectory().getDevice();
					IPath candidatePath = includePath.setDevice(device);
					File dir = candidatePath.toFile();
					if (dir.exists()) {
						include = candidatePath.toString();
					} else {
						final String error = MakeMessages
								.getString("ConsoleParser.Nonexistent_Include_Path_Error_Message"); //$NON-NLS-1$
						TraceUtil.outputError(error, include);
						//						generateMarker(file, -1, error+include, IMarkerGenerator.SEVERITY_WARNING, fileName);
					}
				}
			} else {
				// First try the current working directory
				IPath cwd = getWorkingDirectory();
				if (!cwd.isAbsolute()) {
					cwd = getBaseDirectory().append(cwd);
				}

				IPath filePath = new Path(fileName);
				if (filePath.isAbsolute()) {
					if (filePath.getDevice() == null) {
						String device = getWorkingDirectory().getDevice();
						filePath = filePath.setDevice(device);
					}
				} else {
					// check if the cwd is the right one
					// appending fileName to cwd should yield file path
					filePath = cwd.append(fileName);
				}
				IPath fileLocation = new Path(EFSExtensionManager.getDefault().getPathFromURI(file.getLocationURI()));
				if (!filePath.toString().equalsIgnoreCase(fileLocation.toString())) {
					// must be the cwd is wrong
					// check if file name starts with ".."
					if (fileName.startsWith("..")) { //$NON-NLS-1$
						// probably multiple choices for cwd, hopeless
						final String error = MakeMessages.getString("ConsoleParser.Working_Directory_Error_Message"); //$NON-NLS-1$
						TraceUtil.outputError(error, fileName);
						generateMarker(file, -1, error, IMarkerGenerator.SEVERITY_WARNING, fileName);
						break;
					} else {
						// remove common segments at the end
						IPath tPath = new Path(fileName);
						if (fileName.startsWith(".")) { //$NON-NLS-1$
							tPath = tPath.removeFirstSegments(1);
						}
						// get the file path from the file
						filePath = fileLocation;
						IPath lastFileSegment = filePath
								.removeFirstSegments(filePath.segmentCount() - tPath.segmentCount());
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
				StringBuilder sb = new StringBuilder();
				if (column - 1 > 0) {
					sb.append(path.substring(0, column - 1));
				}
				sb.append(Character.toUpperCase(driveLetter));
				sb.append(path.substring(column));
				path = sb.toString();
			}
		}
		if (path.indexOf('.') == -1 || path.equals(".")) { //$NON-NLS-1$
			return (new Path(path)).toString(); // convert separators to '/'
		}
		// lose "./" segments since they confuse the Path normalization
		StringBuilder buf = new StringBuilder(path);
		int len = buf.length();
		StringBuilder newBuf = new StringBuilder(buf.length());
		int scp = 0; // starting copy point
		int ssp = 0; // starting search point
		int sdot;
		boolean validPrefix;
		while (ssp < len && (sdot = buf.indexOf(".", ssp)) != -1) { //$NON-NLS-1$
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
					} else if (validPrefix && nextChar == '\\') {
						++ssp;
						if (ssp < len - 1 && buf.charAt(ssp) == '\\') {
							++ssp;
						}
						scp = ssp;
					} else {
						// no path delimiter, must be '.' inside the path
						scp = ssp - 1;
					}
				}
			} else if (sdot == ddot) {
				ssp = sdot + 2;
			}
		}
		newBuf.append(buf.substring(scp, len));

		IPath orgPath = new Path(newBuf.toString());
		return orgPath.toString();
	}

}
