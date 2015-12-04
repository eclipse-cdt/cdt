/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.cmake.core.CMakeNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;

public class CMakeLaunchDescriptorType implements ILaunchDescriptorType {

	private Map<IProject, CMakeLaunchDescriptor> descriptors = new HashMap<>();

	@Override
	public ILaunchDescriptor getDescriptor(Object launchObject) throws CoreException {
		if (launchObject instanceof IProject) {
			IProject project = (IProject) launchObject;
			if (CMakeNature.hasNature(project)) {
				CMakeLaunchDescriptor desc = descriptors.get(project);
				if (desc == null) {
					desc = new CMakeLaunchDescriptor(this, project);
					descriptors.put(project, desc);
				}
				return desc;
			}
		}
		return null;
	}

}
