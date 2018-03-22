/*******************************************************************************
 * Copyright (c) 2017, 2018 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Red Hat Inc. - modified for Meson testing
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui.tests;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.meson.ui.tests.utils.CloseWelcomePageRule;
import org.eclipse.cdt.meson.core.MesonNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.epp.logging.aeri.core.ISystemSettings;
import org.eclipse.epp.logging.aeri.core.SendMode;
import org.eclipse.epp.logging.aeri.core.SystemControl;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

@SuppressWarnings("nls")
public class NewMesonProjectTest {

	private static SWTWorkbenchBot bot;
	
	@ClassRule
	public static CloseWelcomePageRule closeWelcomePage = new CloseWelcomePageRule(
			CloseWelcomePageRule.CDT_PERSPECTIVE_ID);

	@BeforeClass
	public static void beforeClass() {
		SWTBotPreferences.TIMEOUT = 50000;
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		bot = new SWTWorkbenchBot();
	}

	@Before
	public void before() {
		ISystemSettings settings = SystemControl.getSystemSettings();
		settings.setSendMode(SendMode.NEVER);
		bot.resetWorkbench();

		for (SWTBotView view : bot.views(withPartName("Welcome"))) {
			view.close();
		}
		
	}

	@Test(timeout = 120000)
	public void createCMakeProject() throws Exception {
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

		// Create the project
		String projectName = "MesonTestProj";
		bot.textWithLabel("Project name:").typeText(projectName);
		bot.button("Finish").click();
		
		newProjectShell.setFocus();
		bot.waitUntil(Conditions.shellCloses(newProjectShell));
		
//		return;
		
//		// Make sure it shows up in Project Explorer
		SWTBotView explorer = bot.viewByPartName("Project Explorer");
		explorer.show();
		explorer.setFocus();
		explorer.bot().tree().getTreeItem(projectName).select();
		

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

}
