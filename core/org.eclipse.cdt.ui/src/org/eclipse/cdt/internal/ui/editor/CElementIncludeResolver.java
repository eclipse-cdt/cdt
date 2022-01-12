/*******************************************************************************
 * Copyright (c) 2016 Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.cdt.utils.UNCPathConverter;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * Handles resolution of an include represented as a CElement (IInclude).
 */
public class CElementIncludeResolver {
	public static List<IPath> resolveInclude(IInclude include) throws CoreException {
		IResource res = include.getUnderlyingResource();
		ArrayList<IPath> filesFound = new ArrayList<>(4);
		String fullFileName = include.getFullFileName();
		if (fullFileName != null) {
			IPath fullPath = new Path(fullFileName);
			if (fullPath.isAbsolute() && fullPath.toFile().exists()) {
				filesFound.add(fullPath);
			} else if (fullPath.isUNC()) {
				IFileStore store = EFS.getStore(UNCPathConverter.getInstance().toURI(fullPath));
				if (store.fetchInfo().exists()) {
					filesFound.add(fullPath);
				}
			}
		}
		if (filesFound.isEmpty() && res != null) {
			IProject proj = res.getProject();
			String includeName = include.getElementName();
			// Search in the scannerInfo information
			IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(proj);
			if (provider != null) {
				IScannerInfo info = provider.getScannerInformation(res);
				// XXXX this should fall back to project by itself
				if (info == null) {
					info = provider.getScannerInformation(proj);
				}
				if (info != null) {
					IExtendedScannerInfo scanInfo = new ExtendedScannerInfo(info);

					boolean isSystemInclude = include.isStandard();

					if (!isSystemInclude) {
						// search in current directory
						IPath location = include.getTranslationUnit().getLocation();
						if (location != null) {
							String currentDir = location.removeLastSegments(1).toOSString();
							findFile(new String[] { currentDir }, includeName, filesFound);
						}
						if (filesFound.isEmpty()) {
							// search in "..." include directories
							String[] localIncludePaths = scanInfo.getLocalIncludePath();
							findFile(localIncludePaths, includeName, filesFound);
						}
					}

					if (filesFound.isEmpty()) {
						// search in <...> include directories
						String[] includePaths = scanInfo.getIncludePaths();
						findFile(includePaths, includeName, filesFound);
					}
				}

				if (filesFound.isEmpty()) {
					// Fall back and search the project
					findFile(proj, new Path(includeName), filesFound);
				}
			}
		}
		return filesFound;
	}

	private static void findFile(String[] includePaths, String name, ArrayList<IPath> list) throws CoreException {
		// in case it is an absolute path
		IPath includeFile = new Path(name);
		if (includeFile.isAbsolute()) {
			includeFile = PathUtil.getCanonicalPathWindows(includeFile);
			if (includeFile.toFile().exists()) {
				list.add(includeFile);
				return;
			}
		}
		HashSet<IPath> foundSet = new HashSet<>();
		for (String includePath : includePaths) {
			IPath path = PathUtil.getCanonicalPathWindows(new Path(includePath).append(includeFile));
			File file = path.toFile();
			if (file.exists()) {
				IPath[] paths = resolveIncludeLink(path);
				for (IPath p : paths) {
					if (foundSet.add(p)) {
						list.add(p);
					}
				}
			}
		}
	}

	/**
	 * Recurse in the project.
	 * @param parent
	 * @param name
	 * @param list
	 * @throws CoreException
	 */
	private static void findFile(IContainer parent, final IPath name, final ArrayList<IPath> list)
			throws CoreException {
		parent.accept(new IResourceProxyVisitor() {

			@Override
			public boolean visit(IResourceProxy proxy) throws CoreException {
				if (proxy.getType() == IResource.FILE && proxy.getName().equalsIgnoreCase(name.lastSegment())) {
					IPath rPath = proxy.requestResource().getLocation();
					if (rPath != null) {
						int numSegToRemove = rPath.segmentCount() - name.segmentCount();
						IPath sPath = rPath.removeFirstSegments(numSegToRemove);
						sPath = sPath.setDevice(name.getDevice());
						if (Platform.getOS().equals(Platform.OS_WIN32)
								? sPath.toOSString().equalsIgnoreCase(name.toOSString())
								: sPath.equals(name)) {
							list.add(rPath);
						}
						return false;
					}
				}
				return true;
			}
		}, 0);
	}

	/**
	 * Returns the path as is, if it points to a workspace resource. If the path
	 * does not point to a workspace resource, but there are linked workspace
	 * resources pointing to it, returns the paths of these resources.
	 * Otherwise, returns the path as is.
	 */
	private static IPath[] resolveIncludeLink(IPath path) {
		if (!isInProject(path)) {
			IFile[] files = ResourceLookup.findFilesForLocation(path);
			if (files.length > 0) {
				IPath[] paths = new IPath[files.length];
				for (int i = 0; i < files.length; i++) {
					paths[i] = files[i].getFullPath();
				}
				return paths;
			}
		}

		return new IPath[] { path };
	}

	private static boolean isInProject(IPath path) {
		return getWorkspaceRoot().getFileForLocation(path) != null;
	}

	private static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
