/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tools.templates.freemarker;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class FMProjectGenerator extends FMGenerator {

	private String projectName;
	private URI locationURI;
	private IProject[] referencedProjects;

	private IProject project;

	protected abstract String[] getProjectNatures();

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public void setLocationURI(URI locationURI) {
		this.locationURI = locationURI;
	}

	public void setReferencedProjects(IProject[] referencedProjects) {
		this.referencedProjects = referencedProjects;
	}

	public IProject getProject() {
		return project;
	}

	@Override
	public void generate(Map<String, Object> model, IProgressMonitor monitor) throws CoreException {
		// Make sure project name is in model
		model.put("projectName", projectName); //$NON-NLS-1$

		// Create the project
		createProject(monitor);

		// Generate the files
		super.generate(model, monitor);
	}

	protected void createProject(IProgressMonitor monitor) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		project = workspace.getRoot().getProject(projectName);
		if (!project.exists()) {
			IProjectDescription description = workspace.newProjectDescription(projectName);
			description.setLocationURI(locationURI);
			if (referencedProjects != null) {
				description.setReferencedProjects(referencedProjects);
			}
			description.setNatureIds(getProjectNatures());
			project.create(description, monitor);
			project.open(monitor);
		} else {
			// TODO make sure it's got all our settings or is this an error
			// condition?
		}
	}

}
