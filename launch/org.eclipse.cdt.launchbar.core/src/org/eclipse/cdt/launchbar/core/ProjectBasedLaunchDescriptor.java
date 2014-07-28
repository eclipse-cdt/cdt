/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems. All Rights Reserved.
 *
 * You must obtain a written license from and pay applicable license fees to QNX
 * Software Systems before you may reproduce, modify or distribute this software,
 * or any work that includes all or part of this software.   Free development
 * licenses are available for evaluation and non-commercial purposes.  For more
 * information visit [http://licensing.qnx.com] or email licensing@qnx.com.
 *
 * This file may contain contributions from others.  Please review this entire
 * file for other proprietary rights or license notices, as well as the QNX
 * Development Suite License Guide at [http://licensing.qnx.com/license-guide/]
 * for other information.
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;

public class ProjectBasedLaunchDescriptor extends ConfigBasedLaunchDescriptor {
	private IProject project;

	public ProjectBasedLaunchDescriptor(ILaunchDescriptorType type, IProject p, ILaunchConfiguration lc) {
		super(type, lc);
		this.project = p;
	}

	public String getName() {
		if (getConfig() == null)
			return project.getName();
		else
			return getConfig().getName();
	}

	public IProject getProject() {
		return project;
	}
}