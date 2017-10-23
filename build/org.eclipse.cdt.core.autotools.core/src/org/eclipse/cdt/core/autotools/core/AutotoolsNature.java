/*******************************************************************************
 * Copyright (c) 2017 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.autotools.core;

import org.eclipse.cdt.core.build.CBuilder;
import org.eclipse.cdt.core.autotools.core.internal.Activator;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

public class AutotoolsNature implements IProjectNature {

	public static final String ID = Activator.PLUGIN_ID + ".autotoolsNature"; //$NON-NLS-1$

	private IProject project;

	public static void setupBuilder(IProjectDescription projDesc) throws CoreException {
		ICommand command = projDesc.newCommand();
		CBuilder.setupBuilder(command);
		projDesc.setBuildSpec(new ICommand[] { command });
	}
	
	@Override
	public void configure() throws CoreException {
		IProjectDescription projDesc = project.getDescription();
		setupBuilder(projDesc);
		project.setDescription(projDesc, new NullProgressMonitor());
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
