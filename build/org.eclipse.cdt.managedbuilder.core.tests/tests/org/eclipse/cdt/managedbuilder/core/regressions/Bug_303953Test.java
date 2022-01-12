/*******************************************************************************
 * Copyright (c) 2011, 2021 Broadcom Corporation and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.settings.model.CSourceEntry;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.testplugin.AbstractBuilderTest;
import org.eclipse.cdt.managedbuilder.testplugin.ResourceDeltaVerifier;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.Test;

/**
 * Tests that removing the last source file from a directory
 * causes the subdir.mk to be regenerated, and associated dervied
 * files should be deleted.
 */
public class Bug_303953Test extends AbstractBuilderTest {

	@Test
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
		verifier.addIgnore(
				new IResource[] { getWorkspace().getRoot(), app, app.getFile(".project"), app.getFile(".cproject") });
		verifyBuild(app, IncrementalProjectBuilder.FULL_BUILD, verifier);

		// Delete helloworldC
		IFile srcFile = app.getFile("src/helloworldC.c");
		assertTrue(srcFile.exists(), "1.1");
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

	/**
	 * Tests that source files in the root of the project are not treated
	 * specially with respect to removing the last of them (bug 575490).
	 */
	@Test
	public void testBuildAfterRootSourcefileDelete() throws CoreException, UnsupportedEncodingException {
		setWorkspace("regressions");
		final IProject app = loadProject("helloworldC");
		setActiveConfigurationByName(app, "Debug");

		// Change the source folder from /src to /
		ManagedBuildManager.getBuildInfo(app).getDefaultConfiguration()
				.setSourceEntries(new ICSourceEntry[] { new CSourceEntry(Path.ROOT, null, 0) });

		// Add a new source file in the root
		app.getFile("additional.c").create(new ByteArrayInputStream("int x = 42;\n".getBytes("UTF-8")), true, null);

		// Build once: This will create a root subdir.mk referring to additional.c
		ICoreRunnable build = new ICoreRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				getWorkspace().build(new IBuildConfiguration[] { app.getActiveBuildConfig() },
						IncrementalProjectBuilder.FULL_BUILD, true, monitor);
			}
		};
		getWorkspace().run(build, null);
		IMarker[] markers = app.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		assertEquals(0, markers.length, "first build should succeed with no error markers");

		// Remove additional.c behind Eclipse's back
		app.getFile("additional.c").getLocation().toFile().delete();

		// Build again: This is expected to fail because at the time the
		// makefile is updated, the absence of additional.c hasn't been noticed
		// yet. Only the refresh done at the end of this build will notice the
		// removal.
		getWorkspace().run(build, null);
		markers = app.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		assertNotEquals(0, markers.length, "second build should produce an error marker");
		// commented out because the exact wording may depend on the version of 'make' used
		// assertEquals("make: *** No rule to make target '../additional.c', needed by 'additional.o'.  Stop.",
		//   markers[0].getAttribute(IMarker.MESSAGE));

		// Build again: The FULL_BUILD will ignore the delta indicating the
		// removal of additional.c and therefore not regenerate the root
		// subdir.mk (because now there are no source files in the root),
		// leaving the stale one there that still refers to additional.c.
		// This should succeed - before the fix, it would fail because the
		// stale subdir.mk would still be included.
		getWorkspace().run(build, null);
		markers = app.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		assertEquals(0, markers.length, "final build should succeed with no error markers");
	}

}
