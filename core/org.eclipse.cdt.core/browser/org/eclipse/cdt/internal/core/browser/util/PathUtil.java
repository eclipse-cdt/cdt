/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.util;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class PathUtil {
	
	private static boolean fGotOS = false;
	private static boolean fIsWindows = false;

	public static boolean isWindowsSystem() {
		if (!fGotOS) {
			String os = System.getProperty("os.name"); //$NON-NLS-1$
			if (os != null && os.startsWith("Win")) { //$NON-NLS-1$
				fIsWindows= true;
			}
			fGotOS = true;
		}
		return fIsWindows;
	}
	
	public static IWorkspaceRoot getWorkspaceRoot() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace != null) {
			return workspace.getRoot();
		}
		return null;
	}
	
	public static IPath getCanonicalPath(String fullPath) {
		File file = new File(fullPath);
		try {
			String canonPath = file.getCanonicalPath();
			return new Path(canonPath);
		} catch (IOException ex) {
		}
		return new Path(fullPath);
	}

	public static IPath getCanonicalPath(IPath fullPath) {
		return getCanonicalPath(fullPath.toString());
	}
	
	public static IPath getWorkspaceRelativePath(IPath fullPath) {
		IWorkspaceRoot workspaceRoot = getWorkspaceRoot();
		if (workspaceRoot != null) {
			IPath workspaceLocation = workspaceRoot.getLocation();
			if (workspaceLocation != null && workspaceLocation.isPrefixOf(fullPath)) {
				int segments = fullPath.matchingFirstSegments(workspaceLocation);
				IPath relPath = fullPath.setDevice(null).removeFirstSegments(segments);
				return new Path("").addTrailingSeparator().append(relPath); //$NON-NLS-1$
			}
		}
		return fullPath;
	}
	
	public static IPath getProjectRelativePath(IPath fullPath, IProject project) {
		IPath projectPath = project.getFullPath();
		if (projectPath.isPrefixOf(fullPath)) {
			return fullPath.removeFirstSegments(projectPath.segmentCount());
		}
		projectPath = project.getLocation();
		if (projectPath.isPrefixOf(fullPath)) {
			return fullPath.removeFirstSegments(projectPath.segmentCount());
		}
		return getWorkspaceRelativePath(fullPath);
	}

	public static IPath getWorkspaceRelativePath(String fullPath) {
		return getWorkspaceRelativePath(new Path(fullPath));
	}

	public static IPath getProjectRelativePath(String fullPath, IProject project) {
		return getProjectRelativePath(new Path(fullPath), project);
	}

	public static IPath getRawLocation(IPath wsRelativePath) {
		IWorkspaceRoot workspaceRoot = getWorkspaceRoot();
		if (workspaceRoot != null && wsRelativePath != null) {
			IPath workspaceLocation = workspaceRoot.getLocation();
			if (workspaceLocation != null && !workspaceLocation.isPrefixOf(wsRelativePath)) {
				return workspaceLocation.append(wsRelativePath);
			}
		}
		return wsRelativePath;
	}
}
