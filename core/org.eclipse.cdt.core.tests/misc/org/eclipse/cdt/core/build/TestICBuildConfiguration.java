/*******************************************************************************
 * Copyright (c) 2022 Renesas Electronics Europe.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.core.build;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for org.eclipse.cdt.core.build.ICBuildConfiguration
 */
public class TestICBuildConfiguration {
	private IProject testProject = null;

	@Before
	public void setup() throws Exception {
		testProject = getProject();
		assertNotNull("Test project must not be null", testProject);
	}

	@After
	public void shutdown() throws Exception {
		if (testProject != null) {
			testProject.delete(true, true, new NullProgressMonitor());
		}
	}

	/**
	 * Tests that ICBuildConfiguration.getBinaryParserIds() meets API. <br>
	 * <code>
	 * List<String> getBinaryParserIds()
	 * </code>
	 */
	@Test
	public void getBinaryParserIdsTest00() throws Exception {
		IBuildConfiguration[] buildConfigs = testProject.getBuildConfigs();
		assertNotNull(buildConfigs, "Must not be null");
		assertNotEquals(0, buildConfigs.length, "Must not be empty");
		IBuildConfiguration buildConfig = buildConfigs[0];
		/*
		 * It's difficult to create a functional BuildConfiguration without a toolchain, so just use
		 * this Error Build Configuration. It is adequate for simply testing the API.
		 */
		ErrorBuildConfiguration errorBuildConfiguration = new ErrorBuildConfiguration(buildConfig, "errorBuildConfig");
		List<String> binaryParserIds = errorBuildConfiguration.getBinaryParserIds();
		assertNull(binaryParserIds, "Must be null");
	}

	private IProject getProject() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProjectDescription desc = root.getWorkspace().newProjectDescription("test");
		IProject project = root.getProject("testProj");
		project.create(desc, new NullProgressMonitor());
		project.open(new NullProgressMonitor());
		return project;
	}
}
