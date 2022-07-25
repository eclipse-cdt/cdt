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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.managedbuilder.testplugin.AbstractBuilderTest;
import org.eclipse.cdt.managedbuilder.testplugin.ResourceDeltaVerifier;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.Test;

/**
 * Tests that removing a directory containing source file(s) is
 * processed correctly.
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
		List<IResource> buildOutputResources = new ArrayList<>();
		buildOutputResources.addAll(getProjectBuildExeResources("helloworldC", "Debug",
				new String[] { "src/helloworldC", "src/test/test" }));

		// Ensure Debug is the active configuration
		setActiveConfigurationByName(app, "Debug");

		ResourceDeltaVerifier verifier = new ResourceDeltaVerifier();
		verifier.addExpectedChange(buildOutputResources.toArray(new IResource[buildOutputResources.size()]),
				IResourceDelta.ADDED, IResourceDelta.NO_CHANGE);
		verifier.addIgnore(
				new IResource[] { getWorkspace().getRoot(), app, app.getFile(".project"), app.getFile(".cproject") });
		verifyBuild(app, IncrementalProjectBuilder.FULL_BUILD, verifier);

		// Delete test folder containing test.c
		assertTrue(testFolder.exists(), "test folder not found");
		testFolder.delete(false, null);

		// Build again
		//   - files and folder derived from test/test.c should be removed
		//   - ignore other changes in the build tree (not the subject of this bug...)

		verifier = new ResourceDeltaVerifier();
		// These resources should be removed
		IResource[] removed = new IResource[] { app.getFile("Debug/src/test/test.o"),
				app.getFile("Debug/src/test/test.d"), app.getFile("Debug/src/test/subdir.mk"), testFolder };
		verifier.addExpectedChange(removed, IResourceDelta.REMOVED, IResourceDelta.NO_CHANGE);

		// Ignore other resources
		Collection<IResource> ignored = getProjectBuildExeResources("helloworldC", "Debug",
				new String[] { "src/helloworldC", "subdir.mk" });
		ignored.removeAll(Arrays.asList(removed));
		ignored.add(getWorkspace().getRoot());
		ignored.add(app);
		verifier.addIgnore(ignored.toArray(new IResource[ignored.size()]));
		verifyBuild(app, IncrementalProjectBuilder.INCREMENTAL_BUILD, verifier);
	}

}
