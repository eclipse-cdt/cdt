/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.core.runtime.SubMonitor;

public abstract class FMProjectGenerator extends FMGenerator {

	private String projectName;
	private URI locationURI;
	private IProject[] referencedProjects;

	private IProject project;

	public FMProjectGenerator(String manifestPath) {
		super(manifestPath);
	}

	protected abstract void initProjectDescription(IProjectDescription description) throws CoreException;

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
	protected void populateModel(Map<String, Object> model) {
		super.populateModel(model);

		// Make sure project name is in model
		model.put("projectName", projectName); //$NON-NLS-1$
	}

	@Override
	public void generate(Map<String, Object> model, IProgressMonitor monitor) throws CoreException {
		// Create the project
		createProject(monitor);

		// Generate the files
		super.generate(model, monitor);
	}

	protected IProject createProject(IProgressMonitor monitor) throws CoreException {
		SubMonitor sub = SubMonitor.convert(monitor, "Creating project", 1);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		project = workspace.getRoot().getProject(projectName);
		if (!project.exists()) {
			IProjectDescription description = workspace.newProjectDescription(projectName);
			description.setLocationURI(locationURI);
			if (referencedProjects != null) {
				description.setReferencedProjects(referencedProjects);
			}
			initProjectDescription(description);
			project.create(description, sub);
			project.open(sub);
		} else {
			// TODO make sure it's got all our settings or is this an error
			// condition?
		}

		sub.worked(1);
		return project;
	}

}
