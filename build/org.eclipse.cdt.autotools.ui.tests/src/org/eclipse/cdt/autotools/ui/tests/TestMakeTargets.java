/*******************************************************************************
 * Copyright (c) 2010, 2015 Red Hat Inc..
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestMakeTargets extends AbstractTest {

	@Test
	// Verify we can build and run the info MakeTarget tool
	public void t1canBuildAndAccessInfoTarget() throws Exception {
		clickProjectContextMenu("Build Project");

		// Wait until the project is built
		SWTBotShell shell = bot.shell("Build Project");
		bot.waitUntil(Conditions.shellCloses(shell), 120000);

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertNotNull(workspace);
		IWorkspaceRoot root = workspace.getRoot();
		assertNotNull(root);
		IProject project = root.getProject(projectName);
		assertNotNull(project);
		IPath path = project.getLocation();
		path = path.append("config.status");
		File f = new File(path.toOSString());
		assertTrue(f.exists());
		f = new File(path.toOSString());
		assertTrue(f.exists());

		projectExplorer.bot().tree().getTreeItem(projectName).select();
		clickContextMenu(projectExplorer.bot().tree().select(projectName), "Build Targets", "Build...");
		shell = bot.shell("Build Targets");
		shell.activate();
		bot.table().getTableItem("info").select();
		bot.button("Build").click();

		SWTBotView consoleView = viewConsole("CDT Build Console");
		Pattern p = Pattern.compile(".*make info.*", Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));

		// Make Targets using right-click on project.
		clickProjectContextMenu("Build Targets", "Build...");
		shell = bot.shell("Build Targets");
		shell.activate();
		bot.table().getTableItem("check").select();
		bot.button("Build").click();
		consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		p = Pattern.compile(".*make check.*Making check in src.*", Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));
	}

}
