/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.application.tests;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.WaitForObjectCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException;
import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class StandaloneTest1 extends StandaloneTest {

	private static final String PROJECT_NAME = "hello";

	@BeforeClass
	public static void beforeClass() throws Exception {
		init(PROJECT_NAME);
	}

	@Test
	@Ignore
	public void Test1() throws Exception {
		// Verify the top-level menus are there
		SWTBotMenu fileMenu = mainShell.menu().menu("File");
		assertNotNull(fileMenu);
		SWTBotMenu editMenu = mainShell.menu().menu("Edit");
		assertNotNull(editMenu);
		SWTBotMenu searchMenu = mainShell.menu().menu("Search");
		assertNotNull(searchMenu);
		SWTBotMenu runMenu = mainShell.menu().menu("Run");
		assertNotNull(runMenu);
		SWTBotMenu windowMenu = mainShell.menu().menu("Window");
		assertNotNull(windowMenu);
		SWTBotMenu helpMenu = mainShell.menu().menu("Help");
		assertNotNull(helpMenu);

		// Verify other common top-level menus are not there
		assertMenuAbsent(mainShell, "Navigate");
		assertMenuAbsent(mainShell, "Refactor");
		assertMenuAbsent(mainShell, "Source");
		assertMenuAbsent(mainShell, "Target");
		assertMenuAbsent(mainShell, "Project");

		SWTBotMenu attachExecutableDialog = fileMenu.menu("Debug Attached Executable...");
		assertNotNull(attachExecutableDialog);
		SWTBotMenu coreFileDialog = fileMenu.menu("Debug Core File...");
		assertNotNull(coreFileDialog);
		SWTBotMenu newExecutableDialog = fileMenu.menu("Debug New Executable...");
		assertNotNull(newExecutableDialog);
		newExecutableDialog.click();
		SWTBotShell shell = bot.shell("Debug New Executable");
		shell.setFocus();
		// Try and have two open debug sessions on same binary
		IPath projectPath = Utilities.getDefault().getProjectPath(PROJECT_NAME).append("a.out");
		shell.bot().textWithLabel("Binary: ").setText(projectPath.toOSString());
		shell.bot().textWithLabel("Arguments: ").setText("1 2 3");
		bot.sleep(2000);

		shell.bot().button("OK").click();

		bot.sleep(1000);

		coreFileDialog.click();

		shell = bot.shell("Debug Core File");
		shell.setFocus();

		SWTBotText text = shell.bot().textWithLabel("Binary: ");
		assertNotNull(text);

		SWTBotText corefile = shell.bot().textWithLabel("Core File Path:");
		assertNotNull(corefile);

		bot.sleep(2000);

		shell.bot().button("Cancel").click();

		bot.sleep(2000);

		SWTBotMenu exitMenu = fileMenu.menu("Exit");
		assertNotNull(exitMenu);
		exitMenu.click();
	}

	@AfterClass
	public static void afterClass() {
		bot.sleep(1000);
	}

	private void assertMenuAbsent(SWTBotShell shell, String menuText) {
		boolean found = false;
		try {
			final Matcher<MenuItem> matcher = withMnemonic(menuText);
			WaitForObjectCondition<MenuItem> waitForMenuItem = Conditions.waitForMenuItem(shell.menu(), matcher, false,
					0);
			bot.waitUntil(waitForMenuItem, 50);
			found = true;
		} catch (TimeoutException e) {
			// correct
		}
		assertFalse(found);
	}
}
