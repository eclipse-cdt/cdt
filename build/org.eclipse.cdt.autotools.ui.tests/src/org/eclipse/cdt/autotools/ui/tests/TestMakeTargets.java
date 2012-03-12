/*******************************************************************************
 * Copyright (c) 2010 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.tests;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuFinder;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestMakeTargets {
	
	private static SWTWorkbenchBot	bot;

	@BeforeClass
	public static void beforeClass() throws Exception {
		SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
		bot = new SWTWorkbenchBot();
		// Close the Welcome view if it exists
		try {
		bot.viewByTitle("Welcome").close();
		// Turn off automatic building by default
		} catch (Exception e) {
			// do nothing
		}
		bot.menu("Window").menu("Preferences").click();
		SWTBotShell shell = bot.shell("Preferences");
		shell.activate();
		bot.tree().expandNode("General").select("Workspace");
		SWTBotCheckBox buildAuto = bot.checkBox("Build automatically");
		if (buildAuto != null && buildAuto.isChecked())
			buildAuto.click();
		bot.button("Apply").click();
		// Ensure that the C/C++ perspective is chosen automatically
		// and doesn't require user intervention
		bot.tree().expandNode("General").select("Perspectives");
		SWTBotRadio radio = bot.radio("Always open");
		if (radio != null && !radio.isSelected())
			radio.click();
		bot.button("OK").click();
		bot.menu("File").menu("New").menu("Project...").click();
		 
		shell = bot.shell("New Project");
		shell.activate();
		bot.tree().expandNode("C/C++").select("C Project");
		bot.button("Next >").click();
 
		bot.textWithLabel("Project name:").setText("GnuProject3");
		bot.tree().expandNode("GNU Autotools").select("Hello World ANSI C Autotools Project");
 
		bot.button("Finish").click();
	}
 
	@Test
	// Verify we can build and run the info MakeTarget tool
	public void canBuildAndAccessInfoTarget() throws Exception {
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject3");
		bot.menu("Project", 1).menu("Build Project").click();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject3");
		assertTrue(project != null);
		IPath path = project.getLocation();
		path = path.append("config.status");
		// We need to wait until the config.status file is created so
		// sleep a bit and look for it...give up after 120 seconds
		for (int i = 0; i < 240; ++i) {
			bot.sleep(500);
			File f = new File(path.toOSString());
			if (f.exists())
				break;
		}
		File f = new File(path.toOSString());
		assertTrue(f.exists());
		view = bot.viewByTitle("Project Explorer");
		// FIXME: there is a problem with the CDT adding multiple MakeTargets
		//        so the Autotools plugin adds them into the saved data all at
		//        once.  The targets aren't refreshed until the project is reopened.
		//        We do this by manually closing and opening the project.  This
		//        will not be needed as of CDT 7.0.0.
		SWTBotTreeItem node = view.bot().tree().getTreeItem("GnuProject3");
		node.setFocus();
		node.select().contextMenu("Close Project").click();
		node.setFocus();
		node.select().contextMenu("Open Project").click();
		view.bot().tree().select("GnuProject3");
		bot.menu("Project", 1).menu("Make Target").menu("Build...").click();
		SWTBotShell shell = bot.shell("Make Targets");
		shell.activate();
		bot.table().getTableItem("info").setFocus();
		bot.table().getTableItem("info").select();
		bot.button("Build").click();
		bot.sleep(3000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		Pattern p = Pattern.compile(".*make info.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
		view = bot.viewByTitle("Project Explorer");
		view.setFocus();
		node = view.bot().tree().getTreeItem("GnuProject3");
		node.setFocus();
		node.select();
		// FIXME: when context menus work properly, add test to try out
		// Make Targets using right-click on project.
//		node.contextMenu("Make Targets").contextMenu("Build...").click();
//			org.hamcrest.Matcher<MenuItem> withMnemonic = withMnemonic("Make Targets");
//			final org.hamcrest.Matcher<MenuItem> matcher = allOf(widgetOfType(MenuItem.class));
//			final ContextMenuFinder menuFinder = new ContextMenuFinder((Control)view.bot().tree().widget);
//			new SWTBot().waitUntil(new DefaultCondition() {
//				public String getFailureMessage() {
//					return "Could not find context menu with text: Make Targets"; //$NON-NLS-1$
//				}
//
//				public boolean test() throws Exception {
//					return !menuFinder.findMenus(matcher).isEmpty();
//				}
//			});
//			List<MenuItem> list = menuFinder.findMenus(matcher);
//		bot.sleep(23000);
//		// Following does not work but should
//		SWTBotMenu menu = node.select().contextMenu("Make Targets");
//		bot.sleep(4000);
//		shell = bot.shell("Make Targets");
//		shell.activate();
//		bot.table().getTableItem("check").setFocus();
//		bot.table().getTableItem("check").select();
//		bot.button("Build").click();
//		bot.sleep(3000);
//		consoleView = bot.viewByTitle("Console");
//		consoleView.setFocus();
//		output = consoleView.bot().styledText().getText();
//		p = Pattern.compile(".*make check.*Making check in src.*", Pattern.DOTALL);
//		m = p.matcher(output);
//		assertTrue(m.matches());
	}
	
	@AfterClass
	public static void sleep() {
		bot.sleep(4000);
	}

}
