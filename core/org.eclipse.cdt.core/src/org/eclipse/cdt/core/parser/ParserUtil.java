/*******************************************************************************
 * Copyright (c) 2002, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.io.IOException;
import java.util.Iterator;

import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.DebugLogConstants;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;
import org.eclipse.cdt.internal.core.parser.ParserLogService;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author jcamelon
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ParserUtil {
	private static IParserLogService parserLogService = new ParserLogService(DebugLogConstants.PARSER);
	private static IParserLogService scannerLogService = new ParserLogService(DebugLogConstants.SCANNER);

	public static IParserLogService getParserLogService() {
		return parserLogService;
	}

	public static IParserLogService getScannerLogService() {
		return scannerLogService;
	}

	public static char[] findWorkingCopyBuffer(String path, Iterator<IWorkingCopy> workingCopies) {
		IResource resultingResource = getResourceForFilename(path);

		if (resultingResource != null && resultingResource.getType() == IResource.FILE) {
			// This is the file for sure. Check the working copy.
			if (workingCopies.hasNext())
				return findWorkingCopy(resultingResource, workingCopies);
		}
		return null;
	}

	@Deprecated
	public static CodeReader createReader(String path, Iterator<IWorkingCopy> workingCopies) {
		// check to see if the file which this path points to points to an
		// IResource in the workspace
		try {
			IResource file = getResourceForFilename(path);
			if (file instanceof IFile) {
				// check for a working copy
				if (workingCopies != null && workingCopies.hasNext()) {
					char[] buffer = findWorkingCopy(file, workingCopies);
					if (buffer != null)
						return new CodeReader(InternalParserUtil.normalizePath(path, (IFile) file), buffer);
				}
				return InternalParserUtil.createWorkspaceFileReader(path, (IFile) file, null);
			}
			return InternalParserUtil.createExternalFileReader(path, null);
		} catch (CoreException ce) {
		} catch (IOException e) {
		} catch (IllegalStateException e) {
		}
		return null;
	}

	public static IResource getResourceForFilename(String finalPath) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace == null)
			return null;
		IPath path = new Path(finalPath);
		IPath initialPath = new Path(finalPath);

		IWorkspaceRoot root = workspace.getRoot();
		if (root.getLocation().isPrefixOf(path))
			path = path.removeFirstSegments(root.getLocation().segmentCount());

		try {
			IFile file = root.getFile(path);
			if (file != null && file.exists())
				return file;

			file = root.getFileForLocation(path);
			if (file != null && file.exists())
				return file;

			// check for linked resources
			file = ResourceLookup.selectFileForLocation(initialPath, null);
			if (file != null && file.exists())
				return file;

			return null;
		} catch (IllegalArgumentException e) { // thrown on invalid paths
			return null;
		}
	}

	protected static char[] findWorkingCopy(IResource resultingResource, Iterator<IWorkingCopy> workingCopies) {
		if (parserLogService.isTracing())
			parserLogService.traceLog("Attempting to find the working copy for " + resultingResource.getName()); //$NON-NLS-1$
		while (workingCopies.hasNext()) {
			IWorkingCopy copy = workingCopies.next();
			if (resultingResource.equals(copy.getResource())) {
				if (parserLogService.isTracing())
					parserLogService.traceLog("Working copy found!!"); //$NON-NLS-1$
				return copy.getContents();
			}
		}
		if (parserLogService.isTracing())
			parserLogService.traceLog("Working copy not found."); //$NON-NLS-1$

		return null;
	}
}
