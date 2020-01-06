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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

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
import org.eclipse.swtbot.swt.finder.SWTBot;
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
public class NewManualNinjaTest {

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
		String projectName = "MesonTestProj3";
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
		String projectName = "MesonTestProj3";
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

		// Make sure the project indexer completes. At that point we're stable.
		IIndexManager indexManager = CCorePlugin.getIndexManager();
		while (!indexManager.isProjectContentSynced(cproject)) {
			Thread.sleep(1000);
		}

		// check the build console output
		SWTBotView console = bot.viewByPartName("Console");
		console.show();
		console.setFocus();

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

		String output = console.bot().styledText().getText();

		String[] lines = output.split("\\r?\\n"); //$NON-NLS-1$

		while (lines.length < 9) {
			output = console.bot().styledText().getText();
			lines = output.split("\\r?\\n"); //$NON-NLS-1$
			bot.sleep(2000);
		}

		assertEquals("Building in: " + projectPath + "/build/default", lines[0]);
		assertEquals("The Meson build system", lines[2]);
		assertTrue(lines[3].startsWith("Version:"));
		assertEquals("Source dir: " + projectPath, lines[4]);
		assertEquals("Build dir: " + projectPath + "/build/default", lines[5]);
		assertEquals("Build type: native build", lines[6]);
		assertEquals("Project name: MesonTestProj3", lines[7]);

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
	public void manualNinja() throws Exception {
		// check the build console output
		String projectName = "MesonTestProj3";
		// Make sure the project indexer completes. At that point we're stable.
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

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
		proj.contextMenu("Run ninja").click();

		bot.sleep(2000);

		SWTBotShell shell = bot.activeShell();

		while (!shell.getText().trim().isEmpty()) {
			bot.sleep(1000);
			shell = bot.activeShell();
		}

		SWTBot shellBot = shell.bot();

		shellBot.textWithLabel("Environment:").setText("CFLAGS=\"-DJeff2\"");
		shellBot.textWithLabel("Options:").setText("clean");

		shellBot.button("Finish").click();

		bot.waitUntil(Conditions.shellCloses(shell));

		SWTBotView console = bot.viewByPartName("Console");
		console.show();
		console.setFocus();

		String[] lines = new String[0];

		while (lines.length < 4) {
			String output = console.bot().styledText().getText();
			lines = output.split("\\r?\\n"); //$NON-NLS-1$
			bot.sleep(2000);
		}

		bot.sleep(2000);

		assertEquals("Building in: " + projectPath + "/build/default", lines[0]);
		assertEquals("[1/1] Cleaning.", lines[1]);
		assertEquals("Cleaning... 3 files.", lines[2]);
		assertEquals("Build complete: " + projectPath + "/build/default", lines[3]);

		// Make sure it shows up in Project Explorer
		explorer = bot.viewByPartName("Project Explorer");
		explorer.show();
		explorer.setFocus();
		proj = explorer.bot().tree().getTreeItem(projectName).select();
		proj.contextMenu("Run ninja").click();

		shell = bot.activeShell();

		while (!shell.getText().trim().isEmpty()) {
			bot.sleep(1000);
			shell = bot.activeShell();
		}

		shellBot = shell.bot();

		// verify last parameters are still present
		assertEquals("CFLAGS=\"-DJeff2\"", shellBot.textWithLabel("Environment:").getText());
		assertEquals("clean", shellBot.textWithLabel("Options:").getText());

		shellBot.button("Cancel").click();

		bot.waitUntil(Conditions.shellCloses(shell));

		// Make sure it shows up in Project Explorer
		explorer = bot.viewByPartName("Project Explorer");
		explorer.show();
		explorer.setFocus();
		proj = explorer.bot().tree().getTreeItem(projectName).select();

		proj.expand();
		bot.sleep(500);
		proj.expandNode("Binaries");

		SWTBotTreeItem binaries = proj.getNode("Binaries").select();
		List<String> binarynodes = binaries.getNodes();

		// make sure that we no longer have the binary after the clean occurs
		for (String node : binarynodes) {
			if (node.contains(projectName)) {
				fail();
			}
		}

	}

}
