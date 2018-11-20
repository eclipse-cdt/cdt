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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestToolActions extends AbstractTest {

	@Test
	// Verify we can set the tools via the Autotools Tools page
	public void t1canSeeTools() throws Exception {
		openProperties("Autotools", "General");
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

	// Verify we can access the aclocal tool
	@Test
	public void t2canAccessAclocal() throws Exception {
		IPath path = checkProject().getLocation();
		// Verify configure does not exist initially
		path = path.append("aclocal.m4");
		File f = new File(path.toOSString());
		if (f.exists()) {
			f.delete();
		}
		clickProjectContextMenu("Invoke Autotools", "Invoke Aclocal");
		SWTBotShell shell = bot.shell("Aclocal Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		bot.sleep(1000);
		SWTBotView consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking aclocal in.*" + projectName + ".*aclocal --help.*Usage: aclocal.*",
				Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));
		// Verify we still don't have an aclocal.m4 file yet
		f = new File(path.toOSString());
		assertFalse(f.exists());
		// Now lets run aclocal for our hello world project which hasn't had any
		// autotool files generated yet.
		clickProjectContextMenu("Invoke Autotools", "Invoke Aclocal");
		shell = bot.shell("Aclocal Options");
		shell.activate();
		bot.button("OK").click();
		consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		p = Pattern.compile(".*Invoking aclocal in.*" + projectName + ".*aclocal.*", Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));
		// We need to wait until the aclocal.m4 file is created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.toOSString());
			if (f.exists()) {
				break;
			}
		}
		// Verify we now have an aclocal.m4 file created
		assertTrue(f.exists());
	}

	// Verify we can access the autoconf tool
	@Test
	public void t3canAccessAutoconf() throws Exception {
		IPath path = checkProject().getLocation();
		// Verify configure does not exist initially
		path = path.append("configure");
		File f = new File(path.toOSString());
		if (f.exists()) {
			f.delete();
		}
		clickProjectContextMenu("Invoke Autotools", "Invoke Autoconf");
		SWTBotView consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking autoconf in.*" + projectName + ".*autoconf.*", Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));
		// We need to wait until the configure file is created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.toOSString());
			if (f.exists()) {
				break;
			}
		}
		// Verify we now have a configure script
		f = new File(path.toOSString());
		assertTrue(f.exists());
		// Now lets delete the configure file and run autoconf from the project
		// explorer menu directly from the configure.ac file.
		assertTrue(f.delete());
		enterProjectFolder();
		clickVolatileContextMenu(projectExplorer.bot().tree().select("configure.ac"), "Invoke Autotools",
				"Invoke Autoconf");
		consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		p = Pattern.compile(".*Invoking autoconf in.*" + projectName + ".*autoconf.*", Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));
		// We need to wait until the configure file is created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.toOSString());
			if (f.exists()) {
				break;
			}
		}
		// Verify we now have a configure script again
		f = new File(path.toOSString());
		assertTrue(f.exists());

		exitProjectFolder();
	}

	// Verify we can access the automake tool
	@Test
	public void t4canAccessAutomake() throws Exception {
		IPath path = checkProject().getLocation();
		// Verify configure does not exist initially
		IPath path2 = path.append("src/Makefile.in");
		path = path.append("Makefile.in");
		File f = new File(path.toOSString());
		if (f.exists()) {
			f.delete();
		}
		File f2 = new File(path2.toOSString());
		if (f2.exists()) {
			f2.delete();
		}
		clickProjectContextMenu("Invoke Autotools", "Invoke Automake");
		SWTBotShell shell = bot.shell("Automake Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		SWTBotView consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking automake in.*" + projectName + ".*automake --help.*Usage:.*",
				Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));
		// Verify we still don't have Makefile.in files yet
		f = new File(path.toOSString());
		assertTrue(!f.exists());
		f2 = new File(path2.toOSString());
		assertTrue(!f2.exists());
		// Now lets run automake for our hello world project which hasn't had
		// any
		// Makefile.in files generated yet.
		clickProjectContextMenu("Invoke Autotools", "Invoke Automake");
		shell = bot.shell("Automake Options");
		shell.activate();
		bot.text(0).typeText("--add-missing"); // need this to successfully run
		// here
		bot.text(1).typeText("Makefile src/Makefile");
		bot.button("OK").click();
		consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		p = Pattern.compile(
				".*Invoking automake in.*" + projectName + ".*automake --add-missing Makefile src/Makefile.*",
				Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));
		// We need to wait until the Makefile.in files are created so
		// sleep a bit and look for it...give up after 20 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.toOSString());
			f2 = new File(path2.toOSString());
			if (f.exists() && f2.exists()) {
				break;
			}
		}
		assertTrue(f.exists() && f2.exists());
		// Verify we now have Makefile.in files created
		f = new File(path.toOSString());
		assertTrue(f.exists());
		f2 = new File(path2.toOSString());
		assertTrue(f2.exists());
	}

	// Verify we can access the libtoolize tool
	@Test
	public void t5canAccessLibtoolize() throws Exception {
		clickProjectContextMenu("Invoke Autotools", "Invoke Libtoolize");
		SWTBotShell shell = bot.shell("Libtoolize Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		SWTBotView consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(
				".*Invoking libtoolize in.*" + projectName + ".*libtoolize --help.*Usage: .*libtoolize.*",
				Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));
	}

	// Verify we can access the libtoolize tool
	@Test
	public void t6canAccessAutoheader() throws Exception {
		clickProjectContextMenu("Invoke Autotools", "Invoke Autoheader");
		SWTBotShell shell = bot.shell("Autoheader Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		SWTBotView consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(
				".*Invoking autoheader in.*" + projectName + ".*autoheader --help.*Usage:.*autoheader.*",
				Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));
	}

	// Verify we can access the autoreconf tool
	@Test
	public void t7canAccessAutoreconf() throws Exception {
		IPath path = checkProject().getLocation();
		// Remove a number of generated files
		File f = new File(path.append("src/Makefile.in").toOSString());
		if (f.exists()) {
			f.delete();
		}
		f = new File(path.append("Makefile.in").toOSString());
		if (f.exists()) {
			f.delete();
		}
		f = new File(path.append("configure").toOSString());
		if (f.exists()) {
			f.delete();
		}
		f = new File(path.append("config.status").toOSString());
		if (f.exists()) {
			f.delete();
		}
		f = new File(path.append("config.sub").toOSString());
		if (f.exists()) {
			f.delete();
		}
		clickProjectContextMenu("Invoke Autotools", "Invoke Autoreconf");
		SWTBotShell shell = bot.shell("Autoreconf Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		SWTBotView consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(
				".*Invoking autoreconf in.*" + projectName + ".*autoreconf --help.*Usage: .*autoreconf.*",
				Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));
		clickProjectContextMenu("Invoke Autotools", "Invoke Autoreconf");
		shell = bot.shell("Autoreconf Options");
		shell.activate();
		bot.text(0).typeText("-i");
		bot.button("OK").click();
		// We need to wait until the Makefile.in file is created so
		// sleep a bit and look for it
		bot.sleep(5000);

		// Verify a number of generated files now exist
		String[] fileList = { "Makefile.in", "src/Makefile.in", "configure", "config.sub" };
		for (String name : fileList) {
			f = new File(path.append(name).toOSString());
			assertTrue("Missing: " + name, f.exists());
		}

		String name = "config.status";
		f = new File(path.append(name).toOSString());
		assertTrue("Mistakenly found: " + name, !f.exists()); // shouldn't have run configure
	}

	@Test
	public void t8canReconfigureProject() {
		IPath path = checkProject().getLocation();
		// Remove a number of generated files
		File f = new File(path.append("src/Makefile.in").toOSString());
		if (f.exists()) {
			f.delete();
		}
		f = new File(path.append("Makefile.in").toOSString());
		if (f.exists()) {
			f.delete();
		}
		f = new File(path.append("configure").toOSString());
		if (f.exists()) {
			f.delete();
		}
		f = new File(path.append("config.status").toOSString());
		if (f.exists()) {
			f.delete();
		}
		f = new File(path.append("config.sub").toOSString());
		if (f.exists()) {
			f.delete();
		}
		clickProjectContextMenu("Reconfigure Project");
		// We need to wait until the config.status file is created so
		// sleep a bit and look for it...give up after 40 seconds
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.append("config.status").toOSString());
			if (f.exists()) {
				break;
			}
		}
		assertTrue(f.exists());
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

	// Verify we can set and reset the tools via the Autotools Tools page
	// Verifies bug #317345
	@Test
	public void t9canResetTools() throws Exception {
		openProperties("Autotools", "General");
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

	// Verify we can set the tools via the Autotools Tools page
	@Test
	public void u1canSetTools() throws Exception {
		openProperties("Autotools", "General");
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

		clickProjectContextMenu("Invoke Autotools", "Invoke Aclocal");
		SWTBotShell shell = bot.shell("Aclocal Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		SWTBotView consoleView = viewConsole("Autotools");
		consoleView.setFocus();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*Invoking aclocal in.*" + projectName + ".*automake --help.*Usage:.*automake.*",
				Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));

		clickProjectContextMenu("Invoke Autotools", "Invoke Automake");
		shell = bot.shell("Automake Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		// Verify we got some help output to the console
		p = Pattern.compile(".*Invoking automake in.*" + projectName + ".*autoconf --help.*Usage:.*autoconf.*",
				Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));

		clickProjectContextMenu("Invoke Autotools", "Invoke Autoheader");
		shell = bot.shell("Autoheader Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		// Verify we got some help output to the console
		p = Pattern.compile(".*Invoking autoheader in.*" + projectName + ".*autoreconf --help.*Usage:.*autoreconf.*",
				Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));

		clickProjectContextMenu("Invoke Autotools", "Invoke Autoreconf");
		shell = bot.shell("Autoreconf Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		// Verify we got some help output to the console
		p = Pattern.compile(".*Invoking autoreconf in.*" + projectName + ".*libtoolize --help.*Usage:.*libtoolize.*",
				Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));

		clickProjectContextMenu("Invoke Autotools", "Invoke Libtoolize");
		shell = bot.shell("Libtoolize Options");
		shell.activate();
		bot.text(0).typeText("--help");
		bot.button("OK").click();
		consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		// Verify we got some help output to the console
		p = Pattern.compile(".*Invoking libtoolize in.*" + projectName + ".*aclocal --help.*Usage:.*aclocal.*",
				Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));
	}

}
