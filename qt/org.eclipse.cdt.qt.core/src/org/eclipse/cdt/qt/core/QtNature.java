/*******************************************************************************
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Doug Schaefer (QNX) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class QtNature implements IProjectNature {
	private static final String ID = "org.eclipse.cdt.qt.core.qtNature";

	public static boolean hasNature(IProject project) {
		try {
			return project.hasNature(ID);
		} catch (CoreException e) {
			QtPlugin.log(e);
			return false;
		}
	}

	public static void addNature(IProject project) throws CoreException {
		if (project.isOpen()) {
			if (hasNature(project))
				return;
			
			IProjectDescription desc = project.getDescription();
			String[] oldIds = desc.getNatureIds();
			String[] newIds = new String[oldIds.length + 1];
			newIds[oldIds.length] = ID;
			desc.setNatureIds(newIds);
		}
	}
	
	private IProject project;

	@Override
	public void configure() throws CoreException {
	}

	@Override
	public void deconfigure() throws CoreException {
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
