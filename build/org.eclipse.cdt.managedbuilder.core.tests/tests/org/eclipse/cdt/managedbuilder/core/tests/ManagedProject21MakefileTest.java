/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/

/**********************************************************************
 * These tests are for a 2.1 style tool integration.  That is, the
 * tool integration does not use any 3.0 elements or attributes,
 * including InputType, OutputType, etc.
 **********************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.projectconverter.UpdateManagedProjectManager;
import org.eclipse.cdt.managedbuilder.testplugin.CTestPlugin;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.dialogs.IOverwriteQuery;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ManagedProject21MakefileTests extends TestCase {
	private IPath resourcesLocation = new Path(
			CTestPlugin.getFileInPlugin(new Path("resources/test21Projects/")).getAbsolutePath());
	public static final String MBS_TEMP_DIR = "MBSTemp";

	boolean pathVariableCreated = false;

	public ManagedProject21MakefileTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedProject21MakefileTests.class.getName());

		suite.addTest(new ManagedProject21MakefileTests("testSingleFileExe"));
		suite.addTest(new ManagedProject21MakefileTests("testTwoFileSO"));
		suite.addTest(new ManagedProject21MakefileTests("testMultiResConfig"));
		suite.addTest(new ManagedProject21MakefileTests("testLinkedLib"));
		//  TODO: testLinkedFolder fails intermittently saying that it cannot find
		//        the makefiles to compare.  This appears to be a test set issue,
		//        rather than an MBS functionality issue
		suite.addTest(new ManagedProject21MakefileTests("testLinkedFolder"));

		return suite;
	}

	@Override
	protected void tearDown() throws Exception {
		removePathVariables();
		super.tearDown();
	}

	private IProject[] createProject(String projName, IPath location, String projectTypeId, boolean containsZip) {
		Path path = new Path("resources/test21Projects/" + projName);
		File testDir = CTestPlugin.getFileInPlugin(path);
		if (testDir == null) {
			fail("Test project directory " + path + " is missing.");
			return null;
		}

		ArrayList<IProject> projectList = null;
		if (containsZip) {
			File projectZips[] = testDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.isDirectory())
						return false;
					return true;
				}
			});

			projectList = new ArrayList<>(projectZips.length);
			for (int i = 0; i < projectZips.length; i++) {
				try {
					String projectName = projectZips[i].getName();
					if (!projectName.endsWith(".zip"))
						continue;

					projectName = projectName.substring(0, projectName.length() - ".zip".length());
					if (projectName.length() == 0)
						continue;
					IProject project = ManagedBuildTestHelper.createProject(projectName, projectZips[i], location,
							projectTypeId);
					if (project != null)
						projectList.add(project);
				} catch (Exception e) {
				}
			}
			if (projectList.size() == 0) {
				fail("No projects found in test project directory " + testDir.getName()
						+ ".  The .zip file may be missing or corrupt.");
				return null;
			}
		} else {
			try {
				IProject project = ManagedBuildTestHelper.createProject(projName, null, location, projectTypeId);
				if (project != null)
					projectList = new ArrayList<>(1);
				projectList.add(project);
			} catch (Exception e) {
			}
		}

		return projectList.toArray(new IProject[projectList.size()]);
	}

	private IProject[] createProjects(String projName, IPath location, String projectTypeId, boolean containsZip) {

		//  In case the projects need to be updated...
		IOverwriteQuery queryALL = new IOverwriteQuery() {
			@Override
			public String queryOverwrite(String file) {
				return ALL;
			}
		};

		UpdateManagedProjectManager.setBackupFileOverwriteQuery(queryALL);
		UpdateManagedProjectManager.setUpdateProjectQuery(queryALL);

		IProject projects[] = createProject(projName, location, projectTypeId, containsZip);
		return projects;
	}

	private boolean buildProjects(String benchmarkDir, IProject projects[], IPath[] files) {
		if (projects == null || projects.length == 0)
			return false;

		boolean succeeded = true;
		for (int i = 0; i < projects.length; i++) {
			IProject curProject = projects[i];

			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(curProject);

			//check whether the managed build info is converted
			boolean isCompatible = UpdateManagedProjectManager.isCompatibleProject(info);
			assertTrue(isCompatible);

			if (isCompatible) {
				// Build the project in order to generate the maekfiles
				try {
					curProject.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
				} catch (CoreException e) {
					fail(e.getStatus().getMessage());
				} catch (OperationCanceledException e) {
					fail("the project \"" + curProject.getName() + "\" build was cancelled, exception message: "
							+ e.getMessage());
				}

				//compare the generated makefiles to their benchmarks
				if (files != null && files.length > 0) {
					if (i == 0) {
						String configName = info.getDefaultConfiguration().getName();
						IPath buildDir = Path.fromOSString(configName);
						//						succeeded = ManagedBuildTestHelper.compareBenchmarks(curProject, buildDir, files);
						IPath benchmarkLocationBase = resourcesLocation.append(benchmarkDir);
						IPath buildLocation = curProject.getLocation().append(buildDir);
						succeeded = ManagedBuildTestHelper.compareBenchmarks(curProject, buildLocation, files,
								benchmarkLocationBase);
					}
				}
			}
		}

		if (succeeded) { //  Otherwise leave the projects around for comparison
			for (int i = 0; i < projects.length; i++)
				ManagedBuildTestHelper.removeProject(projects[i].getName());
		}
		return succeeded;
	}

	private void createPathVariable(IPath tmpDir) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace = ResourcesPlugin.getWorkspace();
		IPathVariableManager pathMan = workspace.getPathVariableManager();
		String name = MBS_TEMP_DIR;
		try {
			if (pathMan.validateName(name).isOK() && pathMan.validateValue(tmpDir).isOK()) {
				pathMan.setValue(name, tmpDir);
				assertTrue(pathMan.isDefined(name));
			} else {
				fail("could not create the path variable " + name);
			}
		} catch (Exception e) {
			fail("could not create the path variable " + name);
		}
	}

	private void removePathVariables() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace = ResourcesPlugin.getWorkspace();
		IPathVariableManager pathMan = workspace.getPathVariableManager();
		pathMan.setValue(MBS_TEMP_DIR, null);
	}

	private void createFileLink(IProject project, IPath tmpDir, String linkName, String fileName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String name = MBS_TEMP_DIR;
		if (!pathVariableCreated) {
			createPathVariable(tmpDir);
			pathVariableCreated = true;
		}

		try {
			// Now we can create a linked resource relative to the defined path variable:
			IFile linkF1 = project.getFile(linkName);
			IPath location = new Path("MBSTemp/" + fileName);
			if (workspace.validateLinkLocation(linkF1, location).isOK()) {
				linkF1.createLink(location, IResource.NONE, null);
			} else {
				fail("could not create the link to " + name);
			}
		} catch (Exception e) {
			fail("could not create the link to " + name + ": " + e);
		}
	}

	/* (non-Javadoc)
	 * tests 2.1 style tool integration for a single file executable
	 */
	public void testSingleFileExe() {
		IPath[] makefiles = { Path.fromOSString("makefile"), Path.fromOSString("objects.mk"),
				Path.fromOSString("sources.mk"), Path.fromOSString("subdir.mk") };
		IProject[] projects = createProjects("singleFileExe", null, null, true);
		buildProjects("singleFileExe", projects, makefiles);
	}

	/* (non-Javadoc)
	 * tests 2.1 style tool integration for a two file SO
	 */
	public void testTwoFileSO() {
		IPath[] makefiles = { Path.fromOSString("makefile"), Path.fromOSString("objects.mk"),
				Path.fromOSString("sources.mk"), Path.fromOSString("subdir.mk") };
		IProject[] projects = createProjects("twoFileSO", null, null, true);
		buildProjects("twoFileSO", projects, makefiles);
	}

	/* (non-Javadoc)
	 * tests 2.1 style tool integration for multiple source files & a resource configuration
	 */
	public void testMultiResConfig() {
		IPath[] makefiles = { Path.fromOSString("makefile"), Path.fromOSString("objects.mk"),
				Path.fromOSString("sources.mk"), Path.fromOSString("subdir.mk"), Path.fromOSString("source1/subdir.mk"),
				Path.fromOSString("source2/subdir.mk"), Path.fromOSString("source2/source21/subdir.mk") };
		IProject[] projects = createProjects("multiResConfig", null, null, true);
		buildProjects("multiResConfig", projects, makefiles);
	}

	/* (non-Javadoc)
	 * tests 2.1 style tool integration for linked files
	 */
	public void testLinkedLib() throws IOException {
		boolean succeeded = false;
		IPath[] makefiles = { Path.fromOSString("makefile"), Path.fromOSString("objects.mk"),
				//Path.fromOSString("subdir.mk")   // Can't compare this yet since it contains absolute paths!
				Path.fromOSString("sources.mk") };
		IPath[] linkedFiles = { Path.fromOSString("f1.c"), Path.fromOSString("f2.c"), Path.fromOSString("test_ar.h") };
		File srcDirFile = CTestPlugin.getFileInPlugin(new Path("resources/test21Projects/linkedLib/"));
		IPath srcDir = Path.fromOSString(srcDirFile.toString());
		IPath tmpRootDir = Path.fromOSString(Files.createTempDirectory("testLinkedLib").toAbsolutePath().toString());
		IPath tmpSubDir = Path.fromOSString("CDTMBSTest");
		IPath tmpDir = ManagedBuildTestHelper.copyFilesToTempDir(srcDir, tmpRootDir, tmpSubDir, linkedFiles);
		try {
			IProject[] projects = createProjects("linkedLib", null, "cdt.managedbuild.target.testgnu21.lib", true);
			//  There should be only one project.  Add our linked files to it.
			IProject project = projects[0];
			createFileLink(project, tmpDir, "f1.c", "f1.c");
			createFileLink(project, tmpDir, "f2link.c", "f2.c");
			createFileLink(project, tmpDir, "test_ar.h", "test_ar.h");
			//  Build the project
			succeeded = buildProjects("linkedLib", projects, makefiles);
		} finally {
			if (succeeded)
				ManagedBuildTestHelper.deleteTempDir(tmpRootDir, tmpSubDir, linkedFiles);
		}
	}

	/* (non-Javadoc)
	 * tests 2.1 style tool integration for a linked folder
	 */
	public void testLinkedFolder() throws IOException {
		boolean succeeded = false;
		IPath[] makefiles = { Path.fromOSString("makefile"), Path.fromOSString("objects.mk"),
				Path.fromOSString("subdir.mk"), Path.fromOSString("sources.mk") };
		IPath[] linkedFiles = { Path.fromOSString("f1.c"), Path.fromOSString("f2.c"), Path.fromOSString("test_ar.h"),
				Path.fromOSString("Benchmarks/makefile"), Path.fromOSString("Benchmarks/objects.mk"),
				Path.fromOSString("Benchmarks/subdir.mk"), Path.fromOSString("Benchmarks/sources.mk") };
		File srcDirFile = CTestPlugin.getFileInPlugin(new Path("resources/test21Projects/linkedFolder/"));
		IPath srcDir = Path.fromOSString(srcDirFile.toString());
		IPath tmpSubDir = Path.fromOSString("CDTMBSTest");
		IPath tmpRootDir = Path.fromOSString(Files.createTempDirectory("testLinkedFolder").toAbsolutePath().toString());
		IPath tmpDir = ManagedBuildTestHelper.copyFilesToTempDir(srcDir, tmpRootDir, tmpSubDir, linkedFiles);
		if (!pathVariableCreated) {
			createPathVariable(tmpDir);
			pathVariableCreated = true;
		}
		IPath location = Path.fromOSString(MBS_TEMP_DIR);
		IProject[] projects = createProjects("linkedFolder", location, "cdt.managedbuild.target.testgnu21.lib", false);
		//  Build the project
		succeeded = buildProjects("linkedFolder", projects, makefiles);
		assertTrue(succeeded);
	}
}
