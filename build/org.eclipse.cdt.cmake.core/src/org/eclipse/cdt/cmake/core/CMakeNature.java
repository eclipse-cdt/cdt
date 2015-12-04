/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core;

import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class CMakeNature implements IProjectNature {

	public static final String ID = Activator.getId() + ".cmakeNature"; //$NON-NLS-1$

	private IProject project;

	public static boolean hasNature(IProject project) {
		try {
			return project.hasNature(ID);
		} catch (CoreException e) {
			Activator.log(e);
			return false;
		}
	}

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
