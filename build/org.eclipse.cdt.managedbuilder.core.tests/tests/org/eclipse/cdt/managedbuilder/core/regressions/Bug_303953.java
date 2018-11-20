/*******************************************************************************
 * Copyright (c) 2011 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Broadcom Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.regressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.managedbuilder.testplugin.AbstractBuilderTest;
import org.eclipse.cdt.managedbuilder.testplugin.ResourceDeltaVerifier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;

/**
 * Tests that removing the last source file from a directory
 * causes the subdir.mk to be regenerated, and associated dervied
 * files should be deleted.
 */
public class Bug_303953 extends AbstractBuilderTest {

	public void testBuildAfterSourcefileDelete() throws CoreException {
		setWorkspace("regressions");
		final IProject app = loadProject("helloworldC");

		List<IResource> buildOutputResources = new ArrayList<>();
		buildOutputResources.addAll(getProjectBuildExeResources("helloworldC", "Debug", "src/helloworldC"));

		// Ensure Debug is the active configuration
		setActiveConfigurationByName(app, "Debug");

		ResourceDeltaVerifier verifier = new ResourceDeltaVerifier();
		verifier.addExpectedChange(buildOutputResources.toArray(new IResource[buildOutputResources.size()]),
				IResourceDelta.ADDED, IResourceDelta.NO_CHANGE);
		verifier.addIgnore(new IResource[] { getWorkspace().getRoot(), app, app.getFile(".project") });
		verifyBuild(app, IncrementalProjectBuilder.FULL_BUILD, verifier);

		// Delete helloworldC
		IFile srcFile = app.getFile("src/helloworldC.c");
		assertTrue("1.1", srcFile.exists());
		srcFile.delete(false, null);

		// Build again
		//   - derived files from helloworldC.c should be removed
		//   - subdir.mk should be changed
		//   - ignore other changes in the build tree (not the subject of this bug...)

		verifier = new ResourceDeltaVerifier();
		// These files should be removed
		IResource[] removed = new IResource[] { app.getFile("Debug/src/helloworldC.o"),
				app.getFile("Debug/src/helloworldC.d") };
		verifier.addExpectedChange(removed, IResourceDelta.REMOVED, IResourceDelta.NO_CHANGE);
		// subdir.mk has been updated
		IResource[] expected = new IResource[] { app.getFile("Debug/src/subdir.mk") };
		verifier.addExpectedChange(expected, IResourceDelta.CHANGED, IResourceDelta.CONTENT);

		// Ignore other resources
		Collection<IResource> ignored = getProjectBuildExeResources("helloworldC", "Debug", "src/helloworldC");
		ignored.removeAll(Arrays.asList(removed));
		ignored.removeAll(Arrays.asList(expected));
		ignored.add(getWorkspace().getRoot());
		ignored.add(app);
		verifier.addIgnore(ignored.toArray(new IResource[ignored.size()]));
		verifyBuild(app, IncrementalProjectBuilder.INCREMENTAL_BUILD, verifier);
	}

}
