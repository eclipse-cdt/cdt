/*******************************************************************************
 * Copyright (c) 2011 Broadcom Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Broadcom Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.testplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Abstract builder test which provides utility methods for:
 * <ul>
 * <li>Importing projects into the workspace</li>
 * <li>Adding expected resources to the delta verifier</li>
 * <li>Verifying the delta</li>
 * <li>Printing markers</li>
 * <li>Cleaning up the workspace at the end</li>
 * </ul>
 */
public abstract class AbstractBuilderTest extends TestCase {
	private static final boolean WINDOWS = java.io.File.separatorChar == '\\';

	static final String PATH = "builderTests";

	private String workspace;
	private List<IProject> projects;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setAutoBuilding(false);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		// Bug 327126 Stop the indexer before tearing down so we don't deadlock
		Job.getJobManager().cancel(CCorePlugin.getPDOMManager());
		Job.getJobManager().join(CCorePlugin.getPDOMManager(), null);

		// Clean-up any projects we were using
		for (IProject project : projects) {
			project.delete(true, null);
		}
		projects.clear();
	}

	/**
	 * Run a build of the specified kind, and verify that the resource changes that
	 * result match the expectations of the given verifier.
	 */
	protected void verifyBuild(final IProject project, final int kind, ResourceDeltaVerifier verifier) throws CoreException {
		verifyBuild(new IBuildConfiguration[]{project.getActiveBuildConfig()}, kind, verifier);
	}
	/**
	 * Build the specified configurations, and verify that the resource changes that
	 * result match the expectations of the given verifier.
	 */
	protected void verifyBuild(final IBuildConfiguration[] configs, final int kind, ResourceDeltaVerifier verifier) throws CoreException {
		getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		getWorkspace().addResourceChangeListener(verifier);
		try {
			// batch changes
			IWorkspaceRunnable body = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					getWorkspace().build(configs, kind, true, monitor);
				}
			};
			getWorkspace().run(body, null);
		} finally {
			assertTrue(verifier.getMessage(), verifier.isDeltaValid());
			getWorkspace().removeResourceChangeListener(verifier);
			printAllMarkers();
		}
	}


	/**
	 * Set the active configuration of a project by configuration name
	 */
	protected void setActiveConfigurationByName(IProject project, String cfgName) throws CoreException {
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		ICProjectDescription desc = mngr.getProjectDescription(project, true);
		ICConfigurationDescription cfg = desc.getConfigurationByName(cfgName);
		assertNotNull(cfg);
		desc.setActiveConfiguration(cfg);
		mngr.setProjectDescription(project, desc);
		// FIXME: enable when cdt.core knows about core.resources build configurations
//		assertTrue(project.getActiveBuildConfig().getName().equals(cfg.getName()));
	}

	protected Collection<IResource> getProjectBuildExeResources(String projectName, String cfgName, String obj) throws CoreException {
		return getProjectBuildExeResources(projectName, cfgName, new String[]{obj});
	}

	protected Collection<IResource> getProjectBuildLibResources(String projectName, String cfgName, String obj) throws CoreException {
		return getProjectBuildLibResources(projectName, cfgName, new String[]{obj});
	}

	protected Collection<IResource> getProjectBuildSharedLibResources(String projectName, String cfgName, String obj) throws CoreException {
		return getProjectBuildSharedLibResources(projectName, cfgName, new String[]{obj});
	}

	protected Collection<IResource> getProjectBuildExeResources(String projectName, String cfgName, String[] objs) throws CoreException {
		Collection<IResource> resources = getProjectBuildResources(projectName, cfgName, objs);
		IProject project = getWorkspace().getRoot().getProject(projectName);
		IFolder buildDir = project.getFolder(cfgName);
		resources.add(buildDir.getFile(projectName + (WINDOWS ? ".exe" : "")));
		return resources;
	}

	protected Collection<IResource> getProjectBuildLibResources(String projectName, String cfgName, String[] objs) throws CoreException {
		Collection<IResource> resources = getProjectBuildResources(projectName, cfgName, objs);
		IProject project = getWorkspace().getRoot().getProject(projectName);
		IFolder buildDir = project.getFolder(cfgName);
		resources.add(buildDir.getFile("lib" + projectName + ".a"));
		return resources;
	}

	protected Collection<IResource> getProjectBuildSharedLibResources(String projectName, String cfgName, String[] objs) throws CoreException {
		Collection<IResource> resources = getProjectBuildResources(projectName, cfgName, objs);
		IProject project = getWorkspace().getRoot().getProject(projectName);
		IFolder buildDir = project.getFolder(cfgName);
		resources.add(buildDir.getFile("lib" + projectName + ".so"));
		return resources;
	}

	/**
	 * Returns an array of resources expected to be generated by building a project configuration.
	 * The object files expected to be output can also be specified.
	 */
	protected Collection<IResource> getProjectBuildResources(String projectName, String cfgName, String[] objs) throws CoreException {
		IProject project = getWorkspace().getRoot().getProject(projectName);
		IFolder buildDir = project.getFolder(cfgName);
		Collection<IResource> resources = new LinkedHashSet<IResource>();
		resources.add(buildDir);
		resources.add(buildDir.getFile("makefile"));
		resources.add(buildDir.getFile("objects.mk"));
		resources.add(buildDir.getFile("sources.mk"));
		for (String obj : objs) {
			resources.add(buildDir.getFile(obj + ".d"));
			resources.add(buildDir.getFile(obj + ".o"));
			// Add subdir.mk in the same directory
			resources.add(buildDir.getFile(new Path(obj).removeLastSegments(1).append("subdir.mk")));
			// If the parent of the obj doesn't exist, then ensure we're expecting that too...
			IPath p = new Path(obj).removeLastSegments(1);
			while (p.segmentCount() > 0) {
				IFolder folder = buildDir.getFolder(p);
				resources.add(folder);
				p = p.removeLastSegments(1);
			}
		}
		return resources;
	}

	public AbstractBuilderTest() {
		super();
	}

	public AbstractBuilderTest(String name) {
		super(name);
	}

	protected void setWorkspace(String name) {
		workspace = name;
		projects = new ArrayList<IProject>();
	}

	protected IProject loadProject(String name) throws CoreException {
		ManagedBuildTestHelper.loadProject(name, PATH + "/" + workspace);
		IProject project = getWorkspace().getRoot().getProject(name);
		assertTrue(project.exists());
		projects.add(project);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		return project;
	}

	private List<IMarker> getAllMarkers() throws CoreException {
		List<IMarker> markers = new ArrayList<IMarker>();
		for (IProject project : projects)
			markers.addAll(Arrays.asList(project.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE)));
		return markers;
	}

	protected void printAllMarkers() throws CoreException {
		List<IMarker> markers = getAllMarkers();
		String[] attributes = new String[] {IMarker.LINE_NUMBER, IMarker.SEVERITY, IMarker.MESSAGE, IMarker.LOCATION /*, ICModelMarker.C_MODEL_MARKER_CONFIGURATION_NAME*/};
		StringBuilder sb = new StringBuilder();
		for (IMarker m : markers) {
			// Project
			if (m.getResource().getProject() == null)
				sb.append("?");
			else
				sb.append(m.getResource().getProject().getName());
			// Resource workspace path
			sb.append(", "); //$NON-NLS-1$
			sb.append(m.getResource().getFullPath());
			Object[] attrs = m.getAttributes(attributes);
			sb.append(":"); //$NON-NLS-1$
			int i = 0;
			// line number
			if (attrs[i] != null)
				sb.append(" line " + attrs[i]);
			// severity
			if (attrs[++i] != null) {
				switch ((Integer)attrs[i++]) {
				case IMarker.SEVERITY_ERROR:
					sb.append(" ERROR");
					break;
				case IMarker.SEVERITY_WARNING:
					sb.append(" WARNING");
					break;
				case IMarker.SEVERITY_INFO:
					sb.append(" INFO");
					break;
				}
			}
			// append the rest of the string fields
			do  {
				if (attrs[i] != null)
					sb.append(" " + attrs[i]);
			} while (++i < attrs.length);
			// Finally print the string
			System.err.println(sb.toString());
			sb.setLength(0);
		}
	}

	protected void setAutoBuilding(boolean value) throws CoreException {
		IWorkspace workspace = getWorkspace();
		if (workspace.isAutoBuilding() == value)
			return;
		IWorkspaceDescription desc = workspace.getDescription();
		desc.setAutoBuilding(value);
		workspace.setDescription(desc);
	}

	protected IWorkspace getWorkspace() throws CoreException {
		return ResourcesPlugin.getWorkspace();
	}

}