/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.browser;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class PathUtil {
	
	public static boolean isWindowsFileSystem() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return (os != null && os.startsWith("Win")); //$NON-NLS-1$
	}
	
	public static IWorkspaceRoot getWorkspaceRoot() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace != null) {
			return workspace.getRoot();
		}
		return null;
	}
	
	public static IPath getCanonicalPath(IPath fullPath) {
	    File file = fullPath.toFile();
		try {
			String canonPath = file.getCanonicalPath();
			return new Path(canonPath);
		} catch (IOException ex) {
		}
		return null;
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

    public static IPath makeRelativePath(IPath path, IPath relativeTo) {
        int segments = relativeTo.matchingFirstSegments(path);
        if (segments > 0) {
            IPath prefix = relativeTo.removeFirstSegments(segments);
            IPath suffix = path.removeFirstSegments(segments);
            IPath relativePath = new Path(""); //$NON-NLS-1$
            for (int i = 0; i < prefix.segmentCount(); ++i) {
                relativePath = relativePath.append(".." + IPath.SEPARATOR); //$NON-NLS-1$
            }
            return relativePath.append(suffix);
        }
        return null;
    }

    public static IPath makeRelativePathToProjectIncludes(IPath fullPath, IProject project) {
        IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
        if (provider != null) {
            IScannerInfo info = provider.getScannerInformation(project);
            if (info != null) {
                return makeRelativePathToIncludes(fullPath, info.getIncludePaths());
            }
        }
        return null;
    }
    
    public static IPath makeRelativePathToIncludes(IPath fullPath, String[] includePaths) {
        IPath relativePath = null;
        int mostSegments = 0;
        for (int i = 0; i < includePaths.length; ++i) {
            IPath includePath = new Path(includePaths[i]);
            if (includePath.isPrefixOf(fullPath)) {
                int segments = includePath.matchingFirstSegments(fullPath);
                if (segments > mostSegments) {
                    relativePath = fullPath.removeFirstSegments(segments).setDevice(null);
                    mostSegments = segments;
                }
            }
        }
        return relativePath;
    }

    public static ICProject getEnclosingProject(IPath fullPath) {
		IWorkspaceRoot root = getWorkspaceRoot();
		if (root != null) {
			IPath path = getWorkspaceRelativePath(fullPath);
			while (!path.isEmpty()) {
				IResource res = root.findMember(path);
				if (res != null)
				    return CoreModel.getDefault().create(res.getProject());

				path = path.removeLastSegments(1);
			}
		}
		return null;
    }
    
    public static IPath getValidEnclosingFolder(IPath fullPath) {
		IWorkspaceRoot root = getWorkspaceRoot();
		if (root != null) {
			IPath path = getWorkspaceRelativePath(fullPath);
			while (!path.isEmpty()) {
				IResource res = root.findMember(path);
				if (res != null && res.exists() && (res.getType() == IResource.PROJECT || res.getType() == IResource.FOLDER))
				    return path;

				path = path.removeLastSegments(1);
			}
		}
		return null;
	}
}
