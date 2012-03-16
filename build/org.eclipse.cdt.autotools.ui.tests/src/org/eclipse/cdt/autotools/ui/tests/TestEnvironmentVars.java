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

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestEnvironmentVars {
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
 
		bot.textWithLabel("Project name:").setText("GnuProject2");
		bot.tree().expandNode("GNU Autotools").select("Hello World ANSI C Autotools Project");
 
		bot.button("Finish").click();
	}
	
	@Test
	// Verify we can pass an unknown env var in configure options and it will be nulled out
	// Verifies fix for Bug: #303616
	public void referenceUnknownEnvVar() throws Exception {
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject2");
		bot.menu("Project", 1).menu("Properties").click();
		SWTBotShell shell = bot.shell("Properties for GnuProject2");
		shell.activate();
		bot.tree().expandNode("Autotools").select("Configure Settings");
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		// Set the configure parameters to be --enable-jeff via user-defined options
		SWTBotText text = bot.textWithLabel("Additional command-line options");
		text.typeText("${some_var}");
		bot.button("OK").click();
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject2");
		bot.menu("Project", 1).menu("Reconfigure Project").click();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject2");
		assertTrue(project != null);
		IPath path = project.getLocation();
		File f = null;
		// We need to wait until the config.status file is created so
		// sleep a bit and look for it...give up after 40 seconds
		for (int i = 0; i < 80; ++i) {
			bot.sleep(500);
			f = new File(path.append("config.status").toOSString());
			if (f.exists())
				break;
		}
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		Pattern p = Pattern.compile(".*some_var.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		// We shouldn't see some_var anywhere in the console
		assertTrue(!m.matches());
	}

	@Test
	// Verify we can set an environment variable and use it as a configure parameter
	// Verifies fix for Bug: #303616
	public void setEnvVar() throws Exception {
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject2");
		bot.menu("Project", 1).menu("Properties").click();
		SWTBotShell shell = bot.shell("Properties for GnuProject2");
		shell.activate();
		bot.tree().expandNode("C/C++ Build").select("Environment");
		bot.button("Add...").click();
		shell = bot.shell("New variable");
		shell.activate();
		SWTBotText text = bot.textWithLabel("Name:");
		text.typeText("some_var");
		text = bot.textWithLabel("Value:");
		text.typeText("--enable-somevar");
		bot.button("OK").click();
		shell = bot.shell("Properties for GnuProject2");
		shell.activate();
		bot.button("OK").click();
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject2");
		bot.menu("Project", 1).menu("Reconfigure Project").click();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject2");
		assertTrue(project != null);
		IPath path = project.getLocation();
		File f = null;
		// We need to wait until the config.status file is created so
		// sleep a bit and look for it...give up after 40 seconds
		for (int i = 0; i < 80; ++i) {
			bot.sleep(500);
			f = new File(path.append("config.status").toOSString());
			if (f.exists())
				break;
		}
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		Pattern p = Pattern.compile(".*--enable-somevar.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		// We should see the expanded some_var variable in the console
		assertTrue(m.matches());
	}
	
	@Test
	// Verify we can set an environment variable prior to the configuration command and
	// it will be seen by the configure script
	public void setEnvVarOnCommandLine() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject2");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Create a fake configure script which prints out the values of
		// envvars some_var1, some_var2, and some_var3
		File f = new File(path.append("fake_configure").toOSString());
		BufferedWriter w = new BufferedWriter(new FileWriter(f));
		w.append("echo VAR1 is ${some_var1}");
		w.newLine();
		w.append("echo VAR2 is ${some_var2}");
		w.newLine();
		w.append("echo VAR3 is ${some_var3}");
		w.newLine();
		w.append("echo VAR4 is ${some_var4}");
		w.newLine();
		w.append("echo VAR5 is ${some_var5}");
		w.newLine();
		w.append("echo VAR6 is ${some_var6}");
		w.newLine();
		w.close();
		// Now change the configure script command to be the fake configure script
		// and set the three envvars on the command itself
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject2");
		bot.menu("Project", 1).menu("Properties").click();
		SWTBotShell shell = bot.shell("Properties for GnuProject2");
		shell.activate();
		bot.tree().expandNode("Autotools").select("Configure Settings");
		bot.treeWithLabel("Configure Settings").select("configure");
		bot.textWithLabel("Command").setText("");
		// Choose three different forms, some using quotes to allow blanks in them
		bot.textWithLabel("Command").typeText("some_var1=\"a boat\" some_var2='a train' some_var3=car fake_configure some_var4=\"a wagon\" some_var5='a plane' some_var6=skates");
		bot.button("OK").click();
		// Reconfigure the project and make sure the env variables are seen in the script
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject2");
		bot.menu("Project", 1).menu("Reconfigure Project").click();
		bot.sleep(3000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		Pattern p = Pattern.compile(".*VAR1 is a boat.*VAR2 is a train.*VAR3 is car.*VAR4 is a wagon.*VAR5 is a plane.*VAR6 is skates.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
	}

	@AfterClass
	public static void sleep() {
		bot.sleep(2000);
	}
}