/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial implementation
 *     Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class MakeBuilderUtil {

	public static IPath getBuildDirectory(IProject project, IPath subPath, String builderID) {
		IPath rootPath = getBuildDirectory(project, builderID);
		return rootPath.append(subPath);
	}

	public static IPath getBuildDirectory(IProject project, String builderID) {
		IMakeBuilderInfo info;
		try {
			info = MakeCorePlugin.createBuildInfo(project, builderID);
		} catch (CoreException e) {
			return project.getLocation();
		}
		return getBuildDirectory(project, info);
	}

	public static IPath getBuildDirectory(IProject project, IMakeBuilderInfo info) {
		IPath buildDirectory = info.getBuildLocation();
		if (!buildDirectory.isEmpty()) {
			IResource res = project.getParent().findMember(buildDirectory);
			if (res instanceof IContainer && res.exists()) {
				buildDirectory = res.getLocation();
			}
		} else {
			buildDirectory = project.getLocation();
		}
		return buildDirectory;
	}
}
