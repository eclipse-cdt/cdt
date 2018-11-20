/*******************************************************************************
 * Copyright (c) 2010, 2016 Red Hat Inc. and others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SetConfigurationParameter extends AbstractTest {

	@BeforeClass
	public static void initClass() {
		initConfigParm();
	}

	// Prepare initial settings that will be tested.
	private static void initConfigParm() {
		// Set the configure parameters to be --enable-jeff via user-defined
		// options
		SWTBotShell shell = openProperties("Autotools", "Configure Settings");
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		SWTBotText text = bot.textWithLabel("Additional command-line options");
		text.typeText("--enable-jeff");
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell), 120000);

		// Create new build configurations that will be used throughout tests
		projectExplorer.bot().tree().select(projectName);
		clickContextMenu(projectExplorer.bot().tree().select(projectName), "Build Configurations", "Manage...");
		shell = bot.shell(projectName + ": Manage Configurations");
		shell.activate();
		bot.button("New...").click();
		shell = bot.shell("Create New Configuration");
		shell.activate();
		SWTBotText t = bot.textWithLabel("Name:");
		t.setText("debug");
		AbstractTest.clickRadioButtonInGroup("Existing configuration", "Copy settings from");
		bot.button("OK").click();
		shell = bot.shell(projectName + ": Manage Configurations");
		shell.activate();
		bot.button("New...").click();
		shell = bot.shell("Create New Configuration");
		shell.activate();
		t = bot.textWithLabel("Name:");
		t.setText("default");
		AbstractTest.clickRadioButtonInGroup("Default configuration", "Copy settings from");
		bot.button("OK").click();
		shell = bot.shell(projectName + ": Manage Configurations");
		shell.activate();
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell));
	}

	// Verify we can set a configuration parameter and that it is recorded in
	// the .autotools file for the project
	@Test
	public void t1canSetConfigParm() throws Exception {
		IProject project = checkProject();
		assertNotNull(project);
		IPath path = project.getLocation();
		path = path.append(".autotools");
		File f = new File(path.toOSString());
		assertTrue(f.exists());

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document d = db.parse(f);
		Element e = d.getDocumentElement();
		// Get the stored configuration data
		NodeList cfgs = e.getElementsByTagName("configuration"); //$NON-NLS-1$
		assertTrue(cfgs.getLength() > 0);
		Node n = cfgs.item(0);
		NodeList l = n.getChildNodes();
		// Verify user field in .autotools file is set to --enable-jeff
		boolean foundUser = false;
		for (int y = 0; y < l.getLength(); ++y) {
			Node child = l.item(y);
			if (child.getNodeName().equals("option")) { //$NON-NLS-1$
				NamedNodeMap optionAttrs = child.getAttributes();
				Node idNode = optionAttrs.getNamedItem("id"); //$NON-NLS-1$
				Node valueNode = optionAttrs.getNamedItem("value"); //$NON-NLS-1$
				assertNotNull(idNode);
				assertNotNull(valueNode);
				String id = idNode.getNodeValue();
				String value = valueNode.getNodeValue();
				if (id.equals("user")) {
					foundUser = true;
					assertEquals(value, "--enable-jeff");
				}
			}
		}
		assertTrue(foundUser);
	}

	// Verify we can build the project with a configuration parameter and that
	// the configuration parameter can be found in the config.status file.
	@Test
	public void t2canBuildWithConfigParm() throws Exception {
		projectExplorer.bot().tree().select(projectName);
		clickContextMenu(projectExplorer.bot().tree().select(projectName), "Build Project");

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
		File f = new File(path.append("src/a.out").toOSString());
		assertTrue(f.exists());
		f = new File(path.append("config.status").toOSString());
		assertTrue(f.exists());

		try (BufferedReader r = new BufferedReader(new FileReader(f))) {
			int ch;
			boolean optionFound = false;
			// Read config.status and look for the string --enable-jeff
			// which is a simple verification that the option was used in the
			// configure step.
			while ((ch = r.read()) != -1) {
				if (ch == '-') {
					char[] buf = new char[12];
					r.mark(100);
					int count = r.read(buf);
					if (count < 12) {
						break;
					}
					String s = new String(buf);
					if (s.equals("-enable-jeff")) {
						optionFound = true;
						break;
					} else {
						r.reset();
					}
				}
			}
			assertTrue(optionFound);
		}

		String output = viewConsole("Configure").bot().styledText().getText();
		// Verify we got some help output to the console
		Pattern p = Pattern.compile(".*WARNING:.*unrecognized.*--enable-jeff.*", Pattern.DOTALL);
		Matcher m = p.matcher(output);
		assertTrue(m.matches());

		// Verify that if we build again, we don't reconfigure.
		// Verifies fix for bug: #308261
		long oldDate = f.lastModified();
		projectExplorer.bot().tree().select(projectName);
		clickContextMenu(projectExplorer.bot().tree().select(projectName), "Build Project");
		path = project.getLocation();
		// We need to wait until the a.out file is created so
		// sleep a bit and look for it...give up after 120 seconds
		for (int i = 0; i < 240; ++i) {
			bot.sleep(500);
			f = new File(path.append("src/a.out").toOSString());
			if (f.exists()) {
				break;
			}
		}
		assertTrue(f.exists());
		f = new File(path.append("config.status").toOSString());
		assertTrue(f.exists());
		long newDate = f.lastModified();
		assertEquals(newDate, oldDate);
	}

	// Verify a new configuration will copy the configuration parameters
	// of its base configuration.
	@Test
	public void t3newConfigCopiesParms() throws Exception {
		projectExplorer.bot().tree().select(projectName);
		clickContextMenu(projectExplorer.bot().tree().select(projectName), "Build Configurations", "Manage...");
		SWTBotShell shell = bot.shell(projectName + ": Manage Configurations");
		shell.activate();
		shell = bot.shell(projectName + ": Manage Configurations");
		shell.activate();
		SWTBotTable table = bot.table();
		assertTrue(table.containsItem("debug"));
		table.getTableItem("debug").select();
		bot.button("Set Active").click();
		bot.button("OK").click();
		// Verify the debug configuration is active and has a user parameter:
		// --enable-jeff
		openProperties("Autotools", "Configure Settings");
		SWTBotCombo configs = bot.comboBoxWithLabel("Configuration: ");
		configs.setFocus();
		String[] items = configs.items();
		for (int i = 0; i < items.length; ++i) {
			if (items[i].contains("debug") && items[i].contains("Active")) {
				configs.setSelection(i);
			}
		}
		assertTrue(configs.getText().contains("debug"));
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		SWTBotText text = bot.textWithLabel("Additional command-line options");
		String val = text.getText();
		assertEquals("--enable-jeff", val);
		// Verify that the build directory for the new configuration has been
		// switched to build-debug
		shell = bot.shell("Properties for " + projectName);
		shell.activate();
		bot.text().setText("");

		bot.tree().select("C/C++ Build");
		String buildDir = bot.textWithLabel("Build directory:").getText();
		assertTrue(buildDir.endsWith("build-debug"));
		// Verify the default configuration has no user setting
		bot.tree().expandNode("Autotools").select("Configure Settings");
		configs = bot.comboBoxWithLabel("Configuration: ");
		configs.setSelection("default");
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		text = bot.textWithLabel("Additional command-line options");
		val = text.getText();
		assertEquals("", val);
		bot.button("OK").click();
		// Build the project again and verify we get a build-debug directory
		projectExplorer.bot().tree().select(projectName);
		clickContextMenu(projectExplorer.bot().tree().select(projectName), "Build Project");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		assertNotNull(workspace);
		IWorkspaceRoot root = workspace.getRoot();
		assertNotNull(root);
		IProject project = root.getProject(projectName);
		assertNotNull(project);
		IPath path = project.getLocation();
		// We need to wait until the config.status file is created so
		// sleep a bit and look for it...give up after 20 seconds
		File f = null;
		for (int i = 0; i < 40; ++i) {
			bot.sleep(500);
			f = new File(path.append("build-debug/src/a.out").toOSString());
			if (f.exists()) {
				break;
			}
		}
		assertTrue(f.exists());
		f = new File(path.append("build-debug/config.status").toOSString());
		assertTrue(f.exists());
		try (BufferedReader r = new BufferedReader(new FileReader(f))) {
			int ch;
			boolean optionFound = false;
			// Read config.status and look for the string --enable-jeff
			// which is a simple verification that the option was used in the
			// configure step.
			while ((ch = r.read()) != -1) {
				if (ch == '-') {
					char[] buf = new char[12];
					r.mark(100);
					int count = r.read(buf);
					if (count < 12) {
						break;
					}
					String s = new String(buf);
					if (s.equals("-enable-jeff")) {
						optionFound = true;
						break;
					} else {
						r.reset();
					}
				}
			}
			assertTrue(optionFound);
		}
		// Verify we cleaned out the top-level build directory (i.e. that there
		// is no config.status there anymore).
		path = project.getLocation().append("config.status");
		f = new File(path.toOSString());
		assertTrue(!f.exists());
		path = project.getLocation().append(".autotools");
		f = new File(path.toOSString());
		assertTrue(f.exists());
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document d = db.parse(f);
		Element e = d.getDocumentElement();
		// Get the stored configuration data
		NodeList cfgs = e.getElementsByTagName("configuration"); //$NON-NLS-1$
		assertEquals(4, cfgs.getLength());
		int foundUser = 0;
		for (int x = 0; x < cfgs.getLength(); ++x) {
			Node n = cfgs.item(x);
			NodeList l = n.getChildNodes();
			// Verify two of the user fields in .autotools file are set to
			// --enable-jeff
			for (int y = 0; y < l.getLength(); ++y) {
				Node child = l.item(y);
				if (child.getNodeName().equals("option")) { //$NON-NLS-1$
					NamedNodeMap optionAttrs = child.getAttributes();
					Node idNode = optionAttrs.getNamedItem("id"); //$NON-NLS-1$
					Node valueNode = optionAttrs.getNamedItem("value"); //$NON-NLS-1$
					assertNotNull(idNode);
					assertNotNull(valueNode);
					String id = idNode.getNodeValue();
					String value = valueNode.getNodeValue();
					if (id.equals("user")) {
						if (value.equals("--enable-jeff")) {
							++foundUser;
						}
					}
				}
			}
		}
		assertEquals(2, foundUser);

		clickContextMenu(projectExplorer.bot().tree().select(projectName), "Build Configurations", "Manage...");
		shell = bot.shell(projectName + ": Manage Configurations");
		shell.activate();
		table = bot.table();
		assertTrue(table.containsItem("Build (GNU)"));
		table.getTableItem("Build (GNU)").select();
		bot.button("Set Active").click();
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell));
	}

	// Verify we can do a double rename of configurations, renaming one
	// configuration to
	// another and then cancel without changing configuration settings.
	@Test
	public void t4doubleRenameCancel() throws Exception {
		openProperties("Autotools", "Configure Settings");
		SWTBotCombo configs = bot.comboBoxWithLabel("Configuration: ");
		bot.button("Manage Configurations...").click();
		// Rename "debug" to "release" and rename "default" to "debug".
		// The settings should follow the rename operation.
		renameConfiguration("debug", "release");
		renameConfiguration("default", "debug");
		bot.button("OK").click();
		// Verify that "release" has --enable-jeff set and that
		// the new "debug" configuration has no user setting.
		SWTBotShell shell = bot.shell("Properties for " + projectName);
		shell.activate();
		configs = bot.comboBoxWithLabel("Configuration: ");
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		SWTBotText text = bot.textWithLabel("Additional command-line options");
		String setting = text.getText();
		assertEquals("--enable-jeff", setting);
		configs.setFocus();
		configs.setSelection("debug");
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		text = bot.textWithLabel("Additional command-line options");
		setting = text.getText();
		assertEquals("", setting);
		bot.button("Cancel").click();
		// Cancel and then verify that "debug" is back to normal with
		// --enable-jeff set and that "default" is back to normal with
		// no user setting.
		openProperties("Autotools", "Configure Settings");
		configs = bot.comboBoxWithLabel("Configuration: ");
		configs.setSelection("debug");
		assertTrue(configs.getText().contains("debug"));
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		text = bot.textWithLabel("Additional command-line options");
		setting = text.getText();
		assertEquals("--enable-jeff", setting);
		configs.setFocus();
		configs.setSelection("default");
		assertTrue(configs.getText().contains("default"));
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		text = bot.textWithLabel("Additional command-line options");
		setting = text.getText();
		assertEquals("", setting);
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell));
	}

	// Verify we can do a double rename of configurations, renaming one
	// configuration to
	// another and inheriting the settings properly.
	@Test
	public void t5doubleRenameOk() throws Exception {
		openProperties("Autotools", "Configure Settings");
		SWTBotCombo configs = bot.comboBoxWithLabel("Configuration: ");
		bot.button("Manage Configurations...").click();
		SWTBotShell shell = bot.shell(projectName + ": Manage Configurations");
		shell.activate();
		bot.table().select("debug");
		bot.button("Set Active").click();
		// Rename "debug" to "release" and rename "default" to "debug".
		// The settings should follow the rename operation.
		renameConfiguration("debug", "release");
		renameConfiguration("default", "debug");
		bot.button("OK").click();
		shell = bot.shell("Properties for " + projectName);
		shell.activate();
		bot.button("OK").click();

		// Verify changes have taken effect permanently
		openProperties("Autotools", "Configure Settings");
		configs = bot.comboBoxWithLabel("Configuration: ");
		assertTrue(configs.getText().contains("release"));
		assertTrue(configs.getText().contains("Active"));
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		SWTBotText text = bot.textWithLabel("Additional command-line options");
		String setting = text.getText();
		assertEquals("--enable-jeff", setting);
		configs.setFocus();
		configs.setSelection("debug");
		assertTrue(configs.getText().contains("debug"));
		bot.treeWithLabel("Configure Settings").expandNode("configure").select("Advanced");
		text = bot.textWithLabel("Additional command-line options");
		setting = text.getText();
		assertEquals("", setting);

		// Undo the changes made by this test
		configs = bot.comboBoxWithLabel("Configuration: ");
		bot.button("Manage Configurations...").click();
		shell = bot.shell(projectName + ": Manage Configurations");
		shell.activate();
		bot.table().select("Build (GNU)");
		bot.button("Set Active").click();
		renameConfiguration("debug", "default");
		renameConfiguration("release", "debug");
		bot.button("OK").click();
		shell = bot.shell("Properties for " + projectName);
		shell.activate();
		bot.button("OK").click();
		bot.waitUntil(Conditions.shellCloses(shell));
	}

	// Renames a configuration. Assumes the "Manage Configurations" dialog will
	// be open.
	private void renameConfiguration(String original, String newname) {
		SWTBotShell shell = bot.shell(projectName + ": Manage Configurations");
		shell.activate();
		bot.table().select(original);
		bot.button("Rename...").click();
		shell = bot.shell("Rename Configuration");
		shell.activate();
		bot.textWithLabel("Name:").setText(newname);
		bot.button("OK").click();
		shell = bot.shell(projectName + ": Manage Configurations");
		shell.activate();
	}

}
