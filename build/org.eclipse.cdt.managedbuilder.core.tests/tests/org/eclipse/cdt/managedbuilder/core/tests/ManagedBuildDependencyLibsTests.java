/*******************************************************************************
 * Copyright (c) 2011, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.FolderInfo;
import org.eclipse.cdt.managedbuilder.tcmodification.IConfigurationModification;
import org.eclipse.cdt.managedbuilder.tcmodification.IToolChainModificationManager;
import org.eclipse.cdt.managedbuilder.testplugin.AbstractBuilderTest;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ManagedBuildDependencyLibsTests extends AbstractBuilderTest {
	private static final String PROJ_PATH = "depLibsProjects";
	private static final String MESSAGE_TAIL = " (see .log file for more details).";

	private IProject fTapp, fTlib, fTobjs;

	private IToolChain[] allToolChains;

	private void buildProject(IProject curProject) {

		try {
			IProject[] referencedProjects = curProject.getReferencedProjects();
			for (int i = 0; i < referencedProjects.length; ++i)
				buildProject(referencedProjects[i]);

			// Build the project in order to generate the makefiles
			curProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new NullProgressMonitor());
		} catch (CoreException e) {
			fail(e.getStatus().getMessage());
		} catch (OperationCanceledException e) {
			fail("the project \"" + curProject.getName() + "\" build was cancelled, exception message: "
					+ e.getMessage());
		}

	}

	@Override
	public void setUp() {
		// Don't run super class setUp
	}

	@BeforeEach
	public void setUpLocal() throws Exception {
		allToolChains = ManagedBuildManager.getRealToolChains();
		IWorkspaceDescription wsDescription = ResourcesPlugin.getWorkspace().getDescription();
		wsDescription.setAutoBuilding(false);
		ResourcesPlugin.getWorkspace().setDescription(wsDescription);
		assertNotNull(fTapp = ManagedBuildTestHelper.loadProject("tapp", PROJ_PATH), "Cannot create tapp project");
		assertNotNull(fTlib = ManagedBuildTestHelper.loadProject("tlib", PROJ_PATH), "Cannot create tlib project");
		assertNotNull(fTobjs = ManagedBuildTestHelper.loadProject("tobjs", PROJ_PATH), "Cannot create tobjs project");
		IProjectDescription projDescription = fTapp.getDescription();
		projDescription.setReferencedProjects(new IProject[] { fTlib, fTobjs });
		fTapp.setDescription(projDescription, new NullProgressMonitor());
		IToolChain toolChain = setToolChain(fTapp, null);
		assertNotNull(toolChain, "No compatible tool chain.");
		setToolChain(fTlib, toolChain);
		setToolChain(fTobjs, toolChain);
	}

	@AfterEach
	public void tearDownLocal() throws Exception {
		ManagedBuildTestHelper.removeProject(fTapp.getName());
		ManagedBuildTestHelper.removeProject(fTlib.getName());
		ManagedBuildTestHelper.removeProject(fTobjs.getName());
	}

	private void findFiles(IResource dir, String pattern, List<IFile> files) {
		IResource resource = null;
		try {
			IResource[] members;
			if (dir instanceof IContainer)
				members = ((IContainer) dir).members(IFolder.INCLUDE_PHANTOMS);
			else if (dir instanceof IFolder)
				members = ((IFolder) dir).members(IFolder.INCLUDE_PHANTOMS);
			else // Not possible
				return;
			for (int i = 0; i < members.length; ++i) {
				resource = members[i];
				if (resource.getType() == IResource.FOLDER)
					findFiles(resource, pattern, files);
				else {
					if (resource.getName().matches(pattern))
						files.add((IFile) resource);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
			fail("Error while collecting files." + MESSAGE_TAIL);
		}
	}

	private IToolChain setToolChain(IProject project, IToolChain setTo) {
		try {
			IConfiguration cfg = getProjectConfiguration(project);
			IToolChain currentToolChain = cfg.getToolChain();

			IToolChainModificationManager mngr = ManagedBuildManager.getToolChainModificationManager();
			IConfigurationModification cfgM = (IConfigurationModification) mngr
					.createModification(cfg.getRootFolderInfo());
			FolderInfo folderInfo = (FolderInfo) cfg.getRootFolderInfo();

			if (setTo == null) {
				for (int i = 0; i < allToolChains.length; ++i) {
					// If predefined tool chain supported, leave it alone
					if (allToolChains[i].equals(currentToolChain))
						return currentToolChain;
					// In the same loop try to find compatible tool chain
					if (setTo == null) {
						IBuilder builder = allToolChains[i].getBuilder();
						if (cfg.isBuilderCompatible(builder) && folderInfo.isToolChainCompatible(allToolChains[i]))
							setTo = allToolChains[i];
					}
				}
			}
			if (null != setTo) {
				cfgM.setToolChain(setTo);
				assertEquals(setTo, cfgM.getToolChain());
				assertEquals(setTo.getBuilder(), cfgM.getBuilder());
			}
		} catch (CoreException e) {
			e.printStackTrace();
			fail("Error. " + MESSAGE_TAIL);
		}
		return setTo;
	}

	private IConfiguration getProjectConfiguration(IProject project) throws CoreException {
		ICProjectDescription cProjDescription = CoreModel.getDefault().createProjectDescription(project, true);
		return ManagedBuildManager.getConfigurationForDescription(cProjDescription.getConfigurations()[0]);

	}

	private void rebuildArtefact(IProject project) {
		//		deleteFiles(getProjectFolder(project),
		//				getArtefactFullName(project),
		//				new NullProgressMonitor());
		try {
			project.build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
			fail("Cannot clean project " + fTapp.getName() + '.' + MESSAGE_TAIL);
		}
		buildProject(project);
	}

	private long getArtifactTimeStamp(IProject project) {
		List<IFile> files = new ArrayList<>();
		findFiles(project, getArtefactFullName(project), files);
		if (files.size() == 0) // File not exists
			return 0;
		IFile artefact = files.iterator().next();
		return artefact.getModificationStamp();
	}

	private String getArtefactFullName(IProject project) {
		IConfiguration cfg = null;
		try {
			cfg = getProjectConfiguration(project);
		} catch (CoreException e) {
			e.printStackTrace();
			fail("Error. " + MESSAGE_TAIL);
		}
		String name = cfg.getArtifactName();
		String ext = cfg.getArtifactExtension();
		if ((null != ext) && (ext.length() > 0)) {
			name += '.';
			name += ext;
		}
		return name;
	}

	@Test
	public void testDepLibs() {
		buildProject(fTapp);
		long timeStamp = getArtifactTimeStamp(fTapp);
		if (timeStamp == 0) {
			fail("Cannot build project " + fTapp.getName());
		}
		try { // To be sure that in case of build the time should be changed
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Do nothing
		}
		// Check if no build any more
		buildProject(fTapp);
		if (timeStamp != getArtifactTimeStamp(fTapp)) {
			fail("Error. This time it should be nothing to build");
		}
		rebuildArtefact(fTlib);
		buildProject(fTapp);
		if (timeStamp == getArtifactTimeStamp(fTapp)) {
			fail("Error. This time it should build application.");
		}
	}

	@Test
	public void testDepUObjs() {
		buildProject(fTapp);
		long timeStamp = getArtifactTimeStamp(fTapp);
		if (timeStamp == 0) {
			fail("Cannot build project " + fTapp.getName());
		}
		try { // To be sure that in case of build the time should be changed
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Do nothing
		}
		// Check if no build any more
		buildProject(fTapp);
		if (timeStamp != getArtifactTimeStamp(fTapp)) {
			fail("Error. This time it should be nothing to build");
		}
		//		deleteFiles(getProjectFolder(fTobjs), "*.o", new NullProgressMonitor());
		rebuildArtefact(fTobjs);
		buildProject(fTapp);
		if (timeStamp == getArtifactTimeStamp(fTapp)) {
			fail("Error. This time it should build application.");
		}
	}

	// Tests that the URI used to get the time stamp of the artifact is escaped correctly
	// See AdditionalInput.getArtifactTimeStamp(IToolChain toolChain)
	@Test
	public void testGetArtifactTimeStampEscapeURI_bug377295() throws CoreException {
		setWorkspace("regressions");
		final IProject project = loadProject("helloworldC");
		IManagedBuildInfo buildInfo = ManagedBuildManager.getBuildInfo(project);
		IConfiguration[] configs = buildInfo.getManagedProject().getConfigurations();
		for (IConfiguration configuration : configs) {
			configuration.setArtifactName("test [377295]");
		}

		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		// This will trigger AdditionalInput.getArtifactTimeStamp to get called, no IllegalArgumentException should be thrown
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
	}
}