/*******************************************************************************
 * Copyright (c) 2017, 2021 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.ui.internal.tests;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.cmake.core.CMakeNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class NewCMakeProjectTest {

	private static SWTWorkbenchBot bot;

	@TempDir
	public static Path TEMP_DIR;

	@BeforeAll
	public static void beforeClass() {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		SWTBotPreferences.TIMEOUT = 10000;
		bot = new SWTWorkbenchBot();
	}

	@BeforeEach
	public void before() {
		bot.resetWorkbench();

		for (SWTBotView view : bot.views(withPartName("Welcome"))) {
			view.close();
		}
		SWTBotPerspective perspective = bot.perspectiveById("org.eclipse.cdt.ui.CPerspective");
		perspective.activate();
		bot.shell().activate();
	}

	@AfterEach
	public void after() {
		// Delete created projects after we are done with them
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		ICoreRunnable runnable = new ICoreRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				for (IProject project : projects) {
					project.delete(true, true, null);
				}
			}
		};
		try {
			workspace.run(runnable, workspace.getRoot(), IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		} catch (CoreException e) {
			fail(e);
		}
	}

	@Test
	@Timeout(value = 2, unit = TimeUnit.MINUTES)
	void createCMakeProject() throws Exception {

		// Activate C/C++ wizard
		bot.menu("File").menu("New").menu("C/C++ Project").click();
		bot.shell("New C/C++ Project").activate();

		// Double click on the template
		SWTBotTable templateTable = bot.table();
		bot.getDisplay().syncExec(() -> {
			for (int i = 0; i < templateTable.rowCount(); ++i) {
				SWTBotTableItem item = templateTable.getTableItem(i);
				if ("CMake Project".equals(item.widget.getData(SWTBotPreferences.DEFAULT_KEY))) {
					item.doubleClick();
					break;
				}
			}
		});

		// Select the shell again since magic wizardry happened
		SWTBotShell newProjectShell = bot.shell("New CMake Project").activate();
		bot.waitUntil(Conditions.shellIsActive("New CMake Project"));
		newProjectShell.setFocus();

		// Create the project
		String projectName = "CMakeTestProj";
		bot.textWithLabel("Project name:").setText(projectName);
		bot.button("Finish").click();
		bot.waitUntil(Conditions.shellCloses(newProjectShell));

		verifyProjectInExplorer(projectName);
	}

	@Test
	@Timeout(value = 2, unit = TimeUnit.MINUTES)
	void importSimpleCMakeProject() throws Exception {
		importCMakeProject("SimpleProject", "SimpleProject");
	}

	@Test
	@Timeout(value = 2, unit = TimeUnit.MINUTES)
	void importNestedCMakeProject() throws Exception {
		importCMakeProject("NestedProject", "NestedProject", "NestedProject/App1", "NestedProject/App2");
	}

	private void importCMakeProject(String projectDir, String... expectedProjects) throws Exception {
		// Copy test project out of the bundle to a temp location first
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		URL url = FileLocator
				.toFileURL(FileLocator.find(bundle, new org.eclipse.core.runtime.Path("projects/" + projectDir), null));
		copyDir(Paths.get(url.getPath()), TEMP_DIR.resolve(projectDir));

		// Activate import wizard
		bot.menu("File").menu("Import...").click();
		bot.shell("Import").activate();

		// Open the smart import wizard
		SWTBotTree wizTree = bot.tree();
		SWTBotTreeItem generalItem = wizTree.getTreeItem("General").expand();
		generalItem.getNode("Projects from Folder or Archive").doubleClick();

		// Select path that contains projects to import and check the project type detection works
		bot.comboBox().setText(TEMP_DIR.resolve(projectDir).toString());
		SWTBotTree projectProposalTree = bot.tree();
		assertEquals(expectedProjects.length, projectProposalTree.getAllItems().length);
		for (SWTBotTreeItem item : projectProposalTree.getAllItems()) {
			assertEquals("CMake Project", item.cell(1), "Project type was not detected");
		}

		// Import and verify the project(s)
		SWTBotShell wizShell = bot.activeShell();
		bot.button("Finish").click();
		bot.waitUntil(Conditions.shellCloses(wizShell));
		for (String expectedProject : expectedProjects) {
			verifyProjectInExplorer(expectedProject);
		}
	}

	private void verifyProjectInExplorer(String projectName) throws Exception {
		// Make sure it shows up in Project Explorer
		SWTBotView explorer = bot.viewByPartName("Project Explorer");
		explorer.show();
		explorer.setFocus();

		// If the project name is a path with >1 segment it was nested, so we need expand the
		// parent project node to make the child project nodes visible
		Path path = Paths.get(projectName);
		SWTBotTreeItem projectNode;
		if (path.getNameCount() > 1) {
			SWTBotTreeItem node = explorer.bot().tree().expandNode(path.getName(0).toString());
			projectNode = node.getNode(path.getFileName().toString());
		} else {
			projectNode = explorer.bot().tree().getTreeItem(path.getFileName().toString());
		}

		// Tests can be unstable if we are too quick, so make sure the project indexer completes
		// and project natures have been assigned before continuing with post-creation verification.
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectNode.getText());
		bot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() throws Exception {
				int natures = project.getDescription().getNatureIds().length;
				ICProject cproject = CoreModel.getDefault().create(project);
				IIndexManager indexManager = CCorePlugin.getIndexManager();
				return natures > 0 && indexManager.isProjectContentSynced(cproject);
			}

			@Override
			public String getFailureMessage() {
				return "Indexer never finished or natures never assigned for project " + project.getName();
			}
		});

		// Make sure it has the right nature
		assertTrue(project.hasNature(CMakeNature.ID), "Project does not have expected nature");

		// Ensure CMakeLists exists
		assertTrue(project.getFile("CMakeLists.txt").exists(), "Project does not have a build file");
	}

	/**
	 * Utility to perform a depth-first copy of a directory tree.
	 */
	private static void copyDir(Path src, Path dest) throws IOException {
		Files.walk(src).forEach(a -> {
			if (!a.equals(src)) {
				Path b = dest.resolve(a.subpath(src.getNameCount(), a.getNameCount()));
				try {
					if (!Files.isDirectory(a)) {
						Files.createDirectories(b.getParent());
						Files.copy(a, b);
					}
				} catch (IOException e) {
					fail(e);
				}
			}
		});
	}
}
