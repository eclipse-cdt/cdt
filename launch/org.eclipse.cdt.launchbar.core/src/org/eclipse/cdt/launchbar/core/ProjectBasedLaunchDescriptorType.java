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

import org.eclipse.cdt.launchbar.core.internal.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public abstract class ProjectBasedLaunchDescriptorType extends ConfigBasedLaunchDescriptorType {
	public ProjectBasedLaunchDescriptorType(String descTypeId, String launchConfigTypeId) {
		super(descTypeId, launchConfigTypeId);
	}

	@Override
	public boolean ownsLaunchObject(Object element) {
		if (super.ownsLaunchObject(element))
			return true;
		if (element instanceof IProject && ownsProject((IProject) element))
			return true;
		return false;
	}

	protected abstract boolean ownsProject(IProject element);

	@Override
	public ILaunchDescriptor getDescriptor(Object element) {
		if (element instanceof ILaunchConfiguration) {
			ILaunchConfiguration llc = (ILaunchConfiguration) element;
			IProject project = getProject(llc);
			return new ProjectBasedLaunchDescriptor(this, project, llc);
		} else if (element instanceof IProject) {
			try {
				ILaunchDescriptor[] lds = getManager().getLaunchDescriptors();
				for (int i = 0; i < lds.length; i++) {
					ILaunchDescriptor ld = lds[i];
					if (ld instanceof ProjectBasedLaunchDescriptor
					        && element.equals(((ProjectBasedLaunchDescriptor) ld).getProject())) {
						return null; // somebody else has it
					}
				}
			} catch (CoreException e) {
				Activator.log(e);
			}
			return new ProjectBasedLaunchDescriptor(this, (IProject) element, null);
		}
		return null;
	}

	protected abstract IProject getProject(ILaunchConfiguration llc);
}