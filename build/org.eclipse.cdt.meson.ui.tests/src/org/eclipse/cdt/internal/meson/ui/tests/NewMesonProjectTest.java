/*******************************************************************************
 * Copyright (c) 2017, 2018 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 		Red Hat Inc. - modified for Meson testing
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui.tests;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withRegex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.meson.ui.tests.utils.CloseWelcomePageRule;
import org.eclipse.cdt.meson.core.MesonNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@SuppressWarnings("nls")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Ignore
public class NewMesonProjectTest {

	private static SWTWorkbenchBot bot;

	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.CDT_PERSPECTIVE_ID);

	@BeforeClass
	public static void beforeClass() {
		SWTBotPreferences.TIMEOUT = 50000;
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		SWTBotPreferences.PLAYBACK_DELAY = 500;
		bot = new SWTWorkbenchBot();
	}

	@Before
	public void before() {
		bot.resetWorkbench();
	}

	@Test(timeout = 120000)
	public void addNewMesonProject() throws Exception {
		// open C++ perspective
		if (!"C/C++".equals(bot.activePerspective().getLabel())) {
			bot.perspectiveByLabel("C/C++").activate();
		}

		// Activate C/C++ wizard
		bot.menu("File").menu("New").menu("C/C++ Project").click();
		bot.shell("New C/C++ Project").activate();

		// Double click on the template
		SWTBotTable templateTable = bot.table();
		bot.getDisplay().syncExec(() -> {
			for (int i = 0; i < templateTable.rowCount(); ++i) {
				SWTBotTableItem item = templateTable.getTableItem(i);
				if ("Meson Project".equals(item.widget.getData(SWTBotPreferences.DEFAULT_KEY))) {
					item.doubleClick();
					break;
				}
			}
		});

		// Select the shell again since magic wizardry happened
		SWTBotShell newProjectShell = bot.shell("New Meson Project").activate();
		bot.waitUntil(Conditions.shellIsActive("New Meson Project"));
		newProjectShell.setFocus();

		// Create the project
		String projectName = "MesonTestProj";
		bot.sleep(2000);
		SWTBotText text = bot.textWithLabel("Project name:");
		text.setText(projectName);
		bot.button("Finish").click();

		bot.waitUntil(Conditions.shellCloses(newProjectShell));

		// Make sure it shows up in Project Explorer
		SWTBotView explorer = bot.viewByPartName("Project Explorer");
		explorer.show();
		explorer.setFocus();

		// Make sure the project indexer completes. At that point we're stable.
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		ICProject cproject = CoreModel.getDefault().create(project);
		IIndexManager indexManager = CCorePlugin.getIndexManager();
		while (!indexManager.isProjectContentSynced(cproject)) {
			Thread.sleep(1000);
		}

		// Make sure it has the right nature
		assertTrue(project.hasNature(MesonNature.ID));
	}

	@Test
	public void buildMesonProject() throws Exception {
		String projectName = "MesonTestProj";
		// Make sure the project indexer completes. At that point we're stable.
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		ICProject cproject = CoreModel.getDefault().create(project);

		IPath projectPath = project.getLocation();

		// open C++ perspective
		if (!"C/C++".equals(bot.activePerspective().getLabel())) {
			bot.perspectiveByLabel("C/C++").activate();
		}

		// Make sure it shows up in Project Explorer
		SWTBotView explorer = bot.viewByPartName("Project Explorer");
		explorer.show();
		explorer.setFocus();
		SWTBotTreeItem proj = explorer.bot().tree().getTreeItem(projectName).select();
		proj.contextMenu("Build Project").click();

		// wait until the binary shows up before testing the console output
		boolean foundExecutable = false;
		while (!foundExecutable) {
			IBinary[] binaries = cproject.getBinaryContainer().getBinaries();
			if (binaries.length > 0) {
				for (IBinary binary : binaries) {
					if (binary.getResource().getName().startsWith(projectName)) {
						foundExecutable = true;
					}
				}
			}
			bot.sleep(1000);
		}
		assertTrue(foundExecutable);

		// check the build console output
		SWTBotView console = bot.viewByPartName("Console");
		console.show();
		console.setFocus();
		String output = console.bot().styledText().getText();

		String[] lines = output.split("\\r?\\n"); //$NON-NLS-1$

		while (lines.length < 15) {
			output = console.bot().styledText().getText();
			lines = output.split("\\r?\\n"); //$NON-NLS-1$
			bot.sleep(2000);
		}

		assertEquals("Building in: " + projectPath + "/build/default", lines[0]);
		assertEquals(" sh -c \"meson   " + projectPath + "\"", lines[1]);
		assertEquals("The Meson build system", lines[2]);
		assertTrue(lines[3].startsWith("Version:"));
		assertEquals("Source dir: " + projectPath, lines[4]);
		assertEquals("Build dir: " + projectPath + "/build/default", lines[5]);
		assertEquals("Build type: native build", lines[6]);
		assertEquals("Project name: MesonTestProj", lines[7]);
		assertTrue(lines[lines.length - 3].startsWith("[1/2] cc"));
		assertTrue(lines[lines.length - 3].contains("-IMesonTestProj@exe"));
		assertTrue(lines[lines.length - 2].startsWith("[2/2] cc"));
		assertTrue(lines[lines.length - 2].contains("-o MesonTestProj"));

		int i = 0;
		while (i < 10 && !lines[lines.length - 1].startsWith("Build complete")) {
			output = console.bot().styledText().getText();
			lines = output.split("\\r?\\n"); //$NON-NLS-1$
			bot.sleep(1000);
			++i;
		}
		assertEquals("Build complete: " + projectPath + "/build/default", lines[lines.length - 1]);
	}

	@Test
	public void runMesonProject() throws Exception {
		String projectName = "MesonTestProj";
		// Make sure the project indexer completes. At that point we're stable.
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		CoreModel.getDefault().create(project);

		// open C++ perspective
		if (!"C/C++".equals(bot.activePerspective().getLabel())) {
			bot.perspectiveByLabel("C/C++").activate();
		}

		// Make sure it shows up in Project Explorer
		SWTBotView explorer = bot.viewByPartName("Project Explorer");
		explorer.show();
		explorer.setFocus();
		SWTBotTreeItem proj = explorer.bot().tree().getTreeItem(projectName).select();

		proj.expand();
		proj.expandNode("Binaries");

		SWTBotTreeItem binaries = proj.getNode("Binaries").select();
		binaries.getNode(0).contextMenu("Run As").menu(withRegex(".*Local C.*"), false, 0).click();
		bot.sleep(4000);

		SWTBotView console = bot.viewByPartName("Console");
		console.show();
		console.setFocus();
		String output = "";

		boolean done = false;
		while (!done) {
			// check the build console output
			output = console.bot().styledText().getText();
			if (output.startsWith("Hello") || !output.startsWith("Building in")) {
				done = true;
			}
			bot.sleep(2000);
		}
		String[] lines = output.split("\\r?\\n"); //$NON-NLS-1$
		assertEquals("Hello World", lines[0]);
	}

	@Test
	public void tryCleaningMesonProject() throws Exception {
		String projectName = "MesonTestProj";
		// Make sure the project indexer completes. At that point we're stable.
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		CoreModel.getDefault().create(project);

		// open C++ perspective
		if (!"C/C++".equals(bot.activePerspective().getLabel())) {
			bot.perspectiveByLabel("C/C++").activate();
		}

		bot.sleep(1000);

		IPath projectPath = project.getLocation();

		SWTBotShell mainShell = bot.activeShell();

		// use mainShell to get Project menu because it will be found somewhere else
		bot.menu(mainShell).menu("Project").menu("Clean...").click();
		bot.waitUntil(Conditions.shellIsActive("Clean"));
		bot.shell("Clean").activate();

		SWTBotShell shell = bot.shell("Clean");
		shell.bot().checkBox("Clean all projects").deselect();

		shell.bot().button("Clean").click();

		bot.waitUntil(Conditions.shellCloses(shell));

		// check the build console output
		SWTBotView console = bot.viewByPartName("Console");
		console.show();
		console.setFocus();
		String output = console.bot().styledText().getText();

		String[] lines = output.split("\\r?\\n"); //$NON-NLS-1$

		int counter = 0;
		while (lines.length < 5 && counter++ < 100) {
			output = console.bot().styledText().getText();
			lines = output.split("\\r?\\n"); //$NON-NLS-1$
			bot.sleep(2000);
		}

		assertEquals("Building in: " + projectPath + "/build/default", lines[0]);
		assertEquals("ninja clean -v", lines[1]);
		assertEquals("[1/1] ninja -t clean", lines[2]);
		assertEquals("Cleaning... 3 files.", lines[3]);
		assertEquals("Build complete: " + projectPath + "/build/default", lines[4]);

	}

}
