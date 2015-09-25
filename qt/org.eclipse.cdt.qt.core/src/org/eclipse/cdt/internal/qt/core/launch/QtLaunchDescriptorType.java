/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.launch;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.internal.qt.core.QtNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.ILaunchDescriptorType;

public class QtLaunchDescriptorType implements ILaunchDescriptorType {

	private Map<IProject, QtLaunchDescriptor> descriptors = new HashMap<>();

	@Override
	public ILaunchDescriptor getDescriptor(Object launchObject) throws CoreException {
		// TODO also check to make sure it's an application project and not a library.
		// qmake -E will give the TEMPLATE variable
		if (launchObject instanceof IProject) {
			IProject project = (IProject) launchObject;
			if (QtNature.hasNature(project)) {
				QtLaunchDescriptor desc = descriptors.get(project);
				if (desc == null) {
					desc = new QtLaunchDescriptor(this, project);
					descriptors.put(project, desc);
				}
				return desc;
			}
		}
		return null;
	}

}
