package org.eclipse.cdt.debug.application.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
	public void Test1() throws Exception {
		// Verify the top-level menus are there
		SWTBotMenu fileMenu = bot.menu("File");
		assertNotNull(fileMenu);
		SWTBotMenu editMenu = bot.menu("Edit");
		assertNotNull(editMenu);
		SWTBotMenu searchMenu = bot.menu("Search");
		assertNotNull(searchMenu);
		SWTBotMenu runMenu = bot.menu("Run");
		assertNotNull(runMenu);
		SWTBotMenu windowMenu = bot.menu("Window");
		assertNotNull(windowMenu);
		SWTBotMenu helpMenu = bot.menu("Help");
		assertNotNull(helpMenu);

		// Verify other common top-level menus are not there
		SWTBotMenu notThere = null;
		try {
			notThere = bot.menu("Navigate");
		} catch (WidgetNotFoundException e) {
			// correct
		}
		assertNull(notThere);
		try {
			notThere = bot.menu("Refactor");
		} catch (WidgetNotFoundException e) {
			// correct
		}
		assertNull(notThere);
		try {
			notThere = bot.menu("Source");
		} catch (WidgetNotFoundException e) {
			// correct
		}
		assertNull(notThere);
		try {
			notThere = bot.menu("Target");
		} catch (WidgetNotFoundException e) {
			// correct
		}
		assertNull(notThere);
		try {
			// We want to prove there isn't a top-level Project menu
			// There happens to be a lower-level Project menu from the Text menu
			// Verify we find it, but no other menus named Project
			SWTBotMenu textMenu = bot.menu("Text");
			@SuppressWarnings("unused")
			SWTBotMenu projectMenu = textMenu.menu("Project");
			notThere = bot.menu("Project", 1);
		} catch (IndexOutOfBoundsException e) {
			// correct
		}
		assertNull(notThere);

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

		bot.button("OK").click();

		bot.sleep(1000);

		coreFileDialog.click();

		shell = bot.shell("Debug Core File");
		shell.setFocus();

		SWTBotText text = shell.bot().textWithLabel("Binary: ");
		assertNotNull(text);

		SWTBotText corefile = shell.bot().textWithLabel("Core File Path:");
		assertNotNull(corefile);

		bot.sleep(2000);

		bot.button("Cancel").click();

		bot.sleep(2000);
		
		SWTBotMenu exitMenu = fileMenu.menu("Exit");
		assertNotNull(exitMenu);
		exitMenu.click();
	}

	@AfterClass
	public static void afterClass() {
		bot.sleep(1000);
	}


}
