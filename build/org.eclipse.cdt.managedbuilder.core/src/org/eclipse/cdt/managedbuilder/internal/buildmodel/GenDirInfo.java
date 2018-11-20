/*******************************************************************************
 * Copyright (c) 2006, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.buildmodel;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.buildmodel.IBuildResource;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This class represents the generated directory information
 *
 * NOTE: This class is subject to change and discuss,
 * and is currently available in experimental mode only
 *
 */
public class GenDirInfo {
	private IProject fProject;
	private IPath fProjPath;
	private Set<IPath> fDirPathSet = new HashSet<>();

	public GenDirInfo(IProject proj) {
		fProject = proj;
		fProjPath = proj.getFullPath();
	}

	public GenDirInfo(IConfiguration cfg) {
		this(cfg.getOwner().getProject());
	}

	public void createDir(IBuildResource rc, IProgressMonitor monitor) {
		IPath path = rc.getFullPath();
		if (path != null && fProjPath.isPrefixOf(path)) {
			path = path.removeLastSegments(1).removeFirstSegments(1);
			createDir(path, monitor);
		}
	}

	public void createIfProjectDir(IPath fullPath, IProgressMonitor monitor) {
		if (fullPath.segmentCount() > fProjPath.segmentCount() && fProjPath.isPrefixOf(fullPath))
			createDir(fullPath.removeFirstSegments(fProjPath.segmentCount()), monitor);
	}

	protected void createDir(IPath path, IProgressMonitor monitor) {
		if (path.segmentCount() > 0 && fDirPathSet.add(path)) {
			IFolder folder = fProject.getFolder(path);
			if (!folder.exists()) {
				createDir(path.removeLastSegments(1), monitor);
				try {
					folder.create(true, true, monitor);
					folder.setDerived(true);
				} catch (CoreException e) {
					if (DbgUtil.DEBUG)
						DbgUtil.trace("GenDirInfo: failed to create dir: " + e.getLocalizedMessage()); //$NON-NLS-1$
					//TODO: log the error
				}
			}
		}

	}

}
