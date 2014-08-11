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

public abstract class ProjectBasedLaunchDescriptorType extends ConfigBasedLaunchDescriptorType {
	public ProjectBasedLaunchDescriptorType(String descTypeId, String launchConfigTypeId) {
		super(descTypeId, launchConfigTypeId);
	}

	@Override
	public boolean ownsLaunchObject(Object element) {
		if (super.ownsLaunchObject(element))
			return true;
		if (element instanceof IProject && ((IProject) element).isOpen() && ownsProject((IProject) element))
			return true;
		return false;
	}

	public boolean ownsConfiguration(ILaunchConfiguration element) {
		return super.ownsConfiguration(element) && getProject(element) != null;
	}

	protected boolean ownsLaunchDescriptor(ILaunchDescriptor ld) {
		if (!(ld instanceof ProjectBasedLaunchDescriptorType))
			return false;
		ProjectBasedLaunchDescriptorType other = (ProjectBasedLaunchDescriptorType) ld;
		return other.getLaunchConfigurationType().equals(getLaunchConfigurationType());
	}

	protected abstract boolean ownsProject(IProject element);

	@Override
	public ILaunchDescriptor getDescriptor(Object element) {
		if (element instanceof ILaunchConfiguration) {
			ILaunchConfiguration llc = (ILaunchConfiguration) element;
			IProject project = getProject(llc);
			if (project == null)
				return null;
			// TODO we need disable project based descriptor here
			return new ProjectBasedLaunchDescriptor(this, project, llc);
		} else if (element instanceof IProject) {
			// this type creates two versions of the descriptor - launch config based
			// and project based. Project based do not have a config. If at least one
			// launch config created, associated with same project, we don't need descriptor with null config
			// anymore so we return null in this case
			IProject project = (IProject) element;
			ProjectBasedLaunchDescriptor desc = new ProjectBasedLaunchDescriptor(this, project, null);
			ILaunchDescriptor[] lds = getManager().getLaunchDescriptors();
			for (int i = 0; i < lds.length; i++) {
				ILaunchDescriptor ld = lds[i];
				if (isBetter(ld, desc)) {
					return null;// there is a better descriptor already
				}
			}
			return desc;
		}
		return null;
	}

	/**
	 * Return true is a is better then b (which would eliminate b)
	 */
	protected boolean isBetter(ILaunchDescriptor a, ILaunchDescriptor b) {
		if (a instanceof ProjectBasedLaunchDescriptor && b instanceof ProjectBasedLaunchDescriptor) {
			ProjectBasedLaunchDescriptor pa = (ProjectBasedLaunchDescriptor) a;
			ProjectBasedLaunchDescriptor pb = (ProjectBasedLaunchDescriptor) b;
			if (pb.getProject().equals(pa.getProject())
			        && pa.getLaunchConfigurationType().equals(pb.getLaunchConfigurationType())
			        && pa.getLaunchConfiguration() != null
			        && pb.getLaunchConfiguration() == null) {
				// a is for same project and same type, but actually have non-null configuraton
				return true;
			}
		}
		return false;
	}

	protected abstract IProject getProject(ILaunchConfiguration llc);
}