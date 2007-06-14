/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial implementation
 *     Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *******************************************************************************/
package org.eclipse.cdt.newmake.core;


public class MakeBuilderUtil {

//FIXME:	public static IPath getBuildDirectory(IProject project, IPath subPath, String builderID) {
//		IPath rootPath = getBuildDirectory(project, builderID);
//		return rootPath.append(subPath);
//	}
//
//	public static IPath getBuildDirectory(IProject project, String builderID) {
//		IMakeBuilderInfo info;
//		try {
//			info = MakeCorePlugin.createBuildInfo(project, builderID);
//		} catch (CoreException e) {
//			return project.getLocation();
//		}
//		return getBuildDirectory(project, info);
//	}
//
//	public static IPath getBuildDirectory(IProject project, IMakeBuilderInfo info) {
//		IPath buildDirectory = info.getBuildLocation();
//		if (!buildDirectory.isEmpty()) {
//			IResource res = project.getParent().findMember(buildDirectory);
//			if (res instanceof IContainer && res.exists()) {
//				buildDirectory = res.getLocation();
//			}
//		} else {
//			buildDirectory = project.getLocation();
//		}
//		return buildDirectory;
//	}
}
