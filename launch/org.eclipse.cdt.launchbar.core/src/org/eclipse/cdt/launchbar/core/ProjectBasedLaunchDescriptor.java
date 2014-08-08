/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;

public class ProjectBasedLaunchDescriptor extends ConfigBasedLaunchDescriptor implements ILaunchDescriptorProjectBased {
	private IProject project;

	public ProjectBasedLaunchDescriptor(ILaunchDescriptorType type, IProject p, ILaunchConfiguration lc) {
		super(type, lc);
		if (p == null)
			throw new NullPointerException();
		this.project = p;
	}

	@Override
	public String getName() {
		ILaunchConfiguration lc = getLaunchConfiguration();
		if (lc != null)
			return lc.getName();
		return project.getName();
	}

	@Override
    public IProject getProject() {
		return project;
	}

	@Override
	public String toString() {
		ILaunchConfiguration lc = getLaunchConfiguration();
		if (lc != null)
			return "LC/" + lc.getName();
		return "P/" + project.getName();
	}
}