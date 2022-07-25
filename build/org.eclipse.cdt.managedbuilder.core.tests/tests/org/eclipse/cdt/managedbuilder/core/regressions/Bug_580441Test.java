/*******************************************************************************
 * Copyright (c) 2011, 2022 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Broadcom Corporation - Bug 303953 test
 *    John Dallaway - Initial implementation (derived from bug 303953 test)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.regressions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;

import org.eclipse.cdt.managedbuilder.testplugin.AbstractBuilderTest;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.junit.jupiter.api.Test;

/**
 * Test that removal of a directory containing source file(s) is
 * processed correctly by the default GnuMakefileGenerator.
 */
public class Bug_580441Test extends AbstractBuilderTest {

	@Test
	public void testBuildAfterPopulatedSourceFolderDelete() throws CoreException {
		setWorkspace("regressions");
		final IProject app = loadProject("helloworldC");

		// Create additional source file at src/test/test.c
		final IFolder testFolder = app.getFolder("src/test");
		testFolder.create(false, false, null);
		testFolder.getFile("test.c").create(new ByteArrayInputStream("int test;".getBytes()), false, null);

		// Build debug configuration
		setActiveConfigurationByName(app, "Debug");
		buildConfig(app.getActiveBuildConfig());
		assertTrue(app.getFile("Debug/src/test/test.o").exists(), "test.o not created");

		// Delete folder containing test.c and build again
		testFolder.delete(false, null);
		buildConfig(app.getActiveBuildConfig());
		assertFalse(app.getFolder("Debug/src/test").exists(), "test folder not deleted");
	}

	private void buildConfig(IBuildConfiguration config) throws CoreException {
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				ResourcesPlugin.getWorkspace().build(new IBuildConfiguration[] { config },
						IncrementalProjectBuilder.INCREMENTAL_BUILD, true, monitor);
			}
		}, null);
	}

}
