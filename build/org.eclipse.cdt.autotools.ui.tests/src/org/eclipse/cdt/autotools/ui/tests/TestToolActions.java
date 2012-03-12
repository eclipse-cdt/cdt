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

import java.io.File;
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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestToolActions {
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
 
		bot.textWithLabel("Project name:").setText("GnuProject1");
		bot.tree().expandNode("GNU Autotools").select("Hello World ANSI C Autotools Project");
 
		bot.button("Finish").click();
	}
 
	@Test
	// Verify we can set the tools via the Autotools Tools page
	public void canSeeTools() throws Exception {
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Properties").click();
		SWTBotShell shell = bot.shell("Properties for GnuProject1");
		shell.activate();
		bot.tree().expandNode("Autotools").select("General");
		bot.tabItem("Tools Settings").activate();
		String aclocalName = bot.textWithLabel("aclocal").getText();
		assertTrue(aclocalName.equals("aclocal"));
		String autoconfName = bot.textWithLabel("autoconf").getText();
		assertTrue(autoconfName.equals("autoconf"));
		String automakeName = bot.textWithLabel("automake").getText();
		assertTrue(automakeName.equals("automake"));
		String autoheaderName = bot.textWithLabel("autoheader").getText();
		assertTrue(autoheaderName.equals("autoheader"));
		String autoreconfName = bot.textWithLabel("autoreconf").getText();
		assertTrue(autoreconfName.equals("autoreconf"));
		String libtoolizeName = bot.textWithLabel("libtoolize").getText();
		assertTrue(libtoolizeName.equals("libtoolize"));
		bot.button("Cancel").click();
	}
	
	@Test
	// Verify we can access the aclocal tool
	public void canAccessAclocal() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Verify configure does not exist initially
		path = path.append("aclocal.m4");
		File f = new File(path.toOSString());
		assertTrue(!f.exists());
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Aclocal").click();
		SWTBotShell shell = bot.shell("Aclocal Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking aclocal in.*GnuProject1.*aclocal --help.*Usage: aclocal.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
		// Verify we still don't have an aclocal.m4 file yet
		f = new File(path.toOSString());
		assertTrue(!f.exists());
		// Now lets run aclocal for our hello world project which hasn't had any
		// autotool files generated yet.
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Aclocal").click();
		shell = bot.shell("Aclocal Options");
		shell.activate();
		bot.button("OK").click();
		bot.sleep(1000);
		consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		output = consoleView.bot().styledText().getText();
		p = Pattern.compile(".*Invoking aclocal in.*GnuProject1.*aclocal.*", Pattern.DOTALL);
		m = p.matcher(output);
		assertTrue(m.matches());
		// We need to wait until the aclocal.m4 file is created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.toOSString());
			if (f.exists())
				break;
		}
		// Verify we now have an aclocal.m4 file created
		assertTrue(f.exists());
	}

	@Test
	// Verify we can access the autoconf tool
	public void canAccessAutoconf() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Verify configure does not exist initially
		path = path.append("configure");
		File f = new File(path.toOSString());
		assertTrue(!f.exists());
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Autoconf").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking autoconf in.*GnuProject1.*autoconf.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
		// We need to wait until the configure file is created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.toOSString());
			if (f.exists())
				break;
		}
		// Verify we now have a configure script
		f = new File(path.toOSString());
		assertTrue(f.exists());
		// Now lets delete the configure file and run autoconf from the project explorer
		// menu directly from the configure.ac file.
		assertTrue(f.delete());
		view = bot.viewByTitle("Project Explorer");
		SWTBotTreeItem node = view.bot().tree().expandNode("GnuProject1").getNode("configure.ac");
		node.setFocus();
		node.select().contextMenu("Invoke Autoconf").click();
		bot.sleep(1000);
		consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		output = consoleView.bot().styledText().getText();
		p = Pattern.compile(".*Invoking autoconf in.*GnuProject1.*autoconf.*", Pattern.DOTALL);
		m = p.matcher(output);
		assertTrue(m.matches());
		// We need to wait until the configure file is created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.toOSString());
			if (f.exists())
				break;
		}
		// Verify we now have a configure script again
		f = new File(path.toOSString());
		assertTrue(f.exists());
	}
	
	@Test
	// Verify we can access the automake tool
	public void canAccessAutomake() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Verify configure does not exist initially
		IPath path2 = path.append("src/Makefile.in");
		path = path.append("Makefile.in");
		File f = new File(path.toOSString());
		assertTrue(!f.exists());
		File f2 = new File(path2.toOSString());
		assertTrue(!f2.exists());
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Automake").click();
		SWTBotShell shell = bot.shell("Automake Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking automake in.*GnuProject1.*automake --help.*Usage:.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
		// Verify we still don't have Makefile.in files yet
		f = new File(path.toOSString());
		assertTrue(!f.exists());
		f2 = new File(path2.toOSString());
		assertTrue(!f2.exists());
		// Now lets run automake for our hello world project which hasn't had any
		// Makefile.in files generated yet.
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Automake").click();
		shell = bot.shell("Automake Options");
		shell.activate();
		bot.text(0).typeText("--add-missing"); // need this to successfully run here
		bot.text(1).typeText("Makefile src/Makefile");
		bot.button("OK").click();
		bot.sleep(2000);
		consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		output = consoleView.bot().styledText().getText();
		p = Pattern.compile(".*Invoking automake in.*GnuProject1.*automake --add-missing Makefile src/Makefile.*", Pattern.DOTALL);
		m = p.matcher(output);
		assertTrue(m.matches());
		// We need to wait until the Makefile.in files are created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.toOSString());
			f2 = new File(path2.toOSString());
			if (f.exists() && f2.exists())
				break;
		}
		// Verify we now have Makefile.in files created
		f = new File(path.toOSString());
		assertTrue(f.exists());
		f2 = new File(path2.toOSString());
		assertTrue(f2.exists());
	}
	
	@Test
	// Verify we can access the libtoolize tool
	public void canAccessLibtoolize() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Libtoolize").click();
		SWTBotShell shell = bot.shell("Libtoolize Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking libtoolize in.*GnuProject1.*libtoolize --help.*Usage: libtoolize.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
	}

	@Test
	// Verify we can access the libtoolize tool
	public void canAccessAutoheader() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Autoheader").click();
		SWTBotShell shell = bot.shell("Autoheader Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking autoheader in.*GnuProject1.*autoheader --help.*Usage:.*autoheader.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
	}

	@Test
	// Verify we can access the autoreconf tool
	public void canAccessAutoreconf() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Remove a number of generated files
		File f = new File(path.append("src/Makefile.in").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("Makefile.in").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("configure").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("config.status").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("config.sub").toOSString());
		if (f.exists())
			f.delete();
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Autoreconf").click();
		SWTBotShell shell = bot.shell("Autoreconf Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking autoreconf in.*GnuProject1.*autoreconf --help.*Usage: .*autoreconf.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Autoreconf").click();
		shell = bot.shell("Autoreconf Options");
		shell.activate();
		bot.text(0).typeText("-i");
		bot.button("OK").click();
		// We need to wait until the Makefile.in file is created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.append("Makefile.in").toOSString());
			if (f.exists())
				break;
		}
		// Verify a number of generated files now exist
		f = new File(path.append("src/Makefile.in").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("Makefile.in").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("configure").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("config.status").toOSString());
		assertTrue(!f.exists()); //shouldn't have run configure
		f = new File(path.append("config.sub").toOSString());
		assertTrue(f.exists());
	}

	@Test
	// Verify we can access the autoreconf tool
	public void canReconfigureProject() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Remove a number of generated files
		File f = new File(path.append("src/Makefile.in").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("Makefile.in").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("configure").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("config.status").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("config.sub").toOSString());
		if (f.exists())
			f.delete();
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Reconfigure Project").click();
		// We need to wait until the config.status file is created so
		// sleep a bit and look for it...give up after 40 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.append("config.status").toOSString());
			if (f.exists())
				break;
		}
		// Verify a number of generated files now exist
		f = new File(path.append("src/Makefile.in").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("Makefile.in").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("configure").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("config.status").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("config.sub").toOSString());
		assertTrue(f.exists());
	}

	@Test
	// Verify we can set and reset the tools via the Autotools Tools page
	// Verifies bug #317345
	public void canResetTools() throws Exception {
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Properties").click();
		SWTBotShell shell = bot.shell("Properties for GnuProject1");
		shell.activate();
		bot.tree().expandNode("Autotools").select("General");
		bot.tabItem("Tools Settings").activate();
		bot.textWithLabel("aclocal").setText("");
		bot.textWithLabel("aclocal").typeText("automake");
		bot.textWithLabel("automake").setText("");
		bot.textWithLabel("automake").typeText("autoconf");
		bot.textWithLabel("autoconf").setText("");
		bot.textWithLabel("autoconf").typeText("autoheader");
		bot.textWithLabel("autoheader").setText("");
		bot.textWithLabel("autoheader").typeText("autoreconf");
		bot.textWithLabel("autoreconf").setText("");
		bot.textWithLabel("autoreconf").typeText("libtoolize");
		bot.textWithLabel("libtoolize").setText("");
		bot.textWithLabel("libtoolize").typeText("aclocal");
		bot.button("Apply").click();
		String aclocalName = bot.textWithLabel("aclocal").getText();
		assertTrue(aclocalName.equals("automake"));
		String autoconfName = bot.textWithLabel("autoconf").getText();
		assertTrue(autoconfName.equals("autoheader"));
		String automakeName = bot.textWithLabel("automake").getText();
		assertTrue(automakeName.equals("autoconf"));
		String autoheaderName = bot.textWithLabel("autoheader").getText();
		assertTrue(autoheaderName.equals("autoreconf"));
		String autoreconfName = bot.textWithLabel("autoreconf").getText();
		assertTrue(autoreconfName.equals("libtoolize"));
		String libtoolizeName = bot.textWithLabel("libtoolize").getText();
		assertTrue(libtoolizeName.equals("aclocal"));
		bot.button("Restore Defaults").click();
		aclocalName = bot.textWithLabel("aclocal").getText();
		assertTrue(aclocalName.equals("aclocal"));
		autoconfName = bot.textWithLabel("autoconf").getText();
		assertTrue(autoconfName.equals("autoconf"));
		automakeName = bot.textWithLabel("automake").getText();
		assertTrue(automakeName.equals("automake"));
		autoheaderName = bot.textWithLabel("autoheader").getText();
		assertTrue(autoheaderName.equals("autoheader"));
		autoreconfName = bot.textWithLabel("autoreconf").getText();
		assertTrue(autoreconfName.equals("autoreconf"));
		libtoolizeName = bot.textWithLabel("libtoolize").getText();
		assertTrue(libtoolizeName.equals("libtoolize"));
		bot.button("OK").click();
	}

	@Test
	// Verify we can access the aclocal tool
	public void canAccessAclocal2() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Verify configure does not exist initially
		path = path.append("aclocal.m4");
		File f = new File(path.toOSString());
		f.delete();
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Aclocal").click();
		SWTBotShell shell = bot.shell("Aclocal Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking aclocal in.*GnuProject1.*aclocal --help.*Usage: aclocal.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
		// Verify we still don't have an aclocal.m4 file yet
		f = new File(path.toOSString());
		assertTrue(!f.exists());
		// Now lets run aclocal for our hello world project which hasn't had any
		// autotool files generated yet.
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Aclocal").click();
		shell = bot.shell("Aclocal Options");
		shell.activate();
		bot.button("OK").click();
		bot.sleep(1000);
		consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		output = consoleView.bot().styledText().getText();
		p = Pattern.compile(".*Invoking aclocal in.*GnuProject1.*aclocal.*", Pattern.DOTALL);
		m = p.matcher(output);
		assertTrue(m.matches());
		// We need to wait until the aclocal.m4 file is created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.toOSString());
			if (f.exists())
				break;
		}
		// Verify we now have an aclocal.m4 file created
		assertTrue(f.exists());
	}

	@Test
	// Verify we can access the autoconf tool
	public void canAccessAutoconf2() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Verify configure does not exist initially
		path = path.append("configure");
		File f = new File(path.toOSString());
		f.delete();
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Autoconf").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking autoconf in.*GnuProject1.*autoconf.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
		// We need to wait until the configure file is created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.toOSString());
			if (f.exists())
				break;
		}
		// Verify we now have a configure script
		f = new File(path.toOSString());
		assertTrue(f.exists());
		// Now lets delete the configure file and run autoconf from the project explorer
		// menu directly from the configure.ac file.
		assertTrue(f.delete());
		view = bot.viewByTitle("Project Explorer");
		SWTBotTreeItem node = view.bot().tree().expandNode("GnuProject1").getNode("configure.ac");
		node.setFocus();
		node.select().contextMenu("Invoke Autoconf").click();
		bot.sleep(1000);
		consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		output = consoleView.bot().styledText().getText();
		p = Pattern.compile(".*Invoking autoconf in.*GnuProject1.*autoconf.*", Pattern.DOTALL);
		m = p.matcher(output);
		assertTrue(m.matches());
		// We need to wait until the configure file is created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.toOSString());
			if (f.exists())
				break;
		}
		// Verify we now have a configure script again
		f = new File(path.toOSString());
		assertTrue(f.exists());
	}
	
	@Test
	// Verify we can access the automake tool
	public void canAccessAutomake2() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Verify configure does not exist initially
		IPath path2 = path.append("src/Makefile.in");
		path = path.append("Makefile.in");
		File f = new File(path.toOSString());
		f.delete();
		File f2 = new File(path2.toOSString());
		f2.delete();
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Automake").click();
		SWTBotShell shell = bot.shell("Automake Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking automake in.*GnuProject1.*automake --help.*Usage:.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
		// Verify we still don't have Makefile.in files yet
		f = new File(path.toOSString());
		assertTrue(!f.exists());
		f2 = new File(path2.toOSString());
		assertTrue(!f2.exists());
		// Now lets run automake for our hello world project which hasn't had any
		// Makefile.in files generated yet.
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Automake").click();
		shell = bot.shell("Automake Options");
		shell.activate();
		bot.text(0).typeText("--add-missing"); // need this to successfully run here
		bot.text(1).typeText("Makefile src/Makefile");
		bot.button("OK").click();
		bot.sleep(2000);
		consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		output = consoleView.bot().styledText().getText();
		p = Pattern.compile(".*Invoking automake in.*GnuProject1.*automake --add-missing Makefile src/Makefile.*", Pattern.DOTALL);
		m = p.matcher(output);
		assertTrue(m.matches());
		// We need to wait until the Makefile.in files are created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.toOSString());
			f2 = new File(path2.toOSString());
			if (f.exists() && f2.exists())
				break;
		}
		// Verify we now have Makefile.in files created
		f = new File(path.toOSString());
		assertTrue(f.exists());
		f2 = new File(path2.toOSString());
		assertTrue(f2.exists());
	}
	
	@Test
	// Verify we can access the libtoolize tool
	public void canAccessLibtoolize2() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Libtoolize").click();
		SWTBotShell shell = bot.shell("Libtoolize Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking libtoolize in.*GnuProject1.*libtoolize --help.*Usage: libtoolize.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
	}

	@Test
	// Verify we can access the libtoolize tool
	public void canAccessAutoheader2() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Autoheader").click();
		SWTBotShell shell = bot.shell("Autoheader Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking autoheader in.*GnuProject1.*autoheader --help.*Usage:.*autoheader.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
	}

	@Test
	// Verify we can access the autoreconf tool
	public void canAccessAutoreconf2() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Remove a number of generated files
		File f = new File(path.append("src/Makefile.in").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("Makefile.in").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("configure").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("config.status").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("config.sub").toOSString());
		if (f.exists())
			f.delete();
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Autoreconf").click();
		SWTBotShell shell = bot.shell("Autoreconf Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking autoreconf in.*GnuProject1.*autoreconf --help.*Usage: .*autoreconf.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Autoreconf").click();
		shell = bot.shell("Autoreconf Options");
		shell.activate();
		bot.text(0).typeText("-i");
		bot.button("OK").click();
		// We need to wait until the Makefile.in file is created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.append("Makefile.in").toOSString());
			if (f.exists())
				break;
		}
		// Verify a number of generated files now exist
		f = new File(path.append("src/Makefile.in").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("Makefile.in").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("configure").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("config.status").toOSString());
		assertTrue(!f.exists()); //shouldn't have run configure
		f = new File(path.append("config.sub").toOSString());
		assertTrue(f.exists());
	}

	@Test
	// Verify we can access the autoreconf tool
	public void canReconfigureProject2() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertTrue(workspace != null);
		IWorkspaceRoot root = workspace.getRoot();
		assertTrue(root != null);
		IProject project = root.getProject("GnuProject1");
		assertTrue(project != null);
		IPath path = project.getLocation();
		// Remove a number of generated files
		File f = new File(path.append("src/Makefile.in").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("Makefile.in").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("configure").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("config.status").toOSString());
		if (f.exists())
			f.delete();
		f = new File(path.append("config.sub").toOSString());
		if (f.exists())
			f.delete();
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Reconfigure Project").click();
		// We need to wait until the config.status file is created so
		// sleep a bit and look for it...give up after 40 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.append("config.status").toOSString());
			if (f.exists())
				break;
		}
		// Verify a number of generated files now exist
		f = new File(path.append("src/Makefile.in").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("Makefile.in").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("configure").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("config.status").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("config.sub").toOSString());
		assertTrue(f.exists());
	}

	@Test
	// Verify we can set the tools via the Autotools Tools page
	public void canSetTools() throws Exception {
		SWTBotView view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Properties").click();
		SWTBotShell shell = bot.shell("Properties for GnuProject1");
		shell.activate();
		bot.tree().expandNode("Autotools").select("General");
		bot.tabItem("Tools Settings").activate();
		bot.textWithLabel("aclocal").setText("");
		bot.textWithLabel("aclocal").typeText("automake");
		bot.textWithLabel("automake").setText("");
		bot.textWithLabel("automake").typeText("autoconf");
		bot.textWithLabel("autoconf").setText("");
		bot.textWithLabel("autoconf").typeText("autoheader");
		bot.textWithLabel("autoheader").setText("");
		bot.textWithLabel("autoheader").typeText("autoreconf");
		bot.textWithLabel("autoreconf").setText("");
		bot.textWithLabel("autoreconf").typeText("libtoolize");
		bot.textWithLabel("libtoolize").setText("");
		bot.textWithLabel("libtoolize").typeText("aclocal");
		bot.button("OK").click();
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");

		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Aclocal").click();
		shell = bot.shell("Aclocal Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking aclocal in.*GnuProject1.*automake --help.*Usage:.*automake.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());

		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Automake").click();
		shell = bot.shell("Automake Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		p = Pattern.compile(".*Invoking automake in.*GnuProject1.*autoconf --help.*Usage:.*autoconf.*", Pattern.DOTALL);
		m = p.matcher(output);
		assertTrue(m.matches());

		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Autoheader").click();
		shell = bot.shell("Autoheader Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		p = Pattern.compile(".*Invoking autoheader in.*GnuProject1.*autoreconf --help.*Usage:.*autoreconf.*", Pattern.DOTALL);
		m = p.matcher(output);
		assertTrue(m.matches());

		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Autoreconf").click();
		shell = bot.shell("Autoreconf Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		p = Pattern.compile(".*Invoking autoreconf in.*GnuProject1.*libtoolize --help.*Usage:.*libtoolize.*", Pattern.DOTALL);
		m = p.matcher(output);
		assertTrue(m.matches());

		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		view = bot.viewByTitle("Project Explorer");
		view.bot().tree().select("GnuProject1");
		bot.menu("Project", 1).menu("Invoke Autotools").menu("Invoke Libtoolize").click();
		shell = bot.shell("Libtoolize Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		consoleView = bot.viewByTitle("Console");
		consoleView.setFocus();
		output = consoleView.bot().styledText().getText();
		// Verify we got some help output to the console
		p = Pattern.compile(".*Invoking libtoolize in.*GnuProject1.*aclocal --help.*Usage:.*aclocal.*", Pattern.DOTALL);
		m = p.matcher(output);
		assertTrue(m.matches());
	}

	@AfterClass
	public static void sleep() {
		bot.sleep(2000);
	}

}
