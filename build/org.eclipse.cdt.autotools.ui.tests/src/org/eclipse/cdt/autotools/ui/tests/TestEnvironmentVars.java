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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestEnvironmentVars extends AbstractTest {

	// Verify we can pass an unknown env var in configure options and it will be
	// nulled out
	// Verifies fix for Bug: #303616
	@Test
	public void t1referenceUnknownEnvVar() throws Exception {
		SWTBotShell shell = openProperties("Autotools", "Configure Settings");
		// Set the configure parameters to be --enable-jeff via user-defined
		// options
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		SWTBotText text = bot.textWithLabel("Additional command-line options");
		text.typeText("${some_var}");
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell), 120000);

		clickContextMenu(projectExplorer.bot().tree().select(projectName), "Reconfigure Project");
		IPath path = checkProject().getLocation();
		File f = null;
		// We need to wait until the config.status file is created so
		// sleep a bit and look for it...give up after 40 seconds
		for (int i = 0; i < 80; ++i) {
			bot.sleep(500);
			f = new File(path.append("config.status").toOSString());
			if (f.exists()) {
				break;
			}
		}
		assertTrue(f.exists());
		bot.sleep(1000);
		SWTBotView consoleView = viewConsole("Configure");
		consoleView.setFocus();
		String output = consoleView.bot().styledText().getText();
		Pattern p = Pattern.compile(".*some_var.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		// We shouldn't see some_var anywhere in the console
		assertFalse(m.matches());

		setEnvVar();
	}

	// Verify we can set an environment variable and use it as a configure
	// parameter
	// Verifies fix for Bug: #303616
	private void setEnvVar() throws Exception {
		openProperties("C/C++ Build", "Environment");
		bot.button("Add...").click();
		SWTBotShell shell = bot.shell("New variable");
		shell.activate();
		SWTBotText text = bot.textWithLabel("Name:");
		text.setText("some_var");
		text = bot.textWithLabel("Value:");
		text.setText("--enable-somevar");
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell));
		shell = bot.shell("Properties for " + projectName);
		shell.activate();
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell));
		clickContextMenu(projectExplorer.bot().tree().select(projectName), "Reconfigure Project");

		IPath path = checkProject().getLocation();
		File f = null;
		// We need to wait until the config.status file is created so
		// sleep a bit and look for it...give up after 40 seconds
		for (int i = 0; i < 80; ++i) {
			bot.sleep(500);
			f = new File(path.append("config.status").toOSString());
			if (f.exists()) {
				break;
			}
		}
		assertTrue(f.exists());
		SWTBotView consoleView = viewConsole("Configure");
		Pattern p = Pattern.compile(".*--enable-somevar.*", Pattern.DOTALL);
		// We should see the expanded some_var variable in the console
		bot.waitUntil(consoleTextMatches(consoleView, p));
		setEnvVarOnCommandLine();
	}

	// Verify we can set an environment variable prior to the configuration
	// command and
	// it will be seen by the configure script
	private void setEnvVarOnCommandLine() throws Exception {
		IPath path = checkProject().getLocation();
		// Create a fake configure script which prints out the values of
		// envvars some_var1, some_var2, and some_var3
		File f = new File(path.append("fake_configure").toOSString());
		try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
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
		}
		// Now change the configure script command to be the fake configure
		// script
		// and set the three envvars on the command itself
		openProperties("Autotools", "Configure Settings");
		bot.treeWithLabel("Configure Settings").select("configure");
		bot.textWithLabel("Command").setText("");
		// Choose three different forms, some using quotes to allow blanks in
		// them
		bot.textWithLabel("Command").typeText(
				"some_var1=\"a boat\" some_var2='a train' some_var3=car fake_configure some_var4=\"a wagon\" some_var5='a plane' some_var6=skates");
		bot.button("OK").click();
		// Reconfigure the project and make sure the env variables are seen in
		// the script
		clickContextMenu(projectExplorer.bot().tree().select(projectName), "Reconfigure Project");
		focusMainShell();
		SWTBotView consoleView = bot.viewByPartName("Console");
		consoleView.setFocus();
		Pattern p = Pattern.compile(".*a boat.*a train.*car.*a wagon.*a plane.*skates.*", Pattern.DOTALL);
		bot.waitUntil(consoleTextMatches(consoleView, p));
	}

}