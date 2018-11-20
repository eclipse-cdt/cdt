/*******************************************************************************
 * Copyright (c) 20116 Kichwa Coders Ltd and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jonah Graham (Kichwa Coders) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import java.util.Collection;

import org.eclipse.cdt.managedbuilder.testplugin.AbstractBuilderTest;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class ManagedBuildClean extends AbstractBuilderTest {
	private static final String PROJ_PATH = "testCleanProjects";
	private IProject fInternalBuilderProject;
	private IProject fExternalBuilderProject;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IWorkspaceDescription wsDescription = ResourcesPlugin.getWorkspace().getDescription();
		wsDescription.setAutoBuilding(false);
		ResourcesPlugin.getWorkspace().setDescription(wsDescription);
		assertNotNull("Cannot create testCleanInternal project",
				fInternalBuilderProject = ManagedBuildTestHelper.loadProject("testCleanInternal", PROJ_PATH));
		assertNotNull("Cannot create testCleanExternal project",
				fExternalBuilderProject = ManagedBuildTestHelper.loadProject("testCleanExternal", PROJ_PATH));
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		ManagedBuildTestHelper.removeProject(fInternalBuilderProject.getName());
	}

	public void testCleanInternal() throws Exception {
		helperTestClean(fInternalBuilderProject, false);
	}

	public void testCleanExternal() throws Exception {
		helperTestClean(fExternalBuilderProject, true);
	}

	private void helperTestClean(IProject project, boolean externalBuilder) throws CoreException {

		// do a build and ensure files are present
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		Collection<IResource> resources = getProjectBuildExeResources(project.getName(), "Debug",
				"src/" + project.getName(), externalBuilder);
		for (IResource resource : resources) {
			assertTrue("Resource not found: " + resource, resource.exists());
		}

		// do a clean and make sure files are gone
		project.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
		for (IResource resource : resources) {
			if (!(resource instanceof IFile)) {
				// Only files are removed by clean, not folders
				continue;
			}
			if (externalBuilder && (resource.getName().endsWith(".mk") || resource.getName().equals("makefile"))) {
				// makefiles are not removed when cleaning
				continue;
			}
			assertFalse("Resource not deleted: " + resource, resource.exists());
		}
	}

}
